<?php
function msg($str, $error = false) {
	echo ($error?"[!!] ":"[--] ") . $str . "\n";
}

msg("MCU-CLI.php Starting...");
msg(date("r"));

// load config, copying from default if one is not found
$cfg_default_filename = "mcu-cli-config.default.php";
$cfg_filename = "mcu-cli-config.php";
if( !file_exists($cfg_filename) ) {
	if( file_exists($cfg_default_filename) ) {
		copy( $cfg_default_filename, $cfg_filename );
	} else {
		msg("Unable to load settings from default!", true);
		exit 1;
	}
}

if( file_exists($cfg_default_filename) )
	include_once $cfg_default_filename;
include_once $cfg_filename;

// load mcu cached state
if( file_exists($mcu_cache_filename) ) {
	msg("Loading mcu cached settings...");
	$cache_json = file_get_contents($mcu_cache_filename);
	$cache = json_decode($cache_json, true);
	if( $cache === NULL ) {
		msg("Unable to parse cached settings, aborting launch", true);
		msg(json_last_error_msg(), true);
		exit 1;
	}
} else {
	msg("First time run detected, starting with fresh cache");
	$cache = array();
}

// TODO: fetch updated serverpack

// TODO: update pack if cached version differs

// flush cache to disk
$cache_json = json_encode($cache);
file_put_contents($mcu_cache_filename, $cache_json);

// TODO: back up existing world data

// start server
if( $server_autostart ) {
	msg("Starting server...");
	exec($java_bin . " -Xms".$server_memory_min . " -Xmx".$server_memory_max . " -jar ".$server_jar . " " . $server_args);
}