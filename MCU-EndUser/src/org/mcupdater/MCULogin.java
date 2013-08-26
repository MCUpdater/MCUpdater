package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.translate.TranslateProxy;

public class MCULogin extends Composite {

	private TranslateProxy translate;
	private Combo profileName;

	public MCULogin(Composite parent) {
		super(parent, SWT.NONE);
		Settings settings = MainShell.getInstance().getSettingsManager().getSettings();
		translate = MainShell.getInstance().translate;
		this.setLayout(new GridLayout(2,false));
		Label profilePrompt = new Label(this, SWT.NONE);
		profilePrompt.setText(translate.profile);
		profileName = new Combo(this, SWT.READ_ONLY);
		refreshProfiles(settings);
	}

	public void refreshProfiles(Settings settings) {
		profileName.removeAll();
		if (settings.getProfiles() == null) { return; }
		for (Profile entry : settings.getProfiles()){
			profileName.add(entry.getUsername());
		}
	}

}
