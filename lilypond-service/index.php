<?php
$query = stripslashes($_GET[q]);
if (trim($query) == "") {
	$empty = true;
} else {
	$resolution = 101;
	$defaultSize = 16;
	$size = floatval($_GET[s]);
	if ($size > 0) {
		if ($size > 64) {
			$size = 64;
		}
		$resolution *= $size / $defaultSize;
	} else {
		$size = $defaultSize;
	}
	
	$id = hash("sha1", $query);
	$dir = "cache/$id";
	$source = "$dir/ly";
	$output = "$dir/$size";
	$score = "$output.png";
	$settings = parse_ini_file("data/settings.ini");
	
	if (file_exists($source)) {
		if (file_get_contents($source) != $query) { // Hash collision
			$compile = true;
			exec("rm $dir/*");
		} else {
			$compile = !file_exists($score);
		}
	} else {
		$compile = true;
		if (!file_exists("cache")) {
			mkdir("cache");
		}
		mkdir($dir);
	}
	
	if ($compile) {
		file_put_contents($source, $query);
		exec("export DYLD_LIBRARY_PATH=; "
			. "$settings[LilyPondCommand] -o $output -fpng -ddelete-intermediate-files -dresolution=$resolution $source; "
			. "$settings[ConvertCommand] -trim $score $score");
		// TODO render only first page
	}	
}

header('Content-Type: image/png');
$scoreFile = ($empty ? "data/empty.png" : (file_exists($score) ? $score : "data/error.png"));
readfile($scoreFile);
?>