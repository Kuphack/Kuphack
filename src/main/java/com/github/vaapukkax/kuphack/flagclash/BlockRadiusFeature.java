package com.github.vaapukkax.kuphack.flagclash;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class BlockRadiusFeature extends Feature implements WorldRenderEvents.AfterEntities, EventHolder {

	private static final Map<Block, Map.Entry<Color, Float>> BLOCKS = Map.of(
		Blocks.CHISELED_POLISHED_BLACKSTONE, Map.entry(Color.RED, 4.5f),
		Blocks.GILDED_BLACKSTONE, Map.entry(new Color(100, 50, 0), 8.5f)
	);
	
	private final HashMap<BlockPos, BlockData> blocks = new HashMap<>();
	private Vec3d prevLocation;
	private boolean queUpdate;
	
	public BlockRadiusFeature() {
		super("Shows you the radius of placed Revokers and Disruptors", Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}

	@EventMention
	public void onEvent(ClientBlockPlaceEvent event) {
		if (BLOCKS.containsKey(event.getBlock())) this.queUpdate = true;
	}
	
	@EventMention
	public void onEvent(BlockUpdateEvent event) {
		if (event.getState().isAir() && this.blocks.containsKey(event.getPos())) queUpdate = true;
		if (BLOCKS.containsKey(event.getState().getBlock())) queUpdate = true;
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
		HashMap<BlockPos, Block> newBlocks = new HashMap<>();
		
		for (int x = -16; x < 16; x++) {
			for (int y = -16; y < 16; y++) {
				for (int z = -16; z < 16; z++) {
					BlockPos pos = client.gameRenderer.getCamera().getBlockPos().add(new BlockPos(x, y, z));
					BlockState state = client.world.getBlockState(pos);
					if (BLOCKS.containsKey(state.getBlock())) newBlocks.put(pos, state.getBlock());
				}
			}
		}
		
		blocks.forEach((pos, data) -> {
			if (!newBlocks.containsKey(pos) && !BLOCKS.containsKey(client.world.getBlockState(pos).getBlock())) {
				blocks.get(pos).removing = true;
			}
		});
		for (BlockPos pos : newBlocks.keySet()) {
			if (!blocks.containsKey(pos)) blocks.put(pos, new BlockData(BLOCKS.get(newBlocks.get(pos))));
			else blocks.get(pos).removing = false;
		}
		this.prevLocation = client.gameRenderer.getCamera().getPos();
	}

	private void drawSphere(MatrixStack matrices, double x, double y, double z, BlockData data) {
		float[] base = Color.RGBtoHSB(data.color.getRed(), data.color.getGreen(), data.color.getBlue(), new float[3]);
		Color color = Color.getHSBColor(base[0], base[1], base[2]*Math.min(1, Math.max(0, data.size/data.defSize)));
		
		Immediate builder = client.getBufferBuilders().getEntityVertexConsumers();
		VertexConsumer vertex = builder.getBuffer(RenderLayer.LINE_STRIP);
		
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = matrices.peek().getNormalMatrix();

        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineX = (float) (x + Math.sin(d) * data.size);
        	final float lineZ = (float) (z + Math.cos(d) * data.size);
        	vertex.vertex(matrix4f, lineX, (float)y, lineZ).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
		}
        builder.drawCurrentLayer();
        vertex = builder.getBuffer(RenderLayer.LINE_STRIP);
        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineX = (float) (x + Math.sin(d) * data.size);
        	final float lineY = (float) (y + Math.cos(d) * data.size);
        	vertex.vertex(matrix4f, lineX, lineY, (float)z).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
		}
        builder.drawCurrentLayer();
        vertex = builder.getBuffer(RenderLayer.LINE_STRIP);
        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineY = (float) (y + Math.cos(d) * data.size);
        	final float lineZ = (float) (z + Math.sin(d) * data.size);
        	vertex.vertex(matrix4f, (float)x, lineY, lineZ).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
		}
        builder.drawCurrentLayer();
	}
	
	private class BlockData {
		
		public final Color color;
		public final float defSize;
		public float size;
		public boolean removing;
	
		public BlockData(Map.Entry<Color, Float> entry) {
			this.color = entry.getKey();
			this.defSize = entry.getValue();
			this.size = entry.getValue();
		}
		
		public void tick(float delta) {
			this.size += (((this.removing ? 0 : this.defSize) - this.size) * 0.2) * delta;
		}
		
	}
	
}
