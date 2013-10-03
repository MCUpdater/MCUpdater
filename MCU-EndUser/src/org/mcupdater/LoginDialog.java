package org.mcupdater;

import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.mcupdater.Yggdrasil.AuthManager;
import org.mcupdater.Yggdrasil.SessionResponse;
import org.mcupdater.settings.Profile;
import org.mcupdater.translate.TranslateProxy;

public class LoginDialog {

	public static Profile doLogin(Shell parent, TranslateProxy translate, String initialUsername){
		final Profile newProfile = new Profile();
		newProfile.setStyle("Invalid");
		final Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		{
			dialog.setText(translate.addProfile);
			GridLayout dialogLayout = new GridLayout(4,false);
			dialogLayout.marginLeft = 5;
			dialogLayout.marginRight = 5;
			dialog.setLayout(dialogLayout);

			Label username = new Label(dialog, SWT.NONE);
			username.setText(translate.username);
			username.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			final Text txtUsername = new Text(dialog, SWT.FILL | SWT.BORDER);
			txtUsername.setText(initialUsername);
			txtUsername.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,3,1));

			Label password = new Label(dialog, SWT.NONE);
			password.setText(translate.password);
			password.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
			final Text txtPassword = new Text(dialog, SWT.FILL | SWT.BORDER | SWT.PASSWORD);
			txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,3,1));

			final Label response = new Label(dialog, SWT.NONE);
			response.setAlignment(SWT.LEFT);
			response.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

			Button login = new Button(dialog, SWT.PUSH);
			login.setText(translate.login);
			login.setLayoutData(new GridData(SWT.RIGHT,SWT.TOP,true,false,3,1));

			Button cancel = new Button(dialog, SWT.PUSH);
			cancel.setText(translate.cancel);
			cancel.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false));

			login.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					AuthManager auth = new AuthManager();
					SessionResponse authResponse = auth.authenticate(txtUsername.getText(), txtPassword.getText(), UUID.randomUUID().toString());
					if (authResponse.getError().isEmpty()){
						newProfile.setStyle("Yggdrasil");
						newProfile.setUsername(txtUsername.getText());
						newProfile.setAccessToken(authResponse.getAccessToken());
						newProfile.setClientToken(authResponse.getClientToken());
						newProfile.setName(authResponse.getSelectedProfile().getName());
						dialog.close();
					} else {
						response.setText(authResponse.getErrorMessage());
					}
					//dialog.close();
				}
			});

			cancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					dialog.close();
				}
			});

			dialog.setDefaultButton(login);
			dialog.pack();
			dialog.open();
			dialog.setSize(dialog.computeSize(360, SWT.DEFAULT));
			while (!dialog.isDisposed()) {
			    if (!parent.getDisplay().readAndDispatch()) {
			    	parent.getDisplay().sleep();
			    }			}
			return newProfile;
		}
	}
}
