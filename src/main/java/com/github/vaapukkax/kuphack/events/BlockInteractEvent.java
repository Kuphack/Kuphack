package com.github.vaapukkax.kuphack.events;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class BlockInteractEvent extends InteractEvent {

	private final BlockState state;
	
	public BlockInteractEvent(BlockState state, Hand hand, ItemStack stack) {
		super(hand, stack);
		this.state = state;
	}

	public BlockState getState() {
		return this.state;
	}
	
	public Block getBlock() {
		return this.getState().getBlock();
	}
		
}
