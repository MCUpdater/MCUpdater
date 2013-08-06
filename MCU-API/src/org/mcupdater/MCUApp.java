package org.mcupdater;

import java.util.logging.*;

import org.mcupdater.util.ServerList;

public abstract class MCUApp {
	
	public Logger baseLogger;

	public abstract void setStatus(String string);
	public abstract void setProgressBar(int i);
	public abstract void log(String msg);
	public abstract boolean requestLogin();
	public abstract void addServer(ServerList entry);
}
