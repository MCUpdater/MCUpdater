package org.mcupdater.settings;

import j7compat.Files;
import j7compat.Path;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.mcupdater.util.MCUpdater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SettingsManager {

	private static SettingsManager instance;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private Settings settings;
	private Path configFile = MCUpdater.getInstance().getArchiveFolder().resolve("config.json");
	
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
			BufferedReader reader = Files.newBufferedReader(configFile);
			this.settings = gson.fromJson(reader, Settings.class);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				BufferedReader reader = Files.newBufferedReader(oldServers);
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
			newSettings.addPackURL(MCUpdater.Customization.getString("InitialServer.text"));
		}
		return newSettings;
	}

	public Settings getDefaultSettings() {
		Settings newSettings = new Settings();
		newSettings.setMinMemory("512M");
		newSettings.setMaxMemory("1GB");
		newSettings.setPermGen("128M");
		newSettings.setResWidth(1280);
		newSettings.setResHeight(720);
		newSettings.setFullScreen(false);
		newSettings.setJrePath(System.getProperty("java.home"));
		if (System.getProperty("os.name").startsWith("Mac")) {
			newSettings.setJrePath("/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0");
		}
		newSettings.setJvmOpts("-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+AggressiveOpts");
		newSettings.setInstanceRoot(MCUpdater.getInstance().getArchiveFolder().resolve("instances").toString());
		newSettings.setProgramWrapper("");
		newSettings.setTimeoutLength(5000);
		newSettings.setAutoConnect(true);
		newSettings.setMinimizeOnLaunch(true);
		newSettings.addPackURL(MCUpdater.Customization.getString("InitialServer.text"));
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
			BufferedWriter writer = Files.newBufferedWriter(configFile);
			writer.append(jsonOut);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
