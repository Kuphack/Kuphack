package dev.watukas.kuphack;

import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Event.EventMention;
import dev.watukas.kuphack.events.InventoryClickEvent;
import dev.watukas.kuphack.finder.MinehutServerListScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public class ServerListReplacement extends Feature implements EventHolder {

	public ServerListReplacement() {
		super("Makes you go to the custom server list when clicking on the official server list in the lobby", SupportedServer.LOBBY);
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

	@Override
	public String getName() {
		return "List Replacement";
	}
	
}
