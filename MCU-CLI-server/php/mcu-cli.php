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
		exit(1);
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
		exit(1);
	}
} else {
	msg("First time run detected, starting with fresh cache");
	$cache = array();
}

libxml_use_internal_errors();	// suppress xml error spam

// fetch updated serverpack
$pack_xml = file_get_contents($pack_url);
if( $pack_xml === FALSE ) {
	msg("Unable to read pack from $pack_url", true);
	exit(1);
} else {
	msg("Read serverpack from $pack_url");
}
$pack = simplexml_load_string($pack_xml);
if( $pack === FALSE ) {
	msg("Unable to parse malformed XML", true);
	print_r(libxml_get_errors());
	exit(1);
}

// identify our desired server entry
function identify_server($server, $id) {
	if( $id == $server->attributes()->id )
		return $server;
	return false;
}

if( !$pack->Server ) {
	msg("Unable to find any <Server/> directive in xml", true);
	exit(2);
}
if( is_array($pack->Server) ) {
	foreach ($pack->Server as $key => $val) {
		$base = identify_server($val, $pack_server_id);
		if( $base ) 
			break;
	}
} else {
	$base = identify_server($pack->Server, $pack_server_id);
}
if( !$base ) {
	msg("Unable to find server id $pack_server_id", true);
	exit(2);
}

// check if we need to change
if( ($got_version = (string)$base->attributes()->version) != $cache["version"]) {
	msg("Identified new pack version $got_version");
	// TODO: update pack
} else {
	msg("Pack version $got_version matches current rev, not updating");
}

// flush cache to disk
$cache["version"] = $got_version;
$cache_json = json_encode($cache);
file_put_contents($mcu_cache_filename, $cache_json);

// TODO: back up existing world data

// start server
if( $server_autostart ) {
	msg("Starting server...");
	if( file_exists($server_jar) ) {
		exec($java_bin . " -Xms".$server_memory_min . " -Xmx".$server_memory_max . " -jar ".$server_jar . " " . $server_args);
	} else {
		msg("Unable to locate $server_jar", true);
	}
}