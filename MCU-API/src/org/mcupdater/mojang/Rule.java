package org.mcupdater.mojang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
	private Action action = Action.ALLOW;
	private OSRestriction os;

	public Action getAppliedAction() {
		if ((this.os != null) && (!this.os.isCurrentOperatingSystem())) { return null; }
		return this.action;
	}
	
	public static enum Action
	{
		ALLOW,
		DISALLOW;
	}
	
	public class OSRestriction {
		private OperatingSystem name;
		private String version;
		
		public OSRestriction() { }

		public boolean isCurrentOperatingSystem() {
			if ((this.name != null) && (this.name != OperatingSystem.getCurrentPlatform())) { return false; }

			if (this.version != null) {
				try {
					Pattern pattern = Pattern.compile(this.version);
					Matcher matcher = pattern.matcher(System.getProperty("os.version"));
					if (!matcher.matches()) { return false; }
				} catch (Throwable localThrowable) { }
			}
			return true;
		}
	}
}
