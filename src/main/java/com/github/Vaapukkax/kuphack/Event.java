package com.github.Vaapukkax.kuphack;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public abstract class Event {

	private static final ArrayList<EventListener> listeners = new ArrayList<>();
	
	private boolean cancelled;
	
	public void setCancelled(boolean value) {
		this.cancelled = value;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public ClientPlayerEntity getClientPlayer() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.player;
	}
	
	public static void register(EventListener listener) {
		if (!listeners.contains(listener)) listeners.add(listener);
	}
	public static void unregister(EventListener listener) {
		if (listeners.contains(listener)) listeners.remove(listener);
	}
	
	public static void call(Event event) {
		for (EventListener listener : new ArrayList<>(listeners)) {
			for (Method method : listener.getClass().getDeclaredMethods()) {
				if (method.getName().equalsIgnoreCase("onEvent")) {
					Parameter[] parameters = method.getParameters();
					if (parameters.length == 1) {
						if (parameters[0].getType().isAssignableFrom(event.getClass())) {
							try {
								method.invoke(listener, event);
							} catch (Throwable e) {
								Kuphack.error(e);
							}
						}
					}
				}
			}
		}
	}
	
}
