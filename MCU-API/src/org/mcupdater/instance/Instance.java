package org.mcupdater.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Instance {
	private String mcversion;
	private String revision;
	private List<FileInfo> instanceFiles = new ArrayList<FileInfo>();
	private List<FileInfo> jarMods = new ArrayList<FileInfo>();
	private Map<String, Boolean> optionalMods = new HashMap<String, Boolean>();
	
	public List<FileInfo> getInstanceFiles() {
		return instanceFiles;
	}
	public void setInstanceFiles(List<FileInfo> instanceFiles) {
		this.instanceFiles = instanceFiles;
	}
	public Map<String, Boolean> getOptionalMods() {
		return this.optionalMods;
	}
	
	public Boolean getModStatus(String key) {
		return this.optionalMods.get(key);
	}
	
	public void setModStatus(String key, Boolean value) {
		this.optionalMods.put(key, value);
	}
	public String getMCVersion() {
		return mcversion;
	}
	public void setMCVersion(String mcversion) {
		this.mcversion = mcversion;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
	public List<FileInfo> getJarMods() {
		return jarMods;
	}
	public void setJarMods(List<FileInfo> jarMods) {
		this.jarMods = jarMods;
	}
}
