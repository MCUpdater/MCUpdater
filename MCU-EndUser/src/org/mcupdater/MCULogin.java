package org.mcupdater;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.translate.TranslateProxy;

public class MCULogin extends Composite {

	private TranslateProxy translate;
	private Combo profileName;
	private Label imgFace;

	public MCULogin(Composite parent) {
		super(parent, SWT.NONE);
		Settings settings = SettingsManager.getInstance().getSettings();
		translate = MainShell.getInstance().translate;
		this.setLayout(new GridLayout(3,false));
		imgFace = new Label(this, SWT.NONE);
		imgFace.setImage(new Image(MainShell.getInstance().getDisplay(), 16, 16));
		Label profilePrompt = new Label(this, SWT.NONE);
		profilePrompt.setText(translate.profile);
		profileName = new Combo(this, SWT.READ_ONLY);
		profileName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e){
				if (!((Combo)e.getSource()).getText().isEmpty()) {
					MainShell.getInstance().setSelectedInstance(SettingsManager.getInstance().getSettings().findProfile(((Combo)e.getSource()).getText()).getLastInstance());
					try {
						URL avatarUrl = new URL("http://cravatar.tomheinan.com/" + ((Combo)e.getSource()).getText() + "/16");
						imgFace.setImage(new Image(MainShell.getInstance().getDisplay(), avatarUrl.openStream()));
						imgFace.pack(true);
					} catch (Exception e1) {
						imgFace.setImage(new Image(MainShell.getInstance().getDisplay(),16,16));
					}
				}
			}
		});
		refreshProfiles(settings);
		this.setSize(this.computeSize(100,SWT.DEFAULT));
	}

	public void refreshProfiles(Settings settings) {
		profileName.removeAll();
		if (settings.getProfiles() == null) { return; }
		for (Profile entry : settings.getProfiles()){
			profileName.add(entry.getName());
		}
		profileName.pack(true);
		this.pack();
	}

	public Profile getSelectedProfile() {
		if (profileName.getSelectionIndex() == -1) {
			return null;
		}
		for (Profile entry : SettingsManager.getInstance().getSettings().getProfiles()){
			if (entry.getName().equals(profileName.getText())) {
				return entry;
			}
		}
		return null;
	}

	public void setSelectedProfile(String lastProfile) {
		for (int i = 0; i < profileName.getItemCount(); i++){
			if (profileName.getItem(i).equals(lastProfile)) {
				profileName.select(i);
				return;
			}
		}
	}

}
