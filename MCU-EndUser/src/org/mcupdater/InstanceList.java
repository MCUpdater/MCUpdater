package org.mcupdater;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class InstanceList extends ScrolledComposite {

	private Color normal;
	private Color hilight;
	private Composite listBase;
	private List<InstanceCell> instances;
	private int selected = -1;

	public InstanceList(Composite parent) {
		super(parent, SWT.V_SCROLL | SWT.BORDER);
		normal = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		hilight = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_SELECTION);
		instances = new ArrayList<InstanceCell>();
		listBase = new Composite(this, SWT.NONE);
		this.setContent(listBase);
		listBase.setBackground(normal);
	}

}
