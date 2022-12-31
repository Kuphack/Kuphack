package com.github.vaapukkax.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.SupportedServer;
import com.github.vaapukkax.kuphack.flagclash.FoodItem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;

@Mixin(InGameHud.class)
public class HudMixin {

	@Shadow
	private int scaledHeight, scaledWidth;

	@Inject(at = @At(value = "INVOKE", ordinal = 30), method = "renderStatusBars", cancellable = true)
	private void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
		if (Kuphack.getServer() != SupportedServer.FLAGCLASH) return;
		ci.cancel();

		boolean renderBubbles = renderBubbles(matrices);
		if (renderBubbles) matrices.translate(0, -10, 0);

		MinecraftClient c = MinecraftClient.getInstance();
		FoodItem item = FoodItem.of(Kuphack.getHolding(c.player));
		
		int i = 0;
		if (item != null) for (String effect : item.getEffects().keySet()) {
			float x = (float) (c.getWindow().getScaledWidth() / 2 - c.textRenderer.getWidth(effect) / 2 + 48);
			float y = (float) (c.getWindow().getScaledHeight() - 38) - i * 9;
			c.textRenderer.drawWithShadow(matrices, effect, x, y, item.getEffects().get(effect).getRGB());
			i++;
		}
		
		if (renderBubbles) matrices.translate(0, 10, 0);
	}
	
	private int getHeartCount2(LivingEntity entity) {
		if (entity != null && entity.isLiving()) {
			float f = entity.getMaxHealth();
			int i = (int) (f + 0.5F) / 2;
			if (i > 30) {
				i = 30;
			}
			return i;
		} else {
			return 0;
		}
	}

	private boolean renderBubbles(MatrixStack matrices) {
		matrices.translate(0, 10, 0);
		MinecraftClient c = MinecraftClient.getInstance();
		c.getProfiler().swap("air");
		boolean b = false;
		int z = c.player.getMaxAir(), x = getHeartCount2(c.player);
		int aa = Math.min(c.player.getAir(), z), ab;
		int t = scaledHeight - 39;
		int n = scaledWidth / 2 + 91;

		if (c.player.isSubmergedIn(FluidTags.WATER) || aa < z) {
			ab = (int) Math.ceil((double) x / 10.0D) - 1;
			t -= ab * 10;
			int ah = MathHelper.ceil((double) (aa - 2) * 10.0D / (double) z);
			int ad = MathHelper.ceil((double) aa * 10.0D / (double) z) - ah;

			for (int aj = 0; aj < ah + ad; ++aj) {
				if (aj < ah) {
					get().drawTexture(matrices, n - aj * 8 - 9, t, 16, 18, 9, 9);
				} else {
					get().drawTexture(matrices, n - aj * 8 - 9, t, 25, 18, 9, 9);
				}
			}
			b = true;
		}
		matrices.translate(0, -10, 0);
		c.getProfiler().pop();
		return b;
	}

	private InGameHud get() {
		return (InGameHud) ((Object) this);
	}
}