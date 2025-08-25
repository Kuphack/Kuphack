package dev.watukas.kuphack.modmenu;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.flagclash.FriendFeature;
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

	private final HashMap<GameProfile, ButtonWidget> widgets = new HashMap<>();
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
    			if (super.keyPressed(keyCode, scanCode, modifiers))
    				return true;
    			if (keyCode != GLFW.GLFW_KEY_ENTER || getText().length() < 3)
    				return false;
    			final String name = this.getText();
    			if (feature.getFriends().stream().anyMatch(profile -> profile.getName().toLowerCase().contains(name.toLowerCase())))
    				return false;
    			
				new Thread(() -> {
					this.active = false;
					this.setFocused(false);
					try {
						GameProfile profile = loadProfile(name);
						if (profile == null)
							return;
						feature.addFriend(profile);
						init();
					} finally {
						this.active = true;
					}
				}).start();
				return true;
    		}
    	};
    	this.fieldWidget.setChangedListener(text -> this.fieldWidget.setSuggestion(text.isEmpty() ? "Add friend" : ""));
    	this.fieldWidget.setCursor(0, false);
    	this.fieldWidget.setTextPredicate(text -> text.matches("^\\w{0,16}$"));
        this.addDrawableChild(this.fieldWidget);
        this.fieldWidget.setFocused(true);
        this.setInitialFocus(this.fieldWidget);
    	
        int y = 65;
        for (GameProfile profile : feature.getFriends().stream().sorted((a, b) -> {
        	if (!isOffline(a) && isOffline(b))
        		return -1;
        	if (isOffline(a) && !isOffline(b))
        		return 1;
        	return a.getName().compareTo(b.getName());
        }).toList()) {
        	final String name = (isOffline(profile) ? "§7" : "§f") + profile.getName();
        	ButtonWidget widget = ButtonWidget.builder(Text.of(name), button -> {
	        	feature.removeFriend(profile);
	        	this.init();
	        }).tooltip(Tooltip.of(Text.of(
	        	"Remove '" + profile.getName() + "' by clicking"
	        ))).position(this.width / 2 - 150 / 2, y).width(150).build();
        	widget.active = !feature.isDisabled();
	        this.widgets.put(profile, buttonList.addWidget(widget));
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
    	super.render(context, mouseX, mouseY, delta);    	
        this.buttonList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        if (feature.getFriends().isEmpty()) {
        	context.drawCenteredTextWithShadow(this.textRenderer, "You have no friends! :(", this.width / 2, this.height / 2, 0xFFFFFF);
        }
    }

    @Override
    public void close() {
    	this.feature.save();
    	this.client.setScreen(parent);
    }
    
    private boolean isOffline(GameProfile profile) {
    	ClientPlayNetworkHandler handler = client.getNetworkHandler();
    	if (handler == null) return false;
    	return handler.getPlayerListEntry(profile.getId()) == null;
    }
    
	private GameProfile loadProfile(String name) {
		ClientPlayNetworkHandler players = client.getNetworkHandler();
		PlayerListEntry entry = players != null ? players.getPlayerListEntry(name) : null;
		if (entry != null) return entry.getProfile();
		
		try (CloseableHttpResponse response = Kuphack.get().getHttpClient().execute(new HttpGet("https://api.mojang.com/users/profiles/minecraft/"+name))) {
			JsonObject object = new Gson().fromJson(
				EntityUtils.toString(response.getEntity())
			, JsonObject.class);
			if (object == null) return null;
			
			try {
				if (object.has("error") && object.get("error").getAsString().equals("No Content"))
					return null;
				return new GameProfile(insert(object.get("id").getAsString()), object.get("name").getAsString());
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