(function(e){function t(t){for(var a,r,i=t[0],u=t[1],c=t[2],l=0,d=[];l<i.length;l++)r=i[l],Object.prototype.hasOwnProperty.call(o,r)&&o[r]&&d.push(o[r][0]),o[r]=0;for(a in u)Object.prototype.hasOwnProperty.call(u,a)&&(e[a]=u[a]);f&&f(t);while(d.length)d.shift()();return s.push.apply(s,c||[]),n()}function n(){for(var e,t=0;t<s.length;t++){for(var n=s[t],a=!0,r=1;r<n.length;r++){var i=n[r];0!==o[i]&&(a=!1)}a&&(s.splice(t--,1),e=u(u.s=n[0]))}return e}var a={},r={app:0},o={app:0},s=[];function i(e){return u.p+"js/"+({}[e]||e)+"."+{"chunk-02c2cf1c":"709a79bf","chunk-145b1440":"4cef10e1","chunk-2d0aa22f":"89d73adf"}[e]+".js"}function u(t){if(a[t])return a[t].exports;var n=a[t]={i:t,l:!1,exports:{}};return e[t].call(n.exports,n,n.exports,u),n.l=!0,n.exports}u.e=function(e){var t=[],n={"chunk-02c2cf1c":1,"chunk-145b1440":1};r[e]?t.push(r[e]):0!==r[e]&&n[e]&&t.push(r[e]=new Promise((function(t,n){for(var a="css/"+({}[e]||e)+"."+{"chunk-02c2cf1c":"4941d6d3","chunk-145b1440":"901db5c8","chunk-2d0aa22f":"31d6cfe0"}[e]+".css",o=u.p+a,s=document.getElementsByTagName("link"),i=0;i<s.length;i++){var c=s[i],l=c.getAttribute("data-href")||c.getAttribute("href");if("stylesheet"===c.rel&&(l===a||l===o))return t()}var d=document.getElementsByTagName("style");for(i=0;i<d.length;i++){c=d[i],l=c.getAttribute("data-href");if(l===a||l===o)return t()}var f=document.createElement("link");f.rel="stylesheet",f.type="text/css",f.onload=t,f.onerror=function(t){var a=t&&t.target&&t.target.src||o,s=new Error("Loading CSS chunk "+e+" failed.\n("+a+")");s.code="CSS_CHUNK_LOAD_FAILED",s.request=a,delete r[e],f.parentNode.removeChild(f),n(s)},f.href=o;var m=document.getElementsByTagName("head")[0];m.appendChild(f)})).then((function(){r[e]=0})));var a=o[e];if(0!==a)if(a)t.push(a[2]);else{var s=new Promise((function(t,n){a=o[e]=[t,n]}));t.push(a[2]=s);var c,l=document.createElement("script");l.charset="utf-8",l.timeout=120,u.nc&&l.setAttribute("nonce",u.nc),l.src=i(e);var d=new Error;c=function(t){l.onerror=l.onload=null,clearTimeout(f);var n=o[e];if(0!==n){if(n){var a=t&&("load"===t.type?"missing":t.type),r=t&&t.target&&t.target.src;d.message="Loading chunk "+e+" failed.\n("+a+": "+r+")",d.name="ChunkLoadError",d.type=a,d.request=r,n[1](d)}o[e]=void 0}};var f=setTimeout((function(){c({type:"timeout",target:l})}),12e4);l.onerror=l.onload=c,document.head.appendChild(l)}return Promise.all(t)},u.m=e,u.c=a,u.d=function(e,t,n){u.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:n})},u.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},u.t=function(e,t){if(1&t&&(e=u(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var n=Object.create(null);if(u.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var a in e)u.d(n,a,function(t){return e[t]}.bind(null,a));return n},u.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return u.d(t,"a",t),t},u.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},u.p="/",u.oe=function(e){throw console.error(e),e};var c=window["webpackJsonp"]=window["webpackJsonp"]||[],l=c.push.bind(c);c.push=t,c=c.slice();for(var d=0;d<c.length;d++)t(c[d]);var f=l;s.push([0,"chunk-vendors"]),n()})({0:function(e,t,n){e.exports=n("56d7")},"21bb":function(e,t,n){"use strict";n("2dad")},"2dad":function(e,t,n){},"56d7":function(e,t,n){"use strict";n.r(t);n("e260"),n("e6cf"),n("cca6"),n("a79d");var a=n("2b0e"),r=n("2f62"),o=n("f309");a["default"].use(o["a"]);var s=new o["a"]({theme:{themes:{dark:{primary:"#3f51b5",secondary:"#b0bec5",accent:"#8c9eff",error:"#b71c1c"},light:{primary:"#2196f3",secondary:"#009688",accent:"#8bc34a",error:"#f44336",warning:"#ff9800",info:"#cddc39",success:"#4caf50"}}}}),i=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("v-app",[n("v-app-bar",{attrs:{app:"",color:"primary",dark:""}},[e.isTeacher?n("v-app-bar-nav-icon",{attrs:{to:"/"}}):e._e(),e.isAccManager?n("v-btn",{attrs:{icon:"",to:"/loaddata"}},[n("v-icon",[e._v("mdi-upload")])],1):e._e(),e.isAccManager?n("v-btn",{attrs:{icon:"",to:"/checkdata"}},[n("v-icon",[e._v("mdi-compare-horizontal")])],1):e._e(),n("v-spacer"),n("v-toolbar-title",[e._v("BKEST-Accounts")]),n("v-spacer"),e.isAdmin?n("v-btn",{attrs:{icon:"",to:"/config"}},[n("v-icon",[e._v("mdi-cog")])],1):e._e(),n("v-btn",{attrs:{href:"",text:""},on:{click:function(t){return t.preventDefault(),e.logOut.apply(null,arguments)}}},[e.currentUser?n("span",{staticClass:"mr-2"},[e._v(e._s(e.currentUser.username)+" ")]):e._e(),n("span",{staticClass:"mr-2"},[e._v("LogOut")]),n("v-icon",[e._v("mdi-logout")])],1)],1),n("v-main",[n("router-view")],1),n("v-footer",{attrs:{app:"",height:"45"}},[n("v-row",{attrs:{"no-gutters":""}},[n("v-col",{attrs:{cols:"1",align:"left"}},[e._v(" "+e._s(e.time))]),n("v-col",{attrs:{cols:"1",align:"left"}},[e._v(" ("+e._s(e.lastUpdate)+")")]),n("v-col",{attrs:{cols:"8",align:"left"}},["false"===e.stale?n("span",[e._v(e._s(e.lastMessage))]):e._e()]),n("v-col",{attrs:{cols:"2",align:"right"}},[n("v-icon",[e._v("mdi-copyright")]),e._v(" J. Hergeth ")],1)],1)],1),e.toDo>0?n("v-footer",{attrs:{app:"",height:"5"}},[n("v-row",{attrs:{"no-gutters":""}},[n("v-col",{attrs:{cols:"12"}},[n("v-progress-linear",{attrs:{"buffer-value":e.progvalue},model:{value:e.progvalue,callback:function(t){e.progvalue=t},expression:"progvalue"}})],1)],1)],1):e._e()],1)},u=[],c=n("5530"),l=n("64db"),d=n("f121"),f={name:"App",data:function(){return{time:"",lastUpdate:"0:00",lastMessage:"",toDo:0,done:0,stale:!1,progvalue:0}},mounted:function(){var e=this;setInterval((function(){l["a"].get(d["i"]).then((function(t){var n=new Date,a=n.getHours()+":"+n.getMinutes()+":"+n.getSeconds();e.time=a,void 0!=t&&(e.lastUpdate=t.timeSet,e.lastMessage=t.message,e.toDo=t.todo,e.done=t.done,e.stale=t.stale,t.todo>0?e.progvalue=t.done/t.todo*100:(e.progvalue=0,e.toDo=0),"false"!==e.stale&&(e.toDo=0))}))}),500)},computed:Object(c["a"])({},Object(r["b"])("auth",["currentUser","isAdmin","isTeacher","isAccManager"])),methods:{logOut:function(){this.$store.dispatch("auth/logout"),this.$router.push("/login")}}},m=f,g=n("2877"),p=n("6544"),v=n.n(p),h=n("7496"),b=n("40dc"),w=n("5bc1"),_=n("8336"),k=n("62ad"),y=n("553a"),C=n("132d"),x=n("f6c4"),O=n("8e36"),I=n("0fd9"),N=n("2fa4"),j=n("2a7f"),A=Object(g["a"])(m,i,u,!1,null,null,null),S=A.exports;v()(A,{VApp:h["a"],VAppBar:b["a"],VAppBarNavIcon:w["a"],VBtn:_["a"],VCol:k["a"],VFooter:y["a"],VIcon:C["a"],VMain:x["a"],VProgressLinear:O["a"],VRow:I["a"],VSpacer:N["a"],VToolbarTitle:j["a"]});n("d3b7"),n("3ca3"),n("ddb0");var T=n("8c4f"),P=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("v-container",[n("v-row",{attrs:{"no-gutters":""}},[n("v-card",{staticClass:"white pa-2 mx-0",attrs:{tile:""}},[n("v-card-title",[e._v("Konten:")]),n("v-card-text",[n("v-row",[n("v-col",{attrs:{cols:"3"}},[n("v-select",{attrs:{items:e.mKlassen,label:"Klasse wählen",align:"top"},on:{change:e.classSelected},model:{value:e.sKlassen,callback:function(t){e.sKlassen=t},expression:"sKlassen"}})],1),n("v-col",{attrs:{cols:"3"}},[n("v-text-field",{attrs:{label:"Neues Passwort"},model:{value:e.passwort,callback:function(t){e.passwort=t},expression:"passwort"}})],1),n("v-col")],1),n("v-alert",{attrs:{value:e.alert,type:e.alertColor,dark:"",icon:"mdi-account",transition:"slide-y-transition"}},[e._v(" "+e._s(e.alertText)+" ")]),n("v-data-table",{staticClass:"table-striped elevation-1",attrs:{headers:e.headers,items:e.konten,"items-per-page":e.dataSize,"item-key":"loginName","hide-default-footer":!0},scopedSlots:e._u([{key:"item.anzeigeName",fn:function(t){var a=t.item;return[a.loginName===e.editedItem.loginName?n("v-text-field",{attrs:{"hide-details":!0,dense:"","single-line":""},model:{value:e.editedItem.anzeigeName,callback:function(t){e.$set(e.editedItem,"anzeigeName",t)},expression:"editedItem.anzeigeName"}}):n("span",[e._v(e._s(a.anzeigeName))])]}},{key:"item.loginName",fn:function(t){var n=t.item;return[e._v(e._s(n.loginName))]}},{key:"item.nachname",fn:function(t){var a=t.item;return[a.loginName===e.editedItem.loginName?n("v-text-field",{attrs:{"hide-details":!0,dense:"","single-line":""},model:{value:e.editedItem.nachname,callback:function(t){e.$set(e.editedItem,"nachname",t)},expression:"editedItem.nachname"}}):n("span",[e._v(e._s(a.nachname))])]}},{key:"item.vorname",fn:function(t){var a=t.item;return[a.loginName===e.editedItem.loginName?n("v-text-field",{attrs:{"hide-details":!0,dense:"","single-line":""},model:{value:e.editedItem.vorname,callback:function(t){e.$set(e.editedItem,"vorname",t)},expression:"editedItem.vorname"}}):n("span",[e._v(e._s(a.vorname))])]}},{key:"item.email",fn:function(t){var a=t.item;return[a.loginName===e.editedItem.loginName?n("v-text-field",{attrs:{"hide-details":!0,dense:"","single-line":""},model:{value:e.editedItem.email,callback:function(t){e.$set(e.editedItem,"email",t)},expression:"editedItem.email"}}):n("span",[e._v(e._s(a.email))])]}},{key:"item.actions",fn:function(t){var a=t.item;return[a.loginName===e.editedItem.loginName?n("div",[n("v-icon",{staticClass:"mr-2",attrs:{color:"red"},on:{click:e.close}},[e._v(" mdi-window-close ")]),n("v-icon",{attrs:{color:"green"},on:{click:e.save}},[e._v(" mdi-content-save ")])],1):n("div",[n("v-icon",{staticClass:"mr-2",attrs:{color:"green"},on:{click:function(t){return e.editItem(a)}}},[e._v(" mdi-pencil ")]),n("v-tooltip",{attrs:{top:""},scopedSlots:e._u([{key:"activator",fn:function(t){var r=t.on,o=t.attrs;return[n("v-icon",e._g(e._b({attrs:{color:"red"},on:{click:function(t){return e.newPass(a)}}},"v-icon",o,!1),r),[e._v(" mdi-lock-alert ")])]}}],null,!0)},[n("span",[e._v("Passwort setzen")])])],1)]}}])})],1)],1)],1)],1)},V=[],D={name:"Home",data:function(){return{sKlassen:[],konten:[],passwort:"bkest202122",login:"",alert:!1,alertColor:"success",alertText:"",editedIndex:-1,editedItem:{anzeigeName:"",loginName:"-1",nachname:"",vorname:"",email:""},dataSize:0,headers:[{text:"Klasse",align:"start",sortable:!0,value:"klasse"},{text:"Name",value:"anzeigeName"},{text:"Login",value:"loginName"},{text:"Nachname",value:"nachname"},{text:"Vorname",value:"vorname"},{text:"EMail",value:"email"},{text:"Actions",value:"actions",sortable:!1}],mKlassen:[]}},computed:Object(c["a"])({},Object(r["b"])("auth",["currentUser"])),mounted:function(){var e=this;this.currentUser?(this.loadConfig(),l["a"].get(d["f"]).then((function(t){void 0!=t&&t.length>0&&(e.mKlassen=t,e.dataSize=t.length)}))):this.$router.push("/login")},methods:{loadConfig:function(){this.alert=!1},editItem:function(e){this.editedIndex=this.konten.indexOf(e),this.editedItem=Object.assign({},e)},close:function(){var e=this;setTimeout((function(){e.editedItem.loginName="",e.editedIndex=-1}),300)},save:function(){var e=this;if(this.editedIndex>-1){Object.assign(this.konten[this.editedIndex],this.editedItem);var t=this;t.login=this.editedItem.loginName,l["a"].post(d["l"],this.editedItem).then((function(n){e.handleAlert(n,t,"Kontodaten geschrieben für "+t.login+".")}))}this.close()},newPass:function(e){var t=this,n=this;n.login=e.loginName,l["a"].post(d["s"],{id:n.login,pw:this.passwort}).then((function(e){t.handleAlert(e,n,"Passwort für "+n.login+" gesetzt")}))},handleAlert:function(e,t,n){console.log(n),t.alert=!0,t.alertColor=e?"success":"error",t.alertText=n,window.setTimeout((function(){t.alert=!1,t.alertText=""}),4e3)},classSelected:function(e){console.log(e);var t=this;this.passwort="bkest202122"+e,l["a"].post(d["e"],e).then((function(e){var n=e.data;void 0!==n&&n.length>0&&(t.konten=n)}))}}},E=D,K=(n("21bb"),n("0798")),$=n("b0af"),L=n("99d9"),M=n("a523"),q=n("8fea"),U=n("b974"),z=n("8654"),F=n("3a2f"),B=Object(g["a"])(E,P,V,!1,null,null,null),R=B.exports;v()(B,{VAlert:K["a"],VCard:$["a"],VCardText:L["a"],VCardTitle:L["b"],VCol:k["a"],VContainer:M["a"],VDataTable:q["a"],VIcon:C["a"],VRow:I["a"],VSelect:U["a"],VTextField:z["a"],VTooltip:F["a"]});var J=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{staticClass:"col-md-12"},[n("div",{staticClass:"card card-container"},[n("img",{staticClass:"profile-img-card",attrs:{id:"profile-img",src:"//ssl.gstatic.com/accounts/ui/avatar_2x.png"}}),n("form",{attrs:{name:"form"},on:{submit:function(t){return t.preventDefault(),e.handleLogin.apply(null,arguments)}}},[n("div",{staticClass:"form-group"},[n("label",{attrs:{for:"username"}},[e._v("Username")]),n("input",{directives:[{name:"model",rawName:"v-model",value:e.user.username,expression:"user.username"},{name:"validate",rawName:"v-validate",value:"required",expression:"'required'"}],staticClass:"form-control",attrs:{type:"text",name:"username"},domProps:{value:e.user.username},on:{input:function(t){t.target.composing||e.$set(e.user,"username",t.target.value)}}}),e.errors.has("username")?n("div",{staticClass:"alert alert-danger",attrs:{role:"alert"}},[e._v("Username is required!")]):e._e()]),n("div",{staticClass:"form-group"},[n("label",{attrs:{for:"password"}},[e._v("Password")]),n("input",{directives:[{name:"model",rawName:"v-model",value:e.user.password,expression:"user.password"},{name:"validate",rawName:"v-validate",value:"required",expression:"'required'"}],staticClass:"form-control",attrs:{type:"password",name:"password"},domProps:{value:e.user.password},on:{input:function(t){t.target.composing||e.$set(e.user,"password",t.target.value)}}}),e.errors.has("password")?n("div",{staticClass:"alert alert-danger",attrs:{role:"alert"}},[e._v("Password is required!")]):e._e()]),n("div",{staticClass:"form-group"},[n("button",{staticClass:"btn btn-primary btn-block",attrs:{disabled:e.loading}},[n("span",{directives:[{name:"show",rawName:"v-show",value:e.loading,expression:"loading"}],staticClass:"spinner-border spinner-border-sm"}),n("span",[e._v("Login")])])]),n("div",{staticClass:"form-group"},[e.message?n("div",{staticClass:"alert alert-danger",attrs:{role:"alert"}},[e._v(e._s(e.message))]):e._e()])])])])},H=[],G=(n("25f0"),n("d4ec")),Q=function e(t,n){Object(G["a"])(this,e),this.username=t,this.password=n},W={name:"Login",data:function(){return{user:new Q("",""),loading:!1,message:""}},computed:{loggedIn:function(){return this.$store.state.auth.status.loggedIn}},created:function(){this.loggedIn&&this.$router.push("/")},methods:{handleLogin:function(){var e=this;this.loading=!0,this.$validator.validateAll().then((function(t){t?e.user.username&&e.user.password&&e.$store.dispatch("auth/login",e.user).then((function(){e.$router.push("/")}),(function(t){e.loading=!1,e.message=t.response&&t.response.data||t.message||t.toString()})):e.loading=!1}))}}},X=W,Y=(n("f3d8"),Object(g["a"])(X,J,H,!1,null,"00d4272c",null)),Z=Y.exports;a["default"].use(T["a"]);var ee=new T["a"]({mode:"hash",routes:[{path:"/",name:"home",component:R,meta:{requiresAuth:!0}},{path:"/login",component:Z,meta:{requiresAuth:!1}},{path:"/config",name:"config",component:function(){return n.e("chunk-2d0aa22f").then(n.bind(null,"1071"))},meta:{requiresAuth:!0}},{path:"/loaddata",name:"loadData",component:function(){return n.e("chunk-02c2cf1c").then(n.bind(null,"a0ca"))},meta:{requiresAuth:!0}},{path:"/checkdata",name:"checkData",component:function(){return n.e("chunk-145b1440").then(n.bind(null,"6de7"))},meta:{requiresAuth:!0}}]});ee.beforeEach((function(e,t,n){var a=localStorage.getItem("user"),r=e.matched.some((function(e){return e.meta.requiresAuth}));r&&!a?n("/login"):n()}));n("caad"),n("2532");var te=n("bee2"),ne=(n("e9c4"),n("bc3a")),ae=n.n(ne),re=function(){function e(){Object(G["a"])(this,e)}return Object(te["a"])(e,[{key:"login",value:function(e){return ae.a.post(d["a"],{username:e.username,password:e.password}).then((function(e){return e.data.access_token&&localStorage.setItem("user",JSON.stringify(e.data)),e.data}))}},{key:"logout",value:function(){localStorage.removeItem("user")}}]),e}(),oe=new re,se=JSON.parse(localStorage.getItem("user")),ie=se?{status:{loggedIn:!0},user:se}:{status:{loggedIn:!1},user:null};function ue(e,t){return!(!e.user||!e.user.roles)&&e.user.roles.includes(t)}var ce={namespaced:!0,state:ie,getters:{currentUser:function(e){return e.user},isAdmin:function(e){return ue(e,"ROLE_ADMIN")},isAccManager:function(e){return ue(e,"ROLE_ACCOUNTMANAGER")},isTeacher:function(e){return ue(e,"ROLE_TEACHER")}},actions:{login:function(e,t){var n=e.commit;return oe.login(t).then((function(e){return n("loginSuccess",e),Promise.resolve(e)}),(function(e){return n("loginFailure"),Promise.reject(e)}))},logout:function(e){var t=e.commit;oe.logout(),t("logout")},register:function(e,t){var n=e.commit;return oe.register(t).then((function(e){return n("registerSuccess"),Promise.resolve(e.data)}),(function(e){return n("registerFailure"),Promise.reject(e)}))}},mutations:{loginSuccess:function(e,t){e.status.loggedIn=!0,e.user=t},loginFailure:function(e){e.status.loggedIn=!1,e.user=null},logout:function(e){e.status.loggedIn=!1,e.user=null},registerSuccess:function(e){e.status.loggedIn=!1,e.user=null},registerFailure:function(e){e.status.loggedIn=!1,e.user=null}}},le=(n("b0c0"),{namespaced:!0,state:{konfig:[],unchanged:0,toChange:[],toCOld:[],toCreate:[],toDelete:[]},getters:{getKonfig:function(e){return e.konfig},getKonfigLoaded:function(e){return null!=e.konfig},getToChange:function(e){return void 0!==e.toChange?e.toChange:[]},getToCOld:function(e){return void 0!==e.toCOld?e.toCOld:[]},getToNew:function(e){return void 0!==e.toCreate?e.toCreate:[]},getToDelete:function(e){return void 0!==e.toDelete?e.toDelete:[]}},mutations:{gotKonfig:function(e,t){e.konfig=t.file},fileFailure:function(e){e.konfig=null},putData:function(e,t){e.konfig[t.name]=t.value},putAccUpdate:function(e,t){e.unchanged=t.au.unchanged,e.toChange=t.au.toChange,e.toCOld=t.au.toCOld,e.toCreate=t.au.toCreate,e.toDelete=t.au.toDelete}},actions:{readAccState:function(e){var t=e.commit;return l["a"].get(d["b"]).then((function(e){return t("putAccUpdate",{au:e}),Promise.resolve(e)}),(function(e){return Promise.reject(e)}))},readKonfig:function(e){var t=e.commit;return l["a"].get(d["h"]).then((function(e){return t("gotKonfig",{file:e}),Promise.resolve(e)}),(function(e){return t("fileFailure","Konfig"),Promise.reject(e)}))},writeDomain:function(e){var t=e.commit,n=e.getters;return l["a"].post(d["r"],n.getKonfig).then((function(e){return Promise.resolve(e)}),(function(e){return t("fileFailure","Konfig"),Promise.reject(e)}))}}});a["default"].use(r["a"]);var de=new r["a"].Store({modules:{auth:ce,domain:le}}),fe=(n("7b17"),n("ab8b"),n("817f")),me=n.n(fe),ge=n("7bb1"),pe=n("ecee"),ve=n("ad3d"),he=n("c074");pe["c"].add(he["a"],he["d"],he["e"],he["b"],he["c"]),a["default"].config.productionTip=!1,a["default"].use(ge["a"]),a["default"].use(s),a["default"].use(me.a),a["default"].use(r["a"]),a["default"].component("font-awesome-icon",ve["a"]),new a["default"]({router:ee,store:de,vuetify:s,render:function(e){return e(S)}}).$mount("#app")},"64db":function(e,t,n){"use strict";var a=n("5530"),r=n("d4ec"),o=n("bee2"),s=(n("d3b7"),n("bc3a")),i=n.n(s);function u(){var e=JSON.parse(localStorage.getItem("user"));return e&&e.access_token?{Authorization:"Bearer "+e.access_token}:{}}var c=function(){function e(){Object(r["a"])(this,e)}return Object(o["a"])(e,[{key:"get",value:function(e){return i.a.get(e,{headers:Object(a["a"])(Object(a["a"])({},{"Content-Type":"multipart/form-data"}),u())}).then((function(e){return new Promise((function(t){t(e.data)}))}))}},{key:"post",value:function(e,t){return i.a.post(e,t,{headers:Object(a["a"])(Object(a["a"])({},{"Content-Type":"application/json"}),u())})}},{key:"postId",value:function(e,t){return i.a.post(e,{id:t},{headers:Object(a["a"])(Object(a["a"])({},{"Content-Type":"application/json"}),u())})}},{key:"postRow",value:function(e,t){return i.a.post(e,t,{headers:Object(a["a"])(Object(a["a"])({},{"Content-Type":"application/json"}),u())})}},{key:"postForm",value:function(e,t){var n=new FormData;return n.append("file",t),i.a.post(e,n,{headers:Object(a["a"])(Object(a["a"])({},{"Content-Type":"multipart/form-data"}),u())})}}]),e}();t["a"]=new c},f121:function(e,t,n){"use strict";n.d(t,"a",(function(){return s})),n.d(t,"f",(function(){return c})),n.d(t,"e",(function(){return l})),n.d(t,"s",(function(){return d})),n.d(t,"j",(function(){return f})),n.d(t,"k",(function(){return m})),n.d(t,"q",(function(){return g})),n.d(t,"m",(function(){return p})),n.d(t,"b",(function(){return v})),n.d(t,"n",(function(){return h})),n.d(t,"p",(function(){return b})),n.d(t,"o",(function(){return w})),n.d(t,"g",(function(){return _})),n.d(t,"c",(function(){return k})),n.d(t,"d",(function(){return y})),n.d(t,"l",(function(){return C})),n.d(t,"h",(function(){return O})),n.d(t,"r",(function(){return I})),n.d(t,"i",(function(){return N}));var a="http://localhost:80",r=a+"/api",o=a+"/auth",s=o+"/login",i=r+"/read",u=r+"/write",c=i+"/getextklassen",l=i+"/getextacc",d=u+"/newpasswort",f=u+"/updaterow",m=u+"/update",g=u+"/updateselected",p=u+"/updnextcloud",v=u+"/compacc",h=u+"/updrowchg",b=u+"/updrownew",w=u+"/updrowdel",_=i+"/getintacc",k=u+"/loadfromfile",y=u+"/loadext",C=u+"/updateextrow",x=r+"/konfig",O=x+"/read",I=x+"/write",N=r+"/status"},f3d8:function(e,t,n){"use strict";n("fd21")},fd21:function(e,t,n){}});
//# sourceMappingURL=app.28240a7a.js.map