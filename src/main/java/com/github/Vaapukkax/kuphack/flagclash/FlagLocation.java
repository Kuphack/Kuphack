package com.github.Vaapukkax.kuphack.flagclash;

import com.github.Vaapukkax.kuphack.EventListener;
import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.kuphack.events.BlockUpdateEvent;
import com.github.Vaapukkax.kuphack.events.ClientBlockBreakEvent;
import com.github.Vaapukkax.kuphack.events.ClientBlockPlaceEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;

/**
 * This feature is used by other features to determine where the flag is.
 * Can't do anything about a feature based system.
 */
public class FlagLocation extends Feature implements EventListener {

	private BlockPos location;
	
	public FlagLocation() {
		super(null, Servers.FLAGCLASH);
	}
	
	/**
	 * Whether the client thinks the flag is still placed. Can't determine if the chunk isn't loaded, so it returns true then
	 */
	public boolean isFlagPlaced() {
		if (location == null) return false;
		
		MinecraftClient client = MinecraftClient.getInstance();
		BlockState state = client.world.getBlockState(location);
		boolean loaded = client.world.getChunkManager().isChunkLoaded(ChunkSectionPos.getSectionCoord(location.getX()), ChunkSectionPos.getSectionCoord(location.getZ()));
		if (this.location != null && (!loaded || isBanner(state.getBlock())))
			return true;
		this.location = null;
		return false;
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
	public void onEvent(ClientBlockBreakEvent e) {
		if (!e.getPos().equals(this.location)) return;
		this.location = null;
		
		if (isBanner(e.getState().getBlock())) {
			Kuphack.get().getFeature(FlagBreakTime.class).show(e.getPos());
		}
	}
	
	public void onEvent(BlockUpdateEvent e) {
		if (e.getPos().equals(this.location) && e.getState().isAir()) this.location = null;
	}

	/**
	 * an ordinary event
	 */
	public void onEvent(ClientBlockPlaceEvent e) {
		if (isBanner(e.getBlock())) {
			if (!isFlagPlaced()) this.location = e.getPos();
		}
	}
	
}
