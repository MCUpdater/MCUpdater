package org.smbarbour.mcu.util;

import java.util.Comparator;

public class ModuleComparator implements Comparator<Module> {

	@Override
	public int compare(Module o1, Module o2) {
		Integer o1weight = (o1.getInJar() ? 0 : (o1.getCoreMod() ? 1 : 2));
		Integer o2weight = (o2.getInJar() ? 0 : (o2.getCoreMod() ? 1 : 2));
		if (o1weight == o2weight && !o1.getInJar() && !o2.getInJar()) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		} else if (o1.getInJar() && o2.getInJar()) {
			return Integer.valueOf(o1.getJarOrder()).compareTo(Integer.valueOf(o2.getJarOrder()));
		}
		else {
			return o1weight.compareTo(o2weight);
		}
	}

}
