package com.github.Vaapukkax.kuphack.flagclash;

import java.util.ArrayList;
import java.util.List;

import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class ItemEntityInfoFeature extends Feature implements WorldRenderEvents.AfterEntities {

	private final Screen screen = new Screen(Text.empty()) {};
	
	public ItemEntityInfoFeature() {
		super("Item Entity Info", Servers.FLAGCLASH);
		WorldRenderEvents.AFTER_ENTITIES.register(this);
		
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> 
			this.screen.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight())
		);
	}

	@Override
	public void afterEntities(WorldRenderContext context) {
		if (!isPlaying()) return;
		
		Vec3d camera = context.camera().getPos();
		context.matrixStack().push();
		context.matrixStack().translate(-camera.x, -camera.y, -camera.z);
		
		final float scale = 0.014f;
		for (Entity entity : context.world().getEntities()) {
			if (!(entity instanceof ItemEntity)) continue;
			
			Vec3d pos = entity.getCameraPosVec(context.tickDelta());
			ItemStack stack = ((ItemEntity)entity).getStack();
			
			List<Text> text = new ArrayList<>();
	        text.add(stack.getName());
			text.addAll(Kuphack.getLore(stack));
			
			context.matrixStack().push();
			context.matrixStack().translate(pos.getX(), pos.getY() + text.size() * (client.textRenderer.fontHeight * scale) + 0.55, pos.getZ());
			
			context.matrixStack().multiply(client.gameRenderer.getCamera().getRotation());
			context.matrixStack().scale(-scale, -scale, scale);
			TextRenderer textRenderer = client.textRenderer;
			
			int longest = 0;
			for (Text line : text) {
				int width = textRenderer.getWidth(line);
				if (width > longest) longest = width;
			}
			int offset = -(longest + 20) / 2;
			
			context.matrixStack().translate(0, 0, -400);
			screen.renderTooltip(
				context.matrixStack(), text, stack.getTooltipData(),
				offset, 0
			);
			context.matrixStack().pop();
		}
		
		context.matrixStack().pop();
	}
	
	@Override
	public String getDescription() {
		return "Includes a tooltip above item entities, for example on shop altars";
	}
	
}
