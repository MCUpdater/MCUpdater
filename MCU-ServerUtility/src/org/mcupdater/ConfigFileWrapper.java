package org.mcupdater;

import org.mcupdater.util.ConfigFile;

public class ConfigFileWrapper {

	private String parentId;
	private ConfigFile configFile;

	public ConfigFileWrapper(String parentId, ConfigFile configFile) {
		this.setParentId(parentId);
		this.setConfigFile(configFile);
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public ConfigFile getConfigFile() {
		return configFile;
	}

	public void setConfigFile(ConfigFile configFile) {
		this.configFile = configFile;
	}
	
	@Override
	public String toString() {
		return this.configFile.getPath();
	}
}
