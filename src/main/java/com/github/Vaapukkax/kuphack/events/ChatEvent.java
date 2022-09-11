package com.github.Vaapukkax.kuphack.events;

import com.github.Vaapukkax.kuphack.Event;

import net.minecraft.text.Text;

public class ChatEvent extends Event {

	private Text message;
	
	public ChatEvent(Text message) {
		this.message = message;
	}
	
	public void setMessage(Text message) {
		this.message = message;
	}
	
	public Text getMessage() {
		return this.message;
	}
	
}