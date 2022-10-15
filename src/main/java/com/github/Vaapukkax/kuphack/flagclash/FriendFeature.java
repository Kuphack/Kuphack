package com.github.Vaapukkax.kuphack.flagclash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.github.Vaapukkax.kuphack.EventListener;
import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.kuphack.events.DamageEvent;
import com.github.Vaapukkax.kuphack.modmenu.SettingsKuphackScreen;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class FriendFeature extends Feature implements EventListener, HudRenderCallback {
	
	private final ArrayList<UUID> friends = new ArrayList<>();
	
	private boolean middlePressed;
	private PlayerEntity lastDamaged;
	
	public FriendFeature() {
		super(Servers.FLAGCLASH, Servers.BITZONE);
		HudRenderCallback.EVENT.register(this);
		ClientLifecycleEvents.CLIENT_STOPPING.register(e -> save());
		load();
	}
	
	/**
	 * Adds the specified player to the friends list and displays a message in chat to clarify such
	 * @param player the player
	 */
	public void addFriend(PlayerEntity player) {
		if (!friends.contains(player.getGameProfile().getId())) {
			friends.add(player.getGameProfile().getId());
			
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null) {
				client.player.sendMessage(Text.of("§a"+player.getName().getString()+" is now a friend"), false);
				client.player.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1, 1);
			}
			
			save();
		}
	}
	
	/**
	 * Removed the specified player from the friends list and displays a message in chat to clarify such
	 * @param player the player
	 */
	public void removeFriend(PlayerEntity player) {
		if (friends.contains(player.getGameProfile().getId())) {
			friends.remove(player.getGameProfile().getId());
			
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null) {
				client.player.sendMessage(Text.of("§2"+player.getName().getString()+" is no longer a friend"), false);
				client.player.playSound(SoundEvents.ENTITY_VILLAGER_HURT, 1, 1);
			}
			save();
		}
	}
	
	/**
	 * Checks if the specified player object is seen as a friend
	 * @param player the player
	 * @return whether the player is a friend
	 */
	public boolean isFriend(PlayerEntity player) {
		return friends.contains(player.getGameProfile().getId());
	}
	
	/**
	 * Hands over an unmodifiable list of friends as uuids
	 */
	public List<UUID> getFriends() {
		return Collections.unmodifiableList(friends);
	}
	
	/**
	 * Loads the friends from the config/save file
	 */
	public void load() {
		Gson gson = new Gson();
		JsonObject object = new JsonObject();
		try {
			object = gson.fromJson(Kuphack.get().readDataFile(), JsonObject.class);
		} catch (Exception e) {}
		
		friends.clear();
		if (object != null && object.has("friends")) {
			object.get("friends").getAsJsonArray().forEach(uuid -> {
				this.friends.add(UUID.fromString(uuid.getAsString()));
			});
		}
	}
	
	/**
	 * Saves friends to the file without overwriting other settings
	 */
	protected void save() {
		Gson gson = new Gson();
		JsonObject object = null;
		try {
			object = gson.fromJson(Kuphack.get().readDataFile(), JsonObject.class);
		} catch (Exception e) {}
		if (object == null) object = new JsonObject();
		
		object.add("friends", toJsonArray());
		
		SettingsKuphackScreen.write(gson.toJson(object));
	}
	
	public JsonArray toJsonArray() {
		JsonArray array = new JsonArray();
		for (UUID uuid : friends) array.add(uuid.toString());
		return array;
	}
	
	/**
	 * Ordinary damage event that handles blocking hits
	 */
	public void onEvent(DamageEvent e) {
		if (e.getTarget() instanceof PlayerEntity) {
			if (isFriend((PlayerEntity)e.getTarget())) {
				e.setCancelled(true);
				if (lastDamaged != e.getTarget())
					e.getClientPlayer().sendMessage(Text.of("§cCan't attack a friend"), false);
				e.getClientPlayer().playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1, 1);
			}
			lastDamaged = (PlayerEntity)e.getTarget();
		}
	}

	/**
	 * Functionality for adding a friend and showing text when looking at a friend
	 */
	@Override
	public void onHudRender(MatrixStack matrix, float delta) {
		if (!isOnServer()) return;
		MinecraftClient client = MinecraftClient.getInstance();
		
		if (!client.player.getAbilities().flying && client.targetedEntity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)client.targetedEntity;
			
			if (client.mouse.wasMiddleButtonClicked()) {
				if (!middlePressed) {
					
					if (!isFriend(player)) {
						if (client.player.isSneaking()) addFriend(player);
					} else removeFriend(player);
					
					middlePressed = true;
				}
			} else middlePressed = false;
			
			if (isFriend(player)) {
				TextRenderer tr = client.textRenderer;
				
				String str = client.targetedEntity.getName().getString()+" Friended";
				tr.drawWithShadow(matrix, str, client.getWindow().getScaledWidth()/2-tr.getWidth(str)/2, 30, new Color(85, 255, 85).getRGB());
				str = "Remove by Middle Clicking";
				tr.drawWithShadow(matrix, str, client.getWindow().getScaledWidth()/2-tr.getWidth(str)/2, 39, new Color(0, 170, 0).getRGB());
			}
		}
	}
	
}
