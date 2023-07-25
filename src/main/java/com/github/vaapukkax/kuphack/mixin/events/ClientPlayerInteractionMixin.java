package com.github.vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.events.BlockInteractEvent;
import com.github.vaapukkax.kuphack.events.ClientBlockBreakEvent;
import com.github.vaapukkax.kuphack.events.DamageEvent;
import com.github.vaapukkax.kuphack.events.InteractEvent;
import com.github.vaapukkax.kuphack.events.InventoryClickEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionMixin {
	
	@Inject(at = @At(value = "INVOKE"), method = "attackEntity", cancellable = true)
	public void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (player != client.player) return;
		
		DamageEvent event = new DamageEvent(target);
		Event.call(event);
		if (event.isCancelled()) ci.cancel();
	}
	
	@Inject(at = @At(value = "INVOKE"), method = "interactItem", cancellable = true)
	public void interactItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (player != client.player) return;
		
		InteractEvent event = new InteractEvent(hand, player.getStackInHand(hand));
		Event.call(event);
		if (event.isCancelled()) ci.setReturnValue(ActionResult.PASS);
	}
	
	@Inject(at = @At(value = "INVOKE"), method = "interactBlock", cancellable = true)
	public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult result, CallbackInfoReturnable<ActionResult> ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (player != client.player) return;

		BlockInteractEvent event = new BlockInteractEvent(player.clientWorld.getBlockState(result.getBlockPos()), hand, player.getStackInHand(hand));
		Event.call(event); 
		if (event.isCancelled()) ci.setReturnValue(ActionResult.FAIL);
	}
	
	@Inject(at = @At(value = "INVOKE"), method = "clickSlot", cancellable = true)
	public void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		MinecraftClient c = MinecraftClient.getInstance();
		if (!(c.currentScreen instanceof GenericContainerScreen))
			return;
		ScreenHandler screenHandler = player.currentScreenHandler;
		if (slotId >= 0 && actionType == SlotActionType.PICKUP) {
			ItemStack stack = screenHandler.getSlot(slotId).getStack();
			if (stack != null && stack.getItem() != Items.AIR) {
				InventoryClickEvent event = new InventoryClickEvent((GenericContainerScreen) c.currentScreen, stack);
				Event.call(event);
				if (event.isCancelled()) ci.cancel();
			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE"), method = "breakBlock", cancellable = true)
	public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		ClientBlockBreakEvent event = new ClientBlockBreakEvent(pos);
		Event.call(event);
		if (event.isCancelled()) ci.setReturnValue(false);
	}
	
}
