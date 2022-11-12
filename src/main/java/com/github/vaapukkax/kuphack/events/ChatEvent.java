package com.github.vaapukkax.kuphack.events;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.Kuphack;

import net.minecraft.text.Text;

public class ChatEvent extends Event {

	private final Text text;
	
	public ChatEvent(Text text) {
		this.text = text;
	}
	
	public Text getText() {
		return this.text;
	}
	
	public String getMessage() {
		return Kuphack.stripColor(this.text.getString());
	}
	
}
