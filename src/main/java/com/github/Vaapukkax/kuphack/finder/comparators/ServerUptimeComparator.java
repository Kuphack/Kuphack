package com.github.vaapukkax.kuphack.finder.comparators;

import java.util.Comparator;
import com.github.vaapukkax.minehut.Server;

public class ServerUptimeComparator implements Comparator<Server> {
	
	@Override
	public int compare(Server o1, Server o2) {
		if (o1.getLastOnline() < o2.getLastOnline()) return-1;
		if (o1.getLastOnline() > o2.getLastOnline()) return 1;
		return 0;
	}
	
}