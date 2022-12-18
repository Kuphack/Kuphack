package com.github.vaapukkax.kuphack;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public abstract class Feature {

	protected final MinecraftClient client = MinecraftClient.getInstance();
	
	public final SupportedServer[] servers;
	private boolean disabled;
	
	private final String description;
	
	public Feature(String description, SupportedServer... servers) {
		this.description = description;
		this.servers = servers;
		this.disabled = doesOccurInDisabled();
	}
	
	private boolean doesOccurInDisabled() {
		JsonObject object = Kuphack.get().readDataFile();
		if (object == null || !object.has("disabled")) return false;
		return object.get("disabled").getAsJsonArray()
			.contains(new JsonPrimitive(this.getClass().getSimpleName()));
	}
	
	protected void onActivate() {
		if (this instanceof EventHolder) Event.register((EventHolder)this);
	}
	
	protected void onDeactivate() {
		if (this instanceof EventHolder) Event.unregister((EventHolder)this);
	}
	
	public boolean isOnServer() {
		SupportedServer current = Kuphack.getServer();
		if (current == null || (!FabricLoader.getInstance().isDevelopmentEnvironment() && client.getCurrentServerEntry() == null))
			return false;
		
		if (servers == null || servers.length == 0) return true;
		for (SupportedServer server : servers) {
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
	
	public String getTextState() {
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
