package dev.watukas.kuphack.finder.comparators;

import java.util.Comparator;
import com.github.vaapukkax.minehut.Server;

public class ServerActivityComparator implements Comparator<Server> {
	
	@Override
	public int compare(Server o1, Server o2) {
		// No longer taking max player count because of external servers
		double o1a = o1.getPlayerCount(); //(double)Math.max(1, o1.getPlan().getPlayerCount());
		double o2a = o2.getPlayerCount(); //(double)Math.max(1, o2.getPlan().getPlayerCount());
		
		if (o1a > o2a) return-1;
		if (o1a < o2a) return 1;
		
		double o1ia = o1.getInactivityTime();
		if (o1ia == 0L) o1ia = System.currentTimeMillis();
		double o2ia = o2.getInactivityTime();
		if (o2ia == 0L) o2ia = System.currentTimeMillis();
		
		if (o1ia > o2ia) return-1;
		if (o1ia < o2ia) return 1;
		
		return 0;
	}
	
}