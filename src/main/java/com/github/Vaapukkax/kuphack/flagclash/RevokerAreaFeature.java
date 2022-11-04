package com.github.vaapukkax.kuphack.flagclash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.Servers;
import com.github.vaapukkax.kuphack.events.BlockUpdateEvent;
import com.github.vaapukkax.kuphack.events.ClientBlockPlaceEvent;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class RevokerAreaFeature extends Feature implements WorldRenderEvents.AfterEntities, EventHolder {

	private static final Block REVOKER = Blocks.CHISELED_POLISHED_BLACKSTONE;
	
	private final HashMap<BlockPos, RevokerData> blocks = new HashMap<>();
	private Vec3d prevLocation;
	private boolean queUpdate;
	
	public RevokerAreaFeature() {
		super("Shows you the areas of placed revokers", Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}

	@EventMention
	public void onEvent(ClientBlockPlaceEvent event) {
		if (event.getBlock() == REVOKER) this.queUpdate = true;
	}
	
	@EventMention
	public void onEvent(BlockUpdateEvent event) {
		if (event.getState().isAir() && this.blocks.containsKey(event.getPos())) queUpdate = true;
		if (event.getState().getBlock().equals(REVOKER)) queUpdate = true;
	}
	
	@Override
	public void afterEntities(WorldRenderContext context) {
		if (!isPlaying()) return;
		
		if (this.queUpdate || prevLocation == null || prevLocation.distanceTo(context.camera().getPos()) >= 8) {
			this.update();
			this.queUpdate = false;
		}
		
		context.matrixStack().push();
		Kuphack.translateCamera(context);
		
		new HashMap<>(blocks).forEach((pos, data) -> {
			if (data.removing && data.size < 0.1) {
				blocks.remove(pos);
			} else {
				drawSphere(context.matrixStack(), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, data);
				data.tick(context.tickDelta());
			}
		});
		
		context.matrixStack().pop();
	}

	private void update() {
		MinecraftClient client = MinecraftClient.getInstance();
		ArrayList<BlockPos> newBlocks = new ArrayList<>();
		
		for (int x = -16; x < 16; x++) {
			for (int y = -16; y < 16; y++) {
				for (int z = -16; z < 16; z++) {
					BlockPos pos = client.gameRenderer.getCamera().getBlockPos().add(new BlockPos(x, y, z));
					BlockState state = client.world.getBlockState(pos);
					if (state.getBlock() == REVOKER) newBlocks.add(pos);
				}
			}
		}
		
		blocks.forEach((pos, data) -> {
			if (!newBlocks.contains(pos)) blocks.get(pos).removing = true;
		});
		for (BlockPos pos : newBlocks) {
			if (!blocks.containsKey(pos)) blocks.put(pos, new RevokerData());
			else blocks.get(pos).removing = false;
		}
		
		prevLocation = client.gameRenderer.getCamera().getPos();
	}

	static void drawSphere(MatrixStack matrices, double x, double y, double z, RevokerData data) {
		Color color = Color.getHSBColor(0, 1, Math.min(Math.max(0, data.size/RevokerData.DEFAULT_SIZE), 1));
		final float size = data.size;
		
		matrices.push();
		MinecraftClient c = MinecraftClient.getInstance();
		Immediate vBuilder = c.getBufferBuilders().getEntityVertexConsumers();
		VertexConsumer vertex = vBuilder.getBuffer(RenderLayer.LINE_STRIP);
		
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = matrices.peek().getNormalMatrix();

        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineX = (float)(x+Math.sin(d)*size);
        	final float lineZ = (float)(z+Math.cos(d)*size);
        	vertex.vertex(matrix4f, lineX, (float)y, lineZ).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
		}
        vBuilder.draw(RenderLayer.LINE_STRIP);
        vertex = vBuilder.getBuffer(RenderLayer.LINE_STRIP);
        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineX = (float)(x+Math.sin(d)*size);
        	final float lineY = (float)(y+Math.cos(d)*size);
        	vertex.vertex(matrix4f, lineX, lineY, (float)z).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
		}
        vBuilder.draw(RenderLayer.LINE_STRIP);
        vertex = vBuilder.getBuffer(RenderLayer.LINE_STRIP);
        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineY = (float)(y+Math.cos(d)*size);
        	final float lineZ = (float)(z+Math.sin(d)*size);
        	vertex.vertex(matrix4f, (float)x, lineY, lineZ).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
		}
        vBuilder.draw(RenderLayer.LINE_STRIP);
        
        
		matrices.pop();
	}
	
	private class RevokerData {
		
		private static final float DEFAULT_SIZE = 4.5f;
		
		public float size;
		public boolean removing;
		
		public void tick(float delta) {
			this.size += (((this.removing ? 0 : DEFAULT_SIZE) - this.size) * 0.2) * delta;
		}
		
	}
	
}
