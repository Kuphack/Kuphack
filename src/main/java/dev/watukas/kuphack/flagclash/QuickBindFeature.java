package dev.watukas.kuphack.flagclash;

import java.util.ArrayList;
import java.util.List;

import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.modmenu.BindManagementScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public class QuickBindFeature extends Feature {
	
	private final List<QuickBind> binds = new ArrayList<>();
	
	public QuickBindFeature() {
		super("Allow binding quick access shortcut keys to certain items", BindManagementScreen::new, SupportedServer.FLAGCLASH);
		
		var config = this.config();
		for (String key : config.keySet()) {
			Item item = Registries.ITEM.get(Identifier.of(key));

			if (item == null) {
				Kuphack.LOGGER.warn("Unknown item '" + key + "' for quick bind");
				continue;
			}
			
			QuickBind bind = new QuickBind(item, config.get(key).getAsInt());
			this.binds.add(bind);
		}
		
		ClientTickEvents.START_CLIENT_TICK.register(this::tick);
	}
	
	private void tick(MinecraftClient c) {
		if (!isPlaying())
			return;
		for (QuickBind bind : this.binds) {
			if (bind.down()) {
				if (!bind.used) {
					bind.use();
					bind.used = true;
				}
			} else bind.used = false;
		}
	}
	
	public List<QuickBind> binds() {
		return this.binds;
	}

	public class QuickBind {
		
		private final Item item;
		private int code;
		
		private boolean used;
		
		public QuickBind(Item item) {
			this.item = item;
		}
		
		public QuickBind(Item item, int code) {
			this.item = item;
			this.code = code;
		}
		
		public String id() {
			return Registries.ITEM.getId(this.item).asMinimalString();
		}

		public Text name() {
			return this.item.getName();
		}
		
		public Item item() {
			return this.item;
		}
		
		public void set(int code) {
			this.code = code;
			writeConfig(config -> {
				config.addProperty(id(), code);
			});
		}
	
		public boolean down() {
			return InputUtil.isKeyPressed(client.getWindow().getHandle(), this.code);
		}
		
		public Text bindedKey() {
			return InputUtil.fromKeyCode(this.code, -1).getLocalizedText();
		}
		
		public void use() {
			if (client.player == null || client.currentScreen != null)
				return;
			PlayerInventory inventory = client.player.getInventory();
			ItemStack offhand = inventory.getStack(PlayerInventory.OFF_HAND_SLOT);
			if (offhand.getItem() == this.item) {
				useHand(Hand.OFF_HAND);
				return;
			}
				
			for (int i = 0; i < PlayerInventory.HOTBAR_SIZE; i++) {
				ItemStack stack = inventory.getStack(i);
				if (stack.getItem() == this.item) {
					int pre = inventory.getSelectedSlot();
					inventory.setSelectedSlot(i);
					useHand(Hand.MAIN_HAND);
					inventory.setSelectedSlot(pre);
					return;
				}
			}
			client.player.sendMessage(Text.literal("§cCouldn't find " + id() + " on the hotbar"), true);
		}
		
		private void useHand(Hand hand) {
			if (client.crosshairTarget != null) {
				switch (client.crosshairTarget.getType()) {
					case ENTITY: {
						EntityHitResult entityHitResult = (EntityHitResult)client.crosshairTarget;
						Entity entity = entityHitResult.getEntity();
						if (!client.world.getWorldBorder().contains(entity.getBlockPos())) {
							return;
						}

						ActionResult actionResult = client.interactionManager.interactEntityAtLocation(client.player, entity, entityHitResult, hand);
						if (!actionResult.isAccepted()) {
							actionResult = client.interactionManager.interactEntity(client.player, entity, hand);
						}

						if (actionResult instanceof ActionResult.Success success) {
							if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
								client.player.swingHand(hand);
							}

							return;
						}
						break;
					}
					case BLOCK: {
						BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;

						ActionResult actionResult = client.interactionManager.interactBlock(client.player, hand, blockHitResult);
						if (actionResult instanceof ActionResult.Success success) {
							if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
								client.player.swingHand(hand);
							}

							return;
						}

						if (actionResult instanceof ActionResult.Fail) {
							return;
						}
					}
					default:
						break;
				}
			}
			
			if (client.interactionManager.interactItem(client.player, hand) instanceof ActionResult.Success success) {
				if (success.swingSource() == ActionResult.SwingSource.CLIENT)
					client.player.swingHand(hand);
			}
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof QuickBind bind ? bind.item == this.item : false;
		}
		
	}

}
