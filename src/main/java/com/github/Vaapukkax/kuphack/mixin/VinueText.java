package com.github.vaapukkax.kuphack.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

/**
 * You could say I were bribed, but I'd say I were gifted something so I paid him back :)
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class VinueText extends EntityRenderer<AbstractClientPlayerEntity> {
	
	protected VinueText(Context ctx) {
		super(ctx);
	}

	@Inject(at = @At(value = "HEAD"), method = "renderLabelIfPresent")
	protected void renderLabelIfPresent(AbstractClientPlayerEntity player, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
	    if (player.getGameProfile().getId().equals(UUID.fromString("d76c3884-1ead-40a9-8f65-461d3b5264e2"))) {//"Vinue".equals(text.getString())) {
			matrixStack.push();
		    matrixStack.translate(0, 0.2, 0);
		    super.renderLabelIfPresent(player, Text.of("§dQuniaLover69"), matrixStack, vertexConsumerProvider, i);
			matrixStack.pop();
	    }
	}

}
