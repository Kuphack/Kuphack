package com.github.vaapukkax.kuphack.flagclash;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.Servers;
import com.github.vaapukkax.kuphack.events.InventoryClickEvent;

import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

/**
 * Pretty much everything relating to the server FlagClash which aren't specific features for it. {@link FlagLocation} kind of is a feature but it isn't even listed as one in {@link FeatureManagementScreen}, and is only used in code
 */
public class FlagClash implements EventHolder {

	private static final String[] suffixes = {
		"k", "m", "b", "t", "q", "p", "s", "e", "o", "n", "d", "u", "v", "h", "i", "j", "l", "r", "w", "x", "y", "z"
	};
	private static final String CURRENCY_REGEX = "[0-9]+(\\.[0-9]+)?("+String.join("|", suffixes)+")";
	
	private static BigInteger upgradeCost = BigInteger.ZERO;
	private static boolean unsureUpgrade = true;
	private static final HashMap<String, String> statCache = new HashMap<>();
	private static long mushroom;
	
	@EventMention
	public void onEvent(InventoryClickEvent e) {
		if (e.has(Items.NETHER_STAR, "Reduce Flag Upgrade Cost",
			line -> line.equals("Reduces your flag upgrade cost"),
			line -> line.matches("From "+CURRENCY_REGEX+" Gold to "+CURRENCY_REGEX+" Gold")
		)) {
			List<String> lore = Kuphack.getStripLore(e.getStack());
			if (getStarite().compareTo(BigInteger.valueOf(Long.valueOf(lore.get(3).split(" ")[0]))) >= 0) {
				upgradeCost = toRealValue(lore.get(1).split(" ")[4]);
			}
		} else if ((Registry.ITEM.getId(e.getItem()).getPath().contains("banner") || e.getItem() == Items.NETHER_STAR) && 
				Arrays.asList("Level Up", "Rebirth").contains(e.getStack().getName().getString())) {
			for (String line : Kuphack.getStripLore(e.getStack())) {
				if (!line.contains("Costs: ")) continue;
				if (getGold().compareTo(toRealValue(line.split(" ")[1])) >= 0) {
					unsureUpgrade = true;
				}
			}
		}
	}
	
	public static BigInteger getUpgradeCost() {
		return FlagClash.upgradeCost;
	}
	
	public static void setUpgradeCost(BigInteger value) {
		FlagClash.upgradeCost = value;
		FlagClash.unsureUpgrade = false;
	}
	
	public static double getUpgradeTime() {
		if (upgradeCost == null || upgradeCost.equals(BigInteger.ZERO))
			return -1;
		return Math.max(0, new BigDecimal((upgradeCost.subtract(getGold())).divide(getGPS())).divide(FlagClash.getMultiplier(), MathContext.DECIMAL128).doubleValue());
	}
	
	public static boolean isUpgradeCostUnsure() {
		if (getUpgradeCost() == null)
			FlagClash.unsureUpgrade = true;
		return FlagClash.unsureUpgrade;
	}
	
	public static String timeAsString(double time) {
		if (time < 60) return (int) time + "s";
		String text = (int)(time / 60 % 60) + "min";
		if (time > 3600) text = Kuphack.round(time / 60.0 / 60.0) + "h";
		return text;
	}
	
	public static BigInteger getGold() {
		try {
			if (isMushroomArc()) {
				String line = getLine(0);
				if (line.isBlank()) return new BigInteger(statCache.get("Gold"));
				String gold = line.split("\\s")[1];
				statCache.put("Gold", gold);
				return new BigInteger(gold);
			}
			return toRealValue(getStat("Gold").split("\\s")[0]);
		} catch (Throwable e) {
			Kuphack.error(e);
			return BigInteger.ZERO;
		}
	}
	
	public static BigInteger getGPS() {
		try {
			if (isMushroomArc()) {
				String line = getLine(1);
				if (line.isBlank()) return statCache.containsKey("Gps") ? new BigInteger(statCache.get("Gps")) : BigInteger.ZERO;
				BigInteger gps = toRealValue(line);
				if (gps.equals(BigInteger.ZERO)) gps = BigInteger.ONE;
				statCache.put("Gps", gps.toString());
				return gps;
			}
			BigInteger integer = toRealValue(getStat("Gps").split("\\s+")[0]);
			if (integer.equals(BigInteger.ZERO)) return BigInteger.ONE;
			return integer;
		} catch (Exception e) {
			Kuphack.error(e);
			return BigInteger.ONE;
		}
	}
	
	public static BigDecimal getMultiplier() {
		try {
			if (isMushroomArc()) return BigDecimal.ONE;
			
			String[] split = getStat("Gps").split("\\s+");
			if (split.length < 2) return BigDecimal.ONE;				
			String percentage = split[split.length-1];
			
			if (percentage.contains("%"))
				return BigDecimal.valueOf(
					Integer.parseInt(percentage.substring(0, percentage.length()-1))/100d
				);
			else return BigDecimal.ONE;
		} catch (Exception e) {
			Kuphack.error(e);
			return BigDecimal.ONE;
		}
	}
	
	public static BigInteger getStarite() {
		try {
			if (isMushroomArc()) return BigInteger.ONE;
			return toRealValue(getStat("Starite"));
		} catch (IllegalArgumentException e) {
			return BigInteger.ZERO;
		} catch (Throwable e) {
			Kuphack.error(e);
			return BigInteger.ZERO;
		}
	}
	
	// TODO needs a recode, this code sucks no cap
	public static BigInteger toRealValue(String s) {
		try {
			return new BigInteger(s);
		} catch (Exception e) {
			try {
				String suffix = (s.charAt(s.length()-1)+"");
				double d = Double.parseDouble(s.substring(0, s.length()-1));
	
				int io = (Arrays.asList(suffixes).indexOf(suffix)+1);
				StringBuilder sb = new StringBuilder("1");
				for (int i = 0; i < io; i++) sb.append("000");
				return new BigDecimal(d).multiply(new BigDecimal(sb.toString())).toBigInteger();
			} catch (Exception e2) {
				Kuphack.LOGGER.error("toRealValue("+(s == null ? "null" : "\""+s+"\"")+")");
				Kuphack.error(e2);
				return BigInteger.ZERO;
			}
		}
	}
	
	public static String toVisualValue(BigInteger value) {
	    int length = value.toString().length();
	    if (length < 4) return value.toString(); // Return the normal value because it is less than 1k
	    int index = (length-4)/3; // index of the suffix in the array
	    
	    double d = Math.pow(1000, index+1);
	    DecimalFormat df = new DecimalFormat("0.00");
	    df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	    return df.format(new BigDecimal(value).divide(BigDecimal.valueOf(d))) + suffixes[index];
	}
		
	protected static String getStat(String stat) throws Exception {
		if (Kuphack.getServer() != Servers.FLAGCLASH)
			return statCache.getOrDefault(stat, "0");
		for (Text line : Kuphack.getScoreboard()) {
			String cutLine = line.getString().strip();
			if (!cutLine.contains(stat)) continue;
			
			String value = Kuphack.stripColor(cutLine.substring(cutLine.indexOf(stat+": ")+(stat+": ").length())).strip();
			statCache.put(stat, value);
			return value;
		}
		// couldn't find statistic on the sidebar
		if (statCache.containsKey(stat)) return statCache.get(stat);
		throw new IllegalArgumentException("Couldn't find '"+stat+"' on the sidebar");
	}
	
	private static String getLine(int index) {
		try {
			return Kuphack.stripColor(Kuphack.getScoreboard().get(index).getString()).strip();
		} catch (IndexOutOfBoundsException e) {
			return "";
		}
	}
	
	public static boolean isMushroomArc() {
		boolean value = Kuphack.getScoreboard().stream()
			.anyMatch(line -> line.getString().contains("Mushroom"));
		if (value) mushroom = System.currentTimeMillis();
		return value || (mushroom != 0 && System.currentTimeMillis() - mushroom < 1000);
	}
	
}
