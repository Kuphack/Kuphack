package dev.watukas.kuphack.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.finder.MinehutButtonState;
import dev.watukas.kuphack.finder.MinehutServerListScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
	
	private ButtonWidget widget;
	
	protected MultiplayerScreenMixin(Text title) {
		super(title);
	}

	@Inject(method = "init", at = @At(value = "HEAD"))
	protected void init(CallbackInfo ci) {
		MinehutButtonState state = Kuphack.settings().minehutButtonState();
		
		if (state != MinehutButtonState.HIDDEN) {
			int x =
				state == MinehutButtonState.RIGHT_CORNER ? this.width - 28 :
				state == MinehutButtonState.LEFT_CORNER ? 8 :
				state == MinehutButtonState.NEXT_TO_JOIN ? this.width / 2 - 154 - 25 : 0
			;
			
			int y = state == MinehutButtonState.NEXT_TO_JOIN ? this.height - 52 : this.height - 28;
			
			this.widget = ButtonWidget.builder(Text.of("MH"), b -> client.setScreen(new MinehutServerListScreen(client.currentScreen)))
				.dimensions(x, y, 20, 20)
				.build();
			widget.setTooltip(Tooltip.of(Text.of("Minehut Server List")));
			this.addDrawableChild(widget);
		}
	}
	
	@Inject(method = "keyPressed", at = @At(value = "INVOKE"), cancellable = true)
	public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> ci) {
		if (this.widget == null)
			return;
		if (keyCode == GLFW.GLFW_KEY_DELETE && (this.widget.active || this.widget.isHovered())) {
			widget.playDownSound(MinecraftClient.getInstance().getSoundManager());
	    	Kuphack.settings().minehutButtonState(MinehutButtonState.HIDDEN);
	    	this.executor.execute(this::clearAndInit);
	    	ci.setReturnValue(true);
		}
	}
	
}
