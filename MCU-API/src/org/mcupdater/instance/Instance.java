package org.mcupdater.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Instance {
	private List<FileInfo> instanceFiles;
	private Map<String, Boolean> optionalMods;

	public Instance(){
		instanceFiles = new ArrayList<FileInfo>();
		optionalMods = new HashMap<String, Boolean>();
	}
	
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
}
