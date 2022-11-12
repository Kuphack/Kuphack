package com.github.vaapukkax.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.Servers;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.ScoreboardObjective;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

	@Inject(method = "renderScoreboardSidebar", at = @At(value = "INVOKE"), cancellable = true)
    private void renderScoreboardSidebar(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo callback) {
		if (Kuphack.getServer() == Servers.FLAGCLASH) {
			callback.cancel();
			Kuphack.renderSidebar(matrices, objective);
		}
    }
	
}