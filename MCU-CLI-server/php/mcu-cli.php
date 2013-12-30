<?php
function msg($str, $error = false) {
	echo ($error?"[!!] ":"[--] ") . $str . "\n";
}

msg("MCU-CLI.php Starting...");
msg(date("r"));

// load config, copying from default if one is not found
$cfg_filename = "mcu-cli-config.php";
if( !file_exists($cfg_filename) ) {
	$cfg_default_filename = "mcu-cli-config.default.php";
	if( file_exists($cfg_default_filename) ) {
		copy( $cfg_default_filename, $cfg_filename );
	}
}