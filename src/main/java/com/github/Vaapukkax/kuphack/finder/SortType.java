package com.github.Vaapukkax.kuphack.finder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.github.Vaapukkax.kuphack.finder.comparators.ServerActivityComparator;
import com.github.Vaapukkax.kuphack.finder.comparators.ServerTrendingComparator;
import com.github.Vaapukkax.kuphack.finder.comparators.ServerUptimeComparator;
import com.github.Vaapukkax.minehut.Server;

public enum SortType {

	ACTIVITY("Sorts by the percentage of players online\ncompared to the max player count.", new ServerActivityComparator()),
	TRENDING("Sorts based on trendy words\nused in the descriptions.", screen -> new ServerTrendingComparator(screen)),
	UPTIME("Sorts by how long a server has been online for.", new ServerUptimeComparator()),
	SHUFFLE("Shuffles the list randomly", (Comparator<Server>)null);
//	PLAN("Sorts by the plan and uptime.\nShortly, it is useless.", new ServerPlanComparator());

	private final String description;
	private final Function<MinehutServerListScreen, Comparator<Server>> comparator;
	
	private SortType(String description, Function<MinehutServerListScreen, Comparator<Server>> comparator) {
		this.description = description;
		this.comparator = comparator;
	}
	
	private SortType(String description, Comparator<Server> comparator) {
		this.description = description;
		this.comparator = screen -> comparator;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public Comparator<Server> getComparator(MinehutServerListScreen screen) {
		return comparator.apply(screen);
	}

	public SortType next() {
		List<SortType> sortTypes = Arrays.asList(values());
		int i = sortTypes.indexOf(this)+1;
		if (i >= sortTypes.size()) i = 0;
		return sortTypes.get(i);
	}
	
	@Override
	public String toString() {
		return Character.toUpperCase(name().charAt(0))+name().substring(1).toLowerCase();
	}
	
}
