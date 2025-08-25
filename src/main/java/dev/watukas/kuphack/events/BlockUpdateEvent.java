package dev.watukas.kuphack.events;

import dev.watukas.kuphack.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockUpdateEvent extends Event {

	private final BlockPos pos;
	private final BlockState state;
	
	public BlockUpdateEvent(BlockPos pos, BlockState state) {
		this.pos = pos;
		this.state = state;
	}
	
	@Override
	public void setCancelled(boolean value) {
		throw new UnsupportedOperationException("Can't cancel updating a block");
	}
	
	public BlockPos getPos() {
		return this.pos;
	}
	
	public BlockState getState() {
		return this.state;
	}

	public Block getBlock() {
		return this.state.getBlock();
	}
	
}
