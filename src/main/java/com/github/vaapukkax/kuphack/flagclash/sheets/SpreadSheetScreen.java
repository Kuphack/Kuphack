package com.github.vaapukkax.kuphack.flagclash.sheets;

import java.util.Map;

import com.github.vaapukkax.kuphack.flagclash.sheets.widgets.Tree;
import com.github.vaapukkax.kuphack.flagclash.sheets.widgets.Widget;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value = EnvType.CLIENT)
public class SpreadSheetScreen extends Screen {
	
	private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
	private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
	
	public static final int WINDOW_WIDTH = 252;
	public static final int WINDOW_HEIGHT = 140;
	public static final int PAGE_WIDTH = 234;
	public static final int PAGE_HEIGHT = 113;
	public static final int field_32302 = 16;
	public static final int field_32303 = 16;
	public static final int field_32304 = 14;
	public static final int field_32305 = 7;
	
	protected float alpha;
	
	private static final Text EMPTY_TEXT = Text.literal("There is no spreadsheet! :(");
	private final Map<Widget, SpreadsheetTab> tabs = Maps.newLinkedHashMap();
	
	private SpreadsheetTab selectedTab;
	private boolean movingTab;
	
	public SpreadSheetScreen() {
		super(Text.of("Spreadsheet"));
	}

	@Override
	protected void init() {
//      // Shop Layout
//		ShopLayoutItem rootItem = LayoutTree.ROOT.getItem();
//		SpreadsheetTab layoutTab = SpreadsheetTab.create(client, this, true, 0, rootItem, new DisplayWidget("Shop Layout", Items.TNT));
//		this.generateWidgets(layoutTab, LayoutTree.ROOT, layoutTab.getRoot(), 0);
//		this.tabs.put(rootItem, layoutTab);
//		
//		// Weapons
//		Weapon rootWeapon = WeaponTree.ROOT.getItem();
//		SpreadsheetTab weaponTab = SpreadsheetTab.create(client, this, true, 0, rootWeapon, new DisplayWidget("Weapons", Items.GOLDEN_SWORD));
//		generateWeaponWidgets(weaponTab, WeaponTree.ROOT, weaponTab.getRoot());
//		this.tabs.put(rootWeapon, weaponTab);

//		// Charms
//		Charm rootCharm = Charm.values()[0];
//		SpreadsheetWidget lastWidget = null;
//		SpreadsheetTab charmTab = SpreadsheetTab.create(client, this, false, 0, rootCharm, new DisplayWidget("Charms", Items.ENDER_EYE));
//		for (Charm charm : Charm.values()) {
//			SpreadsheetWidget widget = charm == rootCharm ? charmTab.getRoot() : charmTab.addWidget(charm);
//			if (lastWidget != null) lastWidget.addChild(widget);
//			lastWidget = widget;	
//		}
//		this.tabs.put(rootCharm, charmTab);
//		
//		selectedTab = this.tabs.values().iterator().next();
	}
	
	protected <T extends Widget> void generateWidgets(SpreadsheetTab tab, Tree<T> root, SpreadsheetWidget lastWidget, int x) {
		int y = 0;
		for (Tree<T> path : root.getPaths()) {
			SpreadsheetWidget widget = tab.addWidget(path.getItem(), x, y);
			lastWidget.addChild(widget);
			this.generateWidgets(tab, path, widget, x+1);
			y++;
		}
 	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && this.tabs.size() > 1) {
			for (SpreadsheetTab advancementTab : this.tabs.values()) {
				if (!advancementTab.isClickOnTab(0, 0, mouseX, mouseY))
					continue;
                this.selectedTab = advancementTab;
				break;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.client.options.advancementsKey.matchesKey(keyCode, scanCode)) {
			this.client.setScreen(null);
			this.client.mouse.lockCursor();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int i = (this.width - 252) / 2;
		int j = (this.height - 140) / 2;
		
		
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
		this.renderBackground(matrices);
		
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f);
		this.drawAdvancementTree(matrices, mouseX, mouseY, i, j);
		
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null) {
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha/2);
			this.renderBackground(matrices);
		}
		
		this.drawWindow(matrices, i, j);
		this.drawWidgetTooltip(matrices, mouseX, mouseY, i, j);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button != 0) {
			this.movingTab = false;
			return false;
		}
		if (!this.movingTab) {
			this.movingTab = true;
		} else if (this.selectedTab != null) {
			this.selectedTab.move(deltaX, deltaY);
		}
		return true;
	}

	private void drawAdvancementTree(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
		if (selectedTab == null) {
			SpreadSheetScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, EMPTY_TEXT,
				this.width / 2, this.height / 2, 0xFFFFFF
			);
			return;
		}
		MatrixStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.push();
		matrixStack.translate(x + 9, y + 18, 0.0);
		RenderSystem.applyModelViewMatrix();
		selectedTab.render(matrices);
		matrixStack.pop();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
	}

	public void drawWindow(MatrixStack matrices, int x, int y) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);

		if (this.tabs.size() > 1) {
			RenderSystem.setShaderTexture(0, TABS_TEXTURE);
			RenderSystem.defaultBlendFunc();
			for (SpreadsheetTab spreadsheetTab : this.tabs.values()) {
				spreadsheetTab.drawIcon(matrices, 0, 0, this.itemRenderer);
			}
			RenderSystem.disableBlend();
		}
	}

	private void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		if (this.selectedTab != null) {
			MatrixStack matrixStack = RenderSystem.getModelViewStack();
			matrixStack.push();
			matrixStack.translate(x + 9, y + 18, 400.0);
			RenderSystem.applyModelViewMatrix();
			RenderSystem.enableDepthTest();
			this.selectedTab.drawWidgetTooltip(matrices, mouseX - x - 9, mouseY - y - 18, x, y);
			RenderSystem.disableDepthTest();
			matrixStack.pop();
			RenderSystem.applyModelViewMatrix();
		}
		boolean bl = false;
		if (this.tabs.size() > 1) {
			for (SpreadsheetTab spreadsheetTab : this.tabs.values()) {
				if (!spreadsheetTab.isClickOnTab(0, 0, mouseX, mouseY))
					continue;
				this.renderTooltip(matrices, spreadsheetTab.getTitle(), spreadsheetTab.getType().getTabX(spreadsheetTab.getIndex())+20, mouseY+10);
				bl = true;
			}
		}
		
		float alphaDestination = bl ? 1f : 0f;
		this.alpha += (alphaDestination - alpha) * 0.2f;
	}

	public SpreadsheetWidget getSpreadsheetWidget(Widget widget) {
		for (SpreadsheetTab tab : tabs.values()) {
			for (SpreadsheetWidget spreadsheetWidget : tab.getWidgets()) {
				if (spreadsheetWidget.getWidget().equals(widget))
					return spreadsheetWidget;
			}
		}
		return null;
	}
	
}