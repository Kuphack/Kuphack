package com.github.vaapukkax.kuphack.mixin.flagclash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.Servers;
import com.github.vaapukkax.kuphack.flagclash.FlagClash;
import com.github.vaapukkax.kuphack.flagclash.FlagLocation;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

	@Shadow
	private int scaledWidth, scaledHeight;
	
	@Shadow
	private static String SCOREBOARD_JOINER;
	
	@Inject(method = "renderScoreboardSidebar", at = @At(value = "INVOKE"), cancellable = true)
    private void renderScoreboardSidebar(MatrixStack matrices, ScoreboardObjective objective, CallbackInfo ci) {
		if (Kuphack.getServer() == Servers.FLAGCLASH) {
			MinecraftClient client = MinecraftClient.getInstance();
			ci.cancel();
						
			int i;
	        Scoreboard scoreboard = objective.getScoreboard();
	        Collection<ScoreboardPlayerScore> collection = scoreboard.getAllPlayerScores(objective);
	        List<ScoreboardPlayerScore> list = collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList());
	        collection = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, collection.size() - 15)) : list;
	        ArrayList<MutableText> list2 = Lists.newArrayListWithCapacity(collection.size());
	        Text text = objective.getDisplayName();
	        int j = i = get().getWidth(text);
	        int k = get().getWidth(SCOREBOARD_JOINER);
	        
	        if (Kuphack.get().getFeature(FlagLocation.class).isFlagPlaced()) {
	        	double time = FlagClash.getUpgradeTime();
	        	if (time != -1) list2.add(Text.literal(
	        		" \u00a7fUpgrade Time: " + (FlagClash.isUpgradeCostUnsure() ? "Unsure" : FlagClash.timeAsString(time))
	        	));
	        }
	        for (ScoreboardPlayerScore scoreboardPlayerScore : collection) {
	            Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
	            MutableText text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));

	            list2.add(text2);
	            j = Math.max(j, get().getWidth(text2) + k + get().getWidth(Integer.toString(scoreboardPlayerScore.getScore())));
	        }
	        
	        int l = list.size() * get().fontHeight;
	        int m = this.scaledHeight / 2 + l / 3;

	        int o = this.scaledWidth - j - 3;
	        int p = 0;
	        int q = client.options.getTextBackgroundColor(0.3f);
	        int r = client.options.getTextBackgroundColor(0.4f);
	        for (Text text3 : list2) {
	            int s = o;
	            int t = m - ++p * get().fontHeight;
	            int u = scaledWidth - 3 + 2;
	            InGameHud.fill(matrices, s - 2, t, u, t + get().fontHeight, q);
	            get().draw(matrices, text3, (float)s, (float)t, -1);

	            if (p != list2.size()) continue;
	            InGameHud.fill(matrices, s - 2, t - get().fontHeight - 1, u, t - 1, r);
	            InGameHud.fill(matrices, s - 2, t - 1, u, t, q);
	            get().draw(matrices, text, (float)(s + j / 2 - i / 2), (float)(t - get().fontHeight), -1);
	        }
		}
    }
	
	private TextRenderer get() {
		return ((InGameHud)((Object)this)).getTextRenderer();
	}
	
}