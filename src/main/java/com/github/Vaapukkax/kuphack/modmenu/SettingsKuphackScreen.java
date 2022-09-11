package com.github.Vaapukkax.kuphack.modmenu;

import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.github.Vaapukkax.kuphack.AdBlockFeature;
import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.finder.MinehutButtonState;
import com.github.Vaapukkax.kuphack.finder.MinehutServerListScreen;
import com.github.Vaapukkax.kuphack.flagclash.FriendFeature;
import com.github.Vaapukkax.kuphack.updater.UpdateChecker;
import com.github.Vaapukkax.kuphack.updater.UpdateStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class SettingsKuphackScreen extends Screen {

	private boolean lobbyInjectToggle = false, autoUpdate = false;
	private boolean checkedForUpdate = false;
	
	private final HashMap<ButtonWidget, String> tooltips = new HashMap<>();
	private ButtonListWidget buttonList;

	private final Screen parent;
	private boolean initialized;
	
	public SettingsKuphackScreen(Screen parent) {
		super(Text.of("Kuphack Settings"));
		this.parent = parent;
	}
	
    @Override
    protected void init() {
    	if (initialized) {
    		this.clearChildren();
    		tooltips.clear();
    	}
    	this.buttonList = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
    	
        int x = this.width / 2 - 155;
        int xR = x + 160;
        int y = this.height / 6 - 3;

        load();
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
        	x, y, 150, 20, // top-left
        	Text.of("MH List Button: "+Kuphack.get().mhButtonState),
        	button -> {
        		List<MinehutButtonState> list = Arrays.asList(MinehutButtonState.values());
        		int i = list.indexOf(Kuphack.get().mhButtonState)+1;
        		if (i >= list.size()) i = 0;
        		
        		Kuphack.get().mhButtonState = list.get(i);
        		
        		button.setMessage(Text.of("MH List Button: "+Kuphack.get().mhButtonState));
        	}
        )), "Affects the location of the Minehut server list button\nin the Multiplayer menu");
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
        	xR, y, 150, 20, // top-right
        	Text.of("List Replacement: "+lobbyInjectToggle),
        	button -> {
        		lobbyInjectToggle = !lobbyInjectToggle;
        		button.setMessage(Text.of("List Replacement: "+lobbyInjectToggle));
        	}
        )), "Whether to use the custom Minehut server list\nwhen opening the server list in the lobby");

        AdBlockFeature adBlock = Kuphack.get().getFeature(AdBlockFeature.class);
        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		x, y += 24, 150, 20, // middle-left
    		Text.of("Ad Block: "+adBlock.toggle),
    		button -> {
    			adBlock.toggle = !adBlock.toggle;
    			button.setMessage(Text.of("Ad Block: "+adBlock.toggle));
    		}
        )), "Removes server advertisments in the lobby");
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		xR, y, 150, 20, // middle-right
    		Text.of("Server List..."),
    		button -> client.setScreen(new MinehutServerListScreen(this))
        )), "Opens the Minehut server list\ncan also be accessed in multiplayer menu");

        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		x, y += 24, 150, 20, // bottom-left
    		Text.of("Auto Update: "+autoUpdate),
    		button -> {
        		autoUpdate = !autoUpdate;
        		button.setMessage(Text.of("Auto Update: "+autoUpdate));
    		}
        )), "Toggles automatic updating of Kuphack.\nDisabled by default cause it downloads the newest\nversion even when the Minecraft version doesn't match.");
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		xR, y, 150, 20, // bottom-right
    		Text.of("Check for Updates"),
    		button -> checkForUpdates(button)
        )), "Retrieves the latest kuphack update\nand downloads it if possible.");

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 28 /*y += 24*/, 200, 20, ScreenTexts.DONE, button -> this.client.setScreen(this.parent)));
        initialized = true;
    }
    
    private void checkForUpdates(ButtonWidget button) {
		if (!checkedForUpdate) {
			tooltips.put(button, "Checking for update status...");
			new Thread(() -> {
				synchronized (button) { // Can't remember why its synchronized
					button.active = false;
				}
				
				boolean updateReceived = false;
				try {
					UpdateChecker.checkAndDownload();
					
					UpdateStatus status = UpdateChecker.takeCheckerStatus();
					if (status != null) {
    					tooltips.put(button, "Update status received:\n"+status.text());
    					if (status.runnable() != null)
    						client.executeSync(status.runnable());
    					updateReceived = true;
					} else tooltips.put(button, "Couldn't find an update");
					checkedForUpdate = true;
				} catch (Exception e) {
					tooltips.put(button, "Could not retrieve status!\nRatelimit reached?");
					e.printStackTrace();
				}
				synchronized (button) {
					if (!updateReceived) button.active = true;
				}
			}).start();
		}
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.buttonList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        
        for (ButtonWidget button : tooltips.keySet()) {
        	if (button.isHovered()) {
        		ArrayList<Text> tooltip = new ArrayList<>();
        		for (String line : tooltips.get(button).split("\n")) {
        			tooltip.add(Text.of(line));
        		}
        		renderTooltip(matrices, tooltip, mouseX, mouseY);
        	}
        }
    }
    
    @Override
    public void removed() {
    	save();
    	super.removed();
    }
    
    @Override
    public void close() {
    	this.client.setScreen(parent);
    }
    
    private void load() {
		JsonObject object = new Gson().fromJson(Kuphack.get().readDataFile(), JsonObject.class);
		if (object == null) return;
		
    	if (object.has("lobbyInject"))
    		lobbyInjectToggle = object.get("lobbyInject").getAsBoolean();
    	if (object.has("mhButtonState"))
    		Kuphack.get().mhButtonState = MinehutButtonState.valueOf(object.get("mhButtonState").getAsString());
    	if (object.has("adblock"))
    		Kuphack.get().getFeature(AdBlockFeature.class).toggle = object.get("adblock").getAsBoolean();
    	if (object.has("auto-update"))
    		Kuphack.get().autoUpdate = object.get("auto-update").getAsBoolean();
    }
    
    protected void save() {
    	Gson gson = new GsonBuilder()
    		.setPrettyPrinting()
    		.create();
    	JsonObject object = new JsonObject();
    	
    	object.add("friends", Kuphack.get().getFeature(FriendFeature.class).toJsonArray());
    	
    	object.addProperty("lobbyInject", lobbyInjectToggle);
    	object.addProperty("mhButtonState", Kuphack.get().mhButtonState.name());
    	object.addProperty("adblock", Kuphack.get().getFeature(AdBlockFeature.class).toggle);
    	object.addProperty("auto-update", Kuphack.get().autoUpdate);
    	
    	write(gson.toJson(object));
    }
    
    public static void write(String text) {
    	try (Writer writer = Files.newBufferedWriter(Kuphack.get().getDataFile(), Charset.defaultCharset(),
    		    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
    		writer.write(text);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

}
