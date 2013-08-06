package org.smbarbour.mcu.EndUser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class SettingsPanel extends Composite {
	
	private ScrolledComposite scroller;
	private Composite content;
	private GridLayout glContent = new GridLayout(3,false);
//	final private GridData gdLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
//	final private GridData gdFillSpan = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
//	final private GridData gdFullSpan = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);

	public SettingsPanel(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		scroller = new ScrolledComposite(this, SWT.V_SCROLL);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		content = new Composite(scroller, SWT.NONE);
		scroller.setContent(content);
		content.setLayout(glContent);
		buildPanel();
		scroller.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
	}

	private void buildPanel() {
		{
			Label lblMinMem = new Label(content, SWT.NONE);
			lblMinMem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblMinMem.setText("Minimum Memory:");
			lblMinMem.setAlignment(SWT.RIGHT);

			Text txtMinMem = new Text(content, SWT.FILL);
			txtMinMem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtMinMem.setText("");
		}
		{
			Label lblMaxMem = new Label(content, SWT.NONE);
			lblMaxMem.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblMaxMem.setText("Maximum Memory:");
			lblMaxMem.setAlignment(SWT.RIGHT);

			Text txtMaxMem = new Text(content, SWT.FILL);
			txtMaxMem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtMaxMem.setText("");
		}
		{
			Label lblPermGen = new Label(content, SWT.NONE);
			lblPermGen.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblPermGen.setText("PermGen Space:");
			lblPermGen.setAlignment(SWT.RIGHT);

			Text txtPermGen = new Text(content, SWT.FILL);
			txtPermGen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtPermGen.setText("");
		}
		{
			Label lblMemNotice = new Label(content, SWT.NONE);
			lblMemNotice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			lblMemNotice.setText("Memory can be specified in MB or GB (i.e. 512M or 1G).\nIncreasing memory may help performance, but often has no measurable impact.");
			lblMemNotice.setAlignment(SWT.CENTER);
		}
		{
			Label lblResolution = new Label(content, SWT.NONE);
			lblResolution.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblResolution.setText("Resolution:");
			lblResolution.setAlignment(SWT.RIGHT);

			Composite resPanel = new Composite(content, SWT.NONE);
			resPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			resPanel.setLayout(new GridLayout(3,false));
			{
				Text txtResWidth = new Text(resPanel, SWT.NONE);
				txtResWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				txtResWidth.setText("");

				Label lblResSeparator = new Label(resPanel, SWT.NONE);
				lblResSeparator.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
				lblResSeparator.setText("X");

				Text txtResHeight = new Text(resPanel, SWT.NONE);
				txtResHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				txtResHeight.setText("");
			}
		}
		{
			Label lblJavaHome = new Label(content, SWT.NONE);
			lblJavaHome.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblJavaHome.setText("Java home path:");

			Text txtJavaHome = new Text(content, SWT.NONE);
			txtJavaHome.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtJavaHome.setText("");

			Button btnJavaHomeBrowse = new Button(content, SWT.PUSH);
			btnJavaHomeBrowse.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			btnJavaHomeBrowse.setText("Browse");
		}
		{
			Label lblJVMOpts = new Label(content, SWT.NONE);
			lblJVMOpts.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblJVMOpts.setText("JVM Opts:");

			Text txtJVMOpts = new Text(content, SWT.NONE);
			txtJVMOpts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtJVMOpts.setText("");
		}
		{
			Label lblInstanceRoot = new Label(content, SWT.NONE);
			lblInstanceRoot.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblInstanceRoot.setText("Instance folder:");
			
			Text txtInstanceRoot = new Text(content, SWT.NONE);
			txtInstanceRoot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtInstanceRoot.setText("");
			
			Button btnInstanceRootBrowse = new Button(content, SWT.PUSH);
			btnInstanceRootBrowse.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			btnInstanceRootBrowse.setText("Browse");
		}
		{
			Label lblWrapper = new Label(content, SWT.NONE);
			lblWrapper.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblWrapper.setText("Program wrapper:");
			
			Text txtWrapper = new Text(content, SWT.NONE);
			txtWrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			txtWrapper.setText("");
		}
		{
			Label lblAutoMinimize = new Label(content, SWT.NONE);
			lblAutoMinimize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblAutoMinimize.setText("Minimize on launch:");
			
			Button chkAutoMinimize = new Button(content, SWT.CHECK);
			chkAutoMinimize.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		}
		{
			Label lblAutoConnect = new Label(content, SWT.NONE);
			lblAutoConnect.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblAutoConnect.setText("Automatically connect:");
			
			Button chkAutoConnect = new Button(content, SWT.CHECK);
			chkAutoConnect.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		}
		{
			Label lblPackList = new Label(content, SWT.NONE);
			lblPackList.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			lblPackList.setText("Defined Pack URLs:");
			
			List lstPackList = new List(content, SWT.V_SCROLL);
			lstPackList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 6));
		}
	}
}
