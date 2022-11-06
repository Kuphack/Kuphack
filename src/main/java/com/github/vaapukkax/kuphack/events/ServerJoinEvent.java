package com.github.vaapukkax.kuphack.events;

import com.github.vaapukkax.kuphack.Event;

import net.minecraft.client.network.ServerInfo;

public class ServerJoinEvent extends Event {

	private final ServerInfo info;
	
	public ServerJoinEvent(ServerInfo info) {
		this.info = info;
	}
	
	@Override
	public void setCancelled(boolean value) {
		throw new UnsupportedOperationException();
	}
	
	public ServerInfo getInfo() {
		return this.info;
	}
	
}
