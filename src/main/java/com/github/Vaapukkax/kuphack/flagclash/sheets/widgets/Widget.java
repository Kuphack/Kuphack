package com.github.vaapukkax.kuphack.flagclash.sheets.widgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public interface Widget {

	public ItemStack getIcon();
	
	public default Text getTitle() {
		ItemStack stack = this.getIcon();
		Text name = stack.getName();
		if (name.getString().equals("minecraft")) return stack.getItem().getName(stack);
		return name;
	}
	
	default List<Text> getDescription() {
		NbtList list = getIcon().getNbt().getCompound(ItemStack.DISPLAY_KEY)
			.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
		ArrayList<Text> text = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			text.add(Text.Serializer.fromJson(list.getString(i)));
		}
		return text;
	}

}
