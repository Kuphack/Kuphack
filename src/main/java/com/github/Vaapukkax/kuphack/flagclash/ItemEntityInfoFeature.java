package com.github.Vaapukkax.kuphack.flagclash;

import java.util.List;

import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Servers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemEntityInfoFeature extends Feature {

	private static final float scale = 0.014f;
	private final Screen screen = new Screen(Text.empty()) {};
	
	public ItemEntityInfoFeature() {
		super("Item Entity Info", Servers.FLAGCLASH);

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> 
			this.screen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight())
		);
	}
	
	public void render(MatrixStack matrices, ItemStack stack) {
		if (!isPlaying()) return;

		List<Text> text = stack.getTooltip(client.player, () -> false);
		
		matrices.push();
		matrices.translate(0, 0.6 + text.size() * (client.textRenderer.fontHeight * scale), 0);
		matrices.multiply(client.gameRenderer.getCamera().getRotation());
		matrices.scale(-scale, -scale, scale);
		TextRenderer textRenderer = client.textRenderer;
		
		int longest = 0;
		for (Text line : text) {
			int width = textRenderer.getWidth(line);
			if (width > longest) longest = width;
		}
		int offset = -(longest + 20) / 2;
		
		matrices.translate(0, 0, -400);
		screen.renderTooltip(
			matrices, text, stack.getTooltipData(),
			offset, 0
		);
		matrices.pop();
	}
	
	@Override
	public String getDescription() {
		return "Includes a tooltip above item entities, for example on shop altars";
	}
	
}
