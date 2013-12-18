package org.mcupdater.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.mcupdater.MCUSettings;
import org.mcupdater.MainShell;
import org.mcupdater.util.MCUpdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SettingsManager {

	private static SettingsManager instance;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Settings settings;
	private Path configFile = MCUpdater.getInstance().getArchiveFolder().resolve("config.json");
	private boolean dirty = false;
	
	public SettingsManager() {
		if (!configFile.toFile().exists()) {
			System.out.println("New config file does not exist!");
			File oldConfig = MCUpdater.getInstance().getArchiveFolder().resolve("config.properties").toFile();
			if (oldConfig.exists()) {
				System.out.println("Importing old config file");
				this.settings = convertOldSettings(oldConfig);
			} else {
				System.out.println("Creating default config");
				this.settings = getDefaultSettings();  
			}
			saveSettings();
			return;
		}
		System.out.println("Loading config");
		loadSettings();
	}

	public void loadSettings() {
		try {
			BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);
			this.settings = gson.fromJson(reader, Settings.class);
			reader.close();
			this.dirty=false;
			MCUSettings.setState(false);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reload() {
		loadSettings();
		MainShell.getInstance().processSettings();
	}
	
	private Settings convertOldSettings(File oldConfigFile) {
		Settings newSettings = new Settings();
		Properties oldConfig = new Properties();
		try {
			oldConfig.load(new FileInputStream(oldConfigFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		newSettings.setMinMemory(oldConfig.getProperty("minimumMemory","512M"));
		newSettings.setMaxMemory(oldConfig.getProperty("maximumMemory","1G"));
		newSettings.setPermGen(oldConfig.getProperty("permGen","128M"));
		newSettings.setResWidth(Integer.parseInt(oldConfig.getProperty("width","1280")));
		newSettings.setResHeight(Integer.parseInt(oldConfig.getProperty("height","720")));
		newSettings.setFullScreen(false);
		newSettings.setJrePath(oldConfig.getProperty("jrePath",System.getProperty("java.home")));
		newSettings.setJvmOpts(oldConfig.getProperty("jvmOpts","-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+AggressiveOpts"));
		newSettings.setInstanceRoot(oldConfig.getProperty("instanceRoot",MCUpdater.getInstance().getArchiveFolder().resolve("instances").toString()));
		newSettings.setProgramWrapper(oldConfig.getProperty("jvmContainer",""));
		newSettings.setTimeoutLength(Integer.parseInt(oldConfig.getProperty("timeoutLength","5000")));
		newSettings.setAutoConnect(Boolean.parseBoolean(oldConfig.getProperty("allowAutoConnect","true")));
		newSettings.setMinimizeOnLaunch(Boolean.parseBoolean(oldConfig.getProperty("minimizeOnLaunch","true")));
		Path oldServers = MCUpdater.getInstance().getArchiveFolder().resolve("mcuServers.dat");
		if (oldServers.toFile().exists()){
			try {
				BufferedReader reader = Files.newBufferedReader(oldServers, StandardCharsets.UTF_8);
				String entry = reader.readLine();
				while(entry != null) {
					newSettings.addPackURL(entry);
					entry = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			newSettings.addPackURL(MainShell.getInstance().getDefaultPack());
		}
		return newSettings;
	}

	public Settings getDefaultSettings() {
		Settings newSettings = new Settings();
		newSettings.setMinMemory("512M");
		newSettings.setMaxMemory("1G");
		newSettings.setPermGen("128M");
		newSettings.setResWidth(1280);
		newSettings.setResHeight(720);
		newSettings.setFullScreen(false);
		newSettings.setJrePath(System.getProperty("java.home"));
		newSettings.setJvmOpts("-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+AggressiveOpts");
		newSettings.setInstanceRoot(MCUpdater.getInstance().getArchiveFolder().resolve("instances").toString());
		newSettings.setProgramWrapper("");
		newSettings.setTimeoutLength(5000);
		newSettings.setAutoConnect(true);
		newSettings.setMinimizeOnLaunch(true);
		newSettings.addPackURL(MainShell.getInstance().getDefaultPack());
		return newSettings;
	}

	public Settings getSettings() {
		return settings;
	}

	public static SettingsManager getInstance() {
		if (instance == null) {
			instance = new SettingsManager();
		}
		return instance;
	}

	public void saveSettings() {
		String jsonOut = gson.toJson(this.settings);
		try {
			BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8);
			writer.append(jsonOut);
			writer.close();
			this.dirty = false;
			MCUSettings.setState(false);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty() {
		this.dirty = true;
		MCUSettings.setState(true);
	}
}
