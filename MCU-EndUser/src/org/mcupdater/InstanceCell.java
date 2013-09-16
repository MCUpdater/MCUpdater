package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
	private Color normal_bg;
	private Color hilight_bg;
	private Color normal_fg;
	private Color hilight_fg;
	private InstanceList parentList;
	private String serverId;
	
	public InstanceCell(Composite parent, int style, String serverId, InstanceList parentList) {
		super(parent, style);
		this.parentList = parentList;
		this.serverId = serverId;
		normal_bg = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		normal_fg = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		hilight_bg = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
		hilight_fg = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		lblIcon = new Label(this,0);
		lblName = new Label(this,0);
		lblVersion = new Label(this,0);
		lblRevision = new Label(this,0);
		FontData[] fd = lblName.getFont().getFontData();
		fd[0].setHeight(8);
		Font font = new Font(Display.getCurrent(), fd[0]);
		lblName.setFont(font);
		lblVersion.setFont(font);
		lblRevision.setFont(font);
		setBackground(normal_bg);
		lblIcon.setBackground(normal_bg);
		lblName.setBackground(normal_bg);
		lblVersion.setBackground(normal_bg);
		lblRevision.setBackground(normal_bg);
		addDisposeListener(new DisposeListener(){
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				InstanceCell.this.widgetDisposed(arg0);
			}
		});
		MouseListener selector = new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				if (event.button == 1) {
					InstanceCell.this.parentList.changeSelection(InstanceCell.this.serverId);
				}
			}			
		};
		addMouseListener(selector);
		for (Control c : this.getChildren()) {
			c.addMouseListener(selector);
		}
		setLayout(new InstanceCellLayout());
	}
	
	public Image getIcon() {
		return lblIcon.getImage();
	}
	
	public void setIcon(Image image) {
		image = new Image(image.getDevice(), image.getImageData().scaledTo(32, 32));
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
		Color newColor_fg;
		Color newColor_bg;
		if (this.isSelected) {
			newColor_fg = hilight_fg;
			newColor_bg = hilight_bg;
		} else {
			newColor_fg = normal_fg;
			newColor_bg = normal_bg;
		}
		setBackground(newColor_bg);
		lblIcon.setBackground(newColor_bg);
		lblName.setBackground(newColor_bg);
		lblName.setForeground(newColor_fg);
		lblVersion.setBackground(newColor_bg);
		lblVersion.setForeground(newColor_fg);
		lblRevision.setBackground(newColor_bg);
		lblRevision.setForeground(newColor_fg);
		layout(true);
	}

	public InstanceList getParentList() {
		return parentList;
	}

	public String getServerId() {
		return serverId;
	}

	private class InstanceCellLayout extends Layout {

		Point iExtent, t1Extent, t2Extent, t3Extent;
		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean changed)
		{
			Control [] children = composite.getChildren();
			if (changed || iExtent == null || t1Extent == null || t2Extent == null || t3Extent == null) {
				iExtent = children[0].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t1Extent = children[1].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t2Extent = children[2].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
				t3Extent = children[3].computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
			}
			int width = Math.max(composite.getParent().getSize().x ,5 + iExtent.x + 10 + Math.max(t1Extent.x, Math.max(t2Extent.x, t3Extent.x)));
			int height = Math.max(iExtent.y, (t1Extent.y + t2Extent.y + t3Extent.y + 4));
			return new Point(width + 2, height + 2);
		}

		@Override
		protected void layout(Composite composite, boolean changed)
		{
			Control [] children = composite.getChildren();
			if (changed || iExtent == null || t1Extent == null || t2Extent == null || t3Extent == null) {
				iExtent = children[0].computeSize(32, 32, changed);
				t1Extent = children[1].computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
				t2Extent = children[2].computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
				t3Extent = children[3].computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			}
			children[0].setBounds(5, 5, iExtent.x, iExtent.y);
			children[1].setBounds(iExtent.x + 10, 1, t1Extent.x, t1Extent.y);
			children[2].setBounds(iExtent.x + 10, t1Extent.y + 2, t2Extent.x, t2Extent.y);
			children[3].setBounds(iExtent.x + 10, t1Extent.y + t2Extent.y + 4, t3Extent.x, t3Extent.y);
		}
	}
}
