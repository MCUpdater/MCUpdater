package org.mcupdater.instance;

import java.util.List;

public class Instance {
	private String instanceName;
	private String instancePath;
	private String serverAddress;
	private boolean autoConnect;
	private List<FileInfo> instanceFiles;

	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public String getInstancePath() {
		return instancePath;
	}
	public void setInstancePath(String instancePath) {
		this.instancePath = instancePath;
	}
	public String getServerAddress() {
		return serverAddress;
	}
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	public boolean isAutoConnect() {
		return autoConnect;
	}
	public void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}
	public List<FileInfo> getInstanceFiles() {
		return instanceFiles;
	}
	public void setInstanceFiles(List<FileInfo> instanceFiles) {
		this.instanceFiles = instanceFiles;
	}
}
