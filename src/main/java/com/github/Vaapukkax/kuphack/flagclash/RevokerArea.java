package com.github.Vaapukkax.kuphack.flagclash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.Vaapukkax.kuphack.EventListener;
import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.kuphack.events.BlockBreakEvent;
import com.github.Vaapukkax.kuphack.events.BlockPlaceEvent;

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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

@Deprecated
public class RevokerArea extends Feature implements WorldRenderEvents.AfterEntities, EventListener {

	private static final Block REVOKER = Blocks.CHISELED_POLISHED_BLACKSTONE;
	
	private final HashMap<BlockPos, RevokerData> blocks = new HashMap<>();
	private Vec3d location;
	
	private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
	
	public RevokerArea() {
		super(Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}

	public void onEvent(BlockPlaceEvent event) {
		if (event.getBlock() == REVOKER) update();
	}
	
	public void onEvent(BlockBreakEvent event) {
		if (event.getState().getBlock() == REVOKER) {
			queue.add(() -> { update(); });
		}
	}
	
	@Override
	public void afterEntities(WorldRenderContext context) {
		if (isOnServer()) {
			try {
				while (queue.size() > 0) queue.take().run();
			} catch (InterruptedException e) {}
			
			if (location == null || location.distanceTo(context.camera().getPos()) >= 8) {
				update();
			}
			
			context.matrixStack().push();
			
			translateCamera(context);
			new HashMap<>(blocks).forEach((pos, data) -> {
				if (data.removing && data.size < 0.1) {
					blocks.remove(pos);
				} else {
					Color color = Color.getHSBColor(0, 1, Math.min(Math.max(0, data.size/RevokerData.DEFAULT_SIZE), 1));
					drawSphere(context.matrixStack(), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, data, color, data.size);
					if (isHoldingBook())
						drawSphere(context.matrixStack(), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, data, Color.YELLOW, data.size+3);
					data.tick(context.tickDelta());
				}
			});
			
			context.matrixStack().pop();
		}
	}
	
	private boolean isHoldingBook() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			ItemStack stack = Kuphack.getHolding(client.player);
			return (stack != null && (stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.BOOK));
		}
		return false;
	}
	
	private void update() {
		MinecraftClient client = MinecraftClient.getInstance();
		ArrayList<BlockPos> newBlocks = new ArrayList<>();
		
		for (int x = -16; x < 16; x++) {
			for (int y = -16; y < 16; y++) {
				for (int z = -16; z < 16; z++) {
					BlockPos pos = client.gameRenderer.getCamera().getBlockPos().add(new BlockPos(x, y, z));
					BlockState state = client.world.getBlockState(pos);
					if (state.getBlock() == REVOKER) {
						newBlocks.add(pos);
					}
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
		
		location = client.gameRenderer.getCamera().getPos();
	}
	
	private static void translateCamera(WorldRenderContext context) {
//		MinecraftClient client = MinecraftClient.getInstance();
		MatrixStack matrices = context.matrixStack();
		Vec3d pos = context.camera().getPos();

//		client.gameRenderer.loadProjectionMatrix(context.projectionMatrix());

		matrices.translate(-(pos.x), -(pos.y), -(pos.z));
	}

	static void drawSphere(MatrixStack matrices, double x, double y, double z, RevokerData data, Color color, final double size) {
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
		
		private static final float DEFAULT_SIZE = 4.75f;
		
		public float size;
		public boolean removing;
		
		public void tick(float delta) {
			size += (((removing ? 0 : DEFAULT_SIZE)-size)*0.1) * delta;
		}
		
	}
	
}
