package dev.watukas.kuphack.flagclash;

import java.awt.Color;
import java.util.HashMap;

import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Event.EventMention;
import dev.watukas.kuphack.events.BlockUpdateEvent;
import dev.watukas.kuphack.events.ClientBlockPlaceEvent;
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
import net.minecraft.util.math.Vec3d;

public class RevokerRadiusFeature extends Feature implements WorldRenderEvents.AfterEntities, EventHolder {

	private static final Block block = Blocks.CHISELED_POLISHED_BLACKSTONE;
	private static final Color color = Color.RED;
	private static final float defSize = 6f;
	
	private final HashMap<BlockPos, BlockData> blocks = new HashMap<>();
	private Vec3d prevLocation;
	private boolean queUpdate;
	
	public RevokerRadiusFeature() {
		super("Shows you the radius of placed Revokers", SupportedServer.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}

	@EventMention
	public void onEvent(ClientBlockPlaceEvent event) {
		if (event.getBlock().equals(block)) this.queUpdate = true;
	}
	
	@EventMention
	public void onEvent(BlockUpdateEvent event) {
		if (event.getState().isAir() && this.blocks.containsKey(event.getPos()))
			this.queUpdate = true;
		if (block.equals(event.getState().getBlock()))
			this.queUpdate = true;
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
				drawSphere(context.matrixStack(), pos.toCenterPos(), data);
				data.tick(context.tickCounter().getFixedDeltaTicks());
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
					if (state.getBlock().equals(block)) newBlocks.put(pos, state.getBlock());
				}
			}
		}
		
		blocks.forEach((pos, data) -> {
			if (!newBlocks.containsKey(pos) && block != client.world.getBlockState(pos).getBlock()) {
				blocks.get(pos).removing = true;
			}
		});
		for (BlockPos pos : newBlocks.keySet()) {
			if (!blocks.containsKey(pos)) blocks.put(pos, new BlockData());
			else blocks.get(pos).removing = false;
		}
		this.prevLocation = client.gameRenderer.getCamera().getPos();
	}

	private void drawSphere(MatrixStack matrices, Vec3d pos, BlockData data) {
		float[] base = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
		Color color = Color.getHSBColor(base[0], base[1], base[2]*Math.min(1, Math.max(0, data.size / defSize)));
		
		Immediate builder = client.getBufferBuilders().getEntityVertexConsumers();
		VertexConsumer vertex = builder.getBuffer(RenderLayer.LINE_STRIP);
		
        MatrixStack.Entry entry = matrices.peek();

        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineX = (float) (pos.x + Math.sin(d) * data.size);
        	final float lineZ = (float) (pos.z + Math.cos(d) * data.size);
        	vertex.vertex(entry, lineX, (float) pos.y, lineZ)
        		.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha()/255f)
        		.normal(entry, 1.0f, 0.0f, 0.0f);
		}
        builder.drawCurrentLayer();
        vertex = builder.getBuffer(RenderLayer.LINE_STRIP);
        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineX = (float) (pos.x + Math.sin(d) * data.size);
        	final float lineY = (float) (pos.y + Math.cos(d) * data.size);
        	vertex.vertex(entry, lineX, lineY, (float) pos.z)
        		.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
        		.normal(entry, 1.0f, 0.0f, 0.0f);
		}
        builder.drawCurrentLayer();
        vertex = builder.getBuffer(RenderLayer.LINE_STRIP);
        for (float d = 0; d < Math.toRadians(361); d += 0.1) {
        	final float lineY = (float) (pos.y + Math.cos(d) * data.size);
        	final float lineZ = (float) (pos.z + Math.sin(d) * data.size);
        	vertex.vertex(entry, (float) pos.x, lineY, lineZ)
        		.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f)
        		.normal(entry, 1.0f, 0.0f, 0.0f);
		}
        builder.drawCurrentLayer();
        builder.draw();
	}
	
	private static class BlockData {
		
		public float size;
		public boolean removing;
	
		public BlockData() {
			this.size = 0.0f;
		}
		
		public void tick(float delta) {
			float dest = this.removing ? 0 : defSize;
			this.size += ((dest - this.size) * 0.25f) * delta;
		}
		
	}
	
}
