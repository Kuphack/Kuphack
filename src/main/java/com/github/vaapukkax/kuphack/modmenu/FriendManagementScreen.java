package com.github.vaapukkax.kuphack.modmenu;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.lwjgl.glfw.GLFW;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.flagclash.FriendFeature;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class FriendManagementScreen extends Screen {

	protected static final HashMap<UUID, String> names = new HashMap<>();
	
	private final HashMap<UUID, ButtonWidget> widgets = new HashMap<>();
	private TextFieldWidget fieldWidget;
	private ButtonList buttonList;
	
	private final FriendFeature feature = Kuphack.get().getFeature(FriendFeature.class);
	private final Screen parent;
	private boolean initialized;
	
	public FriendManagementScreen(Screen parent) {
		super(Text.of("Friend Management"));
		this.parent = parent;
	}
	
    @Override
    protected void init() {
    	if (initialized) {
    		this.clearChildren();
    		this.widgets.clear();
    	}
    	
    	this.buttonList = new ButtonList(this.client, this, 64);
    	this.fieldWidget = new TextFieldWidget(this.textRenderer, this.width / 2 - 75, 40, 150, 20, this.fieldWidget, Text.of("Enter friend name")) {
    		@Override
    		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    			if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
    			if (keyCode != GLFW.GLFW_KEY_ENTER || getText().length() < 3) return false;
    			final String name = this.getText();
    			if (feature.getFriends().stream().map(uuid -> names.get(uuid))
    				.filter(p -> p.equalsIgnoreCase(name))
    				.findFirst().isPresent()) return false;
    			
				new Thread(() -> {
					this.active = false;
					this.setFocused(false);
					try {
						UUID uuid = getMinecraftId(name);
						if (uuid == null) return;
						names.put(uuid, name);
						feature.addFriend(uuid);
						init();
					} finally {
						this.active = true;
					}
				}).start();
				return true;
    		}
    	};
    	this.fieldWidget.setChangedListener(text -> this.fieldWidget.setSuggestion(text.isEmpty() ? "Add friend" : ""));
    	this.fieldWidget.setCursor(0);
    	this.fieldWidget.setTextPredicate(text -> text.matches("^\\w{0,16}$"));
        this.addDrawableChild(this.fieldWidget);
        this.fieldWidget.setFocused(true);
        this.setInitialFocus(this.fieldWidget);
    	
        int y = 65;
        for (UUID uuid : feature.getFriends()) {
        	final String name = (isOffline(uuid) ? "§7" : "§f") + this.getName(uuid);
        	ButtonWidget widget = ButtonWidget.builder(Text.of(name), button -> {
	        	feature.removeFriend(uuid);
	        	this.init();
	        }).tooltip(Tooltip.of(Text.of(
	        	"Remove '" + getName(uuid) + "' by clicking"
	        ))).position(this.width / 2 - 150 / 2, y).width(150).build();
        	widget.active = !feature.isDisabled();
	        this.widgets.put(uuid, buttonList.addWidget(widget));
	        y += 24;
        }
        this.addSelectableChild(this.buttonList);
        
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close())
        	.position(this.width / 2 - 100, this.height - 28)
        	.width(150).build()
        );
        this.addDrawableChild(ButtonWidget.builder(Text.of("["+feature.getTextState()+"]"), button -> {
        	this.feature.toggle();
        	button.setMessage(Text.of("["+this.feature.getTextState()+"]"));
        	this.init();
        }).position(this.width / 2 + 50, this.height - 28).width(50).build());
        this.initialized = true;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.buttonList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);

        if (feature.getFriends().isEmpty()) {
        	context.drawCenteredTextWithShadow(this.textRenderer, "You have no friends! :(", this.width / 2, this.height / 2, 0xFFFFFF);
        }
    }

    @Override
    public void close() {
    	this.feature.save();
    	this.client.setScreen(parent);
    }
    
    private boolean isOffline(UUID uuid) {
    	ClientPlayNetworkHandler handler = client.getNetworkHandler();
    	if (handler == null) return false;
    	return handler.getPlayerListEntry(uuid) == null;
    }
    
    private String getName(UUID uuid) {
    	return names.computeIfAbsent(uuid, id -> {
    		String name = feature.getOnlinePlayerName(uuid);
    		if (name != null) return name;

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
    	});
    }
    
	private UUID getMinecraftId(String name) {
		if (names.containsValue(name)) return names.keySet().stream()
			.filter(key -> names.get(key).equals(name))
			.findFirst().get();
		ClientPlayNetworkHandler players = client.getNetworkHandler();
		PlayerListEntry entry = players != null ? players.getPlayerListEntry(name) : null;
		if (entry != null) return entry.getProfile().getId();
		
		try (CloseableHttpResponse response = Kuphack.get().getHttpClient().execute(new HttpGet("https://api.mojang.com/users/profiles/minecraft/"+name))) {
			JsonObject object = new Gson().fromJson(
				EntityUtils.toString(response.getEntity())
			, JsonObject.class);
			if (object == null) return null;
			
			try {
				if (object.has("error") && object.get("error").getAsString().equals("No Content"))
					return null;
				return insert(object.get("id").getAsString());
			} catch (Exception e) {
				System.err.println(object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static UUID insert(String noDash) {
        BigInteger bi1 = new BigInteger(noDash.substring(0, 16), 16);                
        BigInteger bi2 = new BigInteger(noDash.substring(16, 32), 16);
        return new UUID(bi1.longValue(), bi2.longValue());
	}

}