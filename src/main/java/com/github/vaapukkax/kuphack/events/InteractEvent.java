package com.github.vaapukkax.kuphack.events;

import com.github.vaapukkax.kuphack.Event;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class InteractEvent extends Event {

	private final Hand hand;
	private final ItemStack stack;
	
	public InteractEvent(Hand hand, ItemStack stack) {
		this.hand = hand;
		this.stack = stack;
	}
	
	public ItemStack getStack() {
		return this.stack;
	}
	
	public Item getItem() {
		return this.getStack().getItem();
	}
	
	public Hand getHand() {
		return this.hand;
	}
	
}
