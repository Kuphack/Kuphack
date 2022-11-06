package com.github.vaapukkax.kuphack.flagclash.sheets;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.github.vaapukkax.kuphack.flagclash.sheets.widgets.Widget;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class SpreadsheetTab extends DrawableHelper {

    private final MinecraftClient client;
    private final SpreadSheetScreen screen;
    private final SpreadsheetTabType type;
    private final int index;
    private final Widget display;
    private final ItemStack icon;
    private final Text title;
    private final SpreadsheetWidget rootWidget;
    private final Map<Widget, SpreadsheetWidget> widgets = Maps.newLinkedHashMap();
    private double originX;
    private double originY;
    private int minPanX = Integer.MAX_VALUE;
    private int minPanY = Integer.MAX_VALUE;
    private int maxPanX = Integer.MIN_VALUE;
    private int maxPanY = Integer.MIN_VALUE;

    private boolean initialized;
    private boolean renderLines;
    
    public SpreadsheetTab(MinecraftClient client, SpreadSheetScreen screen, SpreadsheetTabType type, boolean renderLines, int index, Widget root, Widget display) {
        this.client = client;
        this.screen = screen;
        this.type = type;
        this.renderLines = renderLines;
        this.index = index;
        this.display = display;
        this.icon = display.getIcon();
        this.title = display.getTitle();
        this.rootWidget = new SpreadsheetWidget(this, client, root, 0, 0);
        this.addWidget(this.rootWidget, root);
    }

    public SpreadsheetTabType getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public SpreadsheetWidget getRoot() {
        return this.rootWidget;
    }

    public Text getTitle() {
        return this.title;
    }

    public Widget getDisplay() {
        return this.display;
    }
    
    
//    public int getMinX() {
//    	int x = Integer.MAX_VALUE;
//    	for (SpreadsheetWidget widget : getWidgets()) {
//    		if (widget.getX() < x) x = widget.getX();
//    	}
//    	return x;
//    }
//    
//    public int getMaxX() {
//    	int x = Integer.MIN_VALUE;
//    	for (SpreadsheetWidget widget : getWidgets()) {
//    		if (widget.getX() > x) x = widget.getX();
//    	}
//    	return x;
//    }
//    
//    public int getMinY() {
//    	int y = Integer.MAX_VALUE;
//    	for (SpreadsheetWidget widget : getWidgets()) {
//    		if (widget.getY() < y) y = widget.getY();
//    	}
//    	return y;
//    }
//    
//    public int getMaxY() {
//    	int y = Integer.MIN_VALUE;
//    	for (SpreadsheetWidget widget : getWidgets()) {
//    		if (widget.getY() > y) y = widget.getY();
//    	}
//    	return y;
//    }
    
    
    
    public void drawBackground(MatrixStack matrices, int x, int y, boolean selected) {
        this.type.drawBackground(matrices, this, x, y, selected, this.index);
    }

    public void drawIcon(int x, int y, ItemRenderer itemRenderer) {
        this.type.drawIcon(x, y, this.index, itemRenderer, this.icon);
    }

    public void render(MatrixStack matrices) {
        if (!this.initialized) {
            this.originX = 117 - (this.maxPanX + this.minPanX) / 2;
            this.originY = 56 - (this.maxPanY + this.minPanY) / 2;
            this.initialized = true;
        }
        matrices.push();
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(515);

        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
        
        if (renderLines) {
        	this.rootWidget.renderLines(matrices, i, j, true);
        	this.rootWidget.renderLines(matrices, i, j, false);
        }
        this.rootWidget.renderWidgets(matrices, i, j);
        RenderSystem.depthFunc(518);
        matrices.translate(0.0, 0.0, -950.0);
//        RenderSystem.colorMask(false, false, false, false);
//        SpreadsheetTab.fill(matrices, 4680, 2260, -4680, -2260, -16777216);
//        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(515);
        matrices.pop();
    }
    

//    public void renderLines(SpreadsheetWidget widget, MatrixStack matrices, int x, int y, boolean border) {
//        if (widget.getParent() != null) {
//            int n;
//            int i = x + widget.getParent().getX() + 13;
//            int j = x + widget.getParent().getX() + 26 + 4;
//            int k = y + widget.getParent().getY() + 13;
//            int l = x + widget.getX() + 13;
//            int m = y + widget.getY() + 13;
//            int n2 = n = border ? -16777216 : -1;
//            if (border) {
//                this.drawHorizontalLine(matrices, j, i, k - 1, n);
//                this.drawHorizontalLine(matrices, j + 1, i, k, n);
//                this.drawHorizontalLine(matrices, j, i, k + 1, n);
//                this.drawHorizontalLine(matrices, l, j - 1, m - 1, n);
//                this.drawHorizontalLine(matrices, l, j - 1, m, n);
//                this.drawHorizontalLine(matrices, l, j - 1, m + 1, n);
//                this.drawVerticalLine(matrices, j - 1, m, k, n);
//                this.drawVerticalLine(matrices, j + 1, m, k, n);
//            } else {
//                this.drawHorizontalLine(matrices, j, i, k, n);
//                this.drawHorizontalLine(matrices, l, j, m, n);
//                this.drawVerticalLine(matrices, j, m, k, n);
//            }
//        }
//        for (Widget widget2 : widget.getChildren()) {
//        	renderLines(widget2, matrices, x, y, border);
//        }
//    }

    public void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int x, int y) {
        matrices.push();
        matrices.translate(0.0, 0.0, -200.0);
//        SpreadsheetTab.fill(matrices, 0, 0, 234, 113, MathHelper.floor(this.alpha * 255.0f) << 24);
        int i = MathHelper.floor(this.originX);
        int j = MathHelper.floor(this.originY);
//        if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
            for (SpreadsheetWidget advancementWidget : this.widgets.values()) {
                if (!advancementWidget.shouldRender(i, j, mouseX, mouseY)) continue;
                advancementWidget.drawTooltip(matrices, i, j, 1-screen.alpha/1.5f, x, y);
                break;
            }
//        }
        matrices.pop();
    }

    public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY) {
        return this.type.isClickOnTab(screenX, screenY, this.index, mouseX, mouseY);
    }

    public static SpreadsheetTab create(MinecraftClient client, SpreadSheetScreen screen, boolean renderLines, int index, Widget root, Widget display) {

        for (SpreadsheetTabType advancementTabType : SpreadsheetTabType.values()) {
            if (index >= advancementTabType.getTabCount()) {
                index -= advancementTabType.getTabCount();
                continue;
            }
            return new SpreadsheetTab(client, screen, advancementTabType, renderLines, index, root, display);
        }
        return null;
    }

    public void move(double offsetX, double offsetY) {
        if (this.maxPanX - this.minPanX > 234) {
//        	this.originX += offsetX;
            this.originX = MathHelper.clamp(this.originX + offsetX, (double)(-(this.maxPanX - 234)), 0.0);
        }
        if (this.maxPanY - this.minPanY > 113) {
//        	this.originY += offsetY;
            this.originY = MathHelper.clamp(this.originY + offsetY, (double)(-(this.maxPanY - 113)), -minPanY);
        }
//        this.originX = MathHelper.clamp(this.originX, minPanX, maxPanX);
    }

    public SpreadsheetWidget addWidget(Widget widget, int x, int y) {
        SpreadsheetWidget spreadsheetWidget = new SpreadsheetWidget(this, this.client, widget, x, y);
        this.addWidget(spreadsheetWidget, widget);
        return spreadsheetWidget;
    }

    private void addWidget(SpreadsheetWidget spreadsheetWidget, Widget widget) {
        this.widgets.put(widget, spreadsheetWidget);
        int i = spreadsheetWidget.getX();
        int j = i + 28;
        int k = spreadsheetWidget.getY();
        int l = k + 27;
        this.minPanX = Math.min(this.minPanX, i);
        this.maxPanX = Math.max(this.maxPanX, j);
        this.minPanY = Math.min(this.minPanY, k);
        this.maxPanY = Math.max(this.maxPanY, l);
    }

    @Nullable
    public SpreadsheetWidget getWidget(Widget widget) {
        return this.widgets.get(widget);
    }

    public SpreadSheetScreen getScreen() {
        return this.screen;
    }

	public Collection<SpreadsheetWidget> getWidgets() {
		return widgets.values();
	}
}

