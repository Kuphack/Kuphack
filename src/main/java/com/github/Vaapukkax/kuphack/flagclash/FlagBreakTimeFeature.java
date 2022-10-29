package com.github.Vaapukkax.kuphack.flagclash;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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

public class FlagBreakTimeFeature extends Feature implements WorldRenderEvents.AfterEntities {

	public Vec3d location;
	private long time = -1;
	
	public FlagBreakTimeFeature() {
		super("Shows the flag break time above your flag", Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}
	
	public void show(BlockPos pos) {
		location = new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5);
		time = System.currentTimeMillis() + 20_000;
	}

	@Override
	public void afterEntities(WorldRenderContext context) {
		if (time == -1) return;
		
		double seconds = (time - System.currentTimeMillis()) / 1000d;
		if (seconds > 0) {
			MinecraftClient client = MinecraftClient.getInstance();
			MatrixStack matrix = context.matrixStack();
			matrix.push();
			
			Vec3d pos = context.camera().getPos();
			matrix.translate(-pos.x, -pos.y, -pos.z);
			
			matrix.translate(location.x, location.y, location.z);
			DecimalFormat format = new DecimalFormat("0.000");
			format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			Kuphack.renderText(Text.of("§5Breaking in: "+format.format(seconds)+"s"), matrix, client.getBufferBuilders().getEffectVertexConsumers());
			matrix.pop();
		} else this.time = -1;
	}
	
}
