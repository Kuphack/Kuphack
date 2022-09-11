package com.github.Vaapukkax.kuphack.events;

import com.github.Vaapukkax.kuphack.Event;

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
