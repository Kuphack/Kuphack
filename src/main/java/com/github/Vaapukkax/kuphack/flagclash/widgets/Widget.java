package com.github.Vaapukkax.kuphack.flagclash.widgets;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public interface Widget {

	public int getX();
	public int getY();

	public ItemStack getIcon();
	public List<Text> getDescription();
	
	public default Text getTitle() {
		return getIcon().getName();
	}

}
