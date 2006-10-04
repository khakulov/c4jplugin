<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>Sascha Zelzer</title>

		<link rel="stylesheet" href="css/banner.css" type="text/css"/>
		<link rel="stylesheet" href="css/layout.css" type="text/css"/>
		<link rel="stylesheet" href="css/style.css" type="text/css"/>
		
		<!--[if lte IE 6]>
		<style type="text/css">
		#navgrad{
			background-color: transparent;
			background-image: none;
			filter: progid:DXImageTransform.Microsoft.Gradient(GradientType=1, startColorstr=#a9d3ff, endColorstr=#00000000);
			position: absolute;
			top: 66px;
			left: 490px;
			width: 50px;
			height: 12px;
			padding: 0px;
		}
		
		#navgrad2 {
			background-color: transparent;
			background-image: none;
			filter: progid:DXImageTransform.Microsoft.Gradient(GradientType=1, startColorstr=#a4cffc, endColorstr=#00000000);
			position: absolute;
			top: 78px;
			left: 490px;
			width: 50px;
			height: 12px;
			padding: 0px;
			margin: 0px;
			overflow: hidden;
			display: inline;
		}
		</style>
		<![endif]-->
		
</head>
<script language="JavaScript" type="text/javascript">
function adapt_link(category, page) {
	
	if (category.substring(0,3) == "c4j") {
		adapt_c4jlink(category, page);
	}
	else if (category == "extern") {
		adapt_c4jlink(category, "http://c4jplugin.sourceforge.net/" + page);
	}
	
};
</script>

<body>

<div id="header">

	<div id="banner">
	<!--|**START IMENUS**|imenus0,inline-->


	<!-- ****** Infinite Menus Core CSS [Do Not Modify!] ****** -->
	<link rel="stylesheet" href="css/navigation_core.css" type="text/css">
	<!--[if lte IE 6]>
		<link rel="stylesheet" href="css/navigation_core_ie.css" type="text/css">
	<![endif]-->

	<!-- ***** Infinite Menu CSS Style Settings ***** -->
	<link id="ssimenus0" rel="stylesheet" href="css/navigation.css" type="text/css">


	<!--  ****** Infinite Menus Structure & Links ***** -->
	<div id="navigation" style="width:440px;">
		<div class="imcm imde" id="imouter0">
			<ul id="imenus0">
				<li  style="width:115px;">
					<a href="index.php?category=local&page=pages/about.html"><div class="imea imeam"><div></div></div>About Me</a>

					<div class="imsc"><div class="imsubc" style="width:150px;"><ul style="">
						<li><a href="index.php?category=local&page=pages/about.html">Who am I</a></li>
						<li><a href="download/cv_zelzer_web.pdf">Curriculum Vitae</a></li>
					</ul></div></div>
				</li>


				<li  style="width:140px;"><a href="index.php?category=local_mathphys&page=mathphys/interests.html">Math &amp; Physics</a></li>
				<li  style="width:170px;"><a href="#"><div class="imea imeam"><div></div></div>Software Projects</a>

					<div class="imsc"><div class="imsubc" style="width:170px;top:0px;left:0px;"><ul style="">
						<li title="An Eclipse Plugin for the Design by Contract Library C4J"><a href="index.php?category=c4j_main">C4J Eclipse Plugin</a></li>
						<li title="A versatile collection software"><a href="index.php?category=jml_main">JMediaLibrary</a></li>
						<li title="Cryptography packages for Mathematica"><a href="index.php?category=local&page=pages/crypto.html">Cryptography</a></li>
						<li><a href="#"><div class="imea imeas"><div></div></div>Inactive Projects</a>

							<div class="imsc"><div class="imsubc" style="width:100px;top:-24px;left:150px;"><ul style="">
								<li title="X Elastic Interaction Simulation"><a href="index.php?category=local&page=pages/xeis.html">XEIS</a></li>
								<li title="Light Up X - Controls DMX devices"><a href="index.php?category=local&page=pages/lux.html">LUX</a></li>
							</ul></div></div>
						</li>


					</ul></div></div>
				</li>

			</ul>
			<div class="imclear"><div></div></div>
		</div>
	</div>
	<div id="navgrad"></div><div id="navgrad2"></div>
	</div>
</div>

<?php
$category = $_GET['category'];
$page = $_GET['page'];

if (!$category || $category == "") {
	$category = "local";
	$page = "pages/about.html";
}

//$url_base = "http://localhost/~sascha/homepage/";
$url_base = "http://www.mathi.uni-heidelberg.de/~sascha/";
$content = "";
if (strncmp($category, "local", 5) == 0) {
	switch ($category) {
		case "local_mathphys":
			$content = "pages/mathphys.php";
			break;
		default:
			$content = $page;
			break;
	}
}
else if (strncmp($category, "c4j", 3) == 0) {
	//$url_base = "http://localhost/~sascha/c4jplugin/";
	$url_base = "http://c4jplugin.sourceforge.net/";
	$content = "content.php";
}
else if (strncmp($category, "jml", 3) == 0) {
	$url_base = "http://jmedialibrary.sourceforge.net/";
	$content = "content.php";
}

require($url_base.$content."?url_base=".$url_base."&".$_SERVER['QUERY_STRING']);

?>

</body>
<!--  ****** Infinite Menus Scripted Settings (Hybrid Version) ****** -->
<script language="JavaScript">function imenus_data0(){


	this.unlock = "192.168.178.25"

	this.menu_showhide_delay = 150
	this.show_subs_onclick = false
	this.hide_focus_box = false



   /*---------------------------------------------
   Optional Expand Icon Animation Settings
   ---------------------------------------------*/


	this.expand_arrow_animation_frames1 = "8"
	this.expand_arrow_animation_movexy1 = "-6,0"
	this.expand_arrow_animation_frames2 = "8"
	this.expand_arrow_animation_movexy2 = "0,0"




   /*---------------------------------------------
   IE Transition Effects
   ---------------------------------------------*/


	this.subs_ie_transition_show = "filter:progid:DXImageTransform.Microsoft.Fade(duration=0.3);"





}</script>


<!--  ********************************** Infinite Menus Source Code (Do Not Alter!) **********************************

         Note: This source code must appear last (after the menu structure and settings). -->

<script language="JavaScript" src="js/navigation.js"></script>
<!--  *********************************************** End Source Code ******************************************** -->
<!--|**END IMENUS**|-->



</html>