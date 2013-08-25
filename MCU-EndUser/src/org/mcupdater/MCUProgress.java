package org.mcupdater;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class MCUProgress extends Composite{

	private ScrolledComposite scroller;
	private Composite container;
	private RowLayout rlContainer = new RowLayout(SWT.VERTICAL);
	private Map<String, ProgressItem> items = new HashMap<String, ProgressItem>();

	public MCUProgress(Composite parent) {
		super(parent, SWT.NONE);
		scroller = new ScrolledComposite(this, SWT.V_SCROLL);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		container = new Composite(scroller, SWT.NONE);
		scroller.setContent(container);
		rlContainer.fill = true;
		rlContainer.center = true;
		container.setLayout(new GridLayout(3,false));
		this.setLayout(new FillLayout());
		scroller.setMinSize(container.computeSize(SWT.DEFAULT,SWT.DEFAULT));
	}
	
	public void addProgressBar(String jobName) {
		ProgressItem newItem = new ProgressItem(container, jobName);
		this.items.put(jobName, newItem);
		container.pack(true);
		scroller.setMinSize(container.computeSize(SWT.DEFAULT,SWT.DEFAULT));
	}
	
	public synchronized void updateProgress(final String jobName, float newProgress, int totalFiles, int successfulFiles) {
		ProgressItem bar = items.get(jobName);
		synchronized(bar){
			if (bar == null) { return; }
			bar.setProgress(newProgress, totalFiles, successfulFiles);
		}
	}
		
	private class ProgressItem {

		private Label lblName;
		private ProgressBar pbProgress;
		private Label lblStatus;

		public ProgressItem(Composite parent, String jobName) {
			lblName = new Label(parent, SWT.NONE);
			lblName.setText(jobName);
			lblName.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false));
			
			pbProgress = new ProgressBar(parent, SWT.NONE);
			pbProgress.setMinimum(0);
			pbProgress.setMaximum(100);
			pbProgress.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));

			lblStatus = new Label(parent,SWT.NONE);
			lblStatus.setLayoutData(new GridData(SWT.CENTER,SWT.TOP,false,false));
			lblStatus.setText("Inactive");
		}

		public void setProgress(final float progress, final int totalFiles, final int successfulFiles) {
			Display.getCurrent().asyncExec(new Runnable(){

				@Override
				public void run() {
					pbProgress.setSelection((int)(progress * 100.0F));
					lblStatus.setText(String.format("%d/%d",successfulFiles,totalFiles));
					if (pbProgress.getSelection() == pbProgress.getMaximum()){
						lblStatus.setText("Finished");
					}
					container.pack();
				}			
			});
		}		
	}

}
