package com.github.vaapukkax.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.SupportedServer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

	@Inject(method = "renderScoreboardSidebar", at = @At(value = "INVOKE"), cancellable = true)
    private void renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo callback) {
		if (Kuphack.getServer() == SupportedServer.FLAGCLASH) {
			callback.cancel();
			Kuphack.renderSidebar(context, objective);
		}
    }
	
}