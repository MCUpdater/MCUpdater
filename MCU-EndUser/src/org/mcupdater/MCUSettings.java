package org.mcupdater;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.mcupdater.Yggdrasil.AuthManager;
import org.mcupdater.Yggdrasil.SessionResponse;
import org.mcupdater.settings.Profile;
import org.mcupdater.settings.Settings;
import org.mcupdater.settings.SettingsManager;
import org.mcupdater.settings.Settings.TextField;
import org.mcupdater.translate.TranslateProxy;

public class MCUSettings extends Composite {
	
	private ScrolledComposite scroller;
	private Composite content;
	private GridLayout glContent = new GridLayout(3,false);
	private TranslateProxy translate;
	private SettingsManager settingsManager = SettingsManager.getInstance();
//	final private GridData gdLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
//	final private GridData gdFillSpan = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
//	final private GridData gdFullSpan = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
	private Text txtNewUrl;
	private Composite toolbar;
	private Text txtMinMem;
	private Text txtMaxMem;
	private Text txtPermGen;
	private Button chkFullscreen;
	private Text txtResWidth;
	private Text txtResHeight;
	private Text txtJavaHome;
	private Text txtJVMOpts;
	private Text txtInstanceRoot;
	private Text txtWrapper;
	private Button chkAutoMinimize;
	private Button chkAutoConnect;
	private List lstPackList;
	private List lstProfiles;
	private Map<Text,Settings.TextField> fieldMap = new HashMap<Text,Settings.TextField>();
	
	private ModifyListener genericListener = new ModifyListener(){
		@Override
		public void modifyText(ModifyEvent e) {
			settingsManager.getSettings().updateField(fieldMap.get(e.getSource()), ((Text)e.getSource()).getText());
			settingsManager.setDirty();
			}
	};
	
	private SelectionListener checkListener = new SelectionAdapter(){

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(chkAutoConnect)) {
				SettingsManager.getInstance().getSettings().setAutoConnect(chkAutoConnect.getSelection());
			} else if (e.getSource().equals(chkAutoMinimize)) {
				SettingsManager.getInstance().getSettings().setMinimizeOnLaunch(chkAutoMinimize.getSelection());
			} else if (e.getSource().equals(chkFullscreen)) {
				SettingsManager.getInstance().getSettings().setFullScreen(chkFullscreen.getSelection());
			}
			SettingsManager.getInstance().setDirty();
		}
	};
	
	public MCUSettings(Composite parent) {
		super(parent, SWT.NONE);
		FormLayout settingsLayout = new FormLayout();
		toolbar = new Composite(this, SWT.BORDER);
		toolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
		FormData toolbarData = new FormData();
		{
			toolbarData.left = new FormAttachment(0, 0);
			toolbarData.top = new FormAttachment(0, 0);
			toolbarData.right = new FormAttachment(100,0);
		}
		toolbar.setLayoutData(toolbarData);
		this.setLayout(settingsLayout);
		scroller = new ScrolledComposite(this, SWT.V_SCROLL);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		content = new Composite(scroller, SWT.NONE);
		FormData contentData = new FormData();
		{
			contentData.left = new FormAttachment(0,0);
			contentData.right = new FormAttachment(100,0);
			contentData.top = new FormAttachment(toolbar);
			contentData.bottom = new FormAttachment(100,0);
		}
		scroller.setContent(content);
		scroller.setLayoutData(contentData);
		content.setLayout(glContent);
		translate = MainShell.getInstance().translate;
		buildPanel();
		{
			fieldMap.put(txtMinMem, TextField.minMemory);
			fieldMap.put(txtMaxMem, TextField.maxMemory);
			fieldMap.put(txtPermGen, TextField.permGen);
			fieldMap.put(txtInstanceRoot, TextField.instanceRoot);
			fieldMap.put(txtJVMOpts, TextField.jvmOpts);
			fieldMap.put(txtJavaHome, TextField.jrePath);
			fieldMap.put(txtResHeight, TextField.resHeight);
			fieldMap.put(txtResWidth, TextField.resWidth);
			fieldMap.put(txtWrapper, TextField.programWrapper);
		}
		loadFields();
		scroller.setMinSize(content.computeSize(SWT.DEFAULT,SWT.DEFAULT));
		
	}

	private void buildPanel() {
		{
			Button btnSave = new Button(toolbar,SWT.PUSH);
			btnSave.setText(translate.save);
			btnSave.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) { widgetSelected(arg0); }

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					settingsManager.saveSettings();
				}
			});
			
			Button btnReload = new Button(toolbar,SWT.PUSH);
			btnReload.setText(translate.reload);
			btnReload.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) { widgetSelected(arg0); }

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					settingsManager.loadSettings();
					loadFields();
					MainShell.getInstance().refreshInstances();
					MainShell.getInstance().refreshProfiles();
				}
			});
		}

		{ // Profiles
			Label lblProfiles = new Label(content, SWT.NONE);
			lblProfiles.setLayoutData(new GridData(SWT.RIGHT,SWT.TOP,false,false));
			lblProfiles.setText(translate.profiles);
			lblProfiles.setAlignment(SWT.RIGHT);
			
			Composite cmpProfiles = new Composite(content, SWT.NONE);
			cmpProfiles.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false,2,1));
			cmpProfiles.setLayout(new GridLayout(2,false));
			{
				lstProfiles = new List(cmpProfiles,SWT.BORDER);
				lstProfiles.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,1,5));
				
				Composite cmpProfButtonPanel = new Composite(cmpProfiles,SWT.NONE);
				cmpProfButtonPanel.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,true,1,5));
				cmpProfButtonPanel.setLayout(new GridLayout(1,true));
				
				Button btnProfileAdd = new Button(cmpProfButtonPanel,SWT.PUSH);
				btnProfileAdd.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
				btnProfileAdd.setText(translate.add);
				btnProfileAdd.addSelectionListener(new SelectionAdapter() {
					
					public void widgetSelected(SelectionEvent arg0) {
						final Shell dialog = new Shell(MainShell.getInstance().shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
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
										Profile newProfile = new Profile();
										newProfile.setStyle("Yggdrasil");
										newProfile.setUsername(txtUsername.getText());
										newProfile.setAccessToken(authResponse.getAccessToken());
										newProfile.setClientToken(authResponse.getClientToken());
										newProfile.setName(authResponse.getSelectedProfile().getName());
										settingsManager.getSettings().addOrReplaceProfile(newProfile);
										settingsManager.setDirty();
										reloadProfiles();
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
						}
					}
				});
				
				Button btnProfileRemove = new Button(cmpProfButtonPanel,SWT.PUSH);
				btnProfileRemove.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
				btnProfileRemove.setText(translate.remove);
				btnProfileRemove.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						
					}
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) { widgetSelected(arg0); }
				});
			}
		}
		{
			Label lblMinMem = new Label(content, SWT.NONE);
			lblMinMem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblMinMem.setText(translate.minMemory);
			lblMinMem.setAlignment(SWT.RIGHT);

			txtMinMem = new Text(content, SWT.FILL | SWT.BORDER);
			txtMinMem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtMinMem.addModifyListener(genericListener);
		}
		{
			Label lblMaxMem = new Label(content, SWT.NONE);
			lblMaxMem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblMaxMem.setText(translate.maxMemory);
			lblMaxMem.setAlignment(SWT.RIGHT);

			txtMaxMem = new Text(content, SWT.FILL | SWT.BORDER);
			txtMaxMem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtMaxMem.addModifyListener(genericListener); 
		}
		{
			Label lblPermGen = new Label(content, SWT.NONE);
			lblPermGen.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblPermGen.setText(translate.permGen);
			lblPermGen.setAlignment(SWT.RIGHT);

			txtPermGen = new Text(content, SWT.FILL | SWT.BORDER);
			txtPermGen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtPermGen.addModifyListener(genericListener);
		}
		{
			Label lblMemNotice = new Label(content, SWT.NONE);
			lblMemNotice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			lblMemNotice.setText(translate.memDisclaimer);
			lblMemNotice.setAlignment(SWT.CENTER);
		}
		{
			Label lblFullscreen = new Label(content, SWT.NONE);
			lblFullscreen.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblFullscreen.setText(translate.fullscreen);
			
			chkFullscreen = new Button(content, SWT.CHECK);
			chkFullscreen.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			chkFullscreen.addSelectionListener(checkListener);
		}
		{
			Label lblResolution = new Label(content, SWT.NONE);
			lblResolution.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblResolution.setText(translate.resolution);
			lblResolution.setAlignment(SWT.RIGHT);

			Composite resPanel = new Composite(content, SWT.NONE);
			resPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			resPanel.setLayout(new GridLayout(3,false));
			{
				txtResWidth = new Text(resPanel, SWT.BORDER);
				txtResWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				txtResWidth.addModifyListener(genericListener);

				Label lblResSeparator = new Label(resPanel, SWT.NONE);
				lblResSeparator.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
				lblResSeparator.setText("X");

				txtResHeight = new Text(resPanel, SWT.BORDER);
				txtResHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				txtResHeight.addModifyListener(genericListener);
			}
		}
		{
			Label lblJavaHome = new Label(content, SWT.NONE);
			lblJavaHome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblJavaHome.setText(translate.javaHome);

			txtJavaHome = new Text(content, SWT.BORDER);
			txtJavaHome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtJavaHome.addModifyListener(genericListener);

			Button btnJavaHomeBrowse = new Button(content, SWT.PUSH);
			btnJavaHomeBrowse.setText(translate.browse);
			btnJavaHomeBrowse.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					DirectoryDialog directoryDialog = new DirectoryDialog(MainShell.getInstance().shell);
					String newPath = directoryDialog.open();
					if (newPath != null && !newPath.isEmpty()) {
						txtJavaHome.setText(newPath);
						settingsManager.setDirty();
					}
				}
			});
		}
		{
			Label lblJVMOpts = new Label(content, SWT.NONE);
			lblJVMOpts.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblJVMOpts.setText(translate.jvmOpts);

			txtJVMOpts = new Text(content, SWT.BORDER);
			txtJVMOpts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtJVMOpts.addModifyListener(genericListener);
			
		}
		{
			Label lblInstanceRoot = new Label(content, SWT.NONE);
			lblInstanceRoot.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblInstanceRoot.setText(translate.instancePath);
			
			txtInstanceRoot = new Text(content, SWT.BORDER);
			txtInstanceRoot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtInstanceRoot.addModifyListener(genericListener);
	
			Button btnInstanceRootBrowse = new Button(content, SWT.PUSH);
			btnInstanceRootBrowse.setText(translate.browse);
			btnInstanceRootBrowse.addSelectionListener(new SelectionListener(){
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					DirectoryDialog directoryDialog = new DirectoryDialog(MainShell.getInstance().shell);
					directoryDialog.setMessage("Select new instance path");
					directoryDialog.setFilterPath(txtInstanceRoot.getText());
					String newPath = directoryDialog.open();
					if (newPath != null && !newPath.isEmpty()) {
						txtInstanceRoot.setText(newPath);
						settingsManager.setDirty();
					}
				}
			});
			
		}
		{
			Label lblWrapper = new Label(content, SWT.NONE);
			lblWrapper.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblWrapper.setText(translate.programWrapper);
			
			txtWrapper = new Text(content, SWT.BORDER);
			txtWrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtWrapper.addModifyListener(genericListener);

		}
		{
			Label lblAutoMinimize = new Label(content, SWT.NONE);
			lblAutoMinimize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblAutoMinimize.setText(translate.minimize);
			
			chkAutoMinimize = new Button(content, SWT.CHECK);
			chkAutoMinimize.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			chkAutoMinimize.addSelectionListener(checkListener);
		}
		{
			Label lblAutoConnect = new Label(content, SWT.NONE);
			lblAutoConnect.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblAutoConnect.setText(translate.autoConnect);
			
			chkAutoConnect = new Button(content, SWT.CHECK);
			chkAutoConnect.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			chkAutoConnect.addSelectionListener(checkListener);
		}
		{
			Label lblPackList = new Label(content, SWT.NONE);
			lblPackList.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			lblPackList.setText(translate.definedPacks);
			
			Composite cmpListControl = new Composite(content, SWT.NONE);
			cmpListControl.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true, false,2,4));
			cmpListControl.setLayout(new GridLayout(3, false));
			{
				lstPackList = new List(cmpListControl, SWT.V_SCROLL | SWT.BORDER);
				lstPackList.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true, true,3,4));

				txtNewUrl = new Text(cmpListControl, SWT.BORDER);
				txtNewUrl.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true, false,1,1));
				
				Button btnListAdd = new Button(cmpListControl, SWT.PUSH);
				btnListAdd.setText(translate.add);
				btnListAdd.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false,1,1));
				btnListAdd.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						settingsManager.getSettings().addPackURL(txtNewUrl.getText());
						settingsManager.setDirty();
						txtNewUrl.setText("");
						reloadURLs();
						MainShell.getInstance().refreshInstances();
					}					
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) { widgetSelected(arg0); }
				});

				Button btnListRemove = new Button(cmpListControl, SWT.PUSH);
				btnListRemove.setText(translate.remove);
				btnListRemove.setLayoutData(new GridData(SWT.RIGHT,SWT.TOP,false,false,1,1));
				btnListRemove.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						if (lstPackList.getSelectionCount() > 0) {
							settingsManager.getSettings().removePackUrl(lstPackList.getSelection()[0]);
							settingsManager.setDirty();
							reloadURLs();
							MainShell.getInstance().refreshInstances();
						}
					}
					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) { widgetSelected(arg0); }
				});
			}
		}
	}

	protected void loadFields() {
		synchronized(settingsManager.getSettings()) {
			txtMinMem.removeModifyListener(genericListener);
			txtMaxMem.removeModifyListener(genericListener);
			txtPermGen.removeModifyListener(genericListener);
			txtResWidth.removeModifyListener(genericListener);
			txtResHeight.removeModifyListener(genericListener);
			txtJavaHome.removeModifyListener(genericListener);
			txtJVMOpts.removeModifyListener(genericListener);
			txtInstanceRoot.removeModifyListener(genericListener);
			txtWrapper.removeModifyListener(genericListener);
			chkFullscreen.removeSelectionListener(checkListener);
			chkAutoMinimize.removeSelectionListener(checkListener);
			chkAutoConnect.removeSelectionListener(checkListener);
	
			txtMinMem.setText(settingsManager.getSettings().getMinMemory());
			txtMaxMem.setText(settingsManager.getSettings().getMaxMemory());
			txtPermGen.setText(settingsManager.getSettings().getPermGen());
			chkFullscreen.setSelection(settingsManager.getSettings().isFullScreen());
			txtResWidth.setText(String.valueOf(settingsManager.getSettings().getResWidth()));
			txtResHeight.setText(String.valueOf(settingsManager.getSettings().getResHeight()));
			txtJavaHome.setText(settingsManager.getSettings().getJrePath());
			txtJVMOpts.setText(settingsManager.getSettings().getJvmOpts());
			txtInstanceRoot.setText(settingsManager.getSettings().getInstanceRoot());
			txtWrapper.setText(settingsManager.getSettings().getProgramWrapper());
			chkAutoMinimize.setSelection(settingsManager.getSettings().isMinimizeOnLaunch());
			chkAutoConnect.setSelection(settingsManager.getSettings().isAutoConnect());
			reloadURLs();
			reloadProfiles();
	
			txtMinMem.addModifyListener(genericListener);
			txtMaxMem.addModifyListener(genericListener);
			txtPermGen.addModifyListener(genericListener);
			txtResWidth.addModifyListener(genericListener);
			txtResHeight.addModifyListener(genericListener);
			txtJavaHome.addModifyListener(genericListener);
			txtJVMOpts.addModifyListener(genericListener);
			txtInstanceRoot.addModifyListener(genericListener);
			txtWrapper.addModifyListener(genericListener);
			chkFullscreen.addSelectionListener(checkListener);
			chkAutoMinimize.addSelectionListener(checkListener);
			chkAutoConnect.addSelectionListener(checkListener);
		}
	}

	private void reloadProfiles() {
		lstProfiles.removeAll();
		for (Profile entry : settingsManager.getSettings().getProfiles()) {
			lstProfiles.add(entry.getName());
		}
	}

	private void reloadURLs() {
		lstPackList.removeAll();
		for (String entry : settingsManager.getSettings().getPackURLs()) {
			lstPackList.add(entry);
		}		
	}
}
