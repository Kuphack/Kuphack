package com.github.Vaapukkax.kuphack.flagclash.widgets;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import com.github.Vaapukkax.kuphack.Kuphack;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public enum GoldFountain implements Widget {

	VIPp (0.25, 20, 3),
	MVP  (0.5, 20, 6),
	MVPp (1.0, 10, 4),
	MVPpp(0.3, 30, 8),
	MFN  (0.3, 30, 8);
	
	public final double percentage;
	public final int minutes;
	public final int size;

	private GoldFountain(double percentage, int minutes, int size) {
		this.percentage = percentage;
		this.minutes = minutes;
		this.size = size;
	}
	
	public String getRank() {
		String name = name().replaceAll("p", "+");
		return Character.toUpperCase(name.charAt(0))+name.substring(1).toLowerCase();
	}
	
	public String getPercentage() {
		return ((int)(percentage*100))+"%";
	}
	
	public String toString() {
		return getPercentage();
	}
	
	private int x = -1;
	
	@Override
	public int getX() {
		if (x == -1) {
			x = Arrays.asList(values()).indexOf(this);
		}
		return x;
	}

	@Override
	public int getY() {
		return 0;
	}

	@Override
	public ItemStack getIcon() {
		ItemStack stack = new ItemStack(Items.GOLD_BLOCK);
		stack.setCustomName(Text.literal("\u00a76Gold Fountain"));
		return stack;
	}

	@Override
	public List<Text> getDescription() {
		return Arrays.asList(
			Kuphack.color(
				Text.literal(" Place down to spawn a GPS increasing fountain!"),
				Color.decode("#5C5A96")
			), Text.of(" "),
			
			Kuphack.color(
				Text.literal(" +"+getPercentage()),
				Color.decode("#FFFF55")
			),
			Kuphack.color(
				Text.literal(" "+minutes+" Minutes"),
				Color.decode("#FFFF55")
			),
			Kuphack.color(
				Text.literal(" "+size+" Size"),
				Color.decode("#FFFF55")
			), Text.of(" "),
			
			Kuphack.color(
				Text.literal(" Requires: "),
				Color.decode("#C3FFFF")
			).append(
				Kuphack.color(
					Text.literal("["+getRank()+"]"),
					Color.decode("#FFFFFF")
				)
			)
		);
	}

}
