package com.github.vaapukkax.kuphack.updater;

import com.github.vaapukkax.kuphack.Kuphack;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public enum CheckOption {

	OFF(0, "Off"),
	LOOKUP(1, "Lookup"),
	CHECK_AND_DOWNLOAD(2, "Check & Get");

	protected static final CheckOption DEFAULT = CheckOption.LOOKUP;
	
	private final int id;
	private final String string;
	
	private CheckOption(int id, String display) {
		this.id = id;
		this.string = display;
	}

	public CheckOption next() {
		if (this == OFF)
			return LOOKUP;
		if (this == LOOKUP && !(Kuphack.isFeather()) )
			return CHECK_AND_DOWNLOAD;
		return OFF;
	}
	
	public int id() {
		return this.id;
	}
	
	public String toString() {
		return this.string;
	}

	public static CheckOption of(JsonElement element) {
		if (element == null || !(element.isJsonPrimitive()) )
			return CheckOption.DEFAULT;
		JsonPrimitive primitive = element.getAsJsonPrimitive();
		if (primitive.isBoolean()) // Legacy
			return primitive.getAsBoolean() ? (Kuphack.isFeather() ? LOOKUP : CHECK_AND_DOWNLOAD) : OFF;
		if (!primitive.isNumber())
			return CheckOption.DEFAULT;
		for (CheckOption option : CheckOption.values())
			if (primitive.getAsInt() == option.id);
		return CheckOption.DEFAULT;
	}
	
}
