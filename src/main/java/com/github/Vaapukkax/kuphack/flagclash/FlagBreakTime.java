package com.github.Vaapukkax.kuphack.flagclash;

import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FlagBreakTime extends Feature implements WorldRenderEvents.AfterEntities {

	public Vec3d location;
	private long time = -1;
	
	public FlagBreakTime() {
		super(Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}
	
	public void show(BlockPos pos) {
		location = new Vec3d(pos.getX()+0.5, pos.getY()+2, pos.getZ()+0.5);
		time = System.currentTimeMillis()+20000;
	}

	@Override
	public void afterEntities(WorldRenderContext context) {
		if (time != -1) {
			double seconds = (time-System.currentTimeMillis())/1000d;
			if (seconds < 0) {
				time = -1;
			} else {
				MinecraftClient client = MinecraftClient.getInstance();
				
				MatrixStack matrix = context.matrixStack();
				matrix.push();
				
				Vec3d p = context.camera().getPos();
				matrix.translate(-p.x, -p.y, -p.z);
				
				matrix.translate(location.x, location.y, location.z);
				Kuphack.renderText(Text.of("\u00a75Breaking in: "+seconds+"s"), matrix, client.getBufferBuilders().getEffectVertexConsumers());
				matrix.pop();
			}
		}
	}
	
}
