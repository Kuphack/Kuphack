package dev.watukas.kuphack.mixin.flagclash;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.flagclash.FriendFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Mixin(PlayerListHud.class)
public class PlayerListSortMixin {

	private static final UUID flowwave = UUID.fromString("563edf73-f750-41cd-943c-1c77b66f283f");
	
	@Shadow
	private static Comparator<PlayerListEntry> ENTRY_ORDERING;
	
	@Inject(at = @At(value = "RETURN"), method = "collectPlayerEntries", cancellable = true)
	private void collectPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> ci) {
		if (SupportedServer.current() != SupportedServer.FLAGCLASH)
			return;
		List<PlayerListEntry> list = ci.getReturnValue().stream().sorted(comparator()).toList();
		
		MinecraftClient client = MinecraftClient.getInstance();
		for (PlayerListEntry e : list) {
			boolean self = e.getProfile().equals(client.getGameProfile());
			if (e.getProfile().getId().equals(flowwave)) {
				e.setDisplayName(replaceWithStyle(e.getDisplayName(), e.getProfile().getName(), s -> s.withColor(0xFFFFAACF)));
			} else if (Kuphack.get().getFeature(FriendFeature.class).isFriend(e.getProfile()) || self) {
				e.setDisplayName(replaceWithStyle(e.getDisplayName(), e.getProfile().getName(), s -> s.withColor(self ? 0xFFCFFFCC : 0xFFCFFFAA)));
			}
		}
		
		ci.setReturnValue(list);
		
	}
	
	private Comparator<PlayerListEntry> comparator() {
//		return (a, b) -> a.getProfile().getName().compareTo(b.getProfile().getName());
		return Comparator.comparingInt(e -> {
			try {
				String name = e.getDisplayName().getString();
				name = name.substring(name.indexOf("[") + 1, name.indexOf("]"));
				return 0 - Integer.parseInt(name);
			} catch (Exception exc) {
				return 0;
			}
		});
	}
	
	private static Text replaceWithStyle(Text text, String target, UnaryOperator<Style> operator) {
		if (text == null)
			return null;
		Text copy = text.copy();
		copy.getSiblings().replaceAll(a -> {
			if (a.getString().contains(target))
				return a.copy().styled(operator);
			return a;
		});
		return copy;
	}
	
}
