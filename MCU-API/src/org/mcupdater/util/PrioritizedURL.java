package org.mcupdater.util;

public class PrioritizedURL implements Comparable<PrioritizedURL>{
	private String url;
	private int priority;
	
	public PrioritizedURL(String url, int priority)
	{
		this.setUrl(url);
		this.setPriority(priority);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(PrioritizedURL o) {
		return Integer.valueOf(priority).compareTo(o.getPriority());
	}
}
