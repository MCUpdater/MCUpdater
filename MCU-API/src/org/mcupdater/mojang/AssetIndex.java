package org.mcupdater.mojang;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AssetIndex {
	private Map<String, Asset> objects;
	private boolean virtual;
	
	public AssetIndex() {
		this.objects = new LinkedHashMap<String, Asset>();
	}
	
	public Map<String, Asset> getObjects() {
		return this.objects;
	}
	
	public Set<Asset> getUniqueObjects() {
		return new HashSet<Asset>(this.objects.values());
	}
	
	public boolean isVirtual() {
		return this.virtual;
	}
	
	public class Asset {
		private String hash;
		private long size;
		
		public Asset() {}
		
		public String getHash() { return this.hash; }
		public long getSize() {	return this.size; }
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if ((o == null) || (getClass() != o.getClass())) return false;
			
			Asset that = (Asset) o;
			if (this.size != that.size) { return false; }
			if (!this.hash.equals(that.hash)) { return false; }
			
			return true;
		}
		
		@Override
		public int hashCode() {
			int result = this.hash.hashCode();
			result = 31 * result + (int)(this.size ^ this.size >>> 32);
			return result;
		}
	}
}
