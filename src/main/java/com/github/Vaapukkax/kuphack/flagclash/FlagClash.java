package com.github.Vaapukkax.kuphack.flagclash;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;

import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.kuphack.flagclash.widgets.Quest;

import net.minecraft.text.Text;

public class FlagClash {

	public static BigInteger upgradePrice = BigInteger.ZERO;
	
	private static final HashMap<String, String> statCache = new HashMap<>();
	private static final String[] valueSuffix = {
		"k",
		"m",
		"b",
		"t",
		"q",
		"p",
		"s",
		"e",
		"o",
		"n",
		"d",
		"u",
		"v",
		"h",
		"i",
		"j",
		"l",
		"r",
		"w",
		"x",
		"y",
		"z"
	};
	
	public static double getUpgradeTime() {
		BigInteger gps = FlagClash.getGPS();
		BigInteger gold = FlagClash.getCoins();
		if (upgradePrice == null || upgradePrice.equals(BigInteger.ZERO)) return -1;
		
		return Math.max(0, new BigDecimal((upgradePrice.subtract(gold)).divide(gps)).divide(FlagClash.getMultiplier(), MathContext.DECIMAL128).doubleValue());
	}
	
	public static String timeAsString(double time) {
		if (time < 60) return Kuphack.round(time)+"s";
		String text = Kuphack.round(time/60d%60d)+"m";
		if (time > 3600) text = Kuphack.round(time/60/60)+"h";
		return text;
	}
	
	public static Quest getQuest() {
		return Quest.fromName(getStat("Quest").split("\\s")[0]);
	}
	
	// TODO needs a recode, this code sucks no cap
	public static BigInteger toRealValue(String s) {
		try {
			return new BigInteger(s);
		} catch (Exception e) {
			try {
				String suffix = (s.charAt(s.length()-1)+"");
				double d = Double.parseDouble(s.substring(0, s.length()-1));
	
				int io = (Arrays.asList(valueSuffix).indexOf(suffix)+1);
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
		String is = value.toString();
		int io = ((is.length()-1)/3)-1;
		
		if (io < 0) return is.toString();
		
		String suffix = valueSuffix[io];
			
		String p = is.substring(0, is.length()-io*3);
		String prefix = is.substring(0, p.length()/2)+"."+is.substring(p.length()/2, p.length()).substring(0, 2);
		return prefix + suffix;
	}
	
	public static BigInteger getCoins() {
		return toRealValue(getStat("Gold").split("\\s")[0]);
	}
		
	public static String getStat(String stat) {
		if (Kuphack.getServer() != Servers.FLAGCLASH)
			return statCache.getOrDefault(stat, "0");
		for (Text line : Kuphack.getScoreboard()) {
			String cutLine = line.getString().replaceAll("(^\\s+)|(\\s+$)", "");
			if (!cutLine.contains(stat)) continue;
			
			String value = Kuphack.stripColor(cutLine.substring(cutLine.indexOf(stat+": ")+(stat+": ").length()));
			statCache.put(stat, value);
			return value;
		}
		// couldn't find statistic on the sidebar
		if (statCache.containsKey(stat)) return statCache.get(stat);
		throw new IllegalArgumentException("Couldn't find '"+stat+"' on the sidebar");
	}
	
	public static BigInteger getGPS() {
		try {
			BigInteger integer = toRealValue(getStat("Gps").split("\\s")[0]);
			if (integer.equals(BigInteger.ZERO)) return BigInteger.ONE;
			return integer;
		} catch (Exception e) {
			Kuphack.error(e);
			return BigInteger.ONE;
		}
	}
	public static BigDecimal getMultiplier() {
		try {
			String raw = getStat("Gps");
			if (raw.isEmpty()) return BigDecimal.ONE;
			String[] split = raw.split("\\s+");
			if (split.length <= 3) return BigDecimal.ONE;
			
			int index = split.length == 4 ? 2 : 1;
			
			String percentage = split[index];
			String intString = percentage.substring(0, percentage.length()-1);
			if (intString.isEmpty()) intString = "1";
			
			return BigDecimal.valueOf(Integer.parseInt(intString)/100d);
		} catch (Exception e) {
			Kuphack.error(e);
			return BigDecimal.ONE;
		}
	}
	
}
