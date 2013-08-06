package org.mcupdater.util;

public class ServerListPacket {
	private ServerList entry;
	private MCUpdater instance;
	
	public ServerListPacket(ServerList entry, MCUpdater instance)
	{
		this.entry = entry;
		this.instance = instance;
	}
	
	public ServerList getEntry() {
		return entry;
	}
	public void setEntry(ServerList entry) {
		this.entry = entry;
	}
	public MCUpdater getInstance() {
		return instance;
	}
	public void setInstance(MCUpdater instance) {
		this.instance = instance;
	}
}
