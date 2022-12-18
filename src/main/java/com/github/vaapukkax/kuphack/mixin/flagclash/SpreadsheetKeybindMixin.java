package com.github.vaapukkax.kuphack.mixin.flagclash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.SupportedServer;
import com.github.vaapukkax.kuphack.flagclash.sheets.SpreadSheetScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBinding.class)
public class SpreadsheetKeybindMixin {

	@Shadow
	private int timesPressed;
	
	@Inject(at = @At(value = "HEAD"), method = "wasPressed", cancellable = true)
    public void wasPressed(CallbackInfoReturnable<Boolean> ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (((Object)this) == client.options.advancementsKey) {
			if (Kuphack.getServer() == SupportedServer.FLAGCLASH) {
				ci.cancel();
				
		        if (this.timesPressed == 0) {
		            ci.setReturnValue(false);
		            return;
		        }
		        --this.timesPressed;
				client.setScreen(new SpreadSheetScreen());
				ci.setReturnValue(false);
			}
		}
    }
	
}
