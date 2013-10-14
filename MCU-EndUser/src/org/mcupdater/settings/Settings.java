package org.mcupdater.settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Settings {
	public enum TextField {
		minMemory,
		maxMemory,
		permGen,
		resWidth,
		resHeight,
		jrePath,
		jvmOpts,
		instanceRoot,
		programWrapper,
		timeoutLength
	}

	private List<Profile> profiles = new ArrayList<Profile>();
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

	public List<Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<Profile> profiles) {
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
		try {
			this.minMemory = minMemory;
		} catch (Exception e) {
			// ignore errors
		}
		
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
		try {
			this.resWidth = resWidth;
		} catch (Exception e) {
			// ignore errors
		}
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
		this.packURLs.add(newUrl);
	}
	
	public void removePackUrl(String oldUrl) {
		this.packURLs.remove(oldUrl);
	}

	public synchronized void addOrReplaceProfile(Profile newProfile) {
		Iterator<Profile> it = new ArrayList<Profile>(this.profiles).iterator();
		while (it.hasNext()) {
			Profile entry = it.next();
			if (entry.getName().equals(newProfile.getName())) {
				this.profiles.remove(entry);
			}
		}
		this.profiles.add(newProfile);
	}
	
	public void updateField(TextField field, String value) {
		switch(field){
		case instanceRoot:
			setInstanceRoot(value);
			break;
		case jrePath:
			setJrePath(value);
			break;
		case jvmOpts:
			setJvmOpts(value);
			break;
		case maxMemory:
			setMaxMemory(value);
			break;
		case minMemory:
			setMinMemory(value);
			break;
		case permGen:
			setPermGen(value);
			break;
		case programWrapper:
			setProgramWrapper(value);
			break;
		case resHeight:
			try {
				setResHeight(Integer.parseInt(value));
			} catch (Exception e) {
				// ignore errors
			}
			break;
		case resWidth:
			try {
				setResWidth(Integer.parseInt(value));
			} catch (Exception e) {
				// ignore errors
			}
			break;
		case timeoutLength:
			setTimeoutLength(Integer.parseInt(value));
			break;
		default:
			break;
		}
	}

	public Profile findProfile(String name) {
		for (Profile entry : this.profiles) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

	public void removeProfile(String name) {
		this.profiles.remove(findProfile(name));
	}
}
