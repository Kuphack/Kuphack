package dev.watukas.kuphack.flagclash;

import java.util.ArrayList;
import java.util.List;

import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemEntityInfoFeature extends Feature {

	private final Screen screen = new Screen(Text.empty()) {};
	
	public ItemEntityInfoFeature() {
		super("Item Entity Info", SupportedServer.FLAGCLASH);

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> 
			this.screen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight())
		);
	}
	
	public void render(MatrixStack matrix, VertexConsumerProvider provider, ItemStack stack) {
		if (!isPlaying())
			return;
		
		List<Text> text = new ArrayList<>(stack.get(DataComponentTypes.LORE).styledLines());
		text.addFirst(stack.getName());
		
		matrix.push();
		matrix.scale(0.75f, 0.75f, 0.75f);
		matrix.translate(0, 1f, 0);
		Kuphack.renderText(text, matrix, provider);
		matrix.pop();
	}
	
	@Override
	public String getDescription() {
		return "Shows a description above dropped item entities";
	}
	
}
