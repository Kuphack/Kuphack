package com.github.Vaapukkax.kuphack.flagclash;

import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Servers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class StablePipeFeature extends Feature implements ClientTickEvents.StartTick {
	
	protected static final Vec3d PIPE = new Vec3d(-49.5, -59, -24.5);
	public static final Box PIPE_BOX = new Box(
		PIPE.getX()-1, PIPE.getY(),    PIPE.getZ()-1,
		PIPE.getX()+1, PIPE.getY()+25, PIPE.getZ()+1
	);
	
	public StablePipeFeature() {
		super("Allows you to go up the lobby pipe more stably", Servers.FLAGCLASH);
		ClientTickEvents.START_CLIENT_TICK.register(this);
	}

	@Override
	public void onStartTick(MinecraftClient client) {
		if (!isPlaying()) return;
		
		if (client.player.getBoundingBox().intersects(PIPE_BOX)) {
			Vec3d velocity = client.player.getVelocity();
			client.player.setVelocity(velocity.x,
				client.player.isSneaking() ? velocity.y > 0.3 ? 0 : velocity.y : 0.425
			, velocity.z);
		}
	}
	
}
