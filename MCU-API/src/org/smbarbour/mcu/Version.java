package org.smbarbour.mcu;

import java.io.IOException;
import java.util.Properties;

public class Version {
	public static final int BUILD_VERSION;
	public static final String BUILD_BRANCH;
	public static final String BUILD_LABEL;
	static {
		Properties prop = new Properties();
		try {
			prop.load(Version.class.getResourceAsStream("/version.properties"));
		} catch (IOException e) {
		}
		BUILD_VERSION = Integer.valueOf(prop.getProperty("build_version","0"));
		BUILD_BRANCH = prop.getProperty("git_branch","unknown");
		if( BUILD_BRANCH.equals("unknown") || BUILD_BRANCH.equals("master") ) {
			BUILD_LABEL = "";
		} else {
			BUILD_LABEL = " ("+BUILD_BRANCH+")";
		}
	}
	
	public static final int MAJOR_VERSION = 2;
	public static final int MINOR_VERSION = 2;
	public static final String VERSION = "v"+MAJOR_VERSION+"."+MINOR_VERSION+"."+BUILD_VERSION;
	
	public static boolean isVersionOld(String packVersion) {
		if( packVersion == null ) return false;	// can't check anything if they don't tell us
		String parts[] = packVersion.split("\\.");
		try {
			int mcuParts[] = { MAJOR_VERSION, MINOR_VERSION, BUILD_VERSION };
			for( int q = 0; q < mcuParts.length && q < parts.length; ++q ) {
				int packPart = Integer.valueOf(parts[q]);
				if( packPart < mcuParts[q] )
					return false;
			}
			return true;
		} catch( NumberFormatException e ) {
			log("Got non-numerical pack format version '"+packVersion+"'");
		} catch( ArrayIndexOutOfBoundsException e ) {
			log("Got malformed pack format version '"+packVersion+"'");
		}
		return false;
	}
	
	public static boolean isMasterBranch() {
		return BUILD_BRANCH.equals("master");
	}
	public static boolean isDevBranch() {
		return BUILD_BRANCH.equals("develop");
	}
	
	// for error logging support
	public static void setApp( MCUApp app ) {
		_app = app;
	}
	private static MCUApp _app;
	private static void log(String msg) {
		if( _app != null ) {
			_app.log(msg);
		} else {
			System.out.println(msg);
		}
	}
}
