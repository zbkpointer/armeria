/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.server;

import java.net.URISyntaxException;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Ascii;

import com.linecorp.armeria.common.ClosedSessionException;
import com.linecorp.armeria.common.ContentTooLargeException;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.ProtocolViolationException;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.internal.common.ArmeriaHttpUtil;
import com.linecorp.armeria.internal.common.InboundTrafficController;
import com.linecorp.armeria.internal.common.KeepAliveHandler;
import com.linecorp.armeria.internal.common.NoopKeepAliveHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpExpectationFailedEvent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeEvent;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.HttpConversionUtil.ExtensionHeaderNames;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;

final class Http1RequestDecoder extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(Http1RequestDecoder.class);

    private static final Http2Settings DEFAULT_HTTP2_SETTINGS = new Http2Settings();
    private static final ResponseHeaders CONTINUE_RESPONSE = ResponseHeaders.of(HttpStatus.CONTINUE);

    private static final HttpData DATA_DECODER_FAILURE =
            HttpData.ofUtf8(HttpResponseStatus.BAD_REQUEST + "\nDecoder failure");
    private static final HttpData DATA_UNSUPPORTED_METHOD =
            HttpData.ofUtf8(HttpResponseStatus.METHOD_NOT_ALLOWED + "\nUnsupported method");
    private static final HttpData DATA_INVALID_CONTENT_LENGTH =
            HttpData.ofUtf8(HttpResponseStatus.BAD_REQUEST + "\nInvalid content length");
    private static final HttpData DATA_INVALID_REQUEST_PATH =
            HttpData.ofUtf8(HttpResponseStatus.BAD_REQUEST + "\nInvalid request path");
    private static final HttpData DATA_INVALID_DECODER_STATE =
            HttpData.ofUtf8(HttpResponseStatus.BAD_REQUEST + "\nInvalid decoder state");

    private final ServerConfig cfg;
    private final AsciiString scheme;
    private final InboundTrafficController inboundTrafficController;
    private final ServerHttp1ObjectEncoder writer;

    /** The request being decoded currently. */
    @Nullable
    private DecodedHttpRequest req;
    private int receivedRequests;
    private boolean discarding;

    Http1RequestDecoder(ServerConfig cfg, Channel channel, AsciiString scheme,
                        ServerHttp1ObjectEncoder writer) {
        this.cfg = cfg;
        this.scheme = scheme;
        inboundTrafficController = InboundTrafficController.ofHttp1(channel);
        this.writer = writer;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        maybeInitializeKeepAliveHandler(ctx);
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        destroyKeepAliveHandler();
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        maybeInitializeKeepAliveHandler(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        if (req != null) {
            // Ignored if the stream has already been closed.
            req.close(ClosedSessionException.get());
        }

        destroyKeepAliveHandler();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroyKeepAliveHandler();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof HttpObject)) {
            ctx.fireChannelRead(msg);
            return;
        }

        final KeepAliveHandler keepAliveHandler = writer.keepAliveHandler();
        keepAliveHandler.onReadOrWrite();
        // this.req can be set to null by fail(), so we keep it in a local variable.
        DecodedHttpRequest req = this.req;
        final int id = req != null ? req.id() : ++receivedRequests;
        try {
            if (discarding) {
                return;
            }

            if (req == null) {
                if (msg instanceof HttpRequest) {
                    keepAliveHandler.increaseNumRequests();
                    final HttpRequest nettyReq = (HttpRequest) msg;
                    if (!nettyReq.decoderResult().isSuccess()) {
                        fail(id, HttpResponseStatus.BAD_REQUEST, DATA_DECODER_FAILURE);
                        return;
                    }

                    final HttpHeaders nettyHeaders = nettyReq.headers();

                    // Validate the method.
                    if (!HttpMethod.isSupported(nettyReq.method().name())) {
                        fail(id, HttpResponseStatus.METHOD_NOT_ALLOWED, DATA_UNSUPPORTED_METHOD);
                        return;
                    }

                    // Validate the 'content-length' header.
                    final String contentLengthStr = nettyHeaders.get(HttpHeaderNames.CONTENT_LENGTH);
                    final boolean contentEmpty;
                    if (contentLengthStr != null) {
                        final long contentLength;
                        try {
                            contentLength = Long.parseLong(contentLengthStr);
                        } catch (NumberFormatException ignored) {
                            fail(id, HttpResponseStatus.BAD_REQUEST, DATA_INVALID_CONTENT_LENGTH);
                            return;
                        }
                        if (contentLength < 0) {
                            fail(id, HttpResponseStatus.BAD_REQUEST, DATA_INVALID_CONTENT_LENGTH);
                            return;
                        }

                        contentEmpty = contentLength == 0;
                    } else {
                        contentEmpty = true;
                    }

                    if (!handle100Continue(id, nettyReq, nettyHeaders)) {
                        ctx.pipeline().fireUserEventTriggered(HttpExpectationFailedEvent.INSTANCE);
                        fail(id, HttpResponseStatus.EXPECTATION_FAILED, null);
                        return;
                    }

                    nettyHeaders.set(ExtensionHeaderNames.SCHEME.text(), scheme);

                    this.req = req = new DecodedHttpRequest(
                            ctx.channel().eventLoop(),
                            id, 1,
                            ArmeriaHttpUtil.toArmeria(ctx, nettyReq, cfg),
                            HttpUtil.isKeepAlive(nettyReq),
                            inboundTrafficController,
                            // FIXME(trustin): Use a different maxRequestLength for a different virtual host.
                            cfg.defaultVirtualHost().maxRequestLength());

                    // Close the request early when it is sure that there will be
                    // neither content nor trailers.
                    if (contentEmpty && !HttpUtil.isTransferEncodingChunked(nettyReq)) {
                        req.close();
                    }

                    ctx.fireChannelRead(req);
                } else {
                    fail(id, HttpResponseStatus.BAD_REQUEST, DATA_INVALID_DECODER_STATE);
                    return;
                }
            }

            // req is not null.
            if (msg instanceof HttpContent) {
                final HttpContent content = (HttpContent) msg;
                final DecoderResult decoderResult = content.decoderResult();
                if (!decoderResult.isSuccess()) {
                    fail(id, HttpResponseStatus.BAD_REQUEST, DATA_DECODER_FAILURE, Http2Error.PROTOCOL_ERROR);
                    req.close(new ProtocolViolationException(decoderResult.cause()));
                    return;
                }

                final ByteBuf data = content.content();
                final int dataLength = data.readableBytes();
                if (dataLength != 0) {
                    req.increaseTransferredBytes(dataLength);
                    final long maxContentLength = req.maxRequestLength();
                    if (maxContentLength > 0 && req.transferredBytes() > maxContentLength) {
                        fail(id, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, null,
                             Http2Error.CANCEL);
                        req.close(ContentTooLargeException.get());
                        return;
                    }

                    if (req.isOpen()) {
                        req.write(HttpData.wrap(data.retain()));
                    }
                }

                if (msg instanceof LastHttpContent) {
                    final HttpHeaders trailingHeaders = ((LastHttpContent) msg).trailingHeaders();
                    if (!trailingHeaders.isEmpty()) {
                        req.write(ArmeriaHttpUtil.toArmeria(trailingHeaders));
                    }

                    req.close();
                    this.req = req = null;
                }
            }
        } catch (URISyntaxException e) {
            fail(id, HttpResponseStatus.BAD_REQUEST, DATA_INVALID_REQUEST_PATH, Http2Error.CANCEL);
            if (req != null) {
                req.close(e);
            }
        } catch (Throwable t) {
            fail(id, HttpResponseStatus.INTERNAL_SERVER_ERROR, null, Http2Error.INTERNAL_ERROR);
            if (req != null) {
                req.close(t);
            } else {
                logger.warn("Unexpected exception:", t);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private boolean handle100Continue(int id, HttpRequest nettyReq, HttpHeaders nettyHeaders) {
        if (nettyReq.protocolVersion().compareTo(HttpVersion.HTTP_1_1) < 0) {
            // Ignore HTTP/1.0 requests.
            return true;
        }

        final String expectValue = nettyHeaders.get(HttpHeaderNames.EXPECT);
        if (expectValue == null) {
            // No 'expect' header.
            return true;
        }

        // '100-continue' is the only allowed expectation.
        if (!Ascii.equalsIgnoreCase("100-continue", expectValue)) {
            return false;
        }

        // Send a '100 Continue' response.
        writer.writeHeaders(id, 1, CONTINUE_RESPONSE, false);

        // Remove the 'expect' header so that it's handled in a way invisible to a Service.
        nettyHeaders.remove(HttpHeaderNames.EXPECT);
        return true;
    }

    private void fail(int id, HttpResponseStatus status, @Nullable HttpData content, Http2Error error) {
        if (writer.isResponseHeadersSent(id, 1)) {
            // The response is sent or being sent by HttpResponseSubscriber so we cannot send
            // the error response.
            writer.writeReset(id, 1, error);
        } else {
            fail(id, status, content);
        }
    }

    private void fail(int id, HttpResponseStatus status, @Nullable HttpData content) {
        discarding = true;
        req = null;

        final HttpData data = content != null ? content : HttpData.ofUtf8(status.toString());
        final ResponseHeaders headers =
                ResponseHeaders.builder()
                               .status(status.code())
                               .set(HttpHeaderNames.CONNECTION, "close")
                               .setObject(HttpHeaderNames.CONTENT_TYPE, MediaType.PLAIN_TEXT_UTF_8)
                               .setInt(HttpHeaderNames.CONTENT_LENGTH, data.length())
                               .build();
        writer.writeHeaders(id, 1, headers, false);
        writer.writeData(id, 1, data, true).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof UpgradeEvent) {
            // Generate the initial Http2Settings frame,
            // so that the next handler knows the protocol upgrade occurred as well.
            ctx.fireChannelRead(DEFAULT_HTTP2_SETTINGS);

            // Continue handling the upgrade request after the upgrade is complete.
            final FullHttpRequest nettyReq = ((UpgradeEvent) evt).upgradeRequest();

            // Remove the headers related with the upgrade.
            nettyReq.headers().remove(HttpHeaderNames.CONNECTION);
            nettyReq.headers().remove(HttpHeaderNames.UPGRADE);
            nettyReq.headers().remove(Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER);

            if (logger.isDebugEnabled()) {
                logger.debug("{} Handling the pre-upgrade request ({}): {} {} {} ({}B)",
                             ctx.channel(), ((UpgradeEvent) evt).protocol(),
                             nettyReq.method(), nettyReq.uri(), nettyReq.protocolVersion(),
                             nettyReq.content().readableBytes());
            }

            channelRead(ctx, nettyReq);
            channelReadComplete(ctx);
            return;
        }

        ctx.fireUserEventTriggered(evt);
    }

    private void maybeInitializeKeepAliveHandler(ChannelHandlerContext ctx) {
        final KeepAliveHandler keepAliveHandler = writer.keepAliveHandler();
        if (keepAliveHandler != NoopKeepAliveHandler.INSTANCE &&
            ctx.channel().isActive() && ctx.channel().isRegistered()) {
            keepAliveHandler.initialize(ctx);
        }
    }

    private void destroyKeepAliveHandler() {
        writer.keepAliveHandler().destroy();
    }
}
