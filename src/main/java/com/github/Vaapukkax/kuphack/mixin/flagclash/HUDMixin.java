package com.github.Vaapukkax.kuphack.mixin.flagclash;

import java.util.ArrayList;
import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.MathHelper;

@Mixin(InGameHud.class)
public class HUDMixin {

	@Shadow
	private int scaledHeight, scaledWidth;

	@Inject(at = @At(value = "INVOKE", ordinal = 30), method = "renderStatusBars", cancellable = true)
	private void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
		if (Kuphack.getServer() == Servers.FLAGCLASH) {
			ci.cancel();

			boolean renderBubbles = renderBubbles(matrices);
			if (renderBubbles) matrices.translate(0, -10, 0);
			
			// Item info
			MinecraftClient c = MinecraftClient.getInstance();
			TextRenderer r = c.textRenderer;

			ItemStack stack = Kuphack.getHolding(c.player);
			Item item = stack.getItem();

			HashMap<String, Integer> map = new HashMap<>();
			if (item == Items.DRIED_KELP)
				map.put((c.options.language.equals("lol_us") ? "+1 HeightP" : "+1 Health"),
						java.awt.Color.RED.getRGB());
			else if (item == Items.COOKIE) {
				map.put((c.options.language.equals("lol_us") ? "Cocaine II" : "Speed II"),
						new java.awt.Color(148, 243, 255).getRGB());
			} else if (item == Items.COOKED_BEEF) {
				map.put((c.options.language.equals("lol_us") ? "+2 HeightP" : "+2 Health"),
						java.awt.Color.RED.getRGB());
				map.put((c.options.language.equals("lol_us") ? "+2 mor HP" : "+2 Health Boost"),
						new java.awt.Color(255, 64, 96).getRGB());
			} else if (item == Items.PUMPKIN_PIE) {
				map.put((c.options.language.equals("lol_us") ? "+5 HeightP" : "+5 Health"),
						java.awt.Color.RED.getRGB());
			}
			ArrayList<String> list = new ArrayList<>(map.keySet());
			for (int i = 0; i < list.size(); i++) {

				String str = list.get(i);
				int color = map.get(str);
				float x = (float) (c.getWindow().getScaledWidth() / 2 - r.getWidth(str) / 2 + 48);
				float y = (float) (c.getWindow().getScaledHeight() - 38) - i * 9;
				r.drawWithShadow(matrices, str, x, y, color);
			}
			
			if (renderBubbles) matrices.translate(0, 10, 0);
		}
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
		c.getProfiler().pop();
		matrices.translate(0, -10, 0);
		return b;
	}

	private InGameHud get() {
		return (InGameHud) ((Object) this);
	}
}