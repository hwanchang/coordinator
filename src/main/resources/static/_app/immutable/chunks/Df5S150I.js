import{ao as E,ap as b,aq as P,l as R,ai as C,M as H,aj as W,u as Y,ar as T,z as j,ag as B,Z as $,as as L,C as w,x as D,A as z,D as p,F,at as Z,au as x,V as G,L as J,av as K,v as Q,I as U,y as S,p as X,_ as tt,a as rt}from"./CZkFhCqJ.js";import{d as et}from"./DyJux5AM.js";const at=["touchstart","touchmove"];function nt(t){return at.includes(t)}let O=!1;function st(){O||(O=!0,document.addEventListener("reset",t=>{Promise.resolve().then(()=>{var r;if(!t.defaultPrevented)for(const a of t.target.elements)(r=a.__on_r)==null||r.call(a)})},{capture:!0}))}function V(t){var r=P,a=R;E(null),b(null);try{return t()}finally{E(r),b(a)}}function _t(t,r,a,i=a){t.addEventListener(r,()=>V(a));const n=t.__on_r;n?t.__on_r=()=>{n(),i(!0)}:t.__on_r=()=>i(!0),st()}const it=new Set,I=new Set;function ot(t,r,a,i={}){function n(e){if(i.capture||y.call(r,e),!e.cancelBubble)return V(()=>a==null?void 0:a.call(this,e))}return t.startsWith("pointer")||t.startsWith("touch")||t==="wheel"?H(()=>{r.addEventListener(t,n,i)}):r.addEventListener(t,n,i),n}function lt(t,r,a,i,n){var e={capture:i,passive:n},u=ot(t,r,a,e);(r===document.body||r===window||r===document)&&C(()=>{r.removeEventListener(t,u,e)})}function y(t){var A;var r=this,a=r.ownerDocument,i=t.type,n=((A=t.composedPath)==null?void 0:A.call(t))||[],e=n[0]||t.target,u=0,d=t.__root;if(d){var _=n.indexOf(d);if(_!==-1&&(r===document||r===window)){t.__root=r;return}var v=n.indexOf(r);if(v===-1)return;_<=v&&(u=_)}if(e=n[u]||t.target,e!==r){W(t,"currentTarget",{configurable:!0,get(){return e||a}});var m=P,f=R;E(null),b(null);try{for(var s,o=[];e!==null;){var c=e.assignedSlot||e.parentNode||e.host||null;try{var l=e["__"+i];if(l!=null&&(!e.disabled||t.target===e))if(Y(l)){var[M,...q]=l;M.apply(e,[t,...q])}else l.call(e,t)}catch(g){s?o.push(g):s=g}if(t.cancelBubble||c===r||c===null)break;e=c}if(s){for(let g of o)queueMicrotask(()=>{throw g});throw s}}finally{t.__root=r,delete t.currentTarget,E(m),b(f)}}}function dt(t,r){var a=r==null?"":typeof r=="object"?r+"":r;a!==(t.__t??(t.__t=t.nodeValue))&&(t.__t=a,t.nodeValue=a+"")}function ut(t,r){return k(t,r)}function vt(t,r){T(),r.intro=r.intro??!1;const a=r.target,i=S,n=p;try{for(var e=j(a);e&&(e.nodeType!==8||e.data!==B);)e=$(e);if(!e)throw L;w(!0),D(e),z();const u=k(t,{...r,anchor:e});if(p===null||p.nodeType!==8||p.data!==F)throw Z(),L;return w(!1),u}catch(u){if(u===L)return r.recover===!1&&x(),T(),G(a),w(!1),ut(t,r);throw u}finally{w(i),D(n)}}const h=new Map;function k(t,{target:r,anchor:a,props:i={},events:n,context:e,intro:u=!0}){T();var d=new Set,_=f=>{for(var s=0;s<f.length;s++){var o=f[s];if(!d.has(o)){d.add(o);var c=nt(o);r.addEventListener(o,y,{passive:c});var l=h.get(o);l===void 0?(document.addEventListener(o,y,{passive:c}),h.set(o,1)):h.set(o,l+1)}}};_(J(it)),I.add(_);var v=void 0,m=K(()=>{var f=a??r.appendChild(Q());return U(()=>{if(e){X({});var s=tt;s.c=e}n&&(i.$$events=n),S&&et(f,null),v=t(f,i)||{},S&&(R.nodes_end=p),e&&rt()}),()=>{var c;for(var s of d){r.removeEventListener(s,y);var o=h.get(s);--o===0?(document.removeEventListener(s,y),h.delete(s)):h.set(s,o)}I.delete(_),f!==a&&((c=f.parentNode)==null||c.removeChild(f))}});return N.set(v,m),v}let N=new WeakMap;function ht(t,r){const a=N.get(t);return a?(N.delete(t),a(r)):Promise.resolve()}export{st as a,lt as e,vt as h,_t as l,ut as m,dt as s,ht as u};
