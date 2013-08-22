package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.mcupdater.settings.SettingsManager;
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
		scroller.setMinSize(content.computeSize(SWT.DEFAULT,SWT.DEFAULT));
		
	}

	private void buildPanel() {
		toolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
		{
			Button btnSave = new Button(toolbar,SWT.PUSH);
			btnSave.setText(translate.save);
			
			
			Button btnReload = new Button(toolbar,SWT.PUSH);
			btnReload.setText(translate.reload);
		}

		{
			Label lblMinMem = new Label(content, SWT.NONE);
			lblMinMem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblMinMem.setText(translate.minMemory);
			lblMinMem.setAlignment(SWT.RIGHT);

			final Text txtMinMem = new Text(content, SWT.FILL);
			txtMinMem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtMinMem.setText(settingsManager.getSettings().getMinMemory());
			txtMinMem.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setMinMemory(txtMinMem.getText()); }
			});
		}
		{
			Label lblMaxMem = new Label(content, SWT.NONE);
			lblMaxMem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblMaxMem.setText(translate.maxMemory);
			lblMaxMem.setAlignment(SWT.RIGHT);

			final Text txtMaxMem = new Text(content, SWT.FILL);
			txtMaxMem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtMaxMem.setText(settingsManager.getSettings().getMaxMemory());
			txtMaxMem.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setMaxMemory(txtMaxMem.getText()); }
			});
		}
		{
			Label lblPermGen = new Label(content, SWT.NONE);
			lblPermGen.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblPermGen.setText(translate.permGen);
			lblPermGen.setAlignment(SWT.RIGHT);

			final Text txtPermGen = new Text(content, SWT.FILL);
			txtPermGen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtPermGen.setText(settingsManager.getSettings().getPermGen());
			txtPermGen.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setPermGen(txtPermGen.getText()); }
			});
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
			
			final Button chkFullscreen = new Button(content, SWT.CHECK);
			chkFullscreen.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			chkFullscreen.setSelection(settingsManager.getSettings().isFullScreen());
			chkFullscreen.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					settingsManager.getSettings().setFullScreen(chkFullscreen.getSelection());					
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}
			});

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
				final Text txtResWidth = new Text(resPanel, SWT.NONE);
				txtResWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				txtResWidth.setText(String.valueOf(settingsManager.getSettings().getResWidth()));
				txtResWidth.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setResWidth(Integer.parseInt(txtResWidth.getText())); }
				});

				Label lblResSeparator = new Label(resPanel, SWT.NONE);
				lblResSeparator.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
				lblResSeparator.setText("X");

				final Text txtResHeight = new Text(resPanel, SWT.NONE);
				txtResHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				txtResHeight.setText(String.valueOf(settingsManager.getSettings().getResHeight()));
				txtResHeight.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setResHeight(Integer.parseInt(txtResHeight.getText())); }
				});
			}
		}
		{
			Label lblJavaHome = new Label(content, SWT.NONE);
			lblJavaHome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblJavaHome.setText(translate.javaHome);

			final Text txtJavaHome = new Text(content, SWT.NONE);
			txtJavaHome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtJavaHome.setText(settingsManager.getSettings().getJrePath());
			txtJavaHome.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setJrePath(txtJavaHome.getText()); }
			});

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
					}
				}
			});
		}
		{
			Label lblJVMOpts = new Label(content, SWT.NONE);
			lblJVMOpts.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblJVMOpts.setText(translate.jvmOpts);

			final Text txtJVMOpts = new Text(content, SWT.NONE);
			txtJVMOpts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtJVMOpts.setText(settingsManager.getSettings().getJvmOpts());
			txtJVMOpts.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setJvmOpts(txtJVMOpts.getText()); }
			});
			
		}
		{
			Label lblInstanceRoot = new Label(content, SWT.NONE);
			lblInstanceRoot.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblInstanceRoot.setText(translate.instancePath);
			
			final Text txtInstanceRoot = new Text(content, SWT.NONE);
			txtInstanceRoot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtInstanceRoot.setText(settingsManager.getSettings().getInstanceRoot());
			txtInstanceRoot.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setInstanceRoot(txtInstanceRoot.getText()); }
			});
	
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
					}
				}
			});
			
		}
		{
			Label lblWrapper = new Label(content, SWT.NONE);
			lblWrapper.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblWrapper.setText(translate.programWrapper);
			
			final Text txtWrapper = new Text(content, SWT.NONE);
			txtWrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtWrapper.setText(settingsManager.getSettings().getProgramWrapper());
			txtWrapper.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent arg0) { settingsManager.getSettings().setProgramWrapper(txtWrapper.getText()); }
			});

		}
		{
			Label lblAutoMinimize = new Label(content, SWT.NONE);
			lblAutoMinimize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblAutoMinimize.setText(translate.minimize);
			
			final Button chkAutoMinimize = new Button(content, SWT.CHECK);
			chkAutoMinimize.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			chkAutoMinimize.setSelection(settingsManager.getSettings().isMinimizeOnLaunch());
			chkAutoMinimize.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					settingsManager.getSettings().setMinimizeOnLaunch(chkAutoMinimize.getSelection());					
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}
			});
		}
		{
			Label lblAutoConnect = new Label(content, SWT.NONE);
			lblAutoConnect.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblAutoConnect.setText(translate.autoConnect);
			
			final Button chkAutoConnect = new Button(content, SWT.CHECK);
			chkAutoConnect.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			chkAutoConnect.setSelection(settingsManager.getSettings().isAutoConnect());
			chkAutoConnect.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					settingsManager.getSettings().setAutoConnect(chkAutoConnect.getSelection());					
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}
			});
		}
		{
			Label lblPackList = new Label(content, SWT.NONE);
			lblPackList.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			lblPackList.setText(translate.definedPacks);
			
			Composite cmpListControl = new Composite(content, SWT.NONE);
			cmpListControl.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true, false,2,4));
			cmpListControl.setLayout(new GridLayout(3, false));
			{
				List lstPackList = new List(cmpListControl, SWT.V_SCROLL);
				lstPackList.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true, true,3,4));
				for (String entry : settingsManager.getSettings().getPackURLs()) {
					lstPackList.add(entry);
				}

				txtNewUrl = new Text(cmpListControl, SWT.NONE);
				txtNewUrl.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true, false,1,1));
				
				Button btnListAdd = new Button(cmpListControl, SWT.PUSH);
				btnListAdd.setText("Add");
				btnListAdd.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false,1,1));

				Button btnListRemove = new Button(cmpListControl, SWT.PUSH);
				btnListRemove.setText("Remove");
				btnListRemove.setLayoutData(new GridData(SWT.RIGHT,SWT.TOP,false,false,1,1));
			}
		}
	}
}
