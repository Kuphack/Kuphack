package dev.watukas.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.watukas.kuphack.SupportedServer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InGameHud.class)
public class HudMixin {

	@Inject(at = @At(value = "INVOKE"), method = "renderFood", cancellable = true)
	private void renderFood(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
		if (SupportedServer.current() == SupportedServer.FLAGCLASH) {
			ci.cancel();
		}
	}

}