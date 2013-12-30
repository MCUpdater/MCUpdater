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
function find_server($pack, $server_id) {
	if( !$pack->Server ) {
		msg("Unable to find any <Server/> directive in xml", true);
		return false;
	}
	if( is_array($pack->Server) ) {
		foreach ($pack->Server as $key => $val) {
			$base = identify_server($val, $server_id);
			if( $base ) 
				break;
		}
	} else {
		$base = identify_server($pack->Server, $server_id);
	}
	return $base;
}
$base = find_server($pack, $pack_server_id);
if( !$base ) {
	msg("Unable to find server id $pack_server_id", true);
	exit(2);
}
//print_r($base);

// import all imports
function append_children($base, $child) {
	$dom_base = dom_import_simplexml($base);
	foreach( $child->children() as $key => $val ) {
		$dom_child = dom_import_simplexml($val);
		$dom_child = $dom_base->ownerDocument->importNode($dom_child, TRUE);
		$dom_base->appendChild($dom_child);
	}
}

function parse_import($xml) {
	global $cache, $need_update;
	$url = (string)($xml->attributes()->url);
	if( !$url ) {
		msg("Unable to parse bogus <Import/> node", true);
		msg($xml->asXML(), true);
		return false;
	}
	$import_id = (string)$xml;
	msg("Parsing import of '$import_id' from $url...");
	$import = new SimpleXMLElement($url, 0, true);
	$result = find_server($import, $import_id);
	if( $result ) {
		$old_version = $cache["import.".$import_id];
		$cache["import.".$import_id] = (string)$result->attributes()->version;
		if( $old_version != $cache["import.".$import_id] ) {
			msg("Got new version of $import_id, need update...");
			$need_update = true;
		} else {
			msg("Version of $import_id matches cache, not updating");
		}
	}
	return $result;
}

function import_imports($base) {
	$result = new SimpleXMLElement($base->asXML());
	$imports = $base->Import;
	unset($result->Import);
	if( is_array($imports) ) {
		foreach($imports as $key => $val) {
			$import = parse_import($val);
			if( $import ) {
				append_children($result, $import);
			}
		}
	} else {
		$import = parse_import($imports);
		if( $import ) {
			append_children($result, $import);
		}
	}
	return $result;
}
if( $base->Import ) {
	msg("Handling imports...");
	$base = import_imports($base);
	print_r($base);
}

// check if we need to change
if( ($got_version = (string)($base->attributes()->version)) != $cache["version"]) {
	msg("Identified new pack version $got_version");
	$need_update = true;
} else {
	msg("Pack version $got_version matches current rev, not updating");
}

// perform update if necessary
if( $need_update ) {
	msg("Performing update...");
	// TODO: actually update :)
}

// flush cache to disk
$cache["version"] = $got_version;
$cache_json = json_encode($cache);
file_put_contents($mcu_cache_filename, $cache_json);

// TODO: back up existing world data
if( file_exists("server.properties") ) {

}

// start server
if( $server_autostart ) {
	msg("Starting server...");
	if( file_exists($server_jar) ) {
		exec($java_bin . " -Xms".$server_memory_min . " -Xmx".$server_memory_max . " -jar ".$server_jar . " " . $server_args);
	} else {
		msg("Unable to locate $server_jar", true);
	}
}