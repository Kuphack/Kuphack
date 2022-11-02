package com.github.vaapukkax.kuphack.flagclash.sheets.widgets.deprecated;

import com.github.vaapukkax.kuphack.flagclash.sheets.widgets.Tree;

@Deprecated
public class GearTree extends Tree<Gear> {

//	public static final GearTree ROOT = new GearTree(Gear.WOOD_PICKAXE,
//		new GearTree(Gear.SWIFTSTONE_PICKAXE,
//			new GearTree(Gear.IRON_PICKAXE,
//				new GearTree(Gear.DIAMOND_PICKAXE),
//				new GearTree(Gear.BROKEN_PICKAXE)
//			),
//			new GearTree(Gear.SILVER_PICKAXE,
//				new GearTree(Gear.HOLYSTEEL_PICKAXE),
//				new GearTree(Gear.SPARKLEGEM_PICKAXE,
//					new GearTree(Gear.CRYSTAL_PICKAXE,
//						new GearTree(Gear.CRYSTALIUM_CODEX)
//					)
//				)
//			)
//		),
//		new GearTree(Gear.DARKSTONE_PICKAXE,
//			new GearTree(Gear.SHADESTEEL_PICKAXE),
//			new GearTree(Gear.DARKBORN_PICKAXE)
//		),
//		new GearTree(Gear.LEATHER_CHESTPLATE,
//			new GearTree(Gear.CARDBOARD_SHIELD,
//				new GearTree(Gear.IRON_SHIELD,
//					new GearTree(Gear.CACTUS_SHIELD,
//						new GearTree(Gear.BAMBOO_SHIELD),
//						new GearTree(Gear.SHROOMY_SHIELD)
//					),
//					new GearTree(Gear.COBALT_SHIELD,
//						new GearTree(Gear.AMETHYST_SHIELD),
//						new GearTree(Gear.FLAME_SHIELD)
//					)
//				),
//				new GearTree(Gear.PINEWOOD_SHIELD)
//			),
//			new GearTree(Gear.AGED_CHESTPLATE),
//			new GearTree(Gear.SHELL_PLATEMAIL,
//				new GearTree(Gear.CHAINMAIL_CHESTPIECE,
//					new GearTree(Gear.IRON_CHESTPLATE,
//						new GearTree(Gear.DARKSTEEL_PLATEARMOR,
//							new GearTree(Gear.CRAPCLAW_SCALEMAIL)
//						),
//						new GearTree(Gear.OBSIDIAN_BREASTPLATE,
//							new GearTree(Gear.DEEPSLATE_PLATEBODY)
//						)
//					),
//					new GearTree(Gear.COBALT_BREASTPLATE,
//						new GearTree(Gear.FROZENHEART_CHESTPLATE,
//							new GearTree(Gear.SHIVERGEM_HELMET)
//						),
//						new GearTree(Gear.ROYALBLOOD_CHESTPLATE)
//					)
//				),
//				new GearTree(Gear.OPAL_PLATEBODY,
//					new GearTree(Gear.AMETHYST_HELMET),
//					new GearTree(Gear.PHLOEM_CHAINMAIL,
//						new GearTree(Gear.TWISTED_BOOTS),
//						new GearTree(Gear.TANGLEGROOVE_CHESTPLATE,
//							new GearTree(Gear.TANGLELEAF)
//						)
//					)
//				),
//				new GearTree(Gear.SHELLMET,
//					new GearTree(Gear.TURTLE_SHELLMET)
//				)
//			)
//		)
//	);
	
	private GearTree(Gear gear, GearTree... paths) {
		super(gear, paths);
	}
	
//	public int getPathsGoneThrough() {
//		return super.getPathsGoneThrough(ROOT);
//	}
//	
//	public GearTree getParent() {
//		return (GearTree) getParent(ROOT);
//	}
//
//	public static GearTree find(Gear gear) {
//		return (GearTree) find(gear, ROOT);
//	}

}
