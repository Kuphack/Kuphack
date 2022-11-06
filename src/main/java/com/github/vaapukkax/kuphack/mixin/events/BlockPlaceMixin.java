package com.github.vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.events.ClientBlockPlaceEvent;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

@Mixin(ItemStack.class)
public class BlockPlaceMixin {
	
	@Inject(method = "useOnBlock", at = @At(value = "RETURN"))
	public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
		MinecraftClient c = MinecraftClient.getInstance();
		if (ci.getReturnValue() == ActionResult.SUCCESS) {
			BlockPos pos = context.getBlockPos().add(context.getSide().getVector());
			Block block = c.world.getBlockState(pos).getBlock();
			Event.call(new ClientBlockPlaceEvent(pos, block));
		}
	}
}
