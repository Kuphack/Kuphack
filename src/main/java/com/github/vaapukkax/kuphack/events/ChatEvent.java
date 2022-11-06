package com.github.vaapukkax.kuphack.events;

import com.github.vaapukkax.kuphack.Event;

import net.minecraft.text.Text;

public class ChatEvent extends Event {

	private final Text message;
	
	public ChatEvent(Text message) {
		this.message = message;
	}
	
	public Text getMessage() {
		return this.message;
	}
	
}
