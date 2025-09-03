package dev.watukas.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.watukas.kuphack.SupportedServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.render.entity.state.TntEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Mixin(TntEntityRenderer.class)
public class TntEntityRendererMixin {

	@Shadow
	private BlockRenderManager blockRenderManager;
	
	@Inject(at = @At(value = "INVOKE"), method = "render", cancellable = true)
	public void render(TntEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider provider, int light, CallbackInfo ci) {
		if (SupportedServer.current() != SupportedServer.FLAGCLASH)
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.player.squaredDistanceTo(state.x, state.y - 2, state.z) > 1) {
			return;
		}

		ci.cancel();
		
		matrixStack.push();
		matrixStack.translate(0.0F, 0.5F, 0.0F);
		
		float f = state.fuse;
		float g = 1.0F - state.fuse / 10.0F;
		g = MathHelper.clamp(g, 0.0F, 1.0F);
		float h = 0.25F + g * 0.5F;
		matrixStack.scale(h, h, h);

		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
		matrixStack.translate(-0.5F, -0.5F, 0.5F);
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
		if (state.blockState != null) {
			TntMinecartEntityRenderer.renderFlashingBlock(
				this.blockRenderManager, state.blockState, matrixStack, provider, light, (int)f / 5 % 2 == 0
			);
		}

		matrixStack.pop();
	}

}
