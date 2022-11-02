package com.github.vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.events.DamageEvent;
import com.github.vaapukkax.kuphack.events.InventoryClickEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionMixin {
	
	@Inject(at = @At(value = "INVOKE"), method = "attackEntity", cancellable = true)
	public void attackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (player == client.player) {
			DamageEvent event = new DamageEvent(target);
			Event.call(event);
			if (event.isCancelled()) ci.cancel();
		}
	}
	
	@Inject(at = @At(value = "INVOKE"), method = "clickSlot", cancellable = true)
	public void clickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		MinecraftClient c = MinecraftClient.getInstance();
		if (c.currentScreen instanceof GenericContainerScreen) {
			ScreenHandler screenHandler = player.currentScreenHandler;
			if (slotId >= 0 && actionType == SlotActionType.PICKUP) {
				ItemStack stack = screenHandler.getSlot(slotId).getStack();
				if (stack != null && stack.getItem() != Items.AIR) {
					InventoryClickEvent event = new InventoryClickEvent((GenericContainerScreen)c.currentScreen, stack);
					Event.call(event);
					if (event.isCancelled()) ci.cancel();
				}
			}
		}
	}
	
}
