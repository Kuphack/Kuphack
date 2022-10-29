package com.github.Vaapukkax.kuphack.flagclash.sheets.widgets.deprecated;

import java.math.BigInteger;

import org.apache.commons.lang3.text.WordUtils;

import com.github.Vaapukkax.kuphack.flagclash.FlagClash;
import com.github.Vaapukkax.kuphack.flagclash.sheets.widgets.ItemModifier;
import com.github.Vaapukkax.kuphack.flagclash.sheets.widgets.Widget;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Deprecated
public enum Gear implements Widget {

//	WOOD_PICKAXE(Items.WOODEN_PICKAXE, 5, ItemModifier.damage(3), ItemModifier.rate(1)),
//	
//	// [ROOT]
//	SWIFTSTONE_PICKAXE(-1, Items.STONE_PICKAXE, 260, ItemModifier.damage(3), ItemModifier.rate(1), ItemModifier.efficiency(1)),
//	DARKSTONE_PICKAXE(0, Items.STONE_PICKAXE, 200, ItemModifier.damage(4), ItemModifier.rate(1), ItemModifier.custom("Makes you faster when in darkness")),
//	LEATHER_CHESTPLATE(6, Items.LEATHER_CHESTPLATE, 50, ItemModifier.armor(2)),
//	
//	// > Swiftstone Pickaxe
//	IRON_PICKAXE(Items.IRON_PICKAXE, 22_000, ItemModifier.damage(4), ItemModifier.rate(1)),
//	SILVER_PICKAXE(1, Items.IRON_PICKAXE, 22_000, ItemModifier.damage(4), ItemModifier.rate(1), ItemModifier.efficiency(1), ItemModifier.custom("Makes you slower in darkness")),
//	
//	// > Swiftstone Pickaxe > Iron Pickaxe
//	DIAMOND_PICKAXE(Items.DIAMOND_PICKAXE, 1_000_000, ItemModifier.damage(4), ItemModifier.rate(1)),
//	BROKEN_PICKAXE(Items.IRON_HOE, 1_000_000, ItemModifier.damage(5), ItemModifier.rate(1), ItemModifier.custom("This is not a pickaxe!")),
//
//	// > Swiftstone Pickaxe > Silver Pickaxe
//	HOLYSTEEL_PICKAXE(Items.IRON_PICKAXE, 3_000_000, ItemModifier.damage(5), ItemModifier.rate(1), ItemModifier.efficiency(2), ItemModifier.custom("Makes you faster in light")),
//	SPARKLEGEM_PICKAXE(Items.DIAMOND_PICKAXE, 4_000_000, ItemModifier.damage(5), ItemModifier.rate(1), ItemModifier.armor(1), ItemModifier.custom("Gives armor when in your offhand")),
//	
//	// > Swiftstone Pickaxe > Silver Pickaxe > Sparklegem Pickaxe
//	CRYSTAL_PICKAXE(Items.DIAMOND_PICKAXE, 600_000_000, ItemModifier.damage(5), ItemModifier.rate(1), ItemModifier.armor(2), ItemModifier.efficiency(2), ItemModifier.custom("Gives armor when in your offhand")),
//	
//	// > Swiftstone Pickaxe > Silver Pickaxe > Sparklegem Pickaxe > Crystal Pickaxe
//	CRYSTALIUM_CODEX(Items.BOOK, FlagClash.toRealValue("1.000t"), ItemModifier.custom("I is written on the cover seems useless")),
//	
//	// > Darkstone Pickaxe
//	SHADESTEEL_PICKAXE(Items.IRON_PICKAXE, 200_000, ItemModifier.damage(4), ItemModifier.rate(1), ItemModifier.custom("Makes you faster when in darkness")),
//	DARKBORN_PICKAXE(Items.IRON_PICKAXE, 300_000, ItemModifier.damage(4), ItemModifier.rate(1), ItemModifier.custom("Makes you mine faster when in darkness")),
//	
//	// > Leather Chestplate
//	CARDBOARD_SHIELD(-2, Items.SHIELD, 1_000, ItemModifier.sturdiness(1), ItemModifier.fragmenting(5), ItemModifier.swiftness(-1)),
//	AGED_CHESTPLATE(Items.LEATHER_CHESTPLATE, 1_000, ItemModifier.armor(3)),
//	SHELL_PLATEMAIL(Items.LEATHER_CHESTPLATE, 5_000, ItemModifier.armor(3), ItemModifier.swiftness(0.5), ItemModifier.color(new Color(255, 189, 120))),
//	
//	// > Leather Chestplate > Cardboard Shield
//	IRON_SHIELD(Items.SHIELD, 100_000, ItemModifier.sturdiness(20), ItemModifier.fragmenting(2.5), ItemModifier.swiftness(-1), ItemModifier.color(DyeColor.WHITE)),
//	PINEWOOD_SHIELD(Items.SHIELD, 15_000, ItemModifier.sturdiness(5), ItemModifier.fragmenting(1), ItemModifier.color(DyeColor.BROWN)),
//
//	// > Leather Chestplate > Cardboard Shield > Iron Shield
//	CACTUS_SHIELD(-1, Items.SHIELD, 12_000_000, ItemModifier.sturdiness(15), ItemModifier.fragmenting(5), ItemModifier.swiftness(-1), ItemModifier.color(DyeColor.GREEN)),
//	COBALT_SHIELD(Items.SHIELD, 10_000_000, ItemModifier.sturdiness(25), ItemModifier.fragmenting(2), ItemModifier.swiftness(-1), ItemModifier.color(DyeColor.BLUE)),
//	
//	// > Leather Chestplate > Cardboard Shield > Iron Shield > Cobalt Shield
//	AMETHYST_SHIELD(Items.SHIELD, FlagClash.toRealValue("10.00b"), ItemModifier.sturdiness(25), ItemModifier.fragmenting(1.5), ItemModifier.swiftness(-1), ItemModifier.color(DyeColor.PINK)),
//	FLAME_SHIELD(Items.SHIELD, FlagClash.toRealValue("10.00b"), ItemModifier.sturdiness(20), ItemModifier.fragmenting(2), ItemModifier.swiftness(-1), ItemModifier.color(DyeColor.RED)),
//
//	// > Leather Chestplate > Cardboard Shield > Pinewood Shield
//	BAMBOO_SHIELD(Items.SHIELD, FlagClash.toRealValue("20.00m"), ItemModifier.sturdiness(6), ItemModifier.fragmenting(0.25), ItemModifier.color(DyeColor.LIME)),
//	SHROOMY_SHIELD(Items.SHIELD, FlagClash.toRealValue("15.00m"), ItemModifier.sturdiness(2), ItemModifier.fragmenting(0.5), ItemModifier.swiftness(-1), ItemModifier.color(DyeColor.RED)),
//	
//	// > Leather Chestplate > Shell Platemail
//	CHAINMAIL_CHESTPIECE(Items.CHAINMAIL_CHESTPLATE, 100_000, ItemModifier.armor(5)),
//	OPAL_PLATEBODY(Items.LEATHER_CHESTPLATE, 110_000, ItemModifier.armor(4), ItemModifier.swiftness(0.5), ItemModifier.color(new Color(162, 60, 222))),
//	SHELLMET(Items.LEATHER_HELMET, 80_000, ItemModifier.armor(2), ItemModifier.color(new Color(255, 189, 120))),
//
//	// > Leather Chestplate > Shell Platemail > Chainmail Chestpiece
//	IRON_CHESTPLATE(-1, Items.IRON_CHESTPLATE, 40_000_000, ItemModifier.armor(6)),
//	COBALT_BREASTPLATE(Items.LEATHER_CHESTPLATE, 80_000_000, ItemModifier.armor(5), ItemModifier.custom("Fire Protection"), ItemModifier.color(Color.BLUE)),
//
//	// > Leather Chestplate > Shell Platemail > Chainmail Chestpiece > Iron Chestplate
//	DARKSTEEL_PLATEARMOR(Items.NETHERITE_CHESTPLATE, FlagClash.toRealValue("3.00b"), ItemModifier.armor(7.5)),
//	OBSIDIAN_BREASTPLATE(Items.LEATHER_CHESTPLATE, FlagClash.toRealValue("4.00b"), ItemModifier.armor(8), ItemModifier.swiftness(-3), ItemModifier.color(Color.BLACK)),
//	
//	// > Leather Chestplate > Shell Platemail > Chainmail Chestpiece > Iron Chestplate > Obsidian Chestplate
//	DEEPSLATE_PLATEBODY(Items.LEATHER_CHESTPLATE, FlagClash.toRealValue("9.50b"), ItemModifier.armor(9.5), ItemModifier.swiftness(-3.5), ItemModifier.color(Color.DARK_GRAY)),
//	
//	// > Leather Chestplate > Shell Platemail > Chainmail Chestpiece > Iron Chestplate > Darksteel Platearmor
//	CRAPCLAW_SCALEMAIL(Items.LEATHER_CHESTPLATE, FlagClash.toRealValue("300.00b"), ItemModifier.armor(6.5), ItemModifier.rate(4.6), ItemModifier.custom("Increases attack speed of all weapons"), ItemModifier.color(new Color(0, 128, 0))),
//	
//	// > Leather Chestplate > Shell Platemail > Chainmail Chestpiece > Cobalt Breastplate
//	FROZENHEART_CHESTPLATE(Items.LEATHER_CHESTPLATE, 5_000_000, ItemModifier.armor(6), ItemModifier.custom("Fire Protection"), ItemModifier.color(new Color(128, 128, 255))),
//	ROYALBLOOD_CHESTPLATE(Items.LEATHER_CHESTPLATE, 5_000_000, ItemModifier.armor(5.5), ItemModifier.custom("You heal when you're bleeding"), ItemModifier.color(Color.RED)),
//	
//	// > Leather Chestplate > Shell Platemail > Chainmail Chestpiece > Cobalt Breastplate
//	SHIVERGEM_HELMET(Items.LEATHER_HELMET, FlagClash.toRealValue("5.00b"), ItemModifier.armor(2), ItemModifier.custom("Fire Protection"), ItemModifier.color(Color.BLUE)),
//	
//	// > Leather Chestplate > Shell Platemail > Opal Platebody
//	AMETHYST_HELMET(Items.LEATHER_HELMET, 2_000_000, ItemModifier.swiftness(1), ItemModifier.color(new Color(142, 13, 189))),
//	PHLOEM_CHAINMAIL(Items.LEATHER_CHESTPLATE, 2_000_000, ItemModifier.swiftness(0.5), ItemModifier.armor(5), ItemModifier.color(new Color(12, 145, 21))),
//
//	// > Leather Chestplate > Shell Platemail > Opal Platebody > Phloem Chainmail
//	TWISTED_BOOTS(Items.LEATHER_BOOTS, 400_000_000, ItemModifier.swiftness(0.5), ItemModifier.armor(2), ItemModifier.color(new Color(12, 145, 21))),
//	TANGLEGROOVE_CHESTPLATE(Items.LEATHER_CHESTPLATE, 1_000_000_000, ItemModifier.swiftness(0.5), ItemModifier.armor(7), ItemModifier.custom("Blast Protection"), ItemModifier.color(new Color(12, 145, 21))),
//
//	// > Leather Chestplate > Shell Platemail > Opal Platebody > Phloem Chainmail > Tanglegroove Chestplate
//	TANGLELEAF(Items.KELP, FlagClash.toRealValue("70.00q"), ItemModifier.swiftness(1), ItemModifier.custom("Can be held in offhand for extra speed")),
//
//	// > Leather Chestplate > Shell Platemail > Shellmet
//	TURTLE_SHELLMET(Items.TURTLE_HELMET, 2_000_000, ItemModifier.armor(2), ItemModifier.swiftness(-1), ItemModifier.custom("Projectile Protection"));
	
	DEPRECATED(null, 0);
	
	public final Item item;
	private final BigInteger cost;
	
	private final ItemModifier[] modifiers;
	
//	private int x = -1, y = -666;
//	private final int yOffset;
	
	private Gear(Item item, long cost, ItemModifier... modifiers) {
		this(0, item, BigInteger.valueOf(cost), modifiers);
	}
	
	private Gear(Item item, BigInteger cost, ItemModifier... modifiers) {
		this(0, item, cost, modifiers);
	}
	
	private Gear(int yOffset, Item item, long cost, ItemModifier... modifiers) {
		this(yOffset, item, BigInteger.valueOf(cost), modifiers);
	}
	private Gear(int yOffset, Item item, BigInteger cost, ItemModifier... modifiers) {
		this.item = item;
		this.cost = cost;
		this.modifiers = modifiers;
//		this.yOffset = yOffset;
	}
	
	public BigInteger getCost() {
		return cost;
	}
	
	public String toString() {
		return WordUtils.capitalize(name().replaceAll("_", " ").toLowerCase());
	}

//	@Override
//	public int getX() {
//		if (x == -1) {
//			x = GearTree.find(this).getPathsGoneThrough()*2;
//		}
//		return x;
//	}
//
//	@Override
//	public int getY() {
////		y = -666;
//		if (y == -666) {
//			GearTree parent = GearTree.find(this).getParent();
//			if (parent != null) {
//				int length = parent.getPaths().length;
//				
//				int offset = parent.indexOf(this)*length-((length*Math.max(1, length-1)/2));
//				
//				y = parent.getItem().getY()+offset;
//			} else y = 0;
//		}
//		return y+yOffset;
//	}
	
	public Object getModifier(String key) {
		for (ItemModifier modifier : modifiers) {
			if (modifier.getKey().equals(key)) {
				return modifier.getValue();
			}
		}
		return null;
	}

	@Override
	public ItemStack getIcon() {
		ItemStack stack = new ItemStack(item);
		stack.setCustomName(Text.literal(toString()+" "+FlagClash.toVisualValue(cost)));
//		
//		NbtList loreTag = new NbtList();
//		for (ItemModifier modifier : modifiers) {
//			if (modifier.getKey().isEmpty()) {
//				if (modifier.getValue().equals("Blast Protection")) {
//					stack.addEnchantment(Enchantments.BLAST_PROTECTION, 1);
//				} else if (modifier.getValue().equals("Projectile Protection")) {
//					stack.addEnchantment(Enchantments.PROJECTILE_PROTECTION, 1);
//				} else if (modifier.getValue().equals("Fire Protection")) {
//					stack.addEnchantment(Enchantments.FIRE_PROTECTION, 1);
//				}
//			}
//			if (modifier.getKey().equals("Color")) {
//				if (item == Items.SHIELD) {
//					stack.getOrCreateSubNbt("BlockEntityTag").put("Base", NbtInt.of(((DyeColor)modifier.getValue()).getId()));
//				} else stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY).put(ItemStack.COLOR_KEY, NbtInt.of(((Color)modifier.getValue()).getRGB()));
//			} else {
//				MutableText text = Text.of("  ").copy().append(modifier.toText());
//				text.setStyle(text.getStyle().withItalic(false));
//				loreTag.add(NbtString.of(Text.Serializer.toJson(text)));//NbtString.of("[{\"text\":\""+line+"\",\"italic\":\"false\",\"color\":\"#5c5a96\"}]"));
//			}
//		}
////		loreTag.add(NbtString.of(Text.Serializer.toJson(Text.literal("\u00a75 Price: "+FlagClash.toVisualValue(cost)))));
//		stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY).put(ItemStack.LORE_KEY, loreTag);
//		
//		Object efficiency = getModifier("Efficiency");
//		if (efficiency != null) stack.addEnchantment(Enchantments.EFFICIENCY, ((Number)efficiency).intValue());
//		
//		for (TooltipSection section : TooltipSection.values())
//			stack.addHideFlag(section);
//		
		return stack;
	}
	
}
