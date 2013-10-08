package org.mcupdater;

import org.eclipse.swt.widgets.Display;

public class MCUClientTracker implements TrackerListener {

	
	private Display display;
	private MCUProgress progress;
	private int updateCounter = 0;

	public MCUClientTracker(Display display, MCUProgress progress) {
		this.display = display;
		this.progress = progress;
	}

	@Override
	public void onQueueFinished(final DownloadQueue queue) {
		display.syncExec(new Runnable(){
			@Override
			public void run() {
				MainShell.getInstance().log(queue.getParent() + " - " + queue.getName() + ": Finished!");
				if (progress == null || progress.isDisposed()) { return; }
				progress.updateProgress(queue.getName(),queue.getParent(),1.0F,queue.getTotalFileCount(),queue.getSuccessFileCount());
			}
		});

	}

	@Override
	public void onQueueProgress(final DownloadQueue queue) {
		updateCounter++;
		if (updateCounter == 10) {
			display.syncExec(new Runnable(){
				@Override
				public void run() {
					if (progress == null || progress.isDisposed()) { return; }
					progress.updateProgress(queue.getName(),queue.getParent(),queue.getProgress(),queue.getTotalFileCount(),queue.getSuccessFileCount());
				}
			});
			updateCounter = 0;
		}
	}

	@Override
	public void printMessage(final String msg) {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				MainShell.getInstance().log(msg);				
			}
		});
	}

}
