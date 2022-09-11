package com.github.Vaapukkax.kuphack.mixin.events;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.Vaapukkax.kuphack.Event;
import com.github.Vaapukkax.kuphack.events.ChatEvent;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class ChatMixin {

	private ChatEvent lastChatEvent;
	
	@Inject(at = @At(value = "INVOKE"), method = "addMessage", cancellable = true)
	private void addMessage(Text message, CallbackInfo ci) {
		if (lastChatEvent != null && lastChatEvent.isCancelled()) ci.cancel();
		lastChatEvent = null;
	}
	
	@ModifyVariable(method = "addMessage", at = @At("HEAD"), ordinal = 0)
	private Text addMessage(Text message) {
		ChatEvent event = new ChatEvent(message);
		Event.call(event);
		message = event.getMessage();
		this.lastChatEvent = event;
		return message;
	}
}

