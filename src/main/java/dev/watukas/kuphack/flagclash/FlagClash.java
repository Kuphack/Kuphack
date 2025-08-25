package dev.watukas.kuphack.flagclash;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.SupportedServer;
import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Event.EventMention;
import dev.watukas.kuphack.events.InventoryClickEvent;
import dev.watukas.kuphack.modmenu.FeatureManagementScreen;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

/**
 * Pretty much everything relating to the server FlagClash which aren't specific features for it.
 * {@link FlagLocation} kind of is a feature but it isn't even listed as one in {@link FeatureManagementScreen}, and is only used in code
 */
public class FlagClash implements EventHolder {

	private record Stat(String name, String value, String data) {}
	
	private static final Pattern SIDEBAR_PATTERN = Pattern.compile("^\\s*(?<NAME>[^\\s]+):\\s+(?<VALUE>[^\\s]+)(\\s+(?<DATA>.+))?\\s*$");
	private static final HashMap<String, Stat> statCache = new HashMap<>();
	
	private static final String[] suffixes = { "k", "m", "b", "t", "q", "p", "s", "e", "o", "n", "d", "u", "v", "h", "i", "j", "l", "r", "w", "x", "y", "z" };

	private static long upgradeCost = -1;

	@EventMention
	public void onEvent(InventoryClickEvent e) {
		if (Kuphack.getServer() != SupportedServer.FLAGCLASH)
			return;
		if ((Registries.ITEM.getId(e.getItem()).getPath().contains("banner")) && 
				e.getStack().getName().getString().contains("Level Up")) {
			String line = Kuphack.getStripLore(e.getStack()).getLast().strip();
			if (getGold() >= toRealValue(line.split(" ")[0])) {
				upgradeCost = -1;
			}
		}
	}
	
	public static long getUpgradeCost() {
		if (FlagClash.upgradeCost == -1) {
			int level = getLevel();
			if (level < 1)
				return -1;
			int gps = level + level * level;
			
			int time;
			if (level > 10)
				time = 300 + level * 20;
			else
				time = 20 + level * 50;
			
			return Math.min(2000, time) * gps;
		}
		return FlagClash.upgradeCost;
	}
	
	public static void setUpgradeCost(long value) {
		if (value > 0)
			FlagClash.upgradeCost = value;
		else
			FlagClash.upgradeCost = -1;
	}
	
	public static double getUpgradeTime() {
		if (upgradeCost <= 0)
			return -1;
		double time = ((upgradeCost - getGold()) / (getGPS() * FlagClash.getMultiplier()));
		if (time < 0)
			return 0;
		return time;
	}
	
	public static String timeAsString(double time) {
		if (time < 60) return (int) time + "s";
		String text = (int)(time / 60 % 60) + "min";
		if (time > 3600) text = Kuphack.round(time / 60.0 / 60.0) + "h";
		return text;
	}
	
	public static int getLevel() {
		try {
			
			String value = getStat("Level").value;
			value = value.substring(0, value.offsetByCodePoints(0, value.codePointCount(0, value.length()) - 1));
			
			return Integer.parseInt(value);
		} catch (Throwable throwable) {
			Kuphack.error(throwable);
			return 0;
		}
	}
	
	public static long getGold() {
		try {
			return toRealValue(getStat("Gold").value);
		} catch (Throwable throwable) {
			Kuphack.error(throwable);
			return 0;
		}
	}
	
	public static long getGPS() {
		try {
			String[] split = getStat("Gold").data.split("\\s+");
			return toRealValue(split[1]);
		} catch (Exception e) {
			Kuphack.error(e);
			return 1;
		}
	}
	
	public static double getMultiplier() {
		try {
			String[] split = getStat("Gold").data.split("\\s+");
			
			if (split.length < 3)
				return 1;
			
			return 1 + Double.parseDouble(split[2].replace("(+", "").replace("%)", "")) / 100.0;
		} catch (Exception e) {
			Kuphack.error(e);
			return 1;
		}
	}
	
	public static long toRealValue(String text) {
		try {
			return Long.valueOf(text);
		} catch (Exception e) {}
		
		try {
			String suffix = text.charAt(text.length() - 1) + "";
			double d = Double.parseDouble(text.substring(0, text.length() - 1));

			int i = Arrays.asList(suffixes).indexOf(suffix.toLowerCase()) + 1;
			return (long) (d * Math.pow(1000, i));
		} catch (Throwable e2) {
			Kuphack.error(new Exception("Failed calculating value of \"" + text + "\"", e2));
			return 0;
		}
	}
	
	public static String toVisualValue(long value) {
		if (value < 1000)
			return Long.toString(value);
	    int index = (int) (Math.log10(value) / 3 - 1);
	    double d = Math.pow(1000, index + 1);
	    DecimalFormat df = new DecimalFormat("0.00");
	    df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	    df.setRoundingMode(RoundingMode.FLOOR);
	    return df.format(value / d) + suffixes[index];
	}
		
	protected static Stat getStat(String name) {
		if (Kuphack.getServer() != SupportedServer.FLAGCLASH)
			return statCache.getOrDefault(name, new Stat(name, "0", null));
		String displayName = toSmallText(name);
		for (Text line : Kuphack.getScoreboard()) {
			Matcher matcher = SIDEBAR_PATTERN.matcher(Kuphack.stripColor(line));
			if (!matcher.matches())
				continue;
			String foundName = matcher.group("NAME");
			if (!foundName.equals(displayName))
				continue;
			Stat stat = new Stat(name, matcher.group("VALUE"), matcher.group("DATA"));
			statCache.put(name, stat);
			return stat;
		}
		if (statCache.containsKey(name))
			return statCache.get(name);
		throw new IllegalArgumentException("Couldn't find '" + displayName + "' on the sidebar");
	}
	
	private static final String[] smallText = {
		"ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ғ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ", "ɴ", "ᴏ", "ᴘ", "ǫ", "ʀ", "s", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "ʏ", "ᴢ"
	};
	private static final String[] smallDigits = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};

    public static String toSmallText(String input) {
        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
            	result.append(smallText[Character.toLowerCase(c) - 'a']);
            } else if (Character.isLowerCase(c)) {
                result.append(smallText[c - 'a']);
            } else if (Character.isDigit(c)) {
                result.append(smallDigits[c - '0']);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
	
}
