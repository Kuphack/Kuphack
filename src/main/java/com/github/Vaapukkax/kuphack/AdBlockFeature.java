package com.github.Vaapukkax.kuphack;

import com.github.Vaapukkax.kuphack.events.ChatEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

public class AdBlockFeature extends Feature implements EventListener {

	public boolean toggle = !FabricLoader.getInstance().isModLoaded("minehutplus");
	private int total;
	
	public AdBlockFeature() {
		super(Servers.LOBBY);
		Gson gson = new Gson();
		JsonObject object = new JsonObject();
		try {
			object = gson.fromJson(Kuphack.get().readDataFile(), JsonObject.class);
		} catch (Exception e) {}

		if (object != null && object.has("adblock")) {
			toggle = object.get("adblock").getAsBoolean();
		}
	}
	
	public void onEvent(ChatEvent e) {
		if (!toggle) return;
		
		String string = getString(e.getMessage(), new StringBuilder());
		
//		boolean marketAd = string.matches("(.*\n)?\\[(Minehut|Market)]\\s.+");
		
		if (string.startsWith("\u00a7d[AD]") || string.startsWith("[AD]")) {
			total++;
			e.getClientPlayer().sendMessage(Text.of("\u00a7cBlocked "+total+" ad"+(total == 1 ? "" : "s")), true);
			e.setCancelled(true);
		}
	}
	
	public String getString(Text text, StringBuilder builder) {
		builder.append(text.getString());
		for (Text sibling : text.getSiblings()) {
			getString(sibling, builder);
		}
		return Text.literal(builder.toString()).getString();
	}
	
}
