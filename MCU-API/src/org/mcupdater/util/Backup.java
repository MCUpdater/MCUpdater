package org.mcupdater.util;

public class Backup {
	private String description;
	private String filename;
	
	public Backup (String description, String filename) {
		this.description = description;
		this.filename = filename;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
}
