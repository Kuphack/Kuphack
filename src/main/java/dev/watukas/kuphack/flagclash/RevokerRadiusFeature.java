package dev.watukas.kuphack.flagclash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.joml.Vector3f;

import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Event.EventMention;
import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.Rendering;
import dev.watukas.kuphack.SupportedServer;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RevokerRadiusFeature extends Feature implements WorldRenderEvents.AfterEntities, EventHolder {

	private static final Block block = Blocks.CHISELED_POLISHED_BLACKSTONE;
	private static final Color color = Color.RED;
	private static final float defSize = 5f;

	private final HashMap<BlockPos, BlockData> blocks = new HashMap<>();
	private List<BlockPos> positions = Collections.emptyList();
	
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
		if (!isPlaying())
			return;

		if (this.queUpdate || prevLocation == null || prevLocation.distanceTo(context.camera().getPos()) >= 4) {
			this.update();
			this.queUpdate = false;
		}
		
		MatrixStack matrices = context.matrixStack();
		
		matrices.push();
		Kuphack.translateCamera(context);
		
		boolean shaders = Kuphack.isUsingShaders();

		new HashMap<>(blocks).forEach((pos, data) -> {
			if (data.removing && data.size == 0.0) {
				this.blocks.remove(pos);
				this.positions = null;
				return;
			}

			if (shaders || this.client.getDebugHud().shouldShowDebugHud())
				drawSphere(matrices, pos.toCenterPos(), data);

			data.tick(context.tickCounter().getFixedDeltaTicks());
		});
		
		if (!shaders) {
			var builder = client.getBufferBuilders().getEntityVertexConsumers();
			VertexConsumer vertex = builder.getBuffer(Rendering.SECTION_QUADS);
			
			Color c = new Color(200, 0, 100, 128);
			
			if (this.positions == null) {
				this.positions = blocks.values().stream()
					.sorted(Comparator.comparingDouble(data -> data.pos.getSquaredDistance(context.camera().getPos())))
					.flatMap(data -> data.getBlocksInRadius().stream()).distinct().limit(520000).toList();
			}
			
			Map<BlockPos, BlockState> states = new HashMap<>();
			for (BlockPos pos : this.positions) {
				drawFaces(matrices, vertex, pos, c, 0.0001f, states);
			}
	
			builder.draw();
		}

		matrices.pop();
	}


	private void update() {
		HashMap<BlockPos, Block> newBlocks = new HashMap<>();
		
		for (int x = -16; x < 16; x++) {
			for (int y = -16; y < 16; y++) {
				for (int z = -16; z < 16; z++) {
					BlockPos pos = client.gameRenderer.getCamera().getBlockPos().add(new BlockPos(x, y, z));
					BlockState state = client.world.getBlockState(pos);
					if (state.getBlock() == block) newBlocks.put(pos, state.getBlock());
				}
			}
		}

		blocks.forEach((pos, data) -> {
			if (newBlocks.containsKey(pos))
				return;
			if (client.world.getBlockState(pos).getBlock() == block) {
				return;
			}
			blocks.get(pos).removing = true;
		});
		for (BlockPos pos : newBlocks.keySet()) {
			if (!blocks.containsKey(pos)) blocks.put(pos, new BlockData(pos));
			else blocks.get(pos).removing = false;
		}
		this.prevLocation = client.gameRenderer.getCamera().getPos();
		this.positions = null;
	}

	private void drawFaces(MatrixStack matrices, VertexConsumer vertex, BlockPos pos, Color color, float eps, Map<BlockPos, BlockState> states) {
		MatrixStack.Entry entry = matrices.peek();
		Function<BlockPos, BlockState> getState = key -> states.computeIfAbsent(key, k -> client.world.getBlockState(k));
		
		if (getState.apply(pos).isFullCube(client.world, pos))
			return;
		
		for (Direction d : Direction.values()) {
			BlockPos neighborPos = pos.offset(d);
			BlockState neighborState = getState.apply(neighborPos);

			if (!neighborState.isSideSolidFullSquare(client.world, neighborPos, d.getOpposite())) {
				continue;
			}

			float[][] corners = unitFace(d.getOpposite(), eps);

			for (float[] c : corners) {
				vertex.vertex(entry.getPositionMatrix(), neighborPos.getX() + c[0], neighborPos.getY() + c[1], neighborPos.getZ() + c[2])
					.color(color.getRGB())
					.normal(entry, -d.getOffsetX(), -d.getOffsetY(), -d.getOffsetZ());
			}
		}
	}

	private static float[][] unitFace(Direction face, float eps) {
		final float min = 0f - eps;
		final float max = 1f + eps;

		switch (face) {
			case UP:    return new float[][]{{min, max, min}, {max, max, min}, {max, max, max}, {min, max, max}};
			case DOWN:  return new float[][]{{min, min, max}, {max, min, max}, {max, min, min}, {min, min, min}};
			case NORTH: return new float[][]{{min, min, min}, {max, min, min}, {max, max, min}, {min, max, min}};
			case SOUTH: return new float[][]{{max, min, max}, {min, min, max}, {min, max, max}, {max, max, max}};
			case WEST:  return new float[][]{{min, min, max}, {min, min, min}, {min, max, min}, {min, max, max}};
			case EAST:  return new float[][]{{max, min, min}, {max, min, max}, {max, max, max}, {max, max, min}};
			default:    return new float[0][];
		}
	}

	private void drawSphere(MatrixStack matrices, Vec3d pos, BlockData data) {

		float[] base = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
		int color = Color.getHSBColor(base[0], base[1], base[2]*Math.min(1, Math.max(0, data.size / defSize))).getRGB();

		Immediate builder = client.getBufferBuilders().getEntityVertexConsumers();
		VertexConsumer vertex = builder.getBuffer(RenderLayer.getLines());

		MatrixStack.Entry entry = matrices.peek();

		List<Vec3d> points = new ArrayList<>();
		Runnable draw = () -> {
			for (int i = 0; i < points.size(); i++) {
				Vec3d point = points.get(i);
				Vec3d next = points.get((i + 1) % points.size());
				
				Vector3f dir1 = new Vec3d(point.x - pos.x, point.y - pos.y, point.z - pos.z).toVector3f();

				vertex.vertex(entry, (float) point.x, (float) point.y, (float) point.z)
					.color(color)
					.normal(entry, dir1);
				vertex.vertex(entry, (float) next.x, (float) next.y, (float) next.z)
					.color(color)
					.normal(entry, dir1);
			}
			points.clear();
		};
		
		for (float d = 0; d < Math.toRadians(361); d += 0.1) {
			final float lineX = (float) (pos.x + Math.sin(d) * data.size);
			final float lineZ = (float) (pos.z + Math.cos(d) * data.size);
			points.add(new Vec3d(lineX, pos.y, lineZ));
		}
		draw.run();
		
		for (float d = 0; d < Math.toRadians(361); d += 0.1) {
			final float lineX = (float) (pos.x + Math.sin(d) * data.size);
			final float lineY = (float) (pos.y + Math.cos(d) * data.size);
			points.add(new Vec3d(lineX, lineY, pos.z));
		}
		draw.run();

		for (float d = 0; d < Math.toRadians(361); d += 0.1) {
			final float lineY = (float) (pos.y + Math.cos(d) * data.size);
			final float lineZ = (float) (pos.z + Math.sin(d) * data.size);
			points.add(new Vec3d(pos.x, lineY, lineZ));
		}
		draw.run();

		builder.draw();
	}
	
	private class BlockData {

		public float size;
		public boolean removing;

		private final BlockPos pos;

		public BlockData(BlockPos pos) {
			this.pos = pos;
			this.size = 0.0f;
		}

		public void tick(float delta) {
			float dest = this.removing ? 0 : defSize;
			
			if (Math.abs(dest - this.size) < 0.15) {
				if (this.size != dest)
					RevokerRadiusFeature.this.positions = null;
				this.size = dest;
			} else {
				this.size += Math.signum(dest - this.size) * 0.5f * delta;
//				this.size += ((dest - this.size) * 0.25f) * delta;
				RevokerRadiusFeature.this.positions = null;
			}
		}

		public List<BlockPos> getBlocksInRadius() {
			int size = MathHelper.ceil(this.size);

			List<BlockPos> blocks = new ArrayList<>();
			for (BlockPos p : BlockPos.iterateOutwards(pos, size, size, size)) {
				if (this.pos.getSquaredDistance(p) <= this.size * this.size) {
					blocks.add(p.mutableCopy());
				}
			}

			return blocks;
		}

	}

}
