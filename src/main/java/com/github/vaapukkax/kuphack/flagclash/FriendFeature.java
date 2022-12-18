package com.github.vaapukkax.kuphack.flagclash;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.SupportedServer;
import com.github.vaapukkax.kuphack.events.DamageEvent;
import com.github.vaapukkax.kuphack.modmenu.SettingsKuphackScreen;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class FriendFeature extends Feature implements EventHolder, HudRenderCallback {
	
	private final ArrayList<UUID> friends = new ArrayList<>();
	
	private boolean middlePressed;
	private PlayerEntity lastDamaged;
	
	public FriendFeature() {
		super("Lets you not attack your friends", SupportedServer.FLAGCLASH);
		HudRenderCallback.EVENT.register(this);
		ClientLifecycleEvents.CLIENT_STOPPING.register(e -> save());
		load();
	}

	
	/**
	 * Adds the specified player to the friend list and displays a message in chat to clarify such
	 * @param player the player
	 */
	public void addFriend(PlayerEntity player) {
		if (!this.addFriend(player.getUuid())) return;	
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.sendMessage(Text.of("§a"+player.getName().getString()+" is now a friend"), false);
			client.player.playSound(SoundEvents.ENTITY_VILLAGER_CELEBRATE, 1, 1);
		}
	}
	
	/**
	 * Adds the specified player to the friend list and displays a message in chat to clarify such
	 * @param player the player
	 */
	public boolean addFriend(UUID uuid) {
		if (uuid == null || this.friends.contains(uuid)) return false;
		this.friends.add(uuid);
		this.save();
		return true;
	}
	
	/**
	 * Removes the specified player from the friend list and displays a message in chat to clarify such
	 * @param player the player
	 */
	public void removeFriend(PlayerEntity player) {
		if (!this.removeFriend(player.getGameProfile().getId())) return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.sendMessage(Text.of("§2"+player.getName().getString()+" is no longer a friend"), false);
			client.player.playSound(SoundEvents.ENTITY_VILLAGER_HURT, 1, 1);
		}
	}
	
	/**
	 * Removes the specified uuid from the friend list
	 * @param uuid the id of the player
	 */
	public boolean removeFriend(UUID uuid) {
		if (!friends.contains(uuid)) return false;
		this.friends.remove(uuid);
		this.save();
		return true;
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
		JsonObject object = Kuphack.get().readDataFile();
		
		this.friends.clear();
		if (object.has("friends")) object.get("friends").getAsJsonArray().forEach(uuid -> 
			this.friends.add(UUID.fromString(uuid.getAsString()))
		);
	}
	
	/**
	 * Saves friends to the file without overwriting other settings
	 */
	public void save() {
		Gson gson = new Gson();
		JsonObject object = Kuphack.get().readDataFile();
		object.add("friends", toJsonArray());
		SettingsKuphackScreen.write(gson.toJson(object));
	}
	
	public JsonArray toJsonArray() {
		JsonArray array = new JsonArray();
		for (UUID uuid : friends) array.add(uuid.toString());
		return array;
	}
	
	@EventMention
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
						if (client.player.isSneaking()) {
							if (isDisabled()) client.player.sendMessage(
								Text.of("§cFriend feature isn't enabled!")
							, true);
							addFriend(player);
						}
					} else removeFriend(player);
					
					middlePressed = true;
				}
			} else middlePressed = false;
			
			if (isFriend(player)) {
				String text = isDisabled() ? "Friend feature is disabled" : client.targetedEntity.getName().getString() + " Friended";
				client.textRenderer.drawWithShadow(matrix, text,
					client.getWindow().getScaledWidth()/2 - client.textRenderer.getWidth(text)/2, 30
				, (isDisabled() ? new Color(255, 85, 85) : new Color(85, 255, 85)).getRGB());
				text = isDisabled() ? "Remove friend by Middle Clicking" : "Remove by Middle Clicking";
				client.textRenderer.drawWithShadow(matrix, text,
					client.getWindow().getScaledWidth()/2 - client.textRenderer.getWidth(text)/2, 39
				, (isDisabled() ? new Color(170, 0, 0) : new Color(0, 170, 0)).getRGB());
			}
		}
	}
	
	/**
	 * Loads the name of the player from the tablist
	 * @return the name, may be null
	 */
	public String getOnlinePlayerName(UUID uuid) {
		ClientPlayNetworkHandler players = client.getNetworkHandler();
		PlayerListEntry entry = players != null ? players.getPlayerListEntry(uuid) : null;
		return entry != null ? entry.getProfile().getName() : null;
	}
	
	@Override
	public String getDescription() {
		GameOptions options = client.options;
		return "Add friends with ["+options.sneakKey.getBoundKeyLocalizedText().getString()+"] + ["+Text.translatable("key.mouse.middle").getString()+"], you can also manage them by clicking this button";
	}
	
	@Override
	public String getName() {
		return "Friends";
	}
	
}
