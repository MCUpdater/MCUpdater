package org.mcupdater.settings;

import org.mcupdater.Yggdrasil.AuthManager;
import org.mcupdater.Yggdrasil.SessionResponse;

public class Profile {
	private String style;
	private String name;
	private String username;
	private String sessionKey;
	private String clientToken;
	private String accessToken;
	private String lastInstance;

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessionKey() throws Exception {
		if (this.sessionKey == null || this.sessionKey.isEmpty()) {
			if (this.style.equals("Yggdrasil")) {
				AuthManager auth = new AuthManager();
				System.out.println("old-> " + accessToken + ": " + clientToken);
				SessionResponse response = auth.refresh(accessToken, clientToken);
				if (!response.getError().isEmpty()) {
					throw new Exception(response.getErrorMessage());
				} else {
					this.accessToken = response.getAccessToken();
					this.clientToken = response.getClientToken();
					System.out.println("new-> " + accessToken + ": " + clientToken);

					SettingsManager.getInstance().getSettings().addOrReplaceProfile(this);
					if (!SettingsManager.getInstance().isDirty()) {
						System.out.println("Saving settings");
						SettingsManager.getInstance().saveSettings();
					}
				}
				return response.getSessionId();
			}
		}
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public String getClientToken() {
		return clientToken;
	}

	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getLastInstance() {
		return lastInstance;
	}

	public void setLastInstance(String lastInstance) {
		this.lastInstance = lastInstance;
	}
}
