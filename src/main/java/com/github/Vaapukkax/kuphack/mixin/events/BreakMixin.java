package com.github.Vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.Vaapukkax.kuphack.Event;
import com.github.Vaapukkax.kuphack.events.BlockBreakEvent;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayerInteractionManager.class)
public class BreakMixin {

	@Inject(at = @At(value = "INVOKE"), method = "breakBlock", cancellable = true)
	public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		BlockBreakEvent event = new BlockBreakEvent(pos);
		Event.call(event);
		if (event.isCancelled()) ci.setReturnValue(false);
	}
	
}
