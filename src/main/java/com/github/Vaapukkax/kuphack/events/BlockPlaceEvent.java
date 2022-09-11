package com.github.Vaapukkax.kuphack.events;

import com.github.Vaapukkax.kuphack.Event;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class BlockPlaceEvent extends Event {

	private final BlockPos pos;
	private final Block block;
	
	public BlockPlaceEvent(BlockPos pos, Block block) {
		this.pos = pos;
		this.block = block;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public BlockPos getPos() {
		return this.pos;
	}
	
}
