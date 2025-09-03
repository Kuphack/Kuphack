package dev.watukas.kuphack.mixin.flagclash;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.watukas.kuphack.SupportedServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * You could say I was bribed, but instead I'd say I was gifted something so I paid him back :)
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class SneakNameText extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {
	
	private final UUID uuid = UUID.fromString("d76c3884-1ead-40a9-8f65-461d3b5264e2");
	
	protected SneakNameText(Context ctx, PlayerEntityModel model, float f) {
		super(ctx, model, f);
	}
	
	@Inject(at = @At(value = "TAIL"), method = "updateRenderState")
	public void updateRenderState(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float f, CallbackInfo ci) {
		if (SupportedServer.current() != SupportedServer.FLAGCLASH)
			return;
		MinecraftClient c = MinecraftClient.getInstance();
		if (c.player != null && player.isInvisibleTo(c.player))
			return;

		boolean vinue = player.getUuid().equals(uuid);

		if (player.isSneaking() || vinue) {
			if (state.nameLabelPos == null)
				state.nameLabelPos = new Vec3d(0, player.getStandingEyeHeight() + (vinue ? 0.8f : 0.1f), 0);
			state.displayName = vinue
				? Text.literal("QuniaLover69").withColor(0xFFE35995)
				: player.getDisplayName().copy().withColor(0xFFFFFF);
		}
	}

}
