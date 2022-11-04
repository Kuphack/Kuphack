package com.github.vaapukkax.kuphack;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.events.InventoryClickEvent;
import com.github.vaapukkax.kuphack.finder.MinehutServerListScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public class ServerListReplacement extends Feature implements EventHolder {

	public ServerListReplacement() {
		super("Makes you go to the custom server list when clicking on the official server list in the lobby", Servers.LOBBY);
	}
	
	@EventMention
	public void onEvent(InventoryClickEvent e) {
		if (e.getStack().getItem() == Items.BOOK && isPlaying()) {
			e.setCancelled(true);
			e.getClientPlayer().closeHandledScreen();
			
			MinecraftClient client = MinecraftClient.getInstance();
			client.setScreen(new MinehutServerListScreen(client.currentScreen));
		}
	}
	
}
