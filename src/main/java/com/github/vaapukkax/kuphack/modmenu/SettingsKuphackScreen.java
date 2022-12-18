package com.github.vaapukkax.kuphack.modmenu;

import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.github.vaapukkax.kuphack.AdBlockFeature;
import com.github.vaapukkax.kuphack.Feature;
import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.SupportedServer;
import com.github.vaapukkax.kuphack.finder.MinehutButtonState;
import com.github.vaapukkax.kuphack.finder.MinehutServerListScreen;
import com.github.vaapukkax.kuphack.flagclash.FriendFeature;
import com.github.vaapukkax.kuphack.flagclash.StariteTracerFeature;
import com.github.vaapukkax.kuphack.updater.UpdateChecker;
import com.github.vaapukkax.kuphack.updater.UpdateStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class SettingsKuphackScreen extends Screen {

	private boolean checkedForUpdate = false;
	private final HashMap<ButtonWidget, String> tooltips = new HashMap<>();
	private ButtonList buttonList;

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
    	this.buttonList = new ButtonList(this.client, this);
    	
        int x = this.width / 2 - 155;
        int xR = x + 160;
        int y = 40;

        this.load();
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
        	x, y, 150, 20, // top-left
        	Text.of("MH List Button: "+Kuphack.get().serverListButton), button -> {
        		List<MinehutButtonState> list = Arrays.asList(MinehutButtonState.values());
        		int i = list.indexOf(Kuphack.get().serverListButton)+1;
        		if (i >= list.size()) i = 0;
        		 
        		Kuphack.get().serverListButton = list.get(i);
        		
        		button.setMessage(Text.of("MH List Button: "+Kuphack.get().serverListButton));
        	}
        )), "Affects the location of the Minehut server list button in the multiplayer menu");
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		xR, y, 150, 20, // top-right
    		Text.of("Server List..."),
    		button -> client.setScreen(new MinehutServerListScreen(this))
        )), "Opens the Minehut server list which can also be accessed in the multiplayer menu");
        
        String updaterName = Kuphack.isFeather() ? "Update Notifier" : "Auto Update";
        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		x, y += 24, 150, 20, // mid-left
    		Text.of(updaterName + ": "+Kuphack.get().autoUpdate), button -> {
    			Kuphack.get().autoUpdate = !Kuphack.get().autoUpdate;
        		button.setMessage(Text.of(updaterName + ": "+Kuphack.get().autoUpdate));
    		}
        )), Kuphack.isFeather() ? "Notifies you in chat when there is a new Kuphack release"
        	: "Toggles automatic notifying, downloading and updating of Kuphack when a new release is found.");
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		xR, y, 150, 20, // mid-right
    		Text.of("Features..."),
    		button -> this.client.setScreen(new FeatureManagementScreen(this))
        )), "Opens the feature management screen. You can toggle features from there, which ain't recommended but be free to do so");
        
        tooltips.put(this.addDrawableChild(new ButtonWidget(
    		x, y += 24, 150, 20, // bottom-left
    		Text.of("Check for Updates"),
    		button -> checkForUpdates(button)
        )), "Retrieves the latest kuphack update and downloads it if possible.");
        
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 28 /*y += 24*/, 200, 20, ScreenTexts.DONE, button -> this.client.setScreen(this.parent)));
        initialized = true;
    }
    
    private void checkForUpdates(ButtonWidget button) {
		if (checkedForUpdate) return;
		new Thread(() -> {
			button.active = false;
			boolean updateReceived = false;
			try {
				UpdateChecker.checkAndDownload();
				
				UpdateStatus status = UpdateChecker.takeCheckerStatus();
				if (status != null) {
					Kuphack.addToast("Update Status", status.additional());
					if (status.open())
						client.executeSync(() -> open(status.release().getURL()));
					updateReceived = true;
				} else Kuphack.addToast("Update Status", "Couldn't find an update...");
				checkedForUpdate = true;
			} catch (Exception e) {
				Kuphack.addToast("Update Status", "Could not retrieve status!\nRatelimit reached?");
				e.printStackTrace();
			}
			if (!updateReceived) button.active = true;
		}).start();
    }
    
	/**
	 * Open a pop-up to ask confirmation for opening a link
	 * @param url the link
	 */
	private void open(String url) {
		MinecraftClient client = MinecraftClient.getInstance();
		try {
			client.setScreen(new ConfirmLinkScreen((confirmed) -> {
				if (confirmed)
	            	Util.getOperatingSystem().open(url);
	            client.setScreen(this);
	        }, url, true));
		} catch (Throwable e) {
			e.printStackTrace();
			Util.getOperatingSystem().open(url);
		}
	}

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    	SupportedServer server = Kuphack.getServer();
    	Text connected = Text.of(server != null ? "Connected to " + server
    		: Kuphack.isOnMinehut() ? "No extra features on this server"
    	: "");
    
        this.renderBackground(matrices);
        this.buttonList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        textRenderer.drawWithShadow(matrices, connected, this.width - this.textRenderer.getWidth(connected) - 15, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        
        for (ButtonWidget button : tooltips.keySet()) {
        	if (button.isHovered()) {
        		List<OrderedText> tooltip = textRenderer.wrapLines(Text.of(tooltips.get(button)), 250);
        		renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);
        	}
        }
    }
    
    @Override
    public void removed() {
    	this.save();
    	super.removed();
    }
    
    @Override
    public void close() {
    	this.client.setScreen(parent);
    }
    
    private void load() {
		JsonObject object = Kuphack.get().readDataFile();
    	if (object.has("mhButtonState"))
    		Kuphack.get().serverListButton = MinehutButtonState.valueOf(object.get("mhButtonState").getAsString());
    	if (object.has("auto-update"))
    		Kuphack.get().autoUpdate = object.get("auto-update").getAsBoolean();
    }
    
    protected void save() {
    	Gson gson = new GsonBuilder()
    		.setPrettyPrinting()
    		.create();
    	JsonObject object = new JsonObject();
    	
    	object.add("friends", Kuphack.get().getFeature(FriendFeature.class).toJsonArray());
    	
    	JsonArray array = new JsonArray();
    	Kuphack.get().getFeatures().stream().filter(Feature::isDisabled)
    		.filter(feature -> !feature.getClass().equals(StariteTracerFeature.class))
    		.forEach(feature -> array.add(feature.getClass().getSimpleName()));
    	object.add("disabled", array);
    	object.addProperty("ad-block-strict", Kuphack.get().getFeature(AdBlockFeature.class).strict);
    	object.addProperty("mhButtonState", Kuphack.get().serverListButton.name());
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
