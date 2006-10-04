function iao_iframefix(){
	if(ulm_ie&&!ulm_mac){
		for(var i=0;i<(x32=uld.getElementsByTagName("iframe")).length;i++){ 
			if((a=x32[i]).getAttribute("x31")){
				a.style.height=(x33=a.parentNode.getElementsByTagName("UL")[0]).offsetHeight;
				a.style.width=x33.offsetWidth;
			}
		}
	}
};
			
function iao_ifix_add(b){
	if(ulm_ie&&!ulm_mac&&!ulm_oldie&&!ulm_ie7&&window.iao_iframefix)
		b.parentNode.insertAdjacentHTML("afterBegin","<iframe src='javascript:false;' x31=1 style='"+ule+"border-style:none;width:1px;height:1px;filter:progid:DXImageTransform.Microsoft.Alpha(Opacity=0);' frameborder='0'></iframe>");
};

function iao_hideshow(){
	if(b=window.iao_free) b();

	// ulm_boxa.dto["unlock"]
	s1a=ulm_boxa.dto["unlock"];
	if(!s1a) s1a="";

	//s1a=x37(s1a);
	//s1a = "visible";
	// location.hostname
	if((ml=location.hostname)){
		if(s1a.length>2){
			for(i in(sa=s1a.split(":")))
				if((s1a=='visible')||(ml.toLowerCase().indexOf(sa[i])+1))
					return;
		}
		
		alert("Infinite Menus must be purchased for internet use.");
	}
};

function x37(st){
	return st.replace(/./g,x38);
};

function x38(a,b){
	return String.fromCharCode(a.charCodeAt(0)-1-(b-(parseInt(b/4)*4)));
};

function imenus_expandani_init(obj,dto){
	var tid=obj.getElementsByTagName("UL")[0].id.substring(6);
	if(!(ulm_navigator&&ulm_mac)&&!(ulm_safari&&!window.XMLHttpRequest)&&!ulm_iemac&&!dto.expand_arrow_animation_disabled){
		ulm_boxa["expani_go"+tid]=1;
		ulm_boxa.expani_go=1;
	}
	else return;

	if(!ulm_mglobal.expani_lid) ulm_mglobal.expani_lid=new Object();
	if(!ulm_mglobal.expani_t) ulm_mglobal.expani_t=new Object();
	if(window.attachEvent){
		document.attachEvent("onmouseover",imenus_expandani_bodyover);
		obj.attachEvent("onmouseover",imenus_kille);
	}
	else {
		document.addEventListener("mouseover",imenus_expandani_bodyover,false);
		obj.addEventListener("mouseover",imenus_kille,false);
	}
};

function imenus_kille(event,stop_def){
	event=event||window.event;event.cancelBubble=1;
	if(!stop_def&&event.preventDefault) event.preventDefault();
	if(event.stopPropagation) event.stopPropagation();
	if(!stop_def) return false;
};

function imenus_expandani_bodyover(event,lev){
	if(!ulm_mglobal.activate_onclick&&!ulm_mglobal.design_mode)
		imenus_expandani_hideall(lev)
};

function imenus_expandani_hideall(lev){
	for(i in ulm_mglobal.expani_lid){
		if(lev&&parseInt(i.substring(1))<parseInt(lev)) continue;
		if((uobj=ulm_mglobal.expani_lid[i])){
			if(uobj.getAttribute("running")||lev)
				imenus_expandani_hideit(null,uobj,1,i);
		}
	}
};

function imenus_expandani_animateit(hobj,show){
	var tid=parseInt(hobj.id.substring(6));
	if(!ulm_boxa["expani_go"+tid]) return;
	var lev=hobj.level;
	var uge=ulm_mglobal.expani_lid["l"+lev];
	if(show){
		if(mo=document.getElementById("ea"+hobj.id)){
			if(uge&&(uge!=mo)) imenus_expandani_hideall(lev);
			if(ulm_mglobal.expani_t["l"+lev]) return;
			if(!mo.getAttribute("mframe")||ulm_mglobal.design_mode){
				tx=mo.offsetLeft;
				ty=mo.offsetTop;
				var txy=tx+","+ty;
				if(!tx) tx=0;
				if(!ty) ty=0;
				mo.setAttribute("startxy",tx+","+ty);
				mo.setAttribute("initxy",tx+","+ty);
				var last_xy=new Array(tx,ty);
				var vid=hobj.id.substring(6);
				while(vid.indexOf("z")+1) vid=vid.replace("z","_");
				var ismain=false;
				if((vid.split("_")).length==2) ismain=1;
				var j=1;
				while(xy=imenus_expandani_gparam("expand_arrow_animation_movexy",j,ismain,vid,tid)){
					if((xy.split(",")).length<2){
						j++;
						continue;
					}
					xy=eval("new Array("+xy+")");
					var frames=parseInt(imenus_expandani_gparam("expand_arrow_animation_frames",j,ismain,vid,tid));
					if(!frames) frames=1;
					xy[0]+=tx;xy[1]+=ty;
					mo.setAttribute("xy"+j,xy[0]+","+xy[1]);
					var dx=(xy[0]-last_xy[0])/frames;
					var dy=(xy[1]-last_xy[1])/frames;
					mo.setAttribute("distxy"+j,dx+","+dy);
					last_xy=xy;
					mo.setAttribute("msequences",j);
					mo.setAttribute("mframes"+j,frames);
					j++;
				}
				if(j==1)return;
				mo.setAttribute("mframe",1);
				mo.setAttribute("msequence",1);
			}
			ulm_mglobal.expani_lid["l"+lev]=mo;
			if(ulm_mglobal.expani_t["l"+lev]){
				clearTimeout(ulm_mglobal.expani_t["l"+lev]);
				ulm_mglobal.expani_t["l"+lev]=null;
			}
			mo.setAttribute("startxy",mo.getAttribute("initxy"));
			mo.setAttribute("mframe",1);
			mo.setAttribute("msequence",1);
			imenus_expandani_run(mo,lev);
			return;
		}
		else  if(uge){
			imenus_expandani_hideit(null,uge,1,"l"+lev);
		}
	}
	else {
		if(mo=document.getElementById("ea"+hobj.id)){
			if(!mo.getAttribute("running")){
				if(!(hobj.className.indexOf("ishow")+1))
					imenus_expandani_hideit(null,uge,1,"l"+lev);
			}
		}
	}
};

function imenus_expandani_gparam(name,seq,ismain,id,index){
	if(rv=ulm_boxa["dto"+index]["s"+id+"_"+name+seq]) return rv;
	else  if(ismain&&(rv=ulm_boxa["dto"+index]["main_"+name+seq])) return rv;
	return ulm_boxa["dto"+index][name+seq];
};

function imenus_expandani_hideit(hobj,mo,reset,lev_id){
	if(hobj)mo=document.getElementById("ea"+hobj.id);
	if(mo){
		if(ulm_mglobal.expani_t[lev_id]){
			clearTimeout(ulm_mglobal.expani_t[lev_id]);
			ulm_mglobal.expani_t[lev_id]=null;
		}
		if(reset){
			mo.style.top="";mo.style.left="";
		}
	}
};

function imenus_expandani_run(mo,lev){
	mo.setAttribute("running",1);
	sxy=eval("new Array("+mo.getAttribute("startxy")+")");
	msequence=parseInt(mo.getAttribute("msequence"));
	msequences=parseInt(mo.getAttribute("msequences"));
	mframe=parseInt(mo.getAttribute("mframe"));
	mframes=parseInt(mo.getAttribute("mframes"+msequence));
	dxy=eval("new Array("+mo.getAttribute("distxy"+msequence)+")");
	mo.style.left=sxy[0]+parseInt(mframe*dxy[0])+"px";
	mo.style.top=sxy[1]+parseInt(mframe*dxy[1])+"px";
	if(mframe<mframes){
		mframe++;
		mo.setAttribute("mframe",mframe);
	}
	else  if(msequence<msequences){
		txy=eval("new Array("+mo.getAttribute("xy"+msequence)+")");
		mo.style.left=txy[0]+"px";
		mo.style.top=txy[1]+"px";
		mo.setAttribute("startxy",txy[0]+","+txy[1]);
		mo.setAttribute("mframe",1);
		msequence++;
		mo.setAttribute("msequence",msequence++);
	}
	else {
		mo.removeAttribute("running");
		return;
	}
	ulm_mglobal.expani_t["l"+lev]=setTimeout("imenus_expandani_run(document.getElementById('"+mo.id+"'),"+lev+")",8);
};

function imenus_efix_styles(ni){
	var rv=ni+" li a .imefixh{visibility:hidden;}";
	rv+=ni+" li a .imefix{visibility:inherit;}";
	rv+=ni+" li a.iactive .imefixh{visibility:visible;}";
	rv+=ni+" li a.iactive .imefix{visibility:hidden;}";
	return rv;
};

function imenus_efix(x2){
	if(window.name=="hta"||window.name=="imopenmenu") return;
	ulm_mglobal.eimg_fix=1;
	ulm_mglobal.eimg_sub="";
	ulm_mglobal.eimg_sub_hover="";
	ulm_mglobal.eimg_main="";
	ulm_mglobal.eimg_main_hover="";
	if(ss=document.getElementById("ssimenus"+x2)){
		ss=ss.styleSheet;
		for(i in ss.rules){
			if(a=imenus_efix_strip(ss.rules[i],"#imenus"+x2+" .imeamj DIV"))
				ulm_mglobal.eimg_main=a;
			if(a=imenus_efix_strip(ss.rules[i],"#imenus"+x2+" LI A.iactive .imeamj DIV"))
				ulm_mglobal.eimg_main_hover=a;
			if(a=imenus_efix_strip(ss.rules[i],"#imenus"+x2+" UL .imeasj DIV"))
				ulm_mglobal.eimg_sub=a;
			if(a=imenus_efix_strip(ss.rules[i],"#imenus"+x2+" LI A.iactive .imeasj DIV"))
				ulm_mglobal.eimg_sub_hover=a;
		}
	}      
};

function imenus_efix_strip(rule,selector){
	if(rule.selectorText==selector){
		var t=imenus_efix_stripurl(rule.style.backgroundImage);
		rule.style.backgroundImage="";
		return t;
	}
};

function imenus_efix_stripurl(txt){
	wval=txt.toLowerCase();
	if(wval.indexOf("url(")+1){
		txt=txt.substring(4);
		if((commai=txt.indexOf(")"))>-1)
			txt=txt.substring(0,commai);
	}
	return txt;
};

function imenus_efix_add(level,expdiv){
	var x4="main";
	if(level!=1) x4="sub";
	var ih="";
	if(a=ulm_mglobal["eimg_"+x4+"_hover"])
		ih+='<img class="imefixh" style="position:absolute;" src="'+a+'">';
	if(a=ulm_mglobal["eimg_"+x4]) 
		ih+='<img class="imefix" src="'+a+'">';
	expdiv.firstChild.innerHTML=ih;
}

ht_obj=new Object();
cm_obj=new Object();
uld=document;
ule="position:absolute;";
ulf="visibility:visible;";
ulm_boxa=new Object();
var ulm_d;
ulm_mglobal=new Object();
ulm_rss=new Object();
nua=navigator.userAgent;
ulm_ie=window.showHelp;
ulm_ie7=nua.indexOf("MSIE 7")+1;
ulm_mac=nua.indexOf("Mac")+1;
ulm_navigator=nua.indexOf("Netscape")+1;
ulm_version=parseFloat(navigator.vendorSub);
ulm_oldnav=ulm_navigator&&ulm_version<7.1;
ulm_oldie=ulm_ie&&nua.indexOf("MSIE 5.0")+1;
ulm_iemac=ulm_ie&&ulm_mac;ulm_opera=nua.indexOf("Opera")+1;
ulm_safari=nua.indexOf("afari")+1;

if(!window.vdt_doc_effects) vdt_doc_effects=new Object();

ulm_base="http://www.opencube.com/vim9.1/";
x43="_";
ulm_curs="cursor:hand;";

if(!ulm_ie){
	x43="z";
	ulm_curs="cursor:pointer;";
}

ulmpi=window.imenus_add_pointer_image;
var x44;

for(mi=0;mi<(x1=uld.getElementsByTagName("UL")).length;mi++){
	if((x2=x1[mi].id)&&x2.indexOf("imenus")+1){
		dto=new window["imenus_data"+(x2=x2.substring(6))];
		ulm_boxa.dto=dto;
		ulm_boxa["dto"+x2]=dto;
		ulm_d=dto.menu_showhide_delay;
		if(ulm_ie&&!ulm_ie7&&!ulm_mac&&(b=window.imenus_efix))
			b(x2);
		imenus_create_menu(x1[mi].childNodes,x2+x43,dto,x2);
		(ap1=x1[mi].parentNode).id="imouter"+x2;
		ap1.className=ap1.className.replace("imde","");
		if(!ulm_oldnav&&ulmpi) ulmpi(x1[mi],dto,0,x2);
		x6(x2,dto);
		if((ulm_ie&&!ulm_iemac)&&(b=window.iao_iframefix))
			window.attachEvent("onload",b);
		if(window.name=="hta"){
			ulm_base="";
			if(ls=location.search)
				ulm_base=unescape(ls.substring(1)).replace(/\|/g,".");
		}
		if((window.name=="imopenmenu")||(window.name=="hta")){
			var a='<script language="JavaScript" src="';
			vdt_doc_effects[x1[mi].id]=x1[mi].id.substring(0,6);
			sd=a+ulm_base+'vimenus.js"></script>';
			if(!(winvi=window.vdt_doc_effects).initialized){
				sd+=a+ulm_base+'vdesigntool.js"></script>';
				winvi.initialized=1;
			}
			uld.write(sd);
		}
		//if((b=window.iao_hideshow)&&(ulm_ie&&!ulm_mac))
		//	attachEvent("onload",b);
		if(b=window.imenus_box_ani_init) b(ap1,dto);
		if(b=window.imenus_expandani_init) b(ap1,dto);
		if(b=window.imenus_info_addmsg) b(x2,dto);
	}
};

function imenus_create_menu(nodes,prefix,dto,d_toid,sid,level){
	var counter=0;
	if(sid) counter=sid;
	for(var li=0;li<nodes.length;li++){
		var a=nodes[li];
		if(a.tagName=="LI"){
			a.id="ulitem"+prefix+counter;
			(this.atag=a.getElementsByTagName("A")[0]).id="ulaitem"+prefix+counter;
			var level;
			a.level=(level=prefix.split(x43).length-1);
			a.dto=d_toid;
			a.x4=prefix;
			a.sid=counter;
			if((a1=window.imenus_drag_evts)&&level>1)a1(a,dto);
			a.onkeydown=function(e){
				e=e||window.event;
				if(e.keyCode==13&& !ulm_boxa.go) hover_handle(this,1);
			};
			if(dto.hide_focus_box)this.atag.onfocus=function(){
				this.blur()
			};
			imenus_se(a,dto);
			x30=a.getElementsByTagName("UL");
			for(ti=0;ti<x30.length;ti++){
				var b=x30[ti];
				if(!window.name!="hta"&&window.iao_iframefix&&ulm_ie&&!ulm_ie7&&!ulm_mac)
					b.style.position="absolute";
				if(c=window.iao_ifix_add) c(b);
				if((dd=this.atag.firstChild)&&(dd.tagName=="DIV")&&(dd.className.indexOf("imea")+1)){
					if(ulm_mglobal.eimg_fix)imenus_efix_add(level,dd);
					dd.className=dd.className+"j";
					dd.firstChild.id="ea"+a.id;
					dd.setAttribute("imexpandarrow",1);
				}
				b.id="x1ub"+prefix+counter;
				if(!ulm_oldnav&&ulmpi) ulmpi(b.parentNode,dto,level);
				new imenus_create_menu(b.childNodes,prefix+counter+x43,dto,d_toid);
			}
			if((a1=window.imenus_button_add)&&level==1) a1(this.atag,dto);
			if(!sid&&!ulm_navigator&&!ulm_iemac&&(rssurl=a.getAttribute("rssfeed"))&&(c=window.imenus_get_rss_data))
				c(a,rssurl);
			counter++;
		}
	}
};

function imenus_se(a,dto){
	if(!(d=window.imenus_onclick_events)||!d(a,dto)){
		a.onmouseover=function(e){
			if((a=this.getElementsByTagName("A")[0]).className.indexOf("iactive")==-1)
				a.className="ihover";
			if(ht_obj[this.level])clearTimeout(ht_obj[this.level]);
			if(b=window.imenus_expandani_animateit)b(this,1);
			if(ulm_boxa["go"+parseInt(this.id.substring(6))])
				imenus_box_ani(1,this.getElementsByTagName("UL")[0],this,e);
			else ht_obj[this.level]=setTimeout("hover_handle(uld.getElementById('"+this.id+"'),1)",ulm_d);
		};
		a.onmouseout=function(){
			if((a=this.getElementsByTagName("A")[0]).className.indexOf("iactive")==-1)
				a.className="";
			if(!ulm_boxa["go"+parseInt(this.id.substring(6))]){
				clearTimeout(ht_obj[this.level]);
				ht_obj[this.level]=setTimeout("hover_handle(uld.getElementById('"+this.id+"'))",ulm_d);
			}
		};
	}
};

function hover_handle(hobj,show){
	tul=hobj.getElementsByTagName("UL")[0];
	try{
		if((ulm_ie&&!ulm_mac)&&show&&(plobj=tul.filters[0])&&tul.parentNode.currentStyle.visibility=="hidden"){
			if(x44)x44.stop();
			plobj.apply();
			plobj.play();
			x44=plobj;
		}
	}
	catch(e){}
	if(b=window.iao_apos)b(show,tul,hobj);
	hover_2handle(hobj,show,tul)
};

function hover_2handle(hobj,show,tul,skip){
	if((tco=cm_obj[hobj.level])!=null){
		tco.className=tco.className.replace("ishow","");
		tco.firstChild.className="";
	}
	if(show){
		if(!tul) return;
		hobj.firstChild.className="ihover iactive";
		if(ulm_iemac)hobj.className="ishow";
		else hobj.className+=" ishow ";cm_obj[hobj.level]=hobj;
	}
	else  if(!skip){
		if(b=window.imenus_expandani_animateit) b(hobj);
	}
};

function x27(obj){
	var x=0;var y=0;
	do{
		x+=obj.offsetLeft;
		y+=obj.offsetTop;
	}
	while(obj=obj.offsetParent)
		return new Array(x,y);
};

function x6(id,dto){
	x19="#imenus"+id;sd="<style type='text/css'>";
	di=0;
	while((x21=uld.getElementById("ulitem"+id+x43+di))){
		for(i=0;i<(wfl=x21.getElementsByTagName("SPAN")).length;i++){
			if(wfl[i].getAttribute("imrollimage")){
				wfl[i].onclick=function(){
					window.open(this.parentNode.href,((tpt=this.parentNode.target)?tpt:"_self"))
				};
				var a="#ulaitem"+id+x43+di;
				if(!ulm_iemac){
					var b=a+".ihover .ulmroll ";
					sd+=a+" .ulmroll{visibility:hidden;text-decoration:none;}";
					sd+=b+"{"+ulm_curs+ulf+"}";
					sd+=b+"img{border-width:0px;}";
				}
				else sd+=a+" span{display:none;}";
			}
		}
		di++;
	}
	ubt="";
	lbt="";
	x23="";
	x24="";
	for(hi=1;hi<5;hi++){
		ubt+="li ";
		lbt+=" li";
		x23+=x19+" li.ishow "+ubt+" .imsubc";
		x24+=x19+lbt+".ishow .imsubc";
		if(hi!=4){
			x23+=",";
			x24+=",";
		}
	}
	sd+=x23+"{visibility:hidden;}";
	sd+=x24+"{"+ulf+"}";
	sd+=x19+" li a img{vertical-align:bottom;display:inline;border-width:0px;}";
	if(!ulm_ie7) sd+=".imsc .imsubc{background-image:none;}";
	sd+=x19+" li ul{"+((!window.imenus_drag_evts&&window.name!="hta"&&ulm_ie)?dto.subs_ie_transition_show:"")+"}";
	if(!ulm_oldnav) sd+=".imcm{position:relative}.imcm ul{position:relative}";
	if(ulm_iemac||ulm_safari) sd+=".imsc{position:relative;}";
	if(a1=window.imenus_drag_styles) sd+=a1(id,dto);
	if(a1=window.imenus_info_styles) sd+=a1(id,dto);
	if(ulm_mglobal.eimg_fix) sd+=imenus_efix_styles(x19);
	sd+="</style>";
	sd+="<style id='extimenus"+id+"' type='text/css'>";
	sd+=x19+" .ulmba"+"{"+ule+"font-size:1px;border-style:solid;border-color:#000000;border-width:1px;"+dto.box_animation_styles+"}";
	sd+="</style>";
	uld.write(sd);
};
