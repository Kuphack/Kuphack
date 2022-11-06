package com.github.vaapukkax.kuphack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public abstract class Event {

	private static final HashMap<EventHolder, Map<Class<? extends Event>, List<Method>>> holders = new HashMap<>();
	
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
	
	public static void register(EventHolder holder) {
		if (holders.containsKey(holder)) return;
		holders.put(holder, getMethods(holder.getClass()));
	}
	
	public static void unregister(EventHolder holder) {
		try {
			holders.remove(holder);
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}
	}
	
	public static void call(Event event) {
		long start = System.currentTimeMillis();
		new HashMap<>(holders).forEach((holder, methods) -> {
			List<Method> list = methods.get(event.getClass());
			if (list == null) return;
			for (Method method : list) {
				try {
					method.invoke(holder, event);
				} catch (Throwable e) {
					Kuphack.error(e);
				}
			}
		});
		
		long difference = System.currentTimeMillis() - start;
		if (difference > 50) Kuphack.LOGGER.info(
			event.getClass().getSimpleName()+" took a while! approx " + difference + " ms"
		);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<Class<? extends Event>, List<Method>> getMethods(Class<?> holder) {
		HashMap<Class<? extends Event>, List<Method>> map = new HashMap<>();
		for (Method method : holder.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(EventMention.class)) continue;
			
			Parameter[] parameters = method.getParameters();
			if (parameters.length == 1) {
				Class<? extends Event> type = (Class<? extends Event>) parameters[0].getType();
				map.computeIfAbsent(type, k -> new ArrayList<>()).add(method);
			} else throw new IllegalArgumentException("'"+holder+"' has EventMention method which doesn't have the correct amount of parameters");
		}
		return Collections.unmodifiableMap(map);
	}

	public interface EventHolder {
		
	}
	
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface EventMention {
		
	}
	
}
