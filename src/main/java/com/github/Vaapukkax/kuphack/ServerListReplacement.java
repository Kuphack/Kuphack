package com.github.Vaapukkax.kuphack;

import com.github.Vaapukkax.kuphack.events.InventoryClickEvent;
import com.github.Vaapukkax.kuphack.finder.MinehutServerListScreen;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public class ServerListReplacement extends Feature implements EventListener {

	public ServerListReplacement() {
		super(Servers.LOBBY);
	}
	
	/**
	 * ordinary event for clicking
	 */
	public void onEvent(InventoryClickEvent e) {
		if (e.getStack().getItem() == Items.BOOK && isEnabledFromSettings()) {
			e.setCancelled(true);
			e.getClientPlayer().closeHandledScreen();
			
			MinecraftClient client = MinecraftClient.getInstance();
			client.setScreen(new MinehutServerListScreen(client.currentScreen));
		}
	}
	
	private boolean isEnabledFromSettings() {
		try {
			Gson gson = new Gson();
			return gson.fromJson(Kuphack.get().readDataFile(), JsonObject.class).get("lobbyInject").getAsBoolean();
		} catch (Exception e) {}
		return false;
	}
	
}
