package com.github.vaapukkax.kuphack.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.MinecraftClient;

public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			MinecraftClient client = MinecraftClient.getInstance();
			return new SettingsKuphackScreen(client.currentScreen);
		};
	}
	
}
