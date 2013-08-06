package org.mcupdater;

import java.util.List;

import org.mcupdater.util.Module;
import org.mcupdater.util.ServerList;

public class ServerDefinition {

	private ServerList server;
	private List<Module> modules;
	private List<ConfigFileWrapper> configs;

	public ServerDefinition(ServerList server, List<Module> modules, List<ConfigFileWrapper> configs){
		this.setServer(server);
		this.setModules(modules);
		this.setConfigs(configs);
	}

	public ServerList getServer() {
		return server;
	}

	public void setServer(ServerList server) {
		this.server = server;
	}

	public List<Module> getModules() {
		return modules;
	}

	public void setModules(List<Module> modules) {
		this.modules = modules;
	}

	public List<ConfigFileWrapper> getConfigs() {
		return configs;
	}

	public void setConfigs(List<ConfigFileWrapper> configs) {
		this.configs = configs;
	}
	
	public String toString() {
		return this.server.getServerId();
	}
}
