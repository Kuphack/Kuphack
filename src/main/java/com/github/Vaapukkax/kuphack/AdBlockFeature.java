package com.github.Vaapukkax.kuphack;

import com.github.Vaapukkax.kuphack.events.ChatEvent;

import net.minecraft.text.Text;

public class AdBlockFeature extends Feature implements EventListener {

	private int total;
	
	public AdBlockFeature() {
		super("Makes advertisements disappear in the lobby", Servers.LOBBY);
	}
	
	public void onEvent(ChatEvent e) {
		if (!isPlaying()) return;
		
		String string = getString(e.getMessage(), new StringBuilder());
		if (string.startsWith("\u00a7d[AD]") || string.startsWith("[AD]")) {
			this.total++;
			e.getClientPlayer().sendMessage(Text.of("\u00a7cBlocked "+total+" ad"+(total == 1 ? "" : "s")), true);
			e.setCancelled(true);
		}
	}
	
	private String getString(Text text, StringBuilder builder) {
		builder.append(text.getString());
		for (Text sibling : text.getSiblings()) {
			getString(sibling, builder);
		}
		return Text.literal(builder.toString()).getString();
	}
	
}
