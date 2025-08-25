package dev.watukas.kuphack.mixin.events;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.watukas.kuphack.Event;
import dev.watukas.kuphack.events.ChatEvent;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
public class ChatMixin {
	
	@Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE"), cancellable = true)
	private void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator, CallbackInfo ci) {
		ChatEvent event = new ChatEvent(message);
		Event.call(event);
		if (event.isCancelled()) ci.cancel();
	}
	
}