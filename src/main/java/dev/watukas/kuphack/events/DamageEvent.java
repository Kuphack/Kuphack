package dev.watukas.kuphack.events;

import dev.watukas.kuphack.Event;
import net.minecraft.entity.Entity;

public class DamageEvent extends Event {

	private final Entity target;
	
	public DamageEvent(Entity target) {
		this.target = target;
	}
	
	public Entity getTarget() {
		return this.target;
	}
	
}
