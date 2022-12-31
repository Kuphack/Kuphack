package com.github.vaapukkax.kuphack.flagclash;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.SupportedServer;
import com.github.vaapukkax.kuphack.events.BlockUpdateEvent;
import com.github.vaapukkax.kuphack.events.ClientBlockBreakEvent;
import com.github.vaapukkax.kuphack.events.ClientBlockPlaceEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

/**
 * This feature is used by other features to determine where the flag is.
 * Can't do anything about a feature based system. All of this technically could be moved to {@link FlagClash}
 */
public class FlagLocation extends Feature implements EventHolder {

	private BlockPos location;
	
	public FlagLocation() {
		super(null, SupportedServer.FLAGCLASH);
	}
	
	/**
	 * Whether the client thinks the flag is still placed.
	 * Can't determine if the chunk isn't loaded, so it returns true then.
	 */
	public boolean isFlagDown() {
		if (location == null) return false;
		
		MinecraftClient client = MinecraftClient.getInstance();
		BlockState state = client.world.getBlockState(location);
		boolean loaded = client.world.getChunkManager().isChunkLoaded(ChunkSectionPos.getSectionCoord(location.getX()), ChunkSectionPos.getSectionCoord(location.getZ()))
			&& client.player.squaredDistanceTo(this.location.getX(), this.location.getY(), this.location.getZ()) < 600;
		if (this.location != null && (!loaded || isBanner(state.getBlock())))
			return true;
		this.location = null;
		return false;
	}
	
	public BlockPos getLocation() {
		return this.location;
	}
	
	private boolean isBanner(Block block) {
		if (block == null) return false;
		return Registries.BLOCK.getId(block).toString().toLowerCase().contains("banner");
	}
	
	@EventMention
	public void onEvent(ClientBlockBreakEvent e) {
		if (!e.getPos().equals(this.location)) return;
		this.location = null;
		
		if (isBanner(e.getState().getBlock())) {
			Kuphack.get().getFeature(FlagBreakTimeFeature.class).show(e.getPos());
		}
	}
	
	@EventMention
	public void onEvent(BlockUpdateEvent e) {
		if (e.getPos().equals(this.location) && e.getState().isAir()) this.location = null;
	}

	@EventMention
	public void onEvent(ClientBlockPlaceEvent e) {
		if (isBanner(e.getBlock())) {
			if (!isFlagDown()) this.location = e.getPos();
		}
	}
	
}
