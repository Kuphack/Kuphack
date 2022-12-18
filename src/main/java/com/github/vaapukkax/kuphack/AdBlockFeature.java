package com.github.vaapukkax.kuphack;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.events.ChatEvent;
import com.github.vaapukkax.kuphack.modmenu.SettingsKuphackScreen;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.text.Text;

public class AdBlockFeature extends Feature implements EventHolder {

	public boolean strict = true;
	private int total;
	
	public AdBlockFeature() {
		super("Makes advertisements disappear in the lobby", SupportedServer.LOBBY);
	}
	
	@EventMention
	public void onEvent(ChatEvent e) {
		if (!isPlaying()) return;
		
		if (e.getMessage().startsWith(this.strict ? "join" : "[AD]")) {
			this.total++;
			e.getClientPlayer().sendMessage(Text.of("§cBlocked " + total + " ad" + (total == 1 ? "" : "s")), true);
			e.setCancelled(true);
		}
	}
	
	@Override
	public void toggle() {
		if (!this.isDisabled() && this.strict) {
			this.strict = false;
		} else {
			this.strict = true;
			super.toggle();
		}
	}
	
	@Override
	public String getTextState() {
		if (this.isDisabled()) return super.getTextState();
		return this.strict ? "Strict" : "Lazy";
	}
	
	@Override
	public String getDescription() {
		if (this.isDisabled()) return super.getDescription();
		String state = "(" + this.getTextState() +": " + (this.strict ? "Blocks all messages including \"join\"" : "Blocks the messages sent with /ad") + ")";
		return super.getDescription() + "\n" + state;
	}
	
	public void save() {
		JsonObject object = Kuphack.get().readDataFile();
		object.addProperty("ad-block-strict", this.strict);
		SettingsKuphackScreen.write(new Gson().toJson(object));
	}
	
}
