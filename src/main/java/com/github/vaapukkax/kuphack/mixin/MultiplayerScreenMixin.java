package com.github.vaapukkax.kuphack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.finder.MinehutButtonState;
import com.github.vaapukkax.kuphack.finder.MinehutServerListScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
	
	protected MultiplayerScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At(value = "HEAD"))
	protected void init(CallbackInfo ci) {
		MinehutButtonState state = Kuphack.get().serverListButton;
		
		if (state != MinehutButtonState.HIDDEN) {
			int x =
				state == MinehutButtonState.RIGHT_CORNER ? this.width - 28 :
				state == MinehutButtonState.LEFT_CORNER ? 8 :
				state == MinehutButtonState.NEXT_TO_JOIN ? this.width / 2 - 154 - 25 : 0
			;
			
			int y = state == MinehutButtonState.NEXT_TO_JOIN ? this.height - 52 : this.height - 28;
			
			this.addDrawableChild(ButtonWidget.builder(Text.of("MH"),
				button -> client.setScreen(new MinehutServerListScreen(client.currentScreen))
			).position(x, y).size(20, 20).build());
		}
	}
	
//	private MultiplayerScreen get() {
//		return (MultiplayerScreen)((Object)this);
//	}
	
}
