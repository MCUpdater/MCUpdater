package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MCULogin extends Composite {

	public MCULogin(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2,false));
		Label profilePrompt = new Label(this, SWT.NONE);
		profilePrompt.setText("Profile:");
		Combo profileName = new Combo(this, SWT.READ_ONLY);
		profileName.setItems(new String[]{"Melonar","allaryin","Kane_Hart"});
	}

}
