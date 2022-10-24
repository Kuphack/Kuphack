package com.github.Vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.kuphack.Event;
import com.github.Vaapukkax.kuphack.events.BlockUpdateEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientWorld.class)
public class BlockUpdateMixin {

	@Inject(at = @At(value = "HEAD"), method = "handleBlockUpdate")
	public void handleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
		if ((Block.FORCE_STATE & flags) != 0) Event.call(new BlockUpdateEvent(pos, state));
	}
	
}
