package org.smbarbour.mcu.util;

import java.util.HashMap;
import java.util.Map;

public enum Localization {
	INSTANCE;
	
	private Map<String,String> dict;
	
	private Localization() {
		dict = new HashMap<String,String>();
		// TODO: read loc from a properties file
		// TODO: localize all the strings
		
		dict.put("hdr_Manage_Servers", "Manage Servers");
		
		dict.put("hdr_Options", "Options");
		dict.put("tip_Min_Memory", "<html>Minimum RAM that Minecraft will use. 1G is usually sufficient.<br/>"+
					"Should not exceed max memory for obvious reasons.</html>");
		dict.put("tip_Max_Memory", "<html>Maximum RAM that Minecraft will use. 2G is usually sufficient.<br/>"+
					"On 32-bit systems, you will not be able to specify more than 3G.<br/>"+
					"Ridiculous amounts of RAM (more than 4G) may work on 64-bit<br/>"+
					"systems but probably won't actually help you any.</html>");
		dict.put("tip_Max_PermGen", "<html>PermGen space used by the Minecraft process.<br/>Only change this if you know what you're doing.</html>");
		dict.put("tip_JRE_Path", "Path to the JRE you wish to launch Minecraft with.");
		dict.put("tip_JVM_Options", "<html>Additional advanced JVM settings.<br/>Only edit if you know what you're doing.</html>");
		
		dict.put("btn_Update", "Update");
		dict.put("tip_Update", "Download any changes to this serverpack.");
		dict.put("btn_Hard_Update", "Perform \"hard\" update");
		dict.put("tip_Hard_Update", "Delete this instance's folder completely before updating.");
		dict.put("btn_Launch", "Launch Minecraft");
		dict.put("tip_Launch", "Launch Minecraft using the currently selected configuration.");
		
		dict.put("tip_StorePassword", "<html>Enabling this will store the password for use in the future using the same<br/>encryption method that the vanilla launcher uses.<br/><br/>Encryption in such a way as this requires is not particularly secure,<br/>nor can it be by virtue of how it must be implemented.");
		
		dict.put("Default_News", "<HTML><BODY>Please select an instance from the list on the left.</BODY></HTML>");
	}
	
	public static String getText( final String token ) {
		// TODO: add parameterized loc strings
		if( !INSTANCE.dict.containsKey(token) ) {
			INSTANCE.dict.put(token, "**"+token+"**");
		}
		return INSTANCE.dict.get(token);
	}	
}