package dev.watukas.kuphack.flagclash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Event.EventMention;
import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.events.DamageEvent;
import dev.watukas.kuphack.modmenu.FriendManagementScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FriendFeature extends Feature implements EventHolder {
	
	private final ArrayList<GameProfile> friends = new ArrayList<>();
	
	private boolean middlePressed;
	private PlayerEntity lastDamaged;
	
	public FriendFeature() {
		super("Lets you not attack your friends", FriendManagementScreen::new, SupportedServer.FLAGCLASH);
		HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
			Identifier.of("kuphack", "friend-overlay"), this::onHudRender
		);
		ClientLifecycleEvents.CLIENT_STOPPING.register(e -> save());
		load();
	}

	
	/**
	 * Adds the specified player to the friend list and displays a message in chat to clarify such
	 * @param player the player
	 */
	public void addFriend(PlayerEntity player) {
		if (!this.addFriend(player.getGameProfile()))
			return;	
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
	public boolean addFriend(GameProfile profile) {
		if (profile == null || this.friends.contains(profile))
			return false;
		this.friends.add(profile);
		this.save();
		return true;
	}
	
	/**
	 * Removes the specified player from the friend list and displays a message in chat to clarify such
	 * @param player the player
	 */
	public void removeFriend(PlayerEntity player) {
		if (!this.removeFriend(player.getGameProfile()))
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.sendMessage(Text.of("§2" + player.getName().getString() + " is no longer a friend"), false);
			client.player.playSound(SoundEvents.ENTITY_VILLAGER_HURT, 1, 1);
		}
	}
	
	/**
	 * Removes the specified uuid from the friend list
	 * @param uuid the id of the player
	 */
	public boolean removeFriend(GameProfile profile) {
		if (!friends.contains(profile))
			return false;
		this.friends.remove(profile);
		this.save();
		return true;
	}
	
	/**
	 * Checks if the specified player profile is seen as a friend
	 * @param player the player
	 * @return whether the player is a friend
	 */
	public boolean isFriend(GameProfile profile) {
		return friends.contains(profile);
	}
	
	/**
	 * Hands over an unmodifiable list of friends as uuids
	 */
	public List<GameProfile> getFriends() {
		return Collections.unmodifiableList(friends);
	}
	
	/**
	 * Loads the friends from the config/save file
	 */
	public void load() {
		JsonObject object = Kuphack.settings().read();
		
		this.friends.clear();
		if (object.has("friends")) object.get("friends").getAsJsonArray().forEach(element -> {
			if (element.isJsonObject()) {
				this.friends.add(new GameProfile(
					UUID.fromString(element.getAsJsonObject().get("uuid").getAsString()),
					element.getAsJsonObject().get("name").getAsString()
				));
			} else {
				UUID uuid = UUID.fromString(element.getAsString());
				this.friends.add(new GameProfile(uuid, loadName(uuid)));
			}
		});
	}
	
    private static String loadName(UUID uuid) {
		try (CloseableHttpResponse response = Kuphack.get().getHttpClient().execute(
				new HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid))) {
			JsonObject object = new Gson().fromJson(
				EntityUtils.toString(response.getEntity())
			, JsonObject.class);
			if (object == null) return null;
			
			if (object.has("error") && object.get("error").getAsString().equals("No Content"))
				return null;
			return object.get("name").getAsString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
	
	/**
	 * Saves friends to the file without overwriting other settings
	 */
	public void save() {
		JsonObject object = Kuphack.settings().read();
		object.add("friends", toJsonArray());
		Kuphack.settings().write(object);
	}
	
	public JsonArray toJsonArray() {
		JsonArray array = new JsonArray();
		for (GameProfile profile : friends) {
			JsonObject object = new JsonObject();
			object.addProperty("uuid", profile.getId().toString());
			object.addProperty("name", profile.getName());
			array.add(object);
		}
		return array;
	}
	
	@EventMention
	public void onEvent(DamageEvent e) {
		if (e.getTarget() instanceof PlayerEntity target) {
			if (isFriend(target.getGameProfile())) {
				if (target.isInvisibleTo(client.player)) {
					e.getClientPlayer().sendMessage(Text.of("§4Attacked an invisible friend"), true);
				} else {
					e.setCancelled(true);
					if (this.lastDamaged != e.getTarget())
						e.getClientPlayer().sendMessage(Text.of("§cCan't attack a friend"), true);
					e.getClientPlayer().playSoundToPlayer(SoundEvents.ITEM_SHIELD_BLOCK.value(), SoundCategory.PLAYERS, 0.5f, 1.0f);				
				}
			}
			this.lastDamaged = target;
		}
	}

	/**
	 * Functionality for adding a friend and showing text when looking at a friend
	 */
	protected void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
		if (!isOnServer())
			return;
		MinecraftClient client = MinecraftClient.getInstance();
		
		if (!client.player.getAbilities().flying && client.targetedEntity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)client.targetedEntity;
			
			if (client.mouse.wasMiddleButtonClicked()) {
				if (!middlePressed) {
					if (!isFriend(player.getGameProfile())) {
						if (client.player.isSneaking()) {
							if (isDisabled()) client.player.sendMessage(
								Text.of("§cFriend feature isn't enabled!")
							, true);
							if (player.isInvisibleTo(client.player))
								client.player.sendMessage(Text.of("§cCan't friend someone who is invisible"), true);
							else addFriend(player);
						} else {
							client.player.sendMessage(Text.of("§cYou need to §f[SHIFT + MIDDLE CLICK]§c to friend someone"), true);
						}
					} else removeFriend(player);
					
					middlePressed = true;
				}
			} else middlePressed = false;
			
			if (isFriend(player.getGameProfile())) {
				String name = player.isInvisibleTo(client.player) ? "???" : client.targetedEntity.getName().getString();
				String text = isDisabled() ? "Friend feature is disabled" : name + " Friended";
				context.drawTextWithShadow(client.textRenderer, text,
					client.getWindow().getScaledWidth()/2 - client.textRenderer.getWidth(text) / 2, 30
				, isDisabled() ? 0xFFFF5555 : 0xFF55FF55);
				text = isDisabled() ? "Remove friend by Middle Clicking" : "Remove by Middle Clicking";
				context.drawTextWithShadow(client.textRenderer, text,
					client.getWindow().getScaledWidth()/2 - client.textRenderer.getWidth(text) / 2, 39
				, isDisabled() ? 0xFFAA0000 : 0xFF00AA00);
			}
		}
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
