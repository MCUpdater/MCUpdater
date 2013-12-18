package org.mcupdater.mojang;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/*
 * Implementation of version.json format used by Minecraft's launcher
 */
public class MinecraftVersion {
	private String id;
	private String time;
	private String releaseTime;
	private String type;
	private String minecraftArguments;
	private int minimumLauncherVersion;
	private List<Library> libraries;
	private String mainClass;
	private String incompatibilityReason;
	private String assets;
	private List<Rule> rules;
	
	public String getId(){ return id; }
	public String getTime(){ return time; }
	public String getReleaseTime(){ return releaseTime; }
	public String getType(){ return type; }
	public String getMinecraftArguments(){ return minecraftArguments; }
	public int getMinimumLauncherVersion(){ return minimumLauncherVersion; }
	public List<Library> getLibraries(){ return libraries; }
	public String getMainClass(){ return mainClass; }
	public String getIncompatibilityReason(){ return incompatibilityReason; }
	public String getAssets() { return this.assets; }
	public List<Rule> getRules(){ return rules; }
	
	public static MinecraftVersion loadVersion(String version) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.enableComplexMapKeySerialization();
		Gson gson = builder.create();
		
		URLConnection conn;
		try {
			conn = (new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + version + "/" + version + ".json")).openConnection();
			return gson.fromJson(new InputStreamReader(conn.getInputStream()),MinecraftVersion.class);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
