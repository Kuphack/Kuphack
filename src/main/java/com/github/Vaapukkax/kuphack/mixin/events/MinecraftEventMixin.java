package com.github.vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Event;
import com.github.vaapukkax.kuphack.events.ChangeWorldEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

@Mixin(MinecraftClient.class)
public class MinecraftEventMixin {
	
	@Inject(method = "joinWorld", at = @At(value = "RETURN"))
	public void joinWorld(ClientWorld world, CallbackInfo ci) {
		ChangeWorldEvent event = new ChangeWorldEvent(world);
		Event.call(event);
	}
	
}
