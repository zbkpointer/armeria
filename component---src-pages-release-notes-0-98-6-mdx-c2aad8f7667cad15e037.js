(window.webpackJsonp=window.webpackJsonp||[]).push([[88],{"1lec":function(e){e.exports=JSON.parse('{"/release-notes/1.6.0":"v1.6.0","/release-notes/1.5.0":"v1.5.0","/release-notes/1.4.0":"v1.4.0","/release-notes/1.3.0":"v1.3.0","/release-notes/1.2.0":"v1.2.0","/release-notes/1.1.0":"v1.1.0","/release-notes/1.0.0":"v1.0.0","/release-notes/0.99.9":"v0.99.9","/release-notes/0.99.8":"v0.99.8","/release-notes/0.99.7":"v0.99.7","/release-notes/0.99.6":"v0.99.6","/release-notes/0.99.5":"v0.99.5","/release-notes/0.99.4":"v0.99.4","/release-notes/0.99.3":"v0.99.3","/release-notes/0.99.2":"v0.99.2","/release-notes/0.99.1":"v0.99.1"}')},"2+3N":function(e){e.exports=JSON.parse('{"/news/20210202-newsletter-2":"Armeria Newsletter vol. 2","/news/20200703-newsletter-1":"Armeria Newsletter vol. 1","/news/20200514-newsletter-0":"Armeria Newsletter vol. 0"}')},JkCF:function(e,t,a){"use strict";a("tU7J");var n=a("wFql"),r=a("q1tI"),s=a.n(r),i=a("2+3N"),o=a("1lec"),l=a("+ejy"),c=a("+9zj"),b=n.a.Title;t.a=function(e){var t={},a={},n={root:{"Latest news items":"/news","Latest release notes":"/release-notes","Past news items":"/news/list","Past release notes":"/release-notes/list"},"Recent news items":t,"Recent releases":a};Object.entries(i).forEach((function(e){var a=e[0],n=e[1];t[n]=a})),Object.entries(o).forEach((function(e){var t=e[0],n=e[1];a[n]=t}));var r=Object(c.a)(e.location),h=e.version||r.substring(r.lastIndexOf("/")+1);return h.match(/^[0-9]/)||(h=void 0),s.a.createElement(l.a,Object.assign({},e,{candidateMdxNodes:[],index:n,prefix:"release-notes",pageTitle:h?h+" release notes":e.pageTitle,pageTitleSuffix:"Armeria release notes"}),h?s.a.createElement(b,{id:"release-notes",level:1},s.a.createElement("a",{href:"#release-notes","aria-label":"release notes permalink",className:"anchor before"},s.a.createElement("svg",{"aria-hidden":"true",focusable:"false",height:"16",version:"1.1",viewBox:"0 0 16 16",width:"16"},s.a.createElement("path",{fillRule:"evenodd",d:"M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"}))),h," release notes"):"",e.children)}},ciju:function(e,t,a){"use strict";a.r(t),a.d(t,"_frontmatter",(function(){return o})),a.d(t,"default",(function(){return h}));var n,r=a("8o2o"),s=(a("q1tI"),a("7ljp")),i=a("JkCF"),o={},l=(n="ThankYou",function(e){return console.warn("Component "+n+" was not imported, exported, or provided by MDXProvider as global scope"),Object(s.b)("div",e)}),c={_frontmatter:o},b=i.a;function h(e){var t=e.components,a=Object(r.a)(e,["components"]);return Object(s.b)(b,Object.assign({},c,a,{components:t,mdxType:"MDXLayout"}),Object(s.b)("p",{className:"date"},"16th March 2020"),Object(s.b)("h2",{id:"-new-features",style:{position:"relative"}},Object(s.b)("a",{parentName:"h2",href:"#-new-features","aria-label":" new features permalink",className:"anchor before"},Object(s.b)("svg",{parentName:"a","aria-hidden":"true",focusable:"false",height:"16",version:"1.1",viewBox:"0 0 16 16",width:"16"},Object(s.b)("path",{parentName:"svg",fillRule:"evenodd",d:"M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"}))),"🌟 New features"),Object(s.b)("ul",null,Object(s.b)("li",{parentName:"ul"},"Added ",Object(s.b)("inlineCode",{parentName:"li"},"TextFormatter.socketAddress()")," and ",Object(s.b)("inlineCode",{parentName:"li"},"inetAddress()")," that convert a ",Object(s.b)("inlineCode",{parentName:"li"},"SocketAddress")," or an ",Object(s.b)("inlineCode",{parentName:"li"},"InetAddress")," into a ",Object(s.b)("inlineCode",{parentName:"li"},"String")," without repeating an IP address twice. ",Object(s.b)("a",{parentName:"li",href:"https://github.com/line/armeria/issues/2591"},"#2591"))),Object(s.b)("h2",{id:"-improvements",style:{position:"relative"}},Object(s.b)("a",{parentName:"h2",href:"#-improvements","aria-label":" improvements permalink",className:"anchor before"},Object(s.b)("svg",{parentName:"a","aria-hidden":"true",focusable:"false",height:"16",version:"1.1",viewBox:"0 0 16 16",width:"16"},Object(s.b)("path",{parentName:"svg",fillRule:"evenodd",d:"M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"}))),"📈 Improvements"),Object(s.b)("ul",null,Object(s.b)("li",{parentName:"ul"},Object(s.b)("inlineCode",{parentName:"li"},"RequestContext.toString()")," now returns a ",Object(s.b)("inlineCode",{parentName:"li"},"String")," that includes its ",Object(s.b)("inlineCode",{parentName:"li"},"RequestId"),". ",Object(s.b)("a",{parentName:"li",href:"https://github.com/line/armeria/issues/2591"},"#2591"))),Object(s.b)("h2",{id:"️-bug-fixes",style:{position:"relative"}},Object(s.b)("a",{parentName:"h2",href:"#%EF%B8%8F-bug-fixes","aria-label":"️ bug fixes permalink",className:"anchor before"},Object(s.b)("svg",{parentName:"a","aria-hidden":"true",focusable:"false",height:"16",version:"1.1",viewBox:"0 0 16 16",width:"16"},Object(s.b)("path",{parentName:"svg",fillRule:"evenodd",d:"M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"}))),"🛠️ Bug fixes"),Object(s.b)("ul",null,Object(s.b)("li",{parentName:"ul"},"Fixed a bug where a client-side response is not closed quickly enough for a certain case. ",Object(s.b)("a",{parentName:"li",href:"https://github.com/line/armeria/issues/2590"},"#2590")),Object(s.b)("li",{parentName:"ul"},"Fixed a bug where ",Object(s.b)("inlineCode",{parentName:"li"},"Sampler.random(0.01)")," never samples. ",Object(s.b)("a",{parentName:"li",href:"https://github.com/line/armeria/issues/2592"},"#2592")),Object(s.b)("li",{parentName:"ul"},"The ",Object(s.b)("inlineCode",{parentName:"li"},"Logger")," returned by ",Object(s.b)("inlineCode",{parentName:"li"},"RequestContext.makeContextAware(Logger)")," now pushes the context whenever logging a message, so that ",Object(s.b)("inlineCode",{parentName:"li"},"RequestContextExporter")," can retrieve the current context. ",Object(s.b)("a",{parentName:"li",href:"https://github.com/line/armeria/issues/2587"},"#2587"))),Object(s.b)("h2",{id:"-thank-you",style:{position:"relative"}},Object(s.b)("a",{parentName:"h2",href:"#-thank-you","aria-label":" thank you permalink",className:"anchor before"},Object(s.b)("svg",{parentName:"a","aria-hidden":"true",focusable:"false",height:"16",version:"1.1",viewBox:"0 0 16 16",width:"16"},Object(s.b)("path",{parentName:"svg",fillRule:"evenodd",d:"M4 9h1v1H4c-1.5 0-3-1.69-3-3.5S2.55 3 4 3h4c1.45 0 3 1.69 3 3.5 0 1.41-.91 2.72-2 3.25V8.59c.58-.45 1-1.27 1-2.09C10 5.22 8.98 4 8 4H4c-.98 0-2 1.22-2 2.5S3 9 4 9zm9-3h-1v1h1c1 0 2 1.22 2 2.5S13.98 12 13 12H9c-.98 0-2-1.22-2-2.5 0-.83.42-1.64 1-2.09V6.25c-1.09.53-2 1.84-2 3.25C6 11.31 7.55 13 9 13h4c1.45 0 3-1.69 3-3.5S14.5 6 13 6z"}))),"🙇 Thank you"),Object(s.b)(l,{usernames:["anuraaga","ikhoon","KarboniteKream","minwoox","trustin"],mdxType:"ThankYou"}))}h.isMDXComponent=!0}}]);
//# sourceMappingURL=component---src-pages-release-notes-0-98-6-mdx-c2aad8f7667cad15e037.js.map