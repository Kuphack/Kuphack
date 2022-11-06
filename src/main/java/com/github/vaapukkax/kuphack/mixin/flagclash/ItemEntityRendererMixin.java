package com.github.vaapukkax.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.flagclash.ItemEntityInfoFeature;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
	
	@Inject(at = @At(value = "HEAD"), method = "render")
    public void render(ItemEntity entity, float f, float g, MatrixStack matrices, VertexConsumerProvider provider, int i, CallbackInfo ci) {
    	Kuphack.get().getFeature(ItemEntityInfoFeature.class).render(matrices, provider, entity.getStack());
	}
	
}
