package com.github.Vaapukkax.kuphack.events;

import com.github.Vaapukkax.kuphack.Event;

import net.minecraft.client.network.ServerInfo;

public class ServerJoinEvent extends Event {

	private ServerInfo info;
	
	public ServerJoinEvent(ServerInfo info) {
		this.info = info;
	}
	
	public ServerInfo getInfo() {
		return this.info;
	}
	
}
