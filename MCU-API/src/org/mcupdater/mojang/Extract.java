package org.mcupdater.mojang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Extract {
	private List<String> exclude = new ArrayList<String>();
	
	public Extract() { }
	
	public Extract(String[] exclude) {
		if (exclude != null) {
			Collections.addAll(this.exclude, exclude);
		}
	}
	
	public List<String> getExcludes() {
		return this.exclude;
	}
	
	public boolean shouldExtract(String path) {
		if (this.exclude != null) {
			for (String rule : this.exclude){
				if (path.startsWith(rule)) return false;
			}
		}
		return true;
	}
}
