package org.mcupdater;

import j7compat.Path;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.mcupdater.model.GenericModule;
import org.mcupdater.model.ServerList;
import org.mcupdater.mojang.Library;
import org.mcupdater.mojang.MinecraftVersion;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.util.MCUpdater;
import org.mcupdater.util.ServerPackParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MCULogic {
	
	public static void doLaunch(ServerList selected, List<ModuleCheckbox> list, Profile user) throws Exception {
		String playerName;
		String sessionKey;
		{ // Do profile validation
			playerName = user.getName();
			sessionKey = user.getSessionKey();
		}
		MinecraftVersion mcVersion = MinecraftVersion.loadVersion(selected.getVersion());
		String mainClass;
		List<String> args = new ArrayList<String>();
		StringBuilder clArgs = new StringBuilder(mcVersion.getMinecraftArguments());
		List<String> libs = new ArrayList<String>();
		MCUpdater mcu = MCUpdater.getInstance();
		Settings settings = SettingsManager.getInstance().getSettings();
		if (settings.isFullScreen()) {
			clArgs.append(" --fullscreen");
		} else {
			clArgs.append(" --width " + settings.getResWidth() + " --height " + settings.getResHeight());
		}
		if (settings.isAutoConnect() && selected.isAutoConnect()) {
			URI address;
			try {
				address = new URI("my://" + selected.getAddress());
				clArgs.append(" --server " + address.getHost());
				if (address.getPort() != -1) {
					clArgs.append(" --port " + address.getPort());
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		clArgs.append(" --resourcePackDir ${resource_packs}");
		args.add((new Path(settings.getJrePath()).resolve("bin").resolve("java").toString()));
		args.add("-Xms" + settings.getMinMemory());
		args.add("-Xmx" + settings.getMaxMemory());
		args.add("-XX:PermSize=" + settings.getPermGen());
		args.addAll(Arrays.asList(settings.getJvmOpts().split(" ")));
		if (System.getProperty("os.name").startsWith("Mac")) {
			args.add("-Xdock:icon=" + mcu.getArchiveFolder().resolve("assets").resolve("icons").resolve("minecraft.icns").toString());
			args.add("-Xdock:name=Minecraft(MCUpdater)");
		}
		args.add("-Djava.library.path=" + mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("lib").resolve("natives").toString());
		if (!Version.requestedFeatureLevel(selected.getVersion(), "1.6")){
			args.add("-Dminecraft.applet.TargetDirectory=" + mcu.getInstanceRoot().resolve(selected.getServerId()).toString());
		}
		if (!selected.getMainClass().isEmpty()) {
			mainClass = selected.getMainClass();
		} else {
			mainClass = mcVersion.getMainClass();
		}
		for (ModuleCheckbox entry : list) {
			if (entry.isSelected()) {
				if (entry.getModule().getIsLibrary()) {
					libs.add(entry.getModule().getId() + ".jar");
				}
				if (!entry.getModule().getLaunchArgs().isEmpty()) {
					clArgs.append(" " + entry.getModule().getLaunchArgs());
				}
				if (!entry.getModule().getJreArgs().isEmpty()) {
					args.addAll(Arrays.asList(entry.getModule().getJreArgs().split(" ")));
				}
				if (entry.getModule().hasSubmodules()) {
					for (GenericModule sm : entry.getModule().getSubmodules()) {
						if (sm.getIsLibrary()) {
							libs.add(sm.getId() + ".jar");
						}
						if (!sm.getLaunchArgs().isEmpty()) {
							clArgs.append(" " + sm.getLaunchArgs());
						}
						if (!sm.getJreArgs().isEmpty()) {
							args.addAll(Arrays.asList(sm.getJreArgs().split(" ")));
						}
					}
				}
			}
		}
		for (Library lib : mcVersion.getLibraries()) {
			if (lib.validForOS() && !lib.hasNatives()) {
				libs.add(lib.getFilename());
			}
		}
		args.add("-cp");
		StringBuilder classpath = new StringBuilder();
		for (String entry: libs) {
			classpath.append(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("lib").resolve(entry).toString()).append(MCUpdater.cpDelimiter());
		}
		classpath.append(mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("bin").resolve("minecraft.jar").toString());
		args.add(classpath.toString());
		args.add(mainClass);
		String tmpclArgs = clArgs.toString();
		Map<String,String> fields = new HashMap<String,String>();
		StrSubstitutor fieldReplacer = new StrSubstitutor(fields);
		fields.put("auth_player_name", playerName);
		fields.put("auth_session", sessionKey);
		fields.put("version_name", selected.getVersion());
		fields.put("game_directory", mcu.getInstanceRoot().resolve(selected.getServerId()).toString());
		fields.put("game_assets", mcu.getArchiveFolder().resolve("assets").toString());
		fields.put("resource_packs", mcu.getInstanceRoot().resolve(selected.getServerId()).resolve("resourcepacks").toString());
		String[] fieldArr = tmpclArgs.split(" ");
		for (int i = 0; i < fieldArr.length; i++) {
			fieldArr[i] = fieldReplacer.replace(fieldArr[i]);
		}
		args.addAll(Arrays.asList(fieldArr));
		
		MainShell.getInstance().log("Launch args:");
		MainShell.getInstance().log("===============================");
		for (String entry : args) {
			MainShell.getInstance().log(entry);
		}
		MainShell.getInstance().log("===============================");
		final ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(mcu.getInstanceRoot().resolve(selected.getServerId()).toFile());
		pb.redirectErrorStream(true);
		Thread gameThread = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					Process task = pb.start();
					BufferedReader buffRead = new BufferedReader(new InputStreamReader(task.getInputStream()));
					String line;
					while ((line = buffRead.readLine()) != null)
					{
						if( line.length() > 0) {
							//System.out.println(line);
							MainShell.getInstance().consoleWrite(line);
						}
					}
				} catch (Exception e) {
					MainShell.getInstance().consoleWrite(e.getMessage());
				} finally {
					MainShell.getInstance().setPlaying(false);
				}
			}
		});
		gameThread.start();
		MainShell.getInstance().setPlaying(true);

	}

	public static List<ServerList> loadServerList(String defaultUrl) {
		{
			Settings settings = SettingsManager.getInstance().getSettings();
			List<ServerList> slList = new ArrayList<ServerList>();

			Set<String> urls = new HashSet<String>();
			urls.add(defaultUrl);
			urls.addAll(settings.getPackURLs());

			Iterator<String> it = urls.iterator();
			while (it.hasNext()){
				String serverUrl = it.next();
				try {
					Element docEle = null;
					Document serverHeader = ServerPackParser.readXmlFromUrl(serverUrl);
					if (!(serverHeader == null)) {
						Element parent = serverHeader.getDocumentElement();
						if (parent.getNodeName().equals("ServerPack")){
							String mcuVersion = parent.getAttribute("version");
							NodeList servers = parent.getElementsByTagName("Server");
							for (int i = 0; i < servers.getLength(); i++){
								docEle = (Element)servers.item(i);
								ServerList sl = new ServerList(docEle.getAttribute("id"), docEle.getAttribute("name"), serverUrl, docEle.getAttribute("newsUrl"), docEle.getAttribute("iconUrl"), docEle.getAttribute("version"), docEle.getAttribute("serverAddress"), ServerPackParser.parseBoolean(docEle.getAttribute("generateList"), true), ServerPackParser.parseBoolean(docEle.getAttribute("autoConnect"), true), docEle.getAttribute("revision"), ServerPackParser.parseBoolean(docEle.getAttribute("abstract"), false), docEle.getAttribute("mainClass"));
								sl.setMCUVersion(mcuVersion);
								slList.add(sl);
							}					
						} else {
							ServerList sl = new ServerList(parent.getAttribute("id"), parent.getAttribute("name"), serverUrl, parent.getAttribute("newsUrl"), parent.getAttribute("iconUrl"), parent.getAttribute("version"), parent.getAttribute("serverAddress"), ServerPackParser.parseBoolean(parent.getAttribute("generateList"), true), ServerPackParser.parseBoolean(parent.getAttribute("autoConnect"), true), parent.getAttribute("revision"), ServerPackParser.parseBoolean(parent.getAttribute("abstract"), false), parent.getAttribute("mainClass"));
							sl.setMCUVersion("1.0");
							slList.add(sl);
						}
					} else {
						//TODO: Log
						//apiLogger.warning("Unable to get server information from " + serverUrl);
						System.out.println("Unable to get server information from " + serverUrl);
					}
				} catch (Exception e) {
					//TODO: Log
					//apiLogger.log(Level.SEVERE, "General Error", e);
					e.printStackTrace();
				}
			}
			//	String[] arrString = entry.split("\\|");
			//	slList.add(new ServerList(arrString[0], arrString[1], arrString[2]));

			return slList;

		}
	}
}
