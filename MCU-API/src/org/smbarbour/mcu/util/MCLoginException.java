package org.smbarbour.mcu.util;

public class MCLoginException extends Exception {
	
	public enum ResponseType {
		NOCONNECTION("Unable to connect to minecraft.net"),
		BADLOGIN("Login Failed"),
		OLDVERSION("Old Version"),
		OLDLAUNCHER("Outdated Launcher"),
		NOTPREMIUM("User not premium");
		
		public String message;
		
		ResponseType (String message) {
			this.message = message;
		}
	}
	
	private static final long serialVersionUID = -8012092277739360133L;
	
	public MCLoginException(ResponseType response) {
		super(response.message);
	}

	public MCLoginException(String message) {
		super(message);
	}
	
}
