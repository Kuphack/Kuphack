package dev.watukas.kuphack.events;

import dev.watukas.kuphack.Event;
import dev.watukas.kuphack.Kuphack;
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
