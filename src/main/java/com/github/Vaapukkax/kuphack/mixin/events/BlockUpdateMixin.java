package com.github.vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.events.BlockUpdateEvent;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientWorld.class)
public class BlockUpdateMixin {

	@Inject(at = @At(value = "HEAD"), method = "handleBlockUpdate")
	public void handleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
		Event.call(new BlockUpdateEvent(pos, state));
	}
	
}
