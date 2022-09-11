package com.github.Vaapukkax.kuphack;

public abstract class Feature {

	public final Servers[] servers;
	
	public Feature(Servers... servers) {
		this.servers = servers;
	}
	
	public void onEnable() {
		if (this instanceof EventListener) Event.register((EventListener)this);
	}
	
	public void onDisable() {
		if (this instanceof EventListener) Event.unregister((EventListener)this);
	}
	
	public boolean isOnServer() {
		Servers current = Kuphack.getServer();
		if (current == null) return false;
		
		if (servers == null || servers.length == 0) return true;
		for (Servers server : servers) {
			if (current == server) return true;
		}
		return false;
	}
	
}
