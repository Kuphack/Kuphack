package com.github.Vaapukkax.kuphack;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public abstract class HotkeyFeature extends Feature implements ClientTickEvents.StartTick {

	private final KeyBinding binding;
	
	public HotkeyFeature(Servers... servers) {
		super(servers);
		this.binding = null;
	}
	
	public abstract void onPress(MinecraftClient client);
	
	@Override
	public void onStartTick(MinecraftClient client) {
		if (binding == null || !isOnServer() || client.player == null) return;
		
		while (binding.wasPressed()) {
			onPress(client);
		}
	}

}
