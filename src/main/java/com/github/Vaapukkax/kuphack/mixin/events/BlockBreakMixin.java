package com.github.vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.events.ClientBlockBreakEvent;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayerInteractionManager.class)
public class BlockBreakMixin {

	@Inject(at = @At(value = "INVOKE"), method = "breakBlock", cancellable = true)
	public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		ClientBlockBreakEvent event = new ClientBlockBreakEvent(pos);
		Event.call(event);
		if (event.isCancelled()) ci.setReturnValue(false);
	}
	
}
