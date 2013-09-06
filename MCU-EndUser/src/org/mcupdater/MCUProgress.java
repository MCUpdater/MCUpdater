package org.mcupdater;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class MCUProgress extends Composite{

	private ScrolledComposite scroller;
	private Composite container;
	private RowLayout rlContainer = new RowLayout(SWT.VERTICAL);
	private Map<MultiKey, ProgressItem> items = new HashMap<MultiKey, ProgressItem>();

	private class MultiKey {
		private final String parent;
		private final String job;
		
		public MultiKey(String parent, String job){
			this.parent=parent;
			this.job=job;
		}
		
		public String getParent(){
			return parent;
		}
		
		@SuppressWarnings("unused")
		public String getJob(){
			return job;
		}
		
	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof MultiKey)) return false;
	        MultiKey key = (MultiKey) o;
	        return parent.equals(key.parent) && job.equals(key.job);
	    }

	    @Override
	    public int hashCode() {
	        int result = parent.hashCode();
	        result = 31 * result + job.hashCode();
	        return result;
	    }
	}
	
	public MCUProgress(Composite parent) {
		super(parent, SWT.NONE);
		scroller = new ScrolledComposite(this, SWT.V_SCROLL);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		container = new Composite(scroller, SWT.NONE);
		scroller.setContent(container);
		rlContainer.fill = true;
		rlContainer.center = true;
		container.setLayout(new GridLayout(4,false));
		this.setLayout(new FillLayout());
		scroller.setMinSize(container.computeSize(SWT.DEFAULT,SWT.DEFAULT));
	}
	
	public synchronized int getActiveCount() {
		int activeCount = 0;
		for (Entry<MultiKey, ProgressItem> item : items.entrySet()) {
			if (item.getValue().isActive()){
				activeCount++;
			}
		}
		return activeCount;
	}
	
	public synchronized int getActiveById(String serverId) {
		int activeCount = 0;
		for (Entry<MultiKey,ProgressItem> entry : items.entrySet()){
			if (entry.getKey().getParent().equals(serverId)) {
				if (entry.getValue().isActive()) {
					activeCount++;
				}
			}
		}
		return activeCount;
	}

	public synchronized void addProgressBar(String jobName, String parentId) {
		ProgressItem newItem = new ProgressItem(container, jobName, parentId);
		this.items.put(new MultiKey(parentId, jobName), newItem);
		container.pack(true);
		scroller.setMinSize(container.computeSize(SWT.DEFAULT,SWT.DEFAULT));
	}
	
	public synchronized void updateProgress(final String jobName, final String parentId, float newProgress, int totalFiles, int successfulFiles) {
		ProgressItem bar = items.get(new MultiKey(parentId, jobName));
		synchronized(bar){
			if (bar == null) { return; }
			bar.setProgress(newProgress, totalFiles, successfulFiles);
		}
	}
		
	private class ProgressItem {

		private Label lblName;
		private ProgressBar pbProgress;
		private Label lblStatus;
		private Button btnDismiss;
		private boolean active;

		public ProgressItem(final Composite parent, final String jobName, final String parentId) {
			active = true; 
			lblName = new Label(parent, SWT.NONE);
			lblName.setText(parentId + " - " + jobName);
			lblName.setLayoutData(new GridData(SWT.LEFT,SWT.TOP,false,false));
			
			pbProgress = new ProgressBar(parent, SWT.NONE);
			pbProgress.setMinimum(0);
			pbProgress.setMaximum(10000);
			pbProgress.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));

			lblStatus = new Label(parent,SWT.NONE);
			lblStatus.setLayoutData(new GridData(SWT.CENTER,SWT.TOP,false,false));
			lblStatus.setText("Inactive");
			
			btnDismiss = new Button(parent, SWT.ARROW | SWT.LEFT);
			btnDismiss.setEnabled(false);
			btnDismiss.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					lblName.dispose();
					pbProgress.dispose();
					lblStatus.dispose();
					btnDismiss.dispose();
					parent.pack(true);
					items.remove(new MultiKey(jobName, parentId));
				}
			});
		}

		public void setProgress(final float progress, final int totalFiles, final int successfulFiles) {
			Display.getCurrent().asyncExec(new Runnable(){

				@Override
				public void run() {
					try {
						pbProgress.setSelection((int)(progress * 10000.0F));
						lblStatus.setText(String.format("%d/%d",successfulFiles,totalFiles));
						if (pbProgress.getSelection() == pbProgress.getMaximum()){
							lblStatus.setText("Finished");
							btnDismiss.setEnabled(true);
							active = false;
						}
						container.pack();
					} catch (Exception e) {
						// Because weird things sometimes happen when completed jobs are disposed.
						e.printStackTrace();
					}
				}
			});
		}
		
		public boolean isActive() {
			return active;
		}
	}
}
