package dev.watukas.kuphack.flagclash;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.Rendering;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.mixin.EntityHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TNTTrajectoryFeature extends Feature implements WorldRenderEvents.AfterEntities, EventHolder {

	private final DecimalFormat formatter = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));;

	public TNTTrajectoryFeature() {
		super("Shows you the line up for a TNT (it's close enough)", SupportedServer.FLAGCLASH);
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
			Identifier.of("kuphack", "tnt-timer"), this::renderHud
		);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
	}

	@Override
	public void afterEntities(WorldRenderContext context) {
		if (!isPlaying())
			return;

		context.matrixStack().push();
		Kuphack.translateCamera(context);
		
		for (TntEntity entity : context.world().getEntitiesByClass(TntEntity.class, Box.of(client.player.getPos(), 50, 50, 50), tnt -> true)) {
			Text text = Text.of(formatter.format(entity.getFuse() / 20.0) + "s");
			
			context.matrixStack().push();
			context.matrixStack().translate(entity.getCameraPosVec(context.tickCounter().getTickProgress(false)).add(0, 1.5, 0));
			Rendering.renderText(Arrays.asList(text), context.matrixStack(), context.consumers());
			context.matrixStack().pop();

			Immediate builder = client.getBufferBuilders().getEntityVertexConsumers();
			VertexConsumer vertex = builder.getBuffer(RenderLayer.LINES);
			draw(vertex, context.matrixStack().peek(), calculate(entity), 0xFFFF0000);
			builder.draw();
		}
		context.matrixStack().pop();
	}
	
	public void draw(VertexConsumer vertex, MatrixStack.Entry entry, List<Vec3d> points, int color) {
		Vec3d pos = client.gameRenderer.getCamera().getPos().subtract(client.player.getPos());
		for (int i = 0; i < points.size() - 1; i++) {
			Vec3d point = points.get(i);
			Vec3d next = points.get(i + 1);

			vertex.vertex(entry, (float) point.x, (float) point.y, (float) point.z)
				.color(color)
				.normal(entry, pos.toVector3f());
			vertex.vertex(entry, (float) next.x, (float) next.y, (float) next.z)
				.color(color)
				.normal(entry, pos.toVector3f());
		}
		points.clear();
	}
	
	public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		if (!isPlaying())
			return;
		
		TntEntity tnt = client.player.getFirstPassenger() instanceof TntEntity entity ? entity : null;
		
		if (tnt == null) {
			return;
		}

		int x = context.getScaledWindowWidth() / 2 + 91 / 2;
		int y = context.getScaledWindowHeight() - 39;
		
		context.drawItem(new ItemStack(Items.TNT), x, y - 8);
		context.drawTextWithShadow(client.textRenderer, Text.of(formatter.format(tnt.getFuse() / 20.0) + "s"), x + 24, y - 4, 0xFFFF0000);
	}
	
	public List<Vec3d> calculate(TntEntity original) {
		List<Vec3d> points = new ArrayList<>();
		
		TntEntity subject = new TntEntity(EntityType.TNT, original.getWorld());
		subject.copyFrom(original);
		
		if (original.hasVehicle()) {
			Entity vehicle = original.getVehicle();
			
			subject.stopRiding();
			subject.setVelocity(vehicle.getRotationVector().multiply(0.8));
			tickOnly(subject);
			subject.refreshPositionAfterTeleport(vehicle.getEyePos());
		}
		
		final int fuse = subject.getFuse();
		for (int i = -1; i < fuse; i++) {
			points.add(subject.getPos().add(0, 0.5, 0));
			tickOnly(subject);
		}
		
		return points;
	}
	
	private void tickOnly(Entity entity) {
		EntityHelper helper = (EntityHelper) entity;
		
        helper.invokeApplyGravity();
        entity.move(MovementType.SELF, entity.getVelocity());
        entity.setVelocity(entity.getVelocity().multiply(0.98));
        if (entity.isOnGround()) {
            Vec3d vel = entity.getVelocity();
            entity.setVelocity(vel.multiply(0.7, -0.5, 0.7));
        }
        
        helper.invokeUpdateWaterState();
	}

}
