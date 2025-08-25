package dev.watukas.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.flagclash.ItemEntityInfoFeature;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
	
	private static ItemStack stack;
	
	@Inject(at = @At(value = "TAIL"), method = "updateRenderState")
	public void updateRenderState(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float f, CallbackInfo ci) {
		stack = itemEntity.getStack();
	}
	
	@Inject(at = @At(value = "INVOKE"), method = "renderStack")
    private static void renderStack(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStackEntityRenderState state, Random random, Box box, CallbackInfo ci) {
		
		float offset = state instanceof ItemEntityRenderState s ? s.uniqueOffset : 0;
		float h = ItemEntity.getRotation(state.age, offset);
		
    	matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-h));
    	Kuphack.get().getFeature(ItemEntityInfoFeature.class).render(matrices, vertexConsumers, stack);
    	matrices.pop();
    	
	}
	
}
