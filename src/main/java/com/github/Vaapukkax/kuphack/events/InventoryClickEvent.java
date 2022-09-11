package com.github.Vaapukkax.kuphack.events;

import com.github.Vaapukkax.kuphack.Event;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;

public class InventoryClickEvent extends Event {

	private final GenericContainerScreen screen;
	private final ItemStack stack;
	
	public InventoryClickEvent(GenericContainerScreen screen, ItemStack stack) {
		this.screen = screen;
		this.stack = stack;
	}
	
	public GenericContainerScreen getScreen() {
		return this.screen;
	}
	
	public ItemStack getStack() {
		return this.stack;
	}
	
}
