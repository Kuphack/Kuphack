package dev.watukas.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

	@Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At(value = "INVOKE"), cancellable = true)
    private void renderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if (Kuphack.getServer() == SupportedServer.FLAGCLASH) {
			ci.cancel();
			
			MinecraftClient client = MinecraftClient.getInstance();
			ScoreboardObjective objective = client.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
			
			if (objective != null) {
				context.createNewRootLayer();
				Kuphack.renderSidebar(context, objective);
			}
		}
	}
	
//	@Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "INVOKE"), cancellable = true)
//    private void renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo callback) {
//		if (Kuphack.getServer() == SupportedServer.FLAGCLASH) {
//			callback.cancel();
//			Kuphack.renderSidebar(context, objective);
//		}
//    }
	
}