package org.mcupdater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.mcupdater.model.Module;

public class ModuleCheckbox extends Composite {

	private Button chk;
	private Module data;

	public ModuleCheckbox(Composite parent, Module data) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());
		chk = new Button(this, SWT.CHECK);
		this.data = data;
		chk.setText(data.getName());
		if (data.getRequired() || data.getIsDefault()) { chk.setSelection(true); }
		if (data.getRequired()) { chk.setEnabled(false); }
		chk.setSize(chk.computeSize(500, SWT.DEFAULT));
		this.pack();
	}

	public Module getModule() {
		return this.data;
	}

	public void setModule(Module data) {
		this.data = data;
	}

	public boolean isSelected() {
		return this.chk.getSelection();
	}
}
