package com.github.Vaapukkax.kuphack.flagclash.quests;

import java.awt.Color;

import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Servers;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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

@Deprecated
public class DelicateFlowerQuest extends Feature implements WorldRenderEvents.AfterEntities {

//	private static final Block FLOWER = Blocks.LILY_OF_THE_VALLEY;
//	
//	private final ArrayList<BlockPos> blocks = new ArrayList<>();
//	private Vec3d location;
	
	public DelicateFlowerQuest() {
		super(Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}
	
	@Override
	public void afterEntities(WorldRenderContext context) {
//		if (isOnServer() && FlagClash.getQuest() == Quest.DELICATE_FLOWER) {
//			if (location == null || location.distanceTo(context.camera().getPos()) >= 8) {
//				update();
//			}
//			
//			context.matrixStack().push();
//			
//			Kuphack.translateCamera(context);
//			for (BlockPos pos : blocks) {
//				drawLine(context.matrixStack(), pos, context.camera(), Color.GREEN);
//			}
//			
//			context.matrixStack().pop();
//		}
	}
	
//	private void update() {
//		MinecraftClient client = MinecraftClient.getInstance();
//		
//		blocks.clear();
//		for (int x = -64; x < 64; x++) {
//			for (int y = -24; y < 24; y++) {
//				for (int z = -64; z < 64; z++) {
//					BlockPos pos = client.gameRenderer.getCamera().getBlockPos().add(new BlockPos(x, y, z));
//					if (client.isInSingleplayer() || pos.getY() > 3) {
//						BlockState state = client.world.getBlockState(pos);
//						if (state.getBlock() == FLOWER) blocks.add(pos);
//					}
//				}
//			}
//		}
//		
//		location = client.gameRenderer.getCamera().getPos();
//	}

	static void drawLine(MatrixStack matrices, BlockPos pos1, Camera camera, Color color) {
		final float x1 = pos1.getX()+0.5f, y1 = pos1.getY()+0.5f, z1 = pos1.getZ()+0.5f;
		final Vec3d eye = camera.getPos().subtract(0, 0.5, 0);

		matrices.push();
		
		MinecraftClient c = MinecraftClient.getInstance();
		Immediate vBuilder = c.getBufferBuilders().getEntityVertexConsumers();
		VertexConsumer vertex = vBuilder.getBuffer(RenderLayer.LINE_STRIP);
		
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = matrices.peek().getNormalMatrix();

        vertex.vertex(matrix4f, x1, y1, z1).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        vertex.vertex(matrix4f, (float)eye.x, (float)eye.y, (float)eye.z).color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
        
        vBuilder.draw(RenderLayer.LINE_STRIP);
        
		matrices.pop();
	}
	
}
