package dev.watukas.kuphack.mixin.flagclash;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.flagclash.FlagClash;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin(HandledScreen.class)
public class FlagMixin {
	
	private double time = -1;
	
	@Shadow protected int playerInventoryTitleX, playerInventoryTitleY;
	@Shadow protected Text playerInventoryTitle;
	@Shadow protected int titleX, titleY;
	
	@Inject(at = @At(value = "INVOKE"), method = "drawForeground", cancellable = true)
	protected void drawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
		if (get() == null || Kuphack.getServer() != SupportedServer.FLAGCLASH) return;
		MinecraftClient m = MinecraftClient.getInstance();
		Text title = get().getTitle();
		
		if (title.getString().contains("Shop")) {
			if (!isHoldingFlag()) {
				FlagClash.setUpgradeCost(getUpgradePrice());
				time = FlagClash.getUpgradeTime();
			}
			if (time == -1.0) return;
			ci.cancel();
			
			title = title.copy().append(Text.literal(" " + FlagClash.timeAsString(time)).withColor(0xFFCCCCCC));
		}
		
		if (ci.isCancelled()) {
			context.drawTextWithShadow(m.textRenderer, title, this.titleX, this.titleY, 0xFFFFFFFF);
			context.drawText(m.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0xFFFFFFFF, false);
		}
	}
	
	private long getUpgradePrice() {
		try {
			for (ItemStack stack : get().getScreenHandler().slots.stream()
					.map(slot -> slot.getStack())
					.toList()) {
				if (!stack.getName().getString().contains("Level Up"))
					continue;
				
				List<String> lore = Kuphack.getStripLore(stack);
				String[] line = lore.getLast().strip().split("\\s+");
				
				return FlagClash.toRealValue(line[0]);
			}
		} catch (Exception e) {
			Kuphack.error(e);
		}
		return 0;
	}
	
	private boolean isHoldingFlag() {
		ItemStack is = get().getScreenHandler().getCursorStack();
		return (is != null && is.getItem().getName().getString().toLowerCase().contains("banner"));
	}
	
	private GenericContainerScreen get() {
		return (Object) this instanceof GenericContainerScreen cast ? cast : null;
	}
}