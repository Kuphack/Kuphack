package com.github.vaapukkax.kuphack.flagclash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.Servers;
import com.github.vaapukkax.kuphack.events.BlockUpdateEvent;
import com.github.vaapukkax.kuphack.events.ClientBlockBreakEvent;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class StariteTracerFeature extends Feature implements EventHolder, WorldRenderEvents.AfterEntities {

	private static final List<Block> STARITES = Arrays.asList(Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
	private final HashMap<BlockPos, Long> tracers = new HashMap<>();
	
	public StariteTracerFeature() {
		super("When a source of starite spawns, a tracer appears", Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
		this.toggle();
	}
	
	@Override
	public void afterEntities(WorldRenderContext context) {
		if (!isPlaying() || tracers.isEmpty()) return;
		
		context.matrixStack().push();
		Kuphack.translateCamera(context);
		
		ArrayList<BlockPos> queue = new ArrayList<>();
		this.tracers.forEach((pos, time) -> {
			if (System.currentTimeMillis()-time > 60 * 1000) 
				queue.add(pos);
			if (pos.isWithinDistance(context.camera().getPos(), 600))
				drawLine(context.matrixStack(), pos, context.camera(), new Color(64, 64, 255));
		});
		for (BlockPos pos : queue) {
			if (!STARITES.contains(context.world().getBlockState(pos).getBlock()))
				this.tracers.remove(pos);
			else this.tracers.put(pos, System.currentTimeMillis());
		}		
		context.matrixStack().pop();
	}
	
	@EventMention
	public void onEvent(ClientBlockBreakEvent e) {
		if (STARITES.contains(e.getBlock())) tracers.remove(e.getPos());
	}
	
	@EventMention
	public void onEvent(BlockUpdateEvent e) {
		if (STARITES.contains(e.getBlock())) tracers.put(e.getPos(), System.currentTimeMillis());
		else tracers.remove(e.getPos());
	}
	
	static void drawLine(MatrixStack matrices, BlockPos pos1, Camera camera, Color color) {
		double yaw = Math.toRadians(camera.getYaw() + 90);
		double pitch = Math.toRadians(camera.getPitch());
		final Vec3d eye = camera.getPos().add(Math.cos(yaw) * 0.5, -pitch * 0.1 - 0.1, Math.sin(yaw) * 0.5);
		final float x1 = pos1.getX() + 0.5f, y1 = pos1.getY() + 0.5f, z1 = pos1.getZ() + 0.5f;
	
		matrices.push();
		MinecraftClient c = MinecraftClient.getInstance();
		Immediate vBuilder = c.getBufferBuilders().getEntityVertexConsumers();
		VertexConsumer vertex = vBuilder.getBuffer(RenderLayer.LINES);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = matrices.peek().getNormalMatrix();

        vertex.vertex(matrix4f, x1, y1, z1)
        	.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f)
        	.normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertex.vertex(matrix4f, (float)eye.x, (float)eye.y, (float)eye.z)
        	.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f)
        	.normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vBuilder.draw(RenderLayer.LINES);        
		matrices.pop();
	}
	
}
