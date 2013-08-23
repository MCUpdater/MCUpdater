package org.mcupdater.instance;

public class FileInfo {
	private String filename;
	private String MD5;
	private String modid;

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getMD5() {
		return MD5;
	}
	
	public void setMD5(String mD5) {
		this.MD5 = mD5;
	}
	
	public String getModId() {
		return modid;
	}
	
	public void setModId(String modid) {
		this.modid = modid;
	}
}
