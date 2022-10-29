package com.github.Vaapukkax.kuphack;

import java.util.Iterator;
import java.util.function.Predicate;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ScoreboardObjective;

public enum Servers {

	FUNGIFY, ARROWMANCE, BITZONE, FLAGCLASH, FLATLIGHT,
	
	LOBBY(new Predicate<MinecraftClient>() {

		@Override
		public boolean test(MinecraftClient client) {
			if (Kuphack.isOnMinehut() && client.world != null && client.world.getScoreboard() != null) {
				Iterator<ScoreboardObjective> it = client.world.getScoreboard().getObjectives().iterator();
				while (it.hasNext()) {
					String name = it.next().getDisplayName().getString();
					if (name.equals("MINEHUT")) return true;
				}
			}
			return false;
		}
		
	});
	
	private final Predicate<MinecraftClient> predicate;
	
	private Servers() {
		this(null);
	}
	
	private Servers(Predicate<MinecraftClient> predicate) {
		this.predicate = predicate;
	}
	
	public boolean test(MinecraftClient client) {
		return predicate != null && predicate.test(client);
	}
	
	@Override
	public String toString() {
		return name().charAt(0) + name().substring(1).toLowerCase();
	}
	
}
