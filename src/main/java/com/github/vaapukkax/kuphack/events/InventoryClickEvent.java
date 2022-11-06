package com.github.vaapukkax.kuphack.events;

import java.util.List;
import java.util.function.Predicate;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.Kuphack;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryClickEvent extends Event {

	private final GenericContainerScreen screen;
	private final ItemStack stack;
	
	public InventoryClickEvent(GenericContainerScreen screen, ItemStack stack) {
		this.screen = screen;
		this.stack = stack;
	}
	
	@SafeVarargs
	public final boolean has(Item item, String name, Predicate<String>... predicates) {
		return this.has(a -> a == item, b -> b.equals(name), predicates);
	}
	
	@SafeVarargs
	public final boolean has(Predicate<Item> item, Predicate<String> name, Predicate<String>... predicates) {
		if (!item.test(this.stack.getItem())) return false;
		if (name != null && !name.test(Kuphack.stripColor(this.stack.getName()).strip())) return false;
		
		List<String> lore = Kuphack.getStripLore(stack);
		for (int i = 0; i < predicates.length; i++) {
			if (!predicates[i].test(lore.get(i))) return false;
		}
		return true;
	}
	
	public GenericContainerScreen getScreen() {
		return this.screen;
	}
	
	public ItemStack getStack() {
		return this.stack;
	}

	public Item getItem() {
		return this.stack.getItem();
	}
	
}
