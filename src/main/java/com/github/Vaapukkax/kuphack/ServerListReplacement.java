package com.github.Vaapukkax.kuphack;

import com.github.Vaapukkax.kuphack.events.InventoryClickEvent;
import com.github.Vaapukkax.kuphack.finder.MinehutServerListScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public class ServerListReplacement extends Feature implements EventListener {

	public ServerListReplacement() {
		super("Makes you go to the custom server list when clicking on the official server list in the lobby", Servers.LOBBY);
	}
	
	/**
	 * ordinary event for clicking
	 */
	public void onEvent(InventoryClickEvent e) {
		if (e.getStack().getItem() == Items.BOOK && isPlaying()) {
			e.setCancelled(true);
			e.getClientPlayer().closeHandledScreen();
			
			MinecraftClient client = MinecraftClient.getInstance();
			client.setScreen(new MinehutServerListScreen(client.currentScreen));
		}
	}
	
}
