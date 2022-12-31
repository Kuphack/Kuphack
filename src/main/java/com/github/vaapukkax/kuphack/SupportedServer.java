package com.github.vaapukkax.kuphack;

import java.util.function.Predicate;

import net.minecraft.client.MinecraftClient;

public enum SupportedServer {

	LOBBY(client -> {
		if (!Kuphack.isOnMinehut() || client.world == null || client.world.getScoreboard() == null)
			return false;
		return client.world.getScoreboard().getObjectives().stream()
			.anyMatch(a -> a.getDisplayName().getString().equals("MINEHUT"));
	}),
	
	FLAGCLASH, OVERCOOKED;
	
	private final Predicate<MinecraftClient> predicate;
	
	private SupportedServer() {
		this(null);
	}
	
	private SupportedServer(Predicate<MinecraftClient> predicate) {
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
