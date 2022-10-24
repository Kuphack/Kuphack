package com.github.Vaapukkax.kuphack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.MinecraftClient;

public abstract class Feature {

	protected final MinecraftClient client = MinecraftClient.getInstance();
	
	public final Servers[] servers;
	private boolean disabled;
	
	private final String description;
	
	public Feature(String description, Servers... servers) {
		this.description = description;
		this.servers = servers;
		this.disabled = doesOccurInDisabled();
	}
	
	private boolean doesOccurInDisabled() {
		JsonObject object = new Gson().fromJson(Kuphack.get().readDataFile(), JsonObject.class);
		if (object == null || !object.has("disabled")) return false;
		return object.get("disabled").getAsJsonArray()
			.contains(new JsonPrimitive(this.getClass().getSimpleName()));
	}
	
	protected void onActivate() {
		if (this instanceof EventListener) Event.register((EventListener)this);
	}
	
	protected void onDeactivate() {
		if (this instanceof EventListener) Event.unregister((EventListener)this);
	}
	
	public boolean isOnServer() {
		Servers current = Kuphack.getServer();
		if (current == null || MinecraftClient.getInstance().getCurrentServerEntry() == null) return false;
		
		if (servers == null || servers.length == 0) return true;
		for (Servers server : servers) {
			if (current == server) return true;
		}
		return false;
	}
	
	public boolean isPlaying() {
		return !disabled && isOnServer() && client.player != null;
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}
	
	public String getDisableState() {
		return disabled ? "OFF" : "ON";
	}
	
	public void toggle() {
		if (!disabled && isOnServer()) this.onDeactivate();
		this.disabled = !this.disabled;
		if (!disabled && isOnServer()) this.onActivate();
	}

	public String getName() {
		String name = getClass().getSimpleName();
		if (name.endsWith("Feature")) name = name.substring(0, name.length() - "Feature".length());
		
		StringBuilder builder = new StringBuilder();
		for (int c : name.chars().toArray()) {
			if (!builder.isEmpty() && Character.isUpperCase(c)) builder.append(" ");
			builder.append((char)c);
		}
		return builder.toString();
	}

	public String getDescription() {
		return this.description;
	}
	
}
