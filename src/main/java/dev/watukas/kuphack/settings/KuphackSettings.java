package dev.watukas.kuphack.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import dev.watukas.kuphack.Feature;
import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.finder.MinehutButtonState;
import dev.watukas.kuphack.updater.CheckOption;

public class KuphackSettings {

	public Path file;
	
	private MinehutButtonState minehutButtonState = Kuphack.isFeather()
		? MinehutButtonState.LEFT_CORNER
		: MinehutButtonState.RIGHT_CORNER;
	private CheckOption updateOption = CheckOption.LOOKUP;
	private boolean strictAdBlock;
	
	public KuphackSettings(Path file) {
		this.file = file;
		
		// Multiplayer Button Setting
		JsonObject object = this.read();
		if (object.has("mhButtonState"))
			this.minehutButtonState = MinehutButtonState.valueOf(object.get("mhButtonState").getAsString());
		if (object.has("auto-update"))
			this.updateOption = CheckOption.of(object.get("auto-update"));
	}
	
	public List<Feature> disabled() {
		JsonObject object = this.read();
		if (object == null || !object.has("disabled") || !object.get("disabled").isJsonArray())
			return Collections.emptyList();
		JsonArray array = object.get("disabled").getAsJsonArray();
		return Kuphack.get().getFeatures().stream()
			.filter(feature -> !array.contains(new JsonPrimitive(feature.getName())))
			.toList();
	}
	
	public CheckOption updateOption() {
		return this.updateOption;
	}
	
	public void updateOption(CheckOption option) {
		this.updateOption = option;
		this.save();
	}
	
	public boolean strictAdBlock() {
		return this.strictAdBlock;
	}
	
	public void strictAdBlock(boolean value) {
		this.strictAdBlock = value;
		this.save();
	}
	
	public MinehutButtonState minehutButtonState() {
		return this.minehutButtonState;
	}
	
	public void minehutButtonState(MinehutButtonState state) {
		this.minehutButtonState = state;
		this.save();
	}
	
	public JsonObject read() {
		try (BufferedReader reader = Files.newBufferedReader(this.file, Charset.defaultCharset())) {
			return Objects.requireNonNullElseGet(
				new Gson().fromJson(reader.lines().collect(Collectors.joining("\n")), JsonObject.class),
				JsonObject::new
			);
		} catch (IOException | JsonParseException e) {
			e.printStackTrace();
		}
		return new JsonObject();
	}
	
    public void save() {
    	JsonObject object = this.read();
    	
    	JsonArray array = new JsonArray();
    	Kuphack.get().getFeatures().stream().filter(Feature::isDisabled)
    		.forEach(feature -> array.add(feature.getClass().getSimpleName()));
    	object.add("disabled", array);
    	object.addProperty("ad-block-strict", this.strictAdBlock);
    	object.addProperty("mhButtonState", this.minehutButtonState.name());
    	object.addProperty("auto-update", this.updateOption.id());
    	
    	write(object);
    }
    
    public void write(JsonObject object) {
    	Gson gson = new GsonBuilder()
        	.setPrettyPrinting()
        	.create();
    	try (Writer writer = Files.newBufferedWriter(this.file, Charset.defaultCharset(),
    		    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
    		writer.write(gson.toJson(object));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
	
}
