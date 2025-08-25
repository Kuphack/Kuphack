package dev.watukas.kuphack.flagclash;

import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Event.EventMention;
import dev.watukas.kuphack.events.BlockUpdateEvent;
import dev.watukas.kuphack.events.ClientBlockBreakEvent;
import dev.watukas.kuphack.events.ClientBlockPlaceEvent;
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
	private long placeUnixTime;
	
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
		if (!e.getPos().equals(this.location)) 
			return;
		this.location = null;
				
		if (isBanner(e.getState().getBlock())) {

			double secondsSincePlaced = (System.currentTimeMillis() - this.placeUnixTime) / 1000.0;
			double seconds =
				secondsSincePlaced > 160 ?
					27
				: secondsSincePlaced > 60 ?
					18
				: secondsSincePlaced > 5 ?
					10
				: 6;
			
			Kuphack.get().getFeature(FlagBreakTimeFeature.class).show(e.getPos(), seconds);
		}
	}
	
	@EventMention
	public void onEvent(BlockUpdateEvent e) {
		if (e.getPos().equals(this.location)) {
			if (e.getState().isAir())
				this.location = null;
		}
	}

	@EventMention
	public void onEvent(ClientBlockPlaceEvent e) {
		if (isBanner(e.getBlock())) {
			if (!isFlagDown()) {
				this.location = e.getPos();
				this.placeUnixTime = System.currentTimeMillis();
			}
		}
	}
	
}
