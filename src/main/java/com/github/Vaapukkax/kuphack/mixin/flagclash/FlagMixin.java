package com.github.Vaapukkax.kuphack.mixin.flagclash;

import java.math.BigInteger;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.kuphack.flagclash.FlagClash;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

@Mixin(HandledScreen.class)
public class FlagMixin {
	
	private double time = -1;
	
	@Shadow protected int playerInventoryTitleX, playerInventoryTitleY;
	@Shadow protected Text playerInventoryTitle;
	@Shadow protected int titleX, titleY;
	
	@Inject(at = @At(value = "INVOKE"), method = "drawForeground", cancellable = true)
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
		if (get() == null || Kuphack.getServer() != Servers.FLAGCLASH) return;
		MinecraftClient m = MinecraftClient.getInstance();
		Text title = get().getTitle();
		
		if (title.getString().contains("Flag") && title.getString().contains("Upgrade")) {
			if (!isHoldingFlag()) {
				FlagClash.upgradePrice = getUpgradePrice();
				time = FlagClash.getUpgradeTime();
			}
			if (time == -1.0) return;
			ci.cancel();
			
			title = title.copy().append(Text.literal("\u00a77: "+FlagClash.timeAsString(time)));
		}
		
		if (ci.isCancelled()) {
			m.textRenderer.draw(matrices, title, (float)this.titleX, (float)this.titleY, 4210752);
			m.textRenderer.draw(matrices, this.playerInventoryTitle, (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
		}
	}
	
	private BigInteger getUpgradePrice() {
		try {
			GenericContainerScreen t = get();
			MinecraftClient m = MinecraftClient.getInstance();
			
			DefaultedList<Slot> sl = t.getScreenHandler().slots;
			for (int i = 0; i < sl.size(); i++) {
				ItemStack s = sl.get(i).getStack();
				List<Text> l = s.getTooltip(m.player, new TooltipContext() {
					@Override
					public boolean isAdvanced() {
						return false;
					}
				});
				if (l != null && l.size() >= 3) {
					String line = l.get(2).getString();
					if (line.contains("Costs: ")) {
						return FlagClash.toRealValue(line.substring(1).split(" ")[1]);
					}
				}
			}
			return null;
		} catch (Exception e) {
			Kuphack.LOGGER.error("Something went wrong with displaying upgrade time");
			e.printStackTrace();
			return null;
		}
	}
	
	private boolean isHoldingFlag() {
		ItemStack is = get().getScreenHandler().getCursorStack();
		return (is != null && is.getItem().getName().getString().toLowerCase().contains("banner"));
	}
	
	private GenericContainerScreen get() {
		Object o = (Object)this;
		if (o instanceof GenericContainerScreen) {
			GenericContainerScreen oc = (GenericContainerScreen)o;
			return oc;
		}
		return null;
	}
}