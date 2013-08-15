package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

public class InstanceCell extends Composite {

	private Label lblIcon;
	private Label lblName;
	private Label lblVersion;
	private Label lblRevision;
	private boolean isSelected = false;
	private Color normal;
	private Color hilight;
	private InstanceList parentList;
	
	public InstanceCell(InstanceList parent, int style) {
		super(parent, style);
		this.parentList = parent;
		normal = parent.getBackground();
		hilight = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
		lblIcon = new Label(this,0);
		lblName = new Label(this,0);
		lblVersion = new Label(this,0);
		lblRevision = new Label(this,0);
		setBackground(normal);
		lblName.setBackground(normal);
		lblVersion.setBackground(normal);
		lblRevision.setBackground(normal);
		addDisposeListener(new DisposeListener(){
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				InstanceCell.this.widgetDisposed(arg0);
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				
			}
		});
		setLayout(new InstanceCellLayout());
	}
	
	public Image getIcon() {
		return lblIcon.getImage();
	}
	
	public void setIcon(Image image) {
		this.lblIcon.setImage(image);
		layout(true);
	}
	
	public String getServerName() {
		return this.lblName.getText();
	}
	
	public void setServerName(String text) {
		this.lblName.setText(text);
		layout(true);
	}

	public String getVersion() {
		return this.lblVersion.getText();
	}
	
	public void setVersion(String text) {
		this.lblVersion.setText(text);
		layout(true);
	}

	public String getRevision() {
		return this.lblRevision.getText();
	}
	
	public void setRevision(String text) {
		this.lblRevision.setText(text);
		layout(true);
	}

	protected void widgetDisposed(DisposeEvent arg0) {
		lblIcon.dispose();
		lblName.dispose();
		lblVersion.dispose();
		lblRevision.dispose();
	}
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		Color newColor;
		if (this.isSelected) {
			newColor = hilight;
		} else {
			newColor = normal;
		}
		setBackground(newColor);
		lblName.setBackground(newColor);
		lblVersion.setBackground(newColor);
		lblRevision.setBackground(newColor);
		layout(true);
	}

	private class InstanceCellLayout extends Layout {

		Point iExtent, t1Extent, t2Extent, t3Extent;
		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean changed)
		{
			Control [] children = composite.getChildren();
			if (changed || iExtent == null || t1Extent == null) {
				iExtent = children[0].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t1Extent = children[1].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t2Extent = children[2].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t3Extent = children[3].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
			}
			int width = iExtent.x + 5 + t1Extent.x;
			int height = Math.max(iExtent.y, (t1Extent.y + t2Extent.y + t3Extent.y + 4));
			return new Point(width + 2, height + 2);
		}

		@Override
		protected void layout(Composite composite, boolean changed)
		{
			Control [] children = composite.getChildren();
			if (changed || iExtent == null || t1Extent == null) {
				iExtent = children[0].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t1Extent = children[1].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t2Extent = children[2].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t3Extent = children[3].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
			}
			children[0].setBounds(1, 1, iExtent.x, iExtent.y);
			children[1].setBounds(iExtent.x + 5, 1, t1Extent.x, t1Extent.y);
			children[2].setBounds(iExtent.x + 5, t1Extent.y + 2, t2Extent.x, t2Extent.y);
			children[3].setBounds(iExtent.x + 5, t1Extent.y + t2Extent.y + 4, t3Extent.x, t3Extent.y);
		}
	}
}
