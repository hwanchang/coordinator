import{y as o,aa as n,ab as t,ac as i,ad as h,a2 as m,ae as S}from"./CZkFhCqJ.js";import{a as b,l as k}from"./Df5S150I.js";const y=Symbol("is custom element"),A=Symbol("is html");function N(e){if(o){var a=!1,s=()=>{if(!a){if(a=!0,e.hasAttribute("value")){var _=e.value;u(e,"value",null),e.value=_}if(e.hasAttribute("checked")){var r=e.checked;u(e,"checked",null),e.checked=r}}};e.__on_r=s,n(s),b()}}function u(e,a,s,_){var r=E(e);o&&(r[a]=e.getAttribute(a),a==="src"||a==="srcset"||a==="href"&&e.nodeName==="LINK")||r[a]!==(r[a]=s)&&(a==="loading"&&(e[t]=s),e.removeAttribute(a))}function E(e){return e.__attributes??(e.__attributes={[y]:e.nodeName.includes("-"),[A]:e.namespaceURI===i})}function T(e,a,s=a){var _=h();k(e,"input",r=>{var l=r?e.defaultValue:e.value;if(l=v(e)?f(l):l,s(l),_&&l!==(l=a())){var d=e.selectionStart,c=e.selectionEnd;e.value=l??"",c!==null&&(e.selectionStart=d,e.selectionEnd=Math.min(c,e.value.length))}}),(o&&e.defaultValue!==e.value||m(a)==null&&e.value)&&s(v(e)?f(e.value):e.value),S(()=>{var r=a();v(e)&&r===f(e.value)||e.type==="date"&&!r&&!e.value||r!==e.value&&(e.value=r??"")})}function v(e){var a=e.type;return a==="number"||a==="range"}function f(e){return e===""?null:+e}export{T as b,N as r};
