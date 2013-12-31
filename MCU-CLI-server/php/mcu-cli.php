#!/usr/bin/env php
<?php
/**
 * MCU-CLI v1
 *
 * This is a brute force script for performing MCU updates on a forge server.
 * It's neither pretty nor efficient, but it will get the job done.
 *
 * 0) Copy this script and the default config into a folder with the minecraft
 *    server jar.
 * 1) Copy the default config to 'mcu-cli-config.php'
 * 2) Edit your new config, updating at a minimum the pack url, server id,
 *    server jar, and memory settings.
 * 3) Execute mcu-cli.php and hope for the best :)
 *
 * - allaryin [2013-12-30]
 */

function msg($str, $error = false) {
	echo ($error?"[!!] ":"[--] ") . $str . "\n";
}

msg("MCU-CLI.php Starting...");
msg(date("r"));

// verify that we have curl enabled
if( !extension_loaded("curl") ) {
	msg("curl extension not found.", true);
	exit(1);
}
// check for zip on our path
$zip_bin = exec( "which unzip", $tmp, $zip_error );
if( $zip_error ) {
	msg("unable to find unzip command in path.", true);
	exit(1);
}

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
	foreach ($pack->Server as $key => $val) {
		$base = identify_server($val, $server_id);
		if( $base ) 
			break;
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
	global $cache, $pack, $need_update;
	$url = (string)($xml->attributes()->url);
	$import_id = (string)$xml;
	msg("Parsing import of '$import_id' from ".($url?$url:"pack")."...");
	if( $url ) {
		$import = new SimpleXMLElement($url, 0, true);
	} else {
		$import = $pack;
	}
	$result = find_server($import, $import_id);
	if( $result ) {
		$old_revision = $cache["import.".$import_id];
		$cache["import.".$import_id] = (string)$result->attributes()->revision;
		if( $old_revision != $cache["import.".$import_id] ) {
			msg("Got new revision of $import_id, need update...");
			$need_update = true;
		} else {
			msg("Version of $import_id matches cache, not updating");
		}
	} else {
		msg("Unable to find requested import.",true);
	}
	return $result;
}

function import_imports($base) {
	$result = new SimpleXMLElement($base->asXML());
	$imports = $base->Import;
	unset($result->Import);
	foreach($imports as $key => $val) {
		$import = parse_import($val);
		if( $import ) {
			append_children($result, $import);
		}
	}
	return $result;
}
if( $base->Import ) {
	msg("Handling imports...");
	$base = import_imports($base);
	//print_r($base);
}

// check if we need to change
if( ($got_revision = (string)($base->attributes()->revision)) != $cache["revision"]) {
	msg("Identified new pack revision $got_revision");
	$need_update = true;
} else {
	msg("Pack revision $got_revision matches current rev, not updating");
}

// perform update if necessary
function download($url) {
	$fname = tempnam(".", "mcu-download-");
	$ch = curl_init($url);
	$fp = fopen($fname, "w");

	curl_setopt($ch, CURLOPT_FILE, $fp);
	curl_setopt($ch, CURLOPT_HEADER, 0);

	curl_exec($ch);
	curl_close($ch);
	fclose($fp);
	return $fname;
}

function parse_module($xml, $is_submod = false) {
	global $zip_bin;

	$attrs = $xml->attributes();
	msg("Parsing ".($is_submod?"Submodule":"Module")." ".$attrs->name);
	if( (string)$attrs->side == "CLIENT" ) {
		msg("  - skipping client-only mod");
		return;
	}

	$type = (string)$xml->ModType;
	if( $type != "Regular" && $type != "Extract" ) {
		msg("  - skipping unsupported ModType $type");
		return;
	}

	$required = (string)$xml->Required == "true";
	if( !$required ) {
		msg("  - skipping optional mod");
		return;
	}

	$id = (string)$attrs->id;
	$md5 = (string)$xml->MD5;

	// check md5
	$cache_file = ($md5?"cache/$md5":"cache/tmp");
	$need_download = true;
	switch( $type ) {
		case "Regular":
			// check md5 against installed mod
			if( $xml->ModPath ) 
				$path = "./".(string)$xml->ModPath;
			else
				$path = "mods/${id}.jar";
			if( file_exists($path) ) {
				$local_md5 = md5_file($path);
				if( $local_md5 == $md5 )
					$need_download = false;
			}
			break;
		case "Extract":
		default:
			// check md5 against download cache
			$need_download = !file_exists($cache_file);
	}

	// download & cache
	if( $need_download ) {
		$url = (string)$xml->URL;
		msg("  + Downloading $url...");
		$tmp = download($url);
		if( $md5 ) {
			// validate the checksum
			$dl_md5 = md5_file($tmp);
			if( $dl_md5 != $md5 ) {
				msg("  ! MD5 mismatch on downloaded file", true);
				unlink($tmp);
				return;
			}
		} else {
			msg("  - no MD5 specified, trusting download");
		}
		rename($tmp, $cache_file);


		msg("  - Installing to $path");
		switch( $type ) {
			case "Extract":
				if( (string)($xml->ModType->attributes()->inRoot == "true") )
					$dir = ".";
				else
					$dir = "mods";
				$cmd = "$zip_bin $cache_file -d $dir";
				msg("  - \$ $cmd");
				passthru($cmd);
				break;
			case "Regular":
				// make sure the target dir exists
				$dir = dirname($path);
				if( !is_dir($dir) ) {
					msg("  - Creating directory $dir");
					if( !@mkdir($dir, 0755, true) ) {
						msg("  ! mkdir failed", true);
						print_r(error_get_last());
						return;
					}
				}
				copy($cache_file, $path);
		}
	} else {
		msg("  - Skipping, cache hit.");
	}

	// TODO: parse configfiles
	
	// check for submods
	if( !$is_submod && $xml->Submodule ) {
		foreach( $xml->Submodule as $key => $val ) {
			parse_module($val, true);
		}
	}
}

if( $need_update ) {
	msg("Performing update...");
	// verify that we have a download cache dir
	@mkdir("cache", 0755);
	@mkdir("mods", 0755);
	@mkdir("config", 0755);
	// actually update :)
	if( !$base->Module ) {
		msg("No modules defined?!", true);
	} else {
		foreach( $base->Module as $key => $val ) {
			parse_module($val);
		}
	}
}

// flush cache to disk
$cache["revision"] = $got_revision;
$cache_json = json_encode($cache);
file_put_contents($mcu_cache_filename, $cache_json);

// TODO: back up existing world data
if( file_exists("server.properties") ) {

}

// start server
if( $server_autostart ) {
	msg("Starting server...");
	if( file_exists($server_jar) ) {
		if( function_exists("pcntl_exec") ) {
			$args = array(
				"-Xms".$server_memory_min,
				"-Xmx".$server_memory_max,
				"-jar", $server_jar);
			pcntl_exec( $java_bin, array_merge($args,$server_args) );
		} else {
			exec($java_bin . " -Xms".$server_memory_min . " -Xmx".$server_memory_max . " -jar ".$server_jar . " " . $server_args);
		}
	} else {
		msg("Unable to locate $server_jar", true);
	}
}
