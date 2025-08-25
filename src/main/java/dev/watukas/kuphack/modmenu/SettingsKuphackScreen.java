package dev.watukas.kuphack.modmenu;

import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.watukas.kuphack.AdBlockFeature;
import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.finder.MinehutButtonState;
import dev.watukas.kuphack.finder.MinehutServerListScreen;
import dev.watukas.kuphack.updater.UpdateChecker;
import dev.watukas.kuphack.updater.UpdateStatus;
import dev.watukas.kuphack.updater.UpdateToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class SettingsKuphackScreen extends Screen {

	private UpdateStatus receivedStatus;
	private ButtonList buttonList;

	private final Screen parent;
	private boolean initialized;
	
	public SettingsKuphackScreen(Screen parent) {
		super(Text.of("Kuphack Settings"));
		this.parent = parent;
	}
	
    @Override
    protected void init() {
    	if (this.initialized)
    		this.clearChildren();
    	this.buttonList = new ButtonList(this.client, this, 32);
    	
        int x = this.width / 2 - 155;
        int xR = x + 160;
        int y = 40;
        
        this.addDrawableChild(ButtonWidget.builder(
        	Text.of("MH List Button: "+Kuphack.get().serverListButton), button -> {
        		List<MinehutButtonState> list = Arrays.asList(MinehutButtonState.values());
        		int i = list.indexOf(Kuphack.get().serverListButton)+1;
        		if (i >= list.size()) i = 0;
        		 
        		Kuphack.get().serverListButton = list.get(i);
        		
        		button.setMessage(Text.of("MH List Button: "+Kuphack.get().serverListButton));
        	})
        	.position(x, y) // top-left
        	.tooltip(Tooltip.of(Text.of("Affects the location of the Minehut server list button in the multiplayer menu")))
        .build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.of("Server List..."),
        	button -> client.setScreen(new MinehutServerListScreen(this))
        ).tooltip(Tooltip.of(Text.of(
        	"Opens the Minehut server list which can also be accessed in the multiplayer menu"
        ))).position(xR, y).build()); // top-right
        
        this.addDrawableChild(ButtonWidget.builder(
        	Text.of("Update Notifier: "+Kuphack.get().updateOption), button -> {
        		Kuphack.get().updateOption = Kuphack.get().updateOption.next();
        		button.setMessage(Text.of("Update Notifier: "+Kuphack.get().updateOption));
        	}).position(x, y += 24) // mid-left
        .tooltip(Tooltip.of(Text.of(Kuphack.isFeather() ?
        	"Notifies you in chat when there is a new Kuphack release"
        	: "Notifies you in chat when there is a new Kuphack release, optionally can even automatically download it"
        ))).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Features..."),
        	button -> this.client.setScreen(new FeatureManagementScreen(this))
        ).tooltip(Tooltip.of(Text.of(
        	"Opens the feature management screen. You can toggle features through it, which I'd imagine nobody else finding handy"
        ))).position(xR, y).build()); // mid-right
        
        this.addDrawableChild(ButtonWidget.builder(Text.of("Check for Updates"),
        	button -> new Thread(() -> checkForUpdates(button)).start()
    	).tooltip(Tooltip.of(Text.of(
    		"Retrieves the latest kuphack update and downloads it if possible."
    	))).position(x, y += 24).build()); // bottom-left
        
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.client.setScreen(this.parent))
        	.position(this.width / 2 - 100, this.height - 28)
        	.width(200).build());
        initialized = true;
    }
    
    private void checkForUpdates(ButtonWidget button) {
		button.active = false;
		boolean failedStatus = false;
		
		try {
			UpdateStatus status = this.receivedStatus != null ? 
				this.receivedStatus
			: UpdateChecker.checkAndDownload();
			
			if (status != null) {
				if (status.open())
					client.executeSync(() -> open(status.release().url()));
				this.receivedStatus = status;
			}
			client.getToastManager().add(new UpdateToast(status));
		} catch (Exception e) {
			client.getToastManager().add(new UpdateToast(null));
			failedStatus = true;
			e.printStackTrace();
		}
		
		if (!failedStatus) button.active = true;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    	SupportedServer server = Kuphack.getServer();
    	Text connected = Text.of(server != null ? "Connected to " + server
    		: Kuphack.isOnMinehut() ? "No extra features on this server"
    	: "");
    
        this.buttonList.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, connected, this.width - this.textRenderer.getWidth(connected) - 15, 15, 0xFFFFFF);
    }
    
    @Override
    public void removed() {
    	this.saveChanges();
    	super.removed();
    }
    
    @Override
    public void close() {
    	this.client.setScreen(parent);
    }
    
    private void saveChanges() {
    	JsonObject object = Kuphack.get().readDataFile();
    	
    	JsonArray array = new JsonArray();
    	Kuphack.get().getFeatures().stream().filter(Feature::isDisabled)
    		.forEach(feature -> array.add(feature.getClass().getSimpleName()));
    	object.add("disabled", array);
    	object.addProperty("ad-block-strict", Kuphack.get().getFeature(AdBlockFeature.class).strict);
    	object.addProperty("mhButtonState", Kuphack.get().serverListButton.name());
    	object.addProperty("auto-update", Kuphack.get().updateOption.id());
    	
    	write(object);
    }
    
    public static void write(JsonObject object) {
    	Gson gson = new GsonBuilder()
        	.setPrettyPrinting()
        	.create();
    	try (Writer writer = Files.newBufferedWriter(Kuphack.get().getDataFile(), Charset.defaultCharset(),
    		    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
    		writer.write(gson.toJson(object));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

}
