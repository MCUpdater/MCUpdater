package org.mcupdater.instance;

public class FileInfo {
	private String filename;
	private String MD5;
	private boolean library;

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
		MD5 = mD5;
	}
	public boolean isLibrary() {
		return library;
	}
	public void setLibrary(boolean library) {
		this.library = library;
	}
}
