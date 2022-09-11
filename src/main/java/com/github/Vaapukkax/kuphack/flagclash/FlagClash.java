package com.github.Vaapukkax.kuphack.flagclash;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.kuphack.flagclash.widgets.Quest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FlagClash {

	public static BigInteger upgradePrice = BigInteger.ZERO;
	
	private static final String[] ve = {
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
		return time > 60 ? Kuphack.round(time/60d)+"m" : Kuphack.round(time)+"s";
	}
	
	public static Quest getQuest() {
		return Quest.fromName(getStat("Quest").split("\\s")[0]);
	}
	
	public static BigInteger toRealValue(String s) {
		try {
			return new BigInteger(s);
		} catch (Exception e) {
			try {
				String suffix = (s.charAt(s.length()-1)+"");
				double d = Double.parseDouble(s.substring(0, s.length()-1));
	
				int io = (Arrays.asList(ve).indexOf(suffix)+1);
				StringBuilder sb = new StringBuilder("1");
				for (int i = 0; i < io; i++) sb.append("000");
				
				return new BigDecimal(d).multiply(new BigDecimal(sb.toString())).toBigInteger();
			} catch (Exception e2) {
				Kuphack.LOGGER.error("toRealValue("+(s == null ? "null" : "\""+s+"\"")+")");
				error(e2);
				return BigInteger.ZERO;
			}
		}
	}
	public static String toVisualValue(BigInteger value) {
		String is = value.toString();
		
		int io = ((is.length()-1)/3)-1;
		
		if (io < 0) {
			return is.toString();
		} else {
			String suffix = ve[io];
			
			String p = is.substring(0, is.length()-io*3);
			String prefix = is.substring(0, p.length()/2)+"."+is.substring(p.length()/2, p.length()).substring(0, 2);
			return prefix+suffix;
		}
	}
	
	public static BigInteger getCoins() {
		return toRealValue(getStat("Gold").split("\\s")[0]);
	}
		
	private static HashMap<String, String> oldStat = new HashMap<>();
	public static String getStat(String stat) {
		if (Kuphack.getServer() != Servers.FLAGCLASH) return oldStat.containsKey(stat) ? oldStat.get(stat) : "0";
		List<Text> sb = Kuphack.getScoreboard();
		for (int i = 0; i < sb.size(); i++) {
			String sfs = sb.get(i).getString().replaceAll("(^\\s*)|(\\s*$)", "");
			if (sfs.contains(stat)) {
				String s = sfs.substring(sfs.indexOf(stat+": ")+(stat+": ").length());
				s = s.substring(0, s.length()-4);
//				if (s.indexOf(" ") != -1) s = s.substring(0, s.indexOf(" "));
				oldStat.put(stat, s);
				return s;
			}
		}
		return oldStat.containsKey(stat) ? oldStat.get(stat) : "";
	}
	
	public static BigInteger getGPS() {
		try {
			BigInteger integer = toRealValue(getStat("Gps").split("\\s")[0]);
			if (integer.equals(BigInteger.ZERO)) return BigInteger.ONE;
			return integer;
		} catch (Exception e) {
			error(e);
			return BigInteger.ONE;
		}
	}
	public static BigDecimal getMultiplier() {
//		if (true) return BigDecimal.ONE;
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
			error(e);
			return BigDecimal.ONE;
		}
	}
	
	private static void error(Throwable throwable) {
		MinecraftClient c = MinecraftClient.getInstance();
		if (c.player != null) c.player.sendMessage(Text.of("\u00a7c[Kuphack] Error occured related to FlagClash (Printed to console)"), true);
		throwable.printStackTrace();
	}
	
}
