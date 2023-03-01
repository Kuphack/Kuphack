package com.github.vaapukkax.kuphack;

import java.util.regex.Pattern;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.events.ChatEvent;
import com.github.vaapukkax.kuphack.modmenu.SettingsKuphackScreen;
import com.google.gson.JsonObject;

import net.minecraft.text.Text;

public class AdBlockFeature extends Feature implements EventHolder {

	private static final Pattern STRICT = Pattern.compile("(?i)((\\s|/)(join|msg)|/play\\s+\\w{3,12})");
	
	public boolean strict = true;
	private int total;
	
	public AdBlockFeature() {
		super("Makes advertisements disappear in the lobby", SupportedServer.LOBBY);
	}
	
	@EventMention
	public void onEvent(ChatEvent e) {
		if (!isPlaying()) return;
		String message = e.getMessage();
		if (message.startsWith("To ") || message.startsWith("From ")) return;
		
		if (this.strict ? STRICT.matcher(e.getMessage()).find() : message.startsWith("[AD]")) {
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
		String state = "(" + this.getTextState() +": " + (this.strict ? "Blocks all messages including §njoin§r, §n/play§r and §nmsg§r" : "Blocks the messages sent with /ad") + ")";
		return super.getDescription() + "\n" + state;
	}
	
	public void save() {
		JsonObject object = Kuphack.get().readDataFile();
		object.addProperty("ad-block-strict", this.strict);
		SettingsKuphackScreen.write(object);
	}
	
}
