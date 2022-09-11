package com.github.Vaapukkax.kuphack.flagclash;

import com.github.Vaapukkax.kuphack.EventListener;
import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.kuphack.events.BlockBreakEvent;
import com.github.Vaapukkax.kuphack.events.BlockPlaceEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

/**
 * This feature is used by other features to determine where the flag is.
 * Can't do anything about a feature based system.
 */
public class FlagLocation extends Feature implements EventListener {

	private BlockPos location;
	
	public FlagLocation() {
		super(Servers.FLAGCLASH);
	}
	
	public boolean isFlagPlaced() {
		if (location == null) return false;
		
		MinecraftClient client = MinecraftClient.getInstance();
		BlockState state = client.world.getBlockState(location);
		if (isBanner(state.getBlock())) {
			return true;
		} else {
			location = null;
			return false;
		}
	}
	
	public BlockPos getLocation() {
		return location;
	}
	
	private boolean isBanner(Block block) {
		if (block == null) return false;
		return Registry.BLOCK.getId(block).toString().toLowerCase().contains("banner");
	}
	
	/**
	 * an ordinary event, kind of odd how this references the FlagBreakTime feature instead of the other way around.
	 */
	public void onEvent(BlockBreakEvent e) {
		if (e.getPos().equals(location)) {
			this.location = null;
			if (isBanner(e.getState().getBlock())) {
				Kuphack.get().getFeature(FlagBreakTime.class).show(e.getPos());
			}
		}
	}

	/**
	 * an ordinary event
	 */
	public void onEvent(BlockPlaceEvent e) {
		if (isBanner(e.getBlock())) {
			if (!isFlagPlaced()) location = e.getPos();
		}
	}
	
}
