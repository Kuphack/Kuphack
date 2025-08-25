package dev.watukas.kuphack.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
public abstract class VinueText extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {
	
	private final UUID uuid = UUID.fromString("d76c3884-1ead-40a9-8f65-461d3b5264e2");
	
	protected VinueText(Context ctx, PlayerEntityModel model, float f) {
		super(ctx, model, f);
	}
	
	@Inject(at = @At(value = "TAIL"), method = "updateRenderState")
	public void updateRenderState(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float f, CallbackInfo ci) {
		if (!player.getUuid().equals(uuid))
			return;
		MinecraftClient c = MinecraftClient.getInstance();
		if (c.player != null && player.isInvisibleTo(c.player))
			return;
		state.nameLabelPos = new Vec3d(0, 2.4f, 0);
		state.displayName = Text.literal("QuniaLover69").withColor(0xFFE35995);
	}

}
