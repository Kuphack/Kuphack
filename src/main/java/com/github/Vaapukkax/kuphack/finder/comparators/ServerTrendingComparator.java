package com.github.Vaapukkax.kuphack.finder.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.finder.MinehutServerListScreen;
import com.github.Vaapukkax.kuphack.finder.SortType;
import com.github.Vaapukkax.minehut.Server;

public class ServerTrendingComparator implements Comparator<Server> {
	
	private final Comparator<Server> activityComparator;
	private final Map<String, Integer> trending;
	private final Map<Server, Integer> serverPoints = new HashMap<>();
	
	public ServerTrendingComparator(MinehutServerListScreen screen) {
		this.activityComparator = SortType.ACTIVITY.getComparator(screen);
		this.trending = screen.getTrending();
		
		for (Server entry : screen.getAllEntries()) {
			this.serverPoints.put(entry, getPoints(entry));
		}
	}
	
	@Override
	public int compare(Server o1, Server o2) {
		
		double o1p = serverPoints.containsKey(o1) ? serverPoints.get(o1) : getPoints(o1);
		double o2p = serverPoints.containsKey(o2) ? serverPoints.get(o2) : getPoints(o2);
		
		if (o1p > o2p) return-1;
		if (o1p < o2p) return 1;
		return activityComparator.compare(o1, o2);
	}
	
	private int getPoints(Server entry) {
		int total = 0;
		ArrayList<String> words = new ArrayList<>();
		
    	for (String word : Kuphack.stripColor(entry.getMOTD(), '§').split("\\s+")) {
	    	word = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
    		if (word.length() > 2 && !words.contains(word)) {
	    		int points = trending.containsKey(word) ? trending.get(word) : 0;
	    		total += points;
	    		words.add(word);
    		}
    	}
    	return total;
	}
	
}