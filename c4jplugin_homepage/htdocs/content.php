<?php
$category = $_GET['category'];
$page = $_GET['page'];
$expanded = $_GET['expanded'];
$url_base = $_GET['url_base'];

$items = array();
if ($expanded && strlen($expanded) > 0) {
	$items = explode(",", $expanded);
}


if (!$category || $category == "") {
	$category = "c4j_main";
	$page = "news.html";	
}

if (!$page || $page == "") {
	$page = "news.html";
}

?>

<link rel="stylesheet" href="<?php echo $url_base; ?>css/custom_layout.css" type="text/css"/>
<!--[if lte IE 6]>
<style type="text/css">
#sidebar ul.arrow {
	padding-left: 30px;
}
</style>
<![endif]-->


<script language="JavaScript" type="text/javascript">
function adapt_c4jlink(category, page) {
	
	if (category == "extern") {
		window.open(page, "_blank");
	}
	else {
		var link = "index.php?category=" + category;
		
		var expanded = getExpandedItems();
		if (expanded && expanded.length > 0) {
			link += "&expanded=" + expanded;
		}
		
		link += "&page=" + page;
		
		location.href = link;
	}
	
};
</script>
  

<div id="page">
	
	<div id="top">
		<div id="logo"><img src="<?php echo $url_base; ?>logo.png"/></div><h1>C4J <br/>Eclipse Plugin</h1>
	</div>
	
	<div id="main">
		<div id="content">
			<?php
				$type = strtolower(substr($page, -3));
				if ($type == "txt") echo '<pre>';
				if ($category == "c4j_main") {
					require $page;
					if ($type == "txt") echo '</pre>';
				}
				else if ($category == "c4j_help") {
					echo '<link rel="stylesheet" href="http://c4jplugin.sourceforge.net/css/book.css" type="text/css"/>';
					require $page;
					if ($type == "txt") echo '</pre>';
					echo '<link rel="stylesheet" href="http://c4jplugin.sourceforge.net/css/help.css" type="text/css"/>';
				}
			?>
		</div>
	</div>
		
	<div id="sidebar">
		<div id="section">
			<h2>Main</h2>
			<ul class="arrow">
				<li><a onclick="adapt_link('c4j_main', 'home.html'); return false;" href="home.html">Home</a></li>
				<li><a onclick="adapt_link('c4j_main', ''); return false;" href="index.php">News</a></li>
				<li><a href="http://sourceforge.net/projects/c4jplugin" target=_blank>Sourceforge Page</a></li>
				<li><a href="http://c4j.sourceforge.net" target=_blank>C4J Home Page</a></li>
			</ul>
			
		</div>
		
		<div id="section">
			<h2>C4J Plugin</h2>
			<ul class="arrow">
				<li><a onclick="adapt_link('c4j_main', 'c4jplugin/download.html'); return false;" href="c4jplugin/download.html">Download</a></li>
				<li><a onclick="adapt_link('c4j_main', 'c4jplugin/todo.html'); return false;" href="c4jplugin/todo.html">Todo</a></li>
			</ul>
			
			<ul id="tmenu0" style="display:none;">

				
				<li menuid="userguide" <?php if (in_array("userguide", $items)) echo "expanded=1"; ?>><span><b>C4J Plug-in User Guide</b></span>
					<ul>
					<li><a onclick="adapt_link('c4j_help', 'c4jplugin/doc/overview.html'); return false;" href="c4jplugin/doc/overview.html">OverView</a></li>
					<li><a onclick="adapt_link('c4j_help', 'c4jplugin/doc/whats_new.html'); return false;" href="c4jplugin/doc/whats_new.html">What's New</a></li>
					<li><a onclick="adapt_link('c4j_help', 'c4jplugin/doc/legal.html'); return false;" href="c4jplugin/doc/legal.html">Legal</a></li>
					</ul>
				</li>
			</ul>
		</div>
		
		<div id="section">
			<h2>C4J Plugin Runtime</h2>
			<ul class="arrow">
				<li><a onclick="adapt_link('c4j_main', 'runtime/download.html'); return false;" href="runtime/download.html">Download</a></li>
			</ul>
			<ul id="tmenu0" style="display:none;">
				
				<li  menuid="libraryguide" <?php if (in_array("libraryguide", $items)) echo "expanded=1"; ?>><span><b>C4J Library Guide</b></span>
					<ul>
					<li><a onclick="adapt_link('c4j_help', 'runtime/doc/overview.html'); return false;" href="runtime/doc/overview.html">OverView</a></li>
					<li><a onclick="adapt_link('c4j_help', 'runtime/doc/features.html'); return false;" href="runtime/doc/features.html">Features</a></li>
					<li><a onclick="adapt_link('c4j_help', 'runtime/doc/whats_new.html'); return false;" href="runtime/doc/whats_new.html">What's New</a></li>

					<li menuid="started" <?php if (in_array("started", $items)) echo "expanded=1"; ?>><span>Getting Started</span>
						<ul>
						<li><a onclick="adapt_link('c4j_help', 'runtime/doc/getting_started/contracts_targets.html'); return false;" href="runtime/doc/getting_started/contracts_targets.html">Contracts and Targets</a></li>
						<li><a onclick="adapt_link('c4j_help', 'runtime/doc/getting_started/class_invariants.html'); return false;" href="runtime/doc/getting_started/class_invariants.html">Class Invariants</a></li>
						<li><a onclick="adapt_link('c4j_help', 'runtime/doc/getting_started/conditions.html'); return false;" href="runtime/doc/getting_started/conditions.html">Pre and Post Conditions</a></li>
						<li><a onclick="adapt_link('c4j_help', 'runtime/doc/getting_started/target_members.html'); return false;" href="runtime/doc/getting_started/target_members.html">Target Members</a></li>
						<li><a onclick="adapt_link('c4j_help', 'runtime/doc/getting_started/contract_inheritance.html'); return false;" href="runtime/doc/getting_started/contract_inheritance.html">Contract Inheritance</a></li>
						<li><a onclick="adapt_link('c4j_help', 'runtime/doc/getting_started/logging.html'); return false;" href="runtime/doc/getting_started/logging.html">Logging</a></li>
						</ul>
					</li>
					
					<li><a onclick="adapt_link('c4j_help', 'runtime/doc/examples.html'); return false;" href="runtime/doc/examples.html">Examples</a></li>
					<li><a onclick="adapt_link('c4j_help', 'runtime/doc/running.html'); return false;" href="runtime/doc/running.html">Running</a></li>
					<li><a onclick="adapt_link('c4j_help', 'runtime/doc/under_the_hood.html'); return false;" href="runtime/doc/under_the_hood.html">Under the Hood</a></li>
			
					<li menuid="refs" <?php if (in_array("refs", $items)) echo "expanded=1"; ?>><span>References</span>
						<ul>
						<li menuid="apiref" <?php if (in_array("apiref", $items)) echo "expanded=1"; ?>><span>API Reference</span>
							<ul>
							<li><a href="runtime/doc/javadoc/net/sourceforge/c4j/package-summary.html" target=_blank>net.sourceforge.c4j</a></li>
							</ul>
						</li>
						<li><a onclick="adapt_link('c4j_help', 'runtime/doc/references/c4j_arguments.html'); return false;" href="runtime/doc/references/c4j_arguments.html">C4J Arguments</a></li>
						</ul>
					</li>
			
					<li><a onclick="adapt_link('c4j_help', 'runtime/doc/legal.html'); return false;" href="runtime/doc/legal.html">Legal</a></li>
					</ul>
				</li>
			</ul>
		</div>
		
		<div id="logo">
			<a href="http://sourceforge.net" target=_blank><img src="http://sflogo.sourceforge.net/sflogo.php?group_id=173673&amp;type=2" width="125" height="37" alt="SourceForge.net Logo" /></a>
		</div>
		<div id="logo">
			<a href="http://www.eclipse.org" target=_blank><img src="<?php echo $url_base; ?>images/eclipse_logo.jpg" alt="Eclipse.org" /></a>
		</div>
	</div>
	
	<div id="footer">
		<p>Written by Sascha Zelzer &copy; 2006</p>
	</div>
	
	</div>
	
	<script language="JavaScript" src="<?php echo $url_base; ?>tmenu_attributes.php?url_base=<?php echo $url_base; ?>"></script>
	<script language="JavaScript" src="<?php echo $url_base; ?>tmenu_code.js"></script>
