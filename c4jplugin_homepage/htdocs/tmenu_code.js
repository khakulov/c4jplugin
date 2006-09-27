ulm_ie=window.showHelp;
ulm_opera=window.opera;
ulm_strict=((ulm_ie || ulm_opera)&&(document.compatMode=="CSS1Compat"));
ulm_mac=navigator.userAgent.indexOf("Mac")+1;
is_animating=false;

cc3=new Object();
cc4=new Object();

cc26=null;
cc28=0;

cc0=document.getElementsByTagName("UL");

for (mi=0;mi<cc0.length;mi++) {
  if (cc1=cc0[mi].id) {
    if (cc1.indexOf("tmenu")>-1) {
      cc1=cc1.substring(5);
      cc2=new window["tmenudata"+cc1];
      cc3["img"+cc1]=new Image();
      cc3["img"+cc1].src=cc2.plus_image;
      cc4["img"+cc1]=new Image();
      cc4["img"+cc1].src=cc2.minus_image;
      if (!(ulm_mac && ulm_ie)) {
        t_cc9=cc0[mi].getElementsByTagName("UL");
        for (mj=0;mj<t_cc9.length;mj++) {
          cc23=document.createElement("DIV");
          cc23.className="uldivs";
          cc23.appendChild(t_cc9[mj].cloneNode(1));
          t_cc9[mj].parentNode.replaceChild(cc23,t_cc9[mj]);
        }
      }

      cc5(cc0[mi].childNodes,cc1+"_",cc2,cc1);
      write_style(cc1,cc2);
      cc0[mi].style.display="block";
    }
  }
};

function cc5(cc9,cc10,cc2,cc11) {
  eval("cc8=new Array("+cc2.pm_width_height+")");
  this.cc7=0;
  for(this.li=0;this.li<cc9.length;this.li++) {
    if (cc9[this.li].tagName=="LI") {
      this.level=cc10.split("_").length-1;
      cc9[this.li].style.cursor="default";
      this.cc12=false;
      this.cc13=cc9[this.li].childNodes;
      for (this.ti=0;this.ti<this.cc13.length;this.ti++) {
        lookfor="DIV";
        if (ulm_mac && ulm_ie)
          lookfor="UL";
        if (this.cc13[this.ti].tagName==lookfor){
          this.tfs=this.cc13[this.ti].firstChild;
          if (ulm_mac && ulm_ie)
            this.tfs=this.cc13[this.ti];
          this.usource=cc3["img"+cc11].src;
          if ((gev=cc9[this.li].getAttribute("expanded"))&&(parseInt(gev))) {
            this.usource=cc4["img"+cc11].src;
          }
          else this.tfs.style.display="none";

          if (cc2.folder_image) {
            create_images(cc2,cc11,cc2.icon_width_height,cc2.folder_image,cc9[this.li]);
            this.ti=this.ti+2;
          }
        
          this.cc14=document.createElement("IMG");
          this.cc14.setAttribute("width",cc8[0]);
          this.cc14.setAttribute("height",cc8[1]);
          this.cc14.className="plusminus";
          this.cc14.src=this.usource;
          this.cc14.onclick=expand;
          this.cc14.onselectstart=function(){return false};
          this.cc14.setAttribute("cc2_id",cc11);
          this.cc15=document.createElement("div");
          this.cc15.style.display="inline";
          this.cc15.style.paddingLeft=cc2.imgage_gap+"px";
          
          cc9[this.li].insertBefore(this.cc15,cc9[this.li].firstChild);
          cc9[this.li].insertBefore(this.cc14,cc9[this.li].firstChild);
        
          this.ti+=2;
          
          new cc5(this.tfs.childNodes,cc10+this.cc7+"_",cc2,cc11);
          this.cc12=1;
        }
        else if (this.cc13[this.ti].tagName=="SPAN") {
          this.cc13[this.ti].onselectstart=function() {
            return false
          };
          this.cc13[this.ti].onclick=expand;
          this.cc13[this.ti].setAttribute("cc2_id",cc11);
          this.cname="cc24";
          if (this.level>1) this.cname="cc25";
          if (this.level>1)
            this.cc13[this.ti].onmouseover=function(){
              this.className="cc25";
            };
          else 
            this.cc13[this.ti].onmouseover=function() {
              this.className="cc24";
            };
          
          this.cc13[this.ti].onmouseout=function(){this.className="";};
        }
      }
  
      if (!this.cc12) {
      if (cc2.document_image) {
        create_images(cc2,cc11,cc2.icon_width_height,cc2.document_image,cc9[this.li]);
      }
      this.cc15=document.createElement("div");
      this.cc15.style.display="inline";
      if (ulm_ie) this.cc15.style.width=cc2.imgage_gap+cc8[0]+"px";
      else this.cc15.style.paddingLeft=cc2.imgage_gap+cc8[0]+"px";

      cc9[this.li].insertBefore(this.cc15,cc9[this.li].firstChild);
    }
    this.cc7++;
  }
}
};

function create_images(cc2,cc11,iwh,iname,liobj){
  eval("tary=new Array("+iwh+")");
  this.cc15=document.createElement("div");
  this.cc15.style.display="inline";
  this.cc15.style.paddingLeft=cc2.imgage_gap+"px";
  
  liobj.insertBefore(this.cc15,liobj.firstChild);
  this.fi=document.createElement("IMG");
  this.fi.setAttribute("width",tary[0]);
  this.fi.setAttribute("height",tary[1]);
  this.fi.setAttribute("cc2_id",cc11);
  this.fi.className="plusminus";
  this.fi.src=iname;
  this.fi.style.verticalAlign="middle";
  this.fi.onclick=expand;
  liobj.insertBefore(this.fi,liobj.firstChild);
};

function expand(){
  if (is_animating)
    return;

  cc18=this.getAttribute("cc2_id");
  cc2=new window["tmenudata"+cc18];
  cc17=this.parentNode.getElementsByTagName("UL");
  
  if(parseInt(this.parentNode.getAttribute("expanded"))) {
    this.parentNode.setAttribute("expanded",0);
    if(ulm_mac && ulm_ie){
      cc17[0].style.display="none";
    }
    else {
      cc27=cc17[0].parentNode;
      cc27.style.overflow="hidden";
      cc26=cc27;
      cc27.style.height=cc17[0].offsetHeight;
      cc27.style.position="relative";
      cc17[0].style.position="relative";
      
      is_animating=1;
      setTimeout("cc29("+(-cc2.animation_jump)+",false,"+cc2.animation_delay+")",0);
    }
    
    this.parentNode.firstChild.src=cc3["img"+cc18].src;
  }
  else {
    this.parentNode.setAttribute("expanded",1);
    if (ulm_mac && ulm_ie) {
      cc17[0].style.display="block";
    }
    else {
      cc27=cc17[0].parentNode;
      cc27.style.height="1px";
      cc27.style.overflow="hidden";
      cc27.style.position="relative";
      cc26=cc27;
      cc17[0].style.position="relative";
      cc17[0].style.display="block";
      cc28=cc17[0].offsetHeight;
      cc17[0].style.top=-cc28+"px";

      is_animating=1;
      setTimeout("cc29("+cc2.animation_jump+",1,"+cc2.animation_delay+")",0);
    }
    this.parentNode.firstChild.src=cc4["img"+cc18].src;
  }
};

function getExpandedItems() {
  var items = "";
  for (i=0;i<cc0.length;i++) {
    if (ulid=cc0[i].id) {
      if (ulid.indexOf("tmenu")>-1) {
        if (!(ulm_mac && ulm_ie)) {
          menu_li=cc0[i].getElementsByTagName("LI");
          for (j=0;j<menu_li.length;j++) {
            if(parseInt(menu_li[j].getAttribute("expanded"))) {
	   	      items += menu_li[j].getAttribute("menuid") + ",";
		    }
          }
        }
      }
    }
  }
  return items;
};

function cc29(inc,expand,delay) {
  cc26.style.height=(cc26.offsetHeight+inc)+"px";
  cc26.firstChild.style.top=(cc26.firstChild.offsetTop+inc)+"px";
  if ((expand &&(cc26.offsetHeight<(cc28)))||(!expand &&(cc26.offsetHeight>Math.abs(inc))))
    setTimeout("cc29("+inc+","+expand+","+delay+")",delay);
  else {
    if(expand) {
      cc26.style.overflow="visible";
      if((ulm_ie)||(ulm_opera && !ulm_strict))
        cc26.style.height="0px";
      else cc26.style.height="auto";
     
      cc26.firstChild.style.top=0+"px";
    }
    else {
      cc26.firstChild.style.display="none";
      cc26.style.height="0px";
    }

    is_animating=false;
  }
};

function write_style(id,cc2) {
  np_refix="#tmenu"+id;
  cc20="<style type='text/css'>";
  cc19="";

  if (ulm_ie)
    cc19="height:0px;font-size:1px;";

  cc20+=np_refix+" {width:100%;"+cc19+"-moz-user-select:none;margin:0px;padding:0px;list-style:none;"+cc2.main_container_styles+"}";
  cc20+=np_refix+" li{white-space:nowrap;list-style:none;margin:0px;padding:0px;"+cc2.main_item_styles+"}";
  cc20+=np_refix+" ul li{"+cc2.sub_item_styles+"}";
  cc20+=np_refix+" ul{list-style:none;margin:0px;padding:0px;padding-left:"+cc2.indent+"px;"+cc2.sub_container_styles+"}";
  cc20+=np_refix+" a{"+cc2.main_link_styles+"}";
  cc20+=np_refix+" a:hover{"+cc2.main_link_hover_styles+"}";
  cc20+=np_refix+" ul a{"+cc2.sub_link_styles+"}";
  cc20+=np_refix+" ul a:hover{"+cc2.sub_link_hover_styles+"}";
  cc20+=".cc24 {"+cc2.main_expander_hover_styles+"}";

  if (cc2.sub_expander_hover_styles)
    cc20+=".cc25 {"+cc2.sub_expander_hover_styles+"}";
  else 
    cc20+=".cc25 {"+cc2.main_expander_hover_styles+"}";

  if (cc2.use_hand_cursor)
    cc20+=np_refix+" li span,.plusminus{cursor:hand;cursor:pointer;}";
  else 
    cc20+=np_refix+" li span,.plusminus{cursor:default;}";

  document.write(cc20+"</style>");
}
