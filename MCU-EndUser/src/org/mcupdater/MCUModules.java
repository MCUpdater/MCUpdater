package org.mcupdater;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.mcupdater.util.Module;

public class MCUModules extends Composite {

	private Composite container;
	private RowLayout rlContainer = new RowLayout(SWT.VERTICAL);
	private ScrolledComposite scroller;
	private List<ModuleCheckbox> modules = new ArrayList<ModuleCheckbox>();

	public MCUModules(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());
		scroller = new ScrolledComposite(this, SWT.V_SCROLL);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		container = new Composite(scroller, SWT.NONE);
		scroller.setContent(container);
		container.setLayout(rlContainer);
	}

	public void reload(List<Module> modList) {
		for (Control c : container.getChildren()) {
			modules.remove(c);
			c.dispose();
		}
		container.pack(true);
		for (Module m : modList) {
			modules.add(new ModuleCheckbox(container, m));
		}
		container.pack();
		scroller.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public List<ModuleCheckbox> getModules() {
		return this.modules;
	}
}
