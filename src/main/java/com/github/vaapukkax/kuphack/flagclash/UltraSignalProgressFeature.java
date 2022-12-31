package com.github.vaapukkax.kuphack.flagclash;

import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.SupportedServer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class UltraSignalProgressFeature extends Feature implements HudRenderCallback, SoundInstanceListener {

	private long show;
	private double progress;
	
	public UltraSignalProgressFeature() {
		super("Makes text appear on screen when near an Ultra Signal to see how far it has progressed", SupportedServer.FLAGCLASH);
		HudRenderCallback.EVENT.register(this);
	}

	@Override
	public void onActivate() {
		super.onActivate();
		client.getSoundManager().registerListener(this);
	}
	
	@Override
	public void onDeactivate() {
		super.onDeactivate();
		client.getSoundManager().unregisterListener(this);
	}
	
	@Override
	public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet) {
		if (!SoundEvents.BLOCK_NOTE_BLOCK_BIT.matchesId(sound.getId())) return;
		this.progress = sound.getPitch() - 1;
		this.show = System.currentTimeMillis();
	}

	@Override
	public void onHudRender(MatrixStack matrices, float tickDelta) {		
		if (System.currentTimeMillis()-show > 5000) return;
		
		Text text = Text.of("Ultra Signal ("+(int)(progress*100)+"%)");
		client.textRenderer.drawWithShadow(matrices, text,
			client.getWindow().getScaledWidth() / 2 - client.textRenderer.getWidth(text) / 2,
			client.getWindow().getScaledHeight() - 68 - client.textRenderer.fontHeight, 0x0000ff
		);
	}
	
}
