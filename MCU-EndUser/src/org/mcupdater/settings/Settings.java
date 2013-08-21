package org.mcupdater.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings {
	private Profile[] profiles;
	private String lastProfile;
	private String minMemory;
	private String maxMemory;
	private String permGen;
	private int resWidth;
	private int resHeight;
	private boolean fullScreen;
	private String jrePath;
	private String jvmOpts;
	private String instanceRoot;
	private String programWrapper;
	private int timeoutLength;
	private boolean autoConnect;
	private boolean minimizeOnLaunch;
	private List<String> packURLs = new ArrayList<String>();

	public Profile[] getProfiles() {
		return profiles;
	}

	public void setProfiles(Profile[] profiles) {
		this.profiles = profiles;
	}

	public String getLastProfile() {
		return lastProfile;
	}

	public void setLastProfile(String lastProfile) {
		this.lastProfile = lastProfile;
	}

	public String getMinMemory() {
		return minMemory;
	}

	public void setMinMemory(String minMemory) {
		this.minMemory = minMemory;
	}

	public String getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(String maxMemory) {
		this.maxMemory = maxMemory;
	}

	public String getPermGen() {
		return permGen;
	}

	public void setPermGen(String permGen) {
		this.permGen = permGen;
	}

	public int getResWidth() {
		return resWidth;
	}

	public void setResWidth(int resWidth) {
		this.resWidth = resWidth;
	}

	public int getResHeight() {
		return resHeight;
	}

	public void setResHeight(int resHeight) {
		this.resHeight = resHeight;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public String getJrePath() {
		return jrePath;
	}

	public void setJrePath(String jrePath) {
		this.jrePath = jrePath;
	}

	public String getJvmOpts() {
		return jvmOpts;
	}

	public void setJvmOpts(String jvmOpts) {
		this.jvmOpts = jvmOpts;
	}

	public String getInstanceRoot() {
		return instanceRoot;
	}

	public void setInstanceRoot(String instanceRoot) {
		this.instanceRoot = instanceRoot;
	}

	public String getProgramWrapper() {
		return programWrapper;
	}
	
	public void setProgramWrapper(String programWrapper) {
		this.programWrapper = programWrapper;
	}
	
	public int getTimeoutLength() {
		return timeoutLength;
	}
	
	public void setTimeoutLength(int timeoutLength) {
		this.timeoutLength = timeoutLength;
	}
	
	public boolean isAutoConnect() {
		return autoConnect;
	}

	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	public boolean isMinimizeOnLaunch() {
		return minimizeOnLaunch;
	}

	public void setMinimizeOnLaunch(boolean minimizeOnLaunch) {
		this.minimizeOnLaunch = minimizeOnLaunch;
	}

	public List<String> getPackURLs() {
		return packURLs;
	}
	
	public void addPackURL(String newUrl) {
/*		ArrayList<String> urls;
		if (this.packURLs.length > 0) {
			urls = new ArrayList<String>(Arrays.asList(this.packURLs));
		} else {
			urls = new ArrayList<String>();
		}
		urls.add(newUrl);
		this.packURLs = urls.toArray(new String[0]);
		*/
		this.packURLs.add(newUrl);
	}
}
