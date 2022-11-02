package com.github.vaapukkax.kuphack.events;

import com.github.vaapukkax.kuphack.Event;

import net.minecraft.client.world.ClientWorld;

public class ChangeWorldEvent extends Event {

	private ClientWorld world;
	
	public ChangeWorldEvent(ClientWorld world) {
		this.world = world;
	}
	
	public ClientWorld getWorld() {
		return this.world;
	}
	
}
