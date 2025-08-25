package dev.watukas.kuphack.events;

import dev.watukas.kuphack.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class ClientBlockBreakEvent extends Event {

	private final BlockPos pos;
	
	public ClientBlockBreakEvent(BlockPos pos) {
		this.pos = pos;
	}
	
	public BlockState getState() {
		MinecraftClient c = MinecraftClient.getInstance();
		return c.world.getBlockState(pos);
	}
	
	public BlockPos getPos() {
		return this.pos;
	}

	public Block getBlock() {
		return getState().getBlock();
	}
	
}
