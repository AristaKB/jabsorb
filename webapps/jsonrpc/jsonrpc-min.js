var nonce="";var escapeJSONChar=function(){var A=["\b","\t","\n","\f","\r"];return function(C){if(C=='"'||C=="\\"){return"\\"+C}if(C.charCodeAt(0)>=32){return C}for(var B=0;B<A.length;B++){if(C==A[B]){return"\\"+C}}return C}}();function escapeJSONString(B){var C=B.split("");for(var A=0;A<C.length;A++){C[A]=escapeJSONChar(C[A])}return'"'+C.join("")+'"'}function toJSON(F){var A="$_$jabsorbed$813492";var H;var E=[];function C(){var I;while(H){I=H[A].prev;delete H[A];H=I}}var G={};var B;function D(J,I,K){var R=[],M,L,Q,O,N;if(J===null||J===undefined){return"null"}else{if(typeof J==="string"){return escapeJSONString(J)}else{if(typeof J==="number"){return J.toString()}else{if(typeof J==="boolean"){return J.toString()}else{if(J[A]){M=[K];Q=I;while(Q){if(L){L.unshift(Q[A].ref)}if(Q===J){O=Q;L=[O[A].ref]}M.unshift(Q[A].ref);Q=Q[A].parent}if(O){if(JSONRpcClient.fixupCircRefs){M.shift();L.shift();E.push([M,L]);return G}else{C();throw new Error("circular reference detected!")}}else{if(JSONRpcClient.fixupDuplicates){L=[J[A].ref];Q=J[A].parent;while(Q){L.unshift(Q[A].ref);Q=Q[A].parent}M.shift();L.shift();E.push([M,L]);return G}}}else{J[A]={parent:I,prev:H,ref:K};H=J}if(J.constructor===Date){return'{javaClass: "java.util.Date", time: '+J.valueOf()+"}"}else{if(J.constructor===Array){for(N=0;N<J.length;N++){B=D(J[N],J,N);R.push(B===G?null:B)}return"["+R.join(", ")+"]"}else{for(var P in J){if(P===A){}else{if(J[P]===null||J[P]===undefined){R.push('"'+P+'": null')}else{if(typeof J[P]=="function"){}else{B=D(J[P],J,P);if(B!==G){R.push(escapeJSONString(P)+": "+B)}}}}}return"{"+R.join(", ")+"}"}}}}}}}B=D(F,null,"root");C();if(E.length){return{json:B,fixups:E}}else{return{json:B}}}function JSONRpcClient(){var B=0,G,A,D,C;if(typeof arguments[0]=="function"){this.readyCB=arguments[0];B++}this.serverURL=arguments[B];this.user=arguments[B+1];this.pass=arguments[B+2];this.objectID=arguments[B+3];this.javaClass=arguments[B+4];this.JSONRPCType=arguments[B+5];if(JSONRpcClient.knownClasses[this.javaClass]&&(this.JSONRPCType=="CallableReference")){for(var E in JSONRpcClient.knownClasses[this.javaClass]){A=JSONRpcClient.knownClasses[this.javaClass][E];this[E]=JSONRpcClient.bind(A,this)}}else{if(this.objectID){this._addMethods(["listMethods"],this.javaClass);G=this._makeRequest("listMethods",[])}else{this._addMethods(["system.listMethods"],this.javaClass);G=this._makeRequest("system.listMethods",[]);var F=this._makeRequest("system.getNonce",[]);F.cb=function(H,I){nonce=H};this._sendRequest(F)}if(this.readyCB){C=this;G.cb=function(H,I){if(!I){C._addMethods(H)}C.readyCB(H,I)}}D=this._sendRequest(G);if(!this.readyCB){this._addMethods(D,this.javaClass)}}}JSONRpcClient.knownClasses={};JSONRpcClient.Exception=function(E,D,C){this.code=E;var B,A;if(C){this.javaStack=C;A=C.match(/^([^:]*)/);if(A){B=A[0]}}if(B){this.name=B}else{this.name="JSONRpcClientException"}this.message=D};JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION=490;JSONRpcClient.Exception.CODE_ERR_CLIENT=550;JSONRpcClient.Exception.CODE_ERR_PARSE=590;JSONRpcClient.Exception.CODE_ERR_NOMETHOD=591;JSONRpcClient.Exception.CODE_ERR_UNMARSHALL=592;JSONRpcClient.Exception.CODE_ERR_MARSHALL=593;JSONRpcClient.Exception.prototype=new Error();JSONRpcClient.Exception.prototype.toString=function(A,B){return this.name+": "+this.message};JSONRpcClient.default_ex_handler=function(A){alert(A)};JSONRpcClient.toplevel_ex_handler=JSONRpcClient.default_ex_handler;JSONRpcClient.profile_async=false;JSONRpcClient.max_req_active=1;JSONRpcClient.requestId=1;JSONRpcClient.fixupCircRefs=true;JSONRpcClient.fixupDuplicates=true;JSONRpcClient.bind=function(B,A){return function(){return B.apply(A,arguments)}};JSONRpcClient.prototype._createMethod=function(B){var A=function(){var C=[],F;for(var D=0;D<arguments.length;D++){C.push(arguments[D])}if(typeof C[0]=="function"){F=C.shift()}var E=this._makeRequest.call(this,B,C,F);if(!F){return this._sendRequest.call(this,E)}else{JSONRpcClient.async_requests.push(E);JSONRpcClient.kick_async();return E.requestId}};return A};JSONRpcClient.prototype._addMethods=function(C,H){var A,E,D,G,F;if(H){JSONRpcClient.knownClasses[H]={}}for(var B=0;B<C.length;B++){E=this;D=C[B].split(".");for(G=0;G<D.length-1;G++){A=D[G];if(E[A]){E=E[A]}else{E[A]={};E=E[A]}}A=D[D.length-1];if(!E[A]){F=this._createMethod(C[B]);E[A]=JSONRpcClient.bind(F,this);if(H&&(A!="listMethods")){JSONRpcClient.knownClasses[H][A]=F}}}};JSONRpcClient._getCharsetFromHeaders=function(A){var E,D,B;try{E=A.getResponseHeader("Content-type");D=E.split(/\s*;\s*/);for(B=0;B<D.length;B++){if(D[B].substring(0,8)=="charset="){return D[B].substring(8,D[B].length)}}}catch(C){}return"UTF-8"};JSONRpcClient.async_requests=[];JSONRpcClient.async_inflight={};JSONRpcClient.async_responses=[];JSONRpcClient.async_timeout=null;JSONRpcClient.num_req_active=0;JSONRpcClient._async_handler=function(){var A,B;JSONRpcClient.async_timeout=null;while(JSONRpcClient.async_responses.length>0){A=JSONRpcClient.async_responses.shift();if(A.canceled){continue}if(A.profile){A.profile.dispatch=new Date()}try{A.cb(A.result,A.ex,A.profile)}catch(C){JSONRpcClient.toplevel_ex_handler(C)}}while(JSONRpcClient.async_requests.length>0&&JSONRpcClient.num_req_active<JSONRpcClient.max_req_active){B=JSONRpcClient.async_requests.shift();if(B.canceled){continue}B.client._sendRequest.call(B.client,B)}};JSONRpcClient.kick_async=function(){if(!JSONRpcClient.async_timeout){JSONRpcClient.async_timeout=setTimeout(JSONRpcClient._async_handler,0)}};JSONRpcClient.cancelRequest=function(B){if(JSONRpcClient.async_inflight[B]){JSONRpcClient.async_inflight[B].canceled=true;return true}var A;for(A in JSONRpcClient.async_requests){if(JSONRpcClient.async_requests[A].requestId==B){JSONRpcClient.async_requests[A].canceled=true;return true}}for(A in JSONRpcClient.async_responses){if(JSONRpcClient.async_responses[A].requestId==B){JSONRpcClient.async_responses[A].canceled=true;return true}}return false};JSONRpcClient.prototype._makeRequest=function(B,D,A){var E={};E.client=this;E.requestId=JSONRpcClient.requestId++;var F='{"id":'+E.requestId+',"nonce":"'+nonce+'","method":';if(this.objectID){F+='".obj#'+this.objectID+"."+B+'"'}else{F+='"'+B+'"'}if(A){E.cb=A}if(JSONRpcClient.profile_async){E.profile={submit:new Date()}}var C=toJSON(D);F+=',"params":'+C.json;if(C.fixups){F+=',"fixups":'+toJSON(C.fixups).json}E.data=F+"}";return E};JSONRpcClient.prototype._sendRequest=function(C){var B,A;if(C.profile){C.profile.start=new Date()}B=JSONRpcClient.poolGetHTTPRequest();JSONRpcClient.num_req_active++;B.open("POST",this.serverURL,!!C.cb,this.user,this.pass);try{B.setRequestHeader("Content-type","text/plain")}catch(D){}if(C.cb){A=this;B.onreadystatechange=function(){var E;if(B.readyState==4){B.onreadystatechange=function(){};E={cb:C.cb,result:null,ex:null};if(C.profile){E.profile=C.profile;E.profile.end=new Date()}else{E.profile=false}try{E.result=A._handleResponse(B)}catch(F){E.ex=F}if(!JSONRpcClient.async_inflight[C.requestId].canceled){JSONRpcClient.async_responses.push(E)}delete JSONRpcClient.async_inflight[C.requestId];JSONRpcClient.kick_async()}}}else{B.onreadystatechange=function(){}}JSONRpcClient.async_inflight[C.requestId]=C;try{B.send(C.data)}catch(D){JSONRpcClient.poolReturnHTTPRequest(B);JSONRpcClient.num_req_active--;throw new JSONRpcClient.Exception(JSONRpcClient.Exception.CODE_ERR_CLIENT,"Connection failed")}if(!C.cb){delete JSONRpcClient.async_inflight[C.requestId];return this._handleResponse(B)}return null};JSONRpcClient.prototype._handleResponse=function(http){function applyFixups(obj,fixups){function findOriginal(ob,original){for(var i=0,j=original.length;i<j;i++){ob=ob[original[i]]}return ob}function applyFixup(ob,fixups,value){var j=fixups.length-1;for(var i=0;i<j;i++){ob=ob[fixups[i]]}ob[fixups[j]]=value}for(var i=0,j=fixups.length;i<j;i++){applyFixup(obj,fixups[i][0],findOriginal(obj,fixups[i][1]))}}if(!this.charset){this.charset=JSONRpcClient._getCharsetFromHeaders(http)}var status,statusText,data;try{status=http.status;statusText=http.statusText;data=http.responseText}catch(e){JSONRpcClient.poolReturnHTTPRequest(http);JSONRpcClient.num_req_active--;JSONRpcClient.kick_async();throw new JSONRpcClient.Exception(JSONRpcClient.Exception.CODE_ERR_CLIENT,"Connection failed")}JSONRpcClient.poolReturnHTTPRequest(http);JSONRpcClient.num_req_active--;if(status!=200){throw new JSONRpcClient.Exception(status,statusText)}var obj;try{eval("obj = "+data)}catch(e){throw new JSONRpcClient.Exception(550,"error parsing result")}if(obj.error){throw new JSONRpcClient.Exception(obj.error.code,obj.error.msg,obj.error.trace)}var r=obj.result;var i,tmp;if(r){if(r.objectID&&r.JSONRPCType=="CallableReference"){return new JSONRpcClient(this.serverURL,this.user,this.pass,r.objectID,r.javaClass,r.JSONRPCType)}r=JSONRpcClient.extractCallableReferences(this,r)}if(obj.fixups){applyFixups(r,obj.fixups)}return r};JSONRpcClient.extractCallableReferences=function(B,A){var D,C,E;for(D in A){if(typeof (A[D])=="object"){C=JSONRpcClient.makeCallableReference(B,A[D]);if(C){A[D]=C}else{C=JSONRpcClient.extractCallableReferences(B,A[D]);A[D]=C}}if(typeof (D)=="object"){C=JSONRpcClient.makeCallableReference(B,D);if(C){E=A[D];delete A[D];A[C]=E}else{C=JSONRpcClient.extractCallableReferences(B,D);E=A[D];delete A[D];A[C]=E}}}return A};JSONRpcClient.makeCallableReference=function(A,B){if(B&&B.objectID&&B.javaClass&&B.JSONRPCType=="CallableReference"){return new JSONRpcClient(A.serverURL,A.user,A.pass,B.objectID,B.javaClass,B.JSONRPCType)}return null};JSONRpcClient.http_spare=[];JSONRpcClient.http_max_spare=8;JSONRpcClient.poolGetHTTPRequest=function(){if(JSONRpcClient.http_spare.length>0){return JSONRpcClient.http_spare.pop()}return JSONRpcClient.getHTTPRequest()};JSONRpcClient.poolReturnHTTPRequest=function(A){if(JSONRpcClient.http_spare.length>=JSONRpcClient.http_max_spare){delete A}else{JSONRpcClient.http_spare.push(A)}};JSONRpcClient.msxmlNames=["MSXML2.XMLHTTP.6.0","MSXML2.XMLHTTP.3.0","MSXML2.XMLHTTP","MSXML2.XMLHTTP.5.0","MSXML2.XMLHTTP.4.0","Microsoft.XMLHTTP"];JSONRpcClient.getHTTPRequest=function(){try{JSONRpcClient.httpObjectName="XMLHttpRequest";return new XMLHttpRequest()}catch(B){}for(var A=0;A<JSONRpcClient.msxmlNames.length;A++){try{JSONRpcClient.httpObjectName=JSONRpcClient.msxmlNames[A];return new ActiveXObject(JSONRpcClient.msxmlNames[A])}catch(B){}}JSONRpcClient.httpObjectName=null;throw new JSONRpcClient.Exception(0,"Can't create XMLHttpRequest object")}