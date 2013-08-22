package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.mcupdater.translate.TranslateProxy;

public class MCUBrowser extends Composite {

	private TranslateProxy translate;
	private Browser browser;
	private Composite toolbar;

	public MCUBrowser(Composite parent, int style) {
		super(parent, style);
		translate = MainShell.getInstance().translate;
		FormLayout browserLayout = new FormLayout();
		toolbar = new Composite(this, SWT.BORDER);
		toolbar.setLayout(new RowLayout(SWT.HORIZONTAL));
		FormData toolbarData = new FormData();
		{
			toolbarData.left = new FormAttachment(0, 0);
			toolbarData.top = new FormAttachment(0, 0);
			toolbarData.right = new FormAttachment(100,0);
		}
		Button btnBack = new Button(toolbar, SWT.PUSH);
		btnBack.setText(translate.back);
		FontData[] fd = btnBack.getFont().getFontData();
		fd[0].setHeight(8);
		Font font = new Font(Display.getCurrent(), fd[0]);
		btnBack.setFont(font);
		btnBack.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { widgetSelected(arg0); }
			@Override
			public void widgetSelected(SelectionEvent arg0) { browser.back(); }			
		});
		toolbar.setLayoutData(toolbarData);
		browser = new Browser(this, SWT.NONE);
		FormData browserData = new FormData();
		{
			browserData.left = new FormAttachment(0,0);
			browserData.right = new FormAttachment(100,0);
			browserData.top = new FormAttachment(toolbar);
			browserData.bottom = new FormAttachment(100,0);
		}
		browser.setLayoutData(browserData);
		this.setLayout(browserLayout);
	}

	public void setUrl(String url) {
		browser.setUrl(url);
	}

}
