package dev.watukas.kuphack.flagclash;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FlagBreakTimeFeature extends Feature implements WorldRenderEvents.AfterEntities {

	private final DecimalFormat format;
	
	private BlockPos pos;
	private long time = -1;
	
	public FlagBreakTimeFeature() {
		super("Shows the flag break time above your flag", SupportedServer.FLAGCLASH);
		this.format = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}
	
	public void show(BlockPos pos, double time) {
		this.pos = pos;
		this.time = System.currentTimeMillis() + (long) (time * 1000.0);
	}

	private Vec3d getLocation() {
		Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5);
		
		Vec3d offset;
		if (client.world.getBlockState(pos.add(0, 2, 0)).isAir())
			offset = new Vec3d(0, 0.2, 0);
		else {
			offset = client.gameRenderer.getCamera().getPos().subtract(center).normalize()
				.multiply(0.25);
			offset = new Vec3d(offset.x, -0.5, offset.z);
		}
			
		return center.add(offset);
	}
	
	@Override
	public void afterEntities(WorldRenderContext context) {
		if (time == -1)
			return;
		double seconds = (this.time - System.currentTimeMillis()) / 1000d;
		
		if (seconds > 0) {
			MinecraftClient client = MinecraftClient.getInstance();
			Vec3d location = this.getLocation();
			Vec3d camera = context.camera().getPos();
			
			MatrixStack matrix = context.matrixStack();
			matrix.push();
			
			matrix.translate(-camera.x, -camera.y, -camera.z);
			matrix.translate(location.x, location.y, location.z);
			List<Text> text = Arrays.asList(
				Text.of("Removing flag..."),
				Text.literal(format.format(seconds)).withColor(0xFFFF2266)
			);
			Kuphack.renderText(text, matrix, client.getBufferBuilders().getEffectVertexConsumers());
			
			matrix.pop();
		} else this.time = -1;
	}
	
}
