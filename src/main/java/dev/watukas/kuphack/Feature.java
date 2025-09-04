package dev.watukas.kuphack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.JsonObject;

import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.modmenu.FeatureManagementScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public abstract class Feature {

	protected final MinecraftClient client = MinecraftClient.getInstance();
	
	public final SupportedServer[] servers;
	private final Function<FeatureManagementScreen, Screen> screenFunction;
	private boolean disabled;
	
	private final String defName;
	private final String description;
	
	public Feature(String description, SupportedServer... servers) {
		this(description, null, servers);
	}
	
	public Feature(String description, Function<FeatureManagementScreen, Screen> screenFunction, SupportedServer... servers) {
		this.description = description;
		this.screenFunction = screenFunction;
		this.servers = servers;
		this.disabled = shouldBeDisabled();
		
		this.defName = this.getClass().getSimpleName()
			.replaceAll("Feature$", "")
			.replaceAll("([a-z])([A-Z])", "$1 $2")
			.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");
	}
	
	private boolean shouldBeDisabled() {
		return Kuphack.settings().disabled().contains(this);
	}
	
	protected void onActivate() {
		if (this instanceof EventHolder holder) Event.register(holder);
	}
	
	protected void onDeactivate() {
		if (this instanceof EventHolder holder) Event.unregister(holder);
	}
	
	public boolean isOnServer() {
		SupportedServer current = Kuphack.getServer();
		if (current == null || (!FabricLoader.getInstance().isDevelopmentEnvironment() && client.getCurrentServerEntry() == null))
			return false;
		
		if (servers == null || servers.length == 0) return true;
		for (SupportedServer server : servers) {
			if (current == server) return true;
		}
		return false;
	}
	
	public boolean isPlaying() {
		return !disabled && isOnServer() && client.player != null;
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}
	
	public String getTextState() {
		return disabled ? "OFF" : "ON";
	}
	
	public void toggle() {
		if (!disabled && isOnServer()) this.onDeactivate();
		this.disabled = !this.disabled;
		if (!disabled && isOnServer()) this.onActivate();
	}

	public String getName() {
		return this.defName;
	}

	public String getDescription() {
		return this.description;
	}
	
	public Function<FeatureManagementScreen, Screen> screenFunction() {
		return this.screenFunction;
	}
	
	protected JsonObject config() {
		JsonObject settings = Kuphack.settings().read();
		JsonObject features = settings.getAsJsonObject("features");
		if (features == null)
			return new JsonObject();
		JsonObject featureSettings = features.getAsJsonObject(this.getName());
		if (featureSettings == null)
			return new JsonObject();
		return featureSettings;
	}
	
	protected void writeConfig(JsonObject featureConfig) {
		Objects.requireNonNull(featureConfig);
		
		JsonObject settings = Kuphack.settings().read();
		JsonObject features = settings.getAsJsonObject("features");
		if (features == null)
			settings.add("features", features = new JsonObject());
		features.add(this.getName(), featureConfig);
		
		Kuphack.settings().write(settings);
	}
	
	protected void writeConfig(Consumer<JsonObject> consumer) {
		JsonObject config = this.config();
		consumer.accept(config);
		this.writeConfig(config);
	}
	

	
}
