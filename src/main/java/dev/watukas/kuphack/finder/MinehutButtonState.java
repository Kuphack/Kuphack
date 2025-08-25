package dev.watukas.kuphack.finder;

public enum MinehutButtonState {
	
	LEFT_CORNER,
	RIGHT_CORNER,
	NEXT_TO_JOIN,
	HIDDEN;

	@Override
	public String toString() {
		String name = name().toLowerCase().replaceAll("_", " ");
		return Character.toUpperCase(name.charAt(0))+name.substring(1);
	}
	
}
