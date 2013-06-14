package org.smbarbour.mcu.EndUser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SettingsPanel extends Composite {
	
	private ScrolledComposite scroller;
	private Composite content;
	private GridLayout glContent = new GridLayout(3,false);
	final private GridData gdLabel = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
	final private GridData gdFillSpan = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
	final private GridData gdFullSpan = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1);

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
		int maxWidth = 0;
		
		Label lblMinMem = new Label(content, SWT.NONE);
		lblMinMem.setLayoutData(gdLabel);
		lblMinMem.setText("Minimum Memory:");
		lblMinMem.setAlignment(SWT.RIGHT);
		maxWidth = lblMinMem.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		
		Text txtMinMem = new Text(content, SWT.FILL);
		txtMinMem.setLayoutData(gdFillSpan);
		txtMinMem.setText("");

		Label lblMaxMem = new Label(content, SWT.NONE);
		lblMaxMem.setLayoutData(gdLabel);
		lblMaxMem.setText("Maximum Memory:");
		lblMaxMem.setAlignment(SWT.RIGHT);
		maxWidth = Math.max(maxWidth, lblMaxMem.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		
		Text txtMaxMem = new Text(content, SWT.FILL);
		txtMaxMem.setLayoutData(gdFillSpan);
		txtMaxMem.setText("");

		Label lblPermGen = new Label(content, SWT.NONE);
		lblPermGen.setLayoutData(gdLabel);
		lblPermGen.setText("PermGen Space:");
		lblPermGen.setAlignment(SWT.RIGHT);
		maxWidth = Math.max(maxWidth, lblPermGen.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		
		Text txtPermGen = new Text(content, SWT.FILL);
		txtPermGen.setLayoutData(gdFillSpan);
		txtPermGen.setText("");

		Label lblMemNotice = new Label(content, SWT.NONE);
		lblMemNotice.setLayoutData(gdFullSpan);
		lblMemNotice.setText("Memory can be specified in MB or GB (i.e. 512M or 1G).\nIncreasing memory may help performance, but often has no measurable impact.");
		lblMemNotice.setAlignment(SWT.CENTER);
		
		Label lblResolution = new Label(content, SWT.NONE);
		lblResolution.setLayoutData(gdLabel);
		lblResolution.setText("Resolution:");
		lblResolution.setAlignment(SWT.RIGHT);
		maxWidth = Math.max(maxWidth, lblResolution.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		
		gdLabel.widthHint = maxWidth;
	}

	
}
