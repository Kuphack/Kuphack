package com.github.Vaapukkax.kuphack.flagclash.sheets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.Vaapukkax.kuphack.flagclash.sheets.widgets.Widget;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
public class SpreadsheetWidget extends DrawableHelper {
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
//    private static final int field_32286 = 26;
//    private static final int field_32287 = 0;
//    private static final int field_32288 = 200;
//    private static final int field_32289 = 26;
//    private static final int ICON_OFFSET_X = 8;
//    private static final int ICON_OFFSET_Y = 5;
//    private static final int ICON_SIZE = 26;
//    private static final int field_32293 = 3;
//    private static final int field_32294 = 5;
//    private static final int TITLE_OFFSET_X = 32;
//    private static final int TITLE_OFFSET_Y = 9;
//    private static final int TITLE_MAX_WIDTH = 163;
//    private static final int[] SPLIT_OFFSET_CANDIDATES = new int[]{0, 10, -10, 25, -25};
    private final SpreadsheetTab tab;
    private final Widget widget;
    private final OrderedText title;
    private final int width;
    private final List<OrderedText> description;
    private final MinecraftClient client;
    private final List<SpreadsheetWidget> children = Lists.newArrayList();
    private final int x, y;
    
    private SpreadsheetWidget parent;

    public SpreadsheetWidget(SpreadsheetTab tab, MinecraftClient client, Widget widget, int x, int y) {
    	this.tab = tab;
        this.widget = widget;
        this.client = client;

        this.x = x * 28;
        this.y = y * 27;
        
        this.title = widget.getTitle().asOrderedText();//Language.getInstance().reorder(client.textRenderer.trimToWidth(widget.getTitle(), 163));
//        int i = widget.getRequirementCount();
//        int j = String.valueOf(i).length();
//        int k = i > 1 ? client.textRenderer.getWidth("  ") + client.textRenderer.getWidth("0") * j * 2 + client.textRenderer.getWidth("/") : 0;
        int l = 29 + client.textRenderer.getWidth(this.title);
        this.description = asOrdered(widget.getDescription());//getLore(stack);//Language.getInstance().reorder(this.wrapDescription(Texts.setStyleIfAbsent(display.getDescription().shallowCopy(), Style.EMPTY.withColor(display.getFrame().getTitleFormat())), l));
        for (OrderedText orderedText : this.description) {
            l = Math.max(l, client.textRenderer.getWidth(orderedText));
        }
        this.width = l + 3 + 5;
    }
    
    private static List<OrderedText> asOrdered(List<Text> list) {
    	ArrayList<OrderedText> orderedList = new ArrayList<>();
    	if (list == null) return orderedList;
    	
    	for (Text text : list) {
    		orderedList.add(text.asOrderedText());
    	}
    	return orderedList;
    }

//    private static float getMaxWidth(TextHandler textHandler, List<StringVisitable> lines) {
//        return (float)lines.stream().mapToDouble(textHandler::getWidth).max().orElse(0.0);
//    }

//    private List<StringVisitable> wrapDescription(Text text, int width) {
//        TextHandler textHandler = this.client.textRenderer.getTextHandler();
//        List<StringVisitable> list = null;
//        float f = Float.MAX_VALUE;
//        for (int i : SPLIT_OFFSET_CANDIDATES) {
//            List<StringVisitable> list2 = textHandler.wrapLines(text, width - i, Style.EMPTY);
//            float g = Math.abs(SpreadsheetWidget.getMaxWidth(textHandler, list2) - (float)width);
//            if (g <= 10.0f) {
//                return list2;
//            }
//            if (!(g < f)) continue;
//            f = g;
//            list = list2;
//        }
//        return list;
//    }

    SpreadsheetWidget getParent() {
    	if (parent == null) {
	        for (SpreadsheetWidget widget : this.tab.getWidgets()) {
	        	if (widget.children.contains(this)) {
	        		this.parent = widget;
	        		return widget;
	        	}
	        }
	    }
        return parent;
    }

    public void renderLines(MatrixStack matrices, int x, int y, boolean border) {
    	if (getParent() != null) {
            int i = x + parent.getX() + 13;
            int j = x + parent.getX() + 26 + 4;
            int k = y + parent.getY() + 13;
            int l = x + this.getX() + 13;
            int m = y + this.getY() + 13;
            int n = border ? -16777216 : -1;
            if (border) {
                this.drawHorizontalLine(matrices, j, i, k - 1, n);
                this.drawHorizontalLine(matrices, j + 1, i, k, n);
                this.drawHorizontalLine(matrices, j, i, k + 1, n);
                this.drawHorizontalLine(matrices, l, j - 1, m - 1, n);
                this.drawHorizontalLine(matrices, l, j - 1, m, n);
                this.drawHorizontalLine(matrices, l, j - 1, m + 1, n);
                this.drawVerticalLine(matrices, j - 1, m, k, n);
                this.drawVerticalLine(matrices, j + 1, m, k, n);
            } else {
                this.drawHorizontalLine(matrices, j, i, k, n);
                this.drawHorizontalLine(matrices, l, j, m, n);
                this.drawVerticalLine(matrices, j, m, k, n);
            }
        }
        for (SpreadsheetWidget spreadsheetWidget : this.children) {
            spreadsheetWidget.renderLines(matrices, x, y, border);
        }
    }

    public Widget getWidget() {
        return this.widget;
    }
    
    public int getWidth() {
        return this.width;
    }

    public void addChild(SpreadsheetWidget widget) {
        this.children.add(widget);
    }

    public void renderWidgets(MatrixStack matrices, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);

        AdvancementFrame frame = AdvancementFrame.TASK;
        int sprite = AdvancementObtainedStatus.OBTAINED.getSpriteIndex();
        this.drawTexture(matrices, x + this.getX() + 3, y + this.getY(), frame.getTextureV(), 128 + sprite * 26, 26, 26);
        this.client.getItemRenderer().renderInGui(widget.getIcon(), x + this.getX() + 8, y + this.getY() + 5);

        for (SpreadsheetWidget spreadsheetWidget : children) {
//        	Widget widget = spreadsheetWidget.getWidget();
//        	if (widget == Perk.ECONOMIC) System.out.println(widget);
        	spreadsheetWidget.renderWidgets(matrices, x, y);
        }
    }
    
    public void drawTooltip(MatrixStack matrices, int originX, int originY, float alpha, int x, int y) {
    	
//        AdvancementObtainedStatus advancementObtainedStatus3;
        AdvancementObtainedStatus advancementObtainedStatus2;
        AdvancementObtainedStatus advancementObtainedStatus;
        boolean bl = x + originX + this.getX() + this.width + 26 >= this.tab.getScreen().width;
        boolean bl2 = false;//113 - originY - this.getY() - 26 <= 6 + this.description.size() * this.client.textRenderer.fontHeight;
        
        float f = 0.0f;
        int j = MathHelper.floor(f * (float)this.width);
        
        if (f >= 1.0f) {
            j = this.width / 2;
            advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
//            advancementObtainedStatus3 = AdvancementObtainedStatus.OBTAINED;
        } else if (j < 2) {
            j = this.width / 2;
            advancementObtainedStatus = AdvancementObtainedStatus.UNOBTAINED;
            advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
//            advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
        } else if (j > this.width - 2) {
            j = this.width / 2;
            advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
//            advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
        } else {
            advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
            advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
//            advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
        }
        int k = this.width - j;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        RenderSystem.enableBlend();
        int l = originY + this.getY();
        int m = bl ? originX + this.getX() - this.width + 26 + 6 : originX + this.getX();
        int n = 32 + this.description.size() * this.client.textRenderer.fontHeight;
        if (!this.description.isEmpty()) {
            if (bl2) {
                this.renderDescriptionBackground(matrices, m, l + 26 - n, this.width, n, 10, 200, 26, 0, 52);
            } else {
                this.renderDescriptionBackground(matrices, m, l, this.width, n, 10, 200, 26, 0, 52);
            }
        }
        this.drawTexture(matrices, m, l, 0, advancementObtainedStatus.getSpriteIndex() * 26, j, 26);
        this.drawTexture(matrices, m + j, l, 200 - k, advancementObtainedStatus2.getSpriteIndex() * 26, k, 26);
//        this.drawTexture(matrices, originX + this.x + 3, originY + this.y, this.display.getFrame().getTextureV(), 128 + advancementObtainedStatus3.getSpriteIndex() * 26, 26, 26);
        
        if (bl) {
            this.client.textRenderer.drawWithShadow(matrices, this.title, (float)(m + 5), (float)(originY + this.getY() + 9), -1);
//            if (string != null) {
//                this.client.textRenderer.drawWithShadow(matrices, string, (float)(originX + this.x - i), (float)(originY + this.y + 9), -1);
//            }
        } else {
            this.client.textRenderer.drawWithShadow(matrices, this.title, (float)(originX + this.getX() + 32), (float)(originY + this.getY() + 9), -1);
//            if (string != null) {
//                this.client.textRenderer.drawWithShadow(matrices, string, (float)(originX + this.x + this.width - i - 5), (float)(originY + this.y + 9), -1);
//            }
        }
        if (bl2) {
            for (int o = 0; o < this.description.size(); ++o) {
                this.client.textRenderer.draw(matrices, this.description.get(o), (float)(m + 5), (float)(l + 26 - n + 7 + o * this.client.textRenderer.fontHeight), -5592406);
            }
        } else {
            for (int o = 0; o < this.description.size(); ++o) {
                this.client.textRenderer.draw(matrices, this.description.get(o), (float)(m + 5), (float)(originY + this.getY() + 9 + 17 + o * this.client.textRenderer.fontHeight), -5592406);
            }
        }
        this.client.getItemRenderer().renderInGui(this.widget.getIcon(), originX + this.getX() + 8, originY + this.getY() + 5);
    }

    /**
     * Renders the description background.
     * 
     * @implNote This splits the area into 9 parts (4 corners, 4 edges and 1
     * central box) and draws each of them.
     */
    protected void renderDescriptionBackground(MatrixStack matrices, int x, int y, int width, int height, int cornerSize, int textureWidth, int textureHeight, int u, int v) {
        this.drawTexture(matrices, x, y, u, v, cornerSize, cornerSize);
        this.drawTextureRepeatedly(matrices, x + cornerSize, y, width - cornerSize - cornerSize, cornerSize, u + cornerSize, v, textureWidth - cornerSize - cornerSize, textureHeight);
        this.drawTexture(matrices, x + width - cornerSize, y, u + textureWidth - cornerSize, v, cornerSize, cornerSize);
        this.drawTexture(matrices, x, y + height - cornerSize, u, v + textureHeight - cornerSize, cornerSize, cornerSize);
        this.drawTextureRepeatedly(matrices, x + cornerSize, y + height - cornerSize, width - cornerSize - cornerSize, cornerSize, u + cornerSize, v + textureHeight - cornerSize, textureWidth - cornerSize - cornerSize, textureHeight);
        this.drawTexture(matrices, x + width - cornerSize, y + height - cornerSize, u + textureWidth - cornerSize, v + textureHeight - cornerSize, cornerSize, cornerSize);
        this.drawTextureRepeatedly(matrices, x, y + cornerSize, cornerSize, height - cornerSize - cornerSize, u, v + cornerSize, textureWidth, textureHeight - cornerSize - cornerSize);
        this.drawTextureRepeatedly(matrices, x + cornerSize, y + cornerSize, width - cornerSize - cornerSize, height - cornerSize - cornerSize, u + cornerSize, v + cornerSize, textureWidth - cornerSize - cornerSize, textureHeight - cornerSize - cornerSize);
        this.drawTextureRepeatedly(matrices, x + width - cornerSize, y + cornerSize, cornerSize, height - cornerSize - cornerSize, u + textureWidth - cornerSize, v + cornerSize, textureWidth, textureHeight - cornerSize - cornerSize);
    }

    /**
     * Draws a textured rectangle repeatedly to cover the area of {@code
     * width} and {@code height}. The last texture is clipped to fit the area.
     */
    protected void drawTextureRepeatedly(MatrixStack matrices, int x, int y, int width, int height, int u, int v, int textureWidth, int textureHeight) {
        for (int i = 0; i < width; i += textureWidth) {
            int j = x + i;
            int k = Math.min(textureWidth, width - i);
            for (int l = 0; l < height; l += textureHeight) {
                int m = y + l;
                int n = Math.min(textureHeight, height - l);
                this.drawTexture(matrices, j, m, u, v, k, n);
            }
        }
    }

    public boolean shouldRender(int originX, int originY, int mouseX, int mouseY) {
//        if (this.display.isHidden() && (this.progress == null || !this.progress.isDone())) {
//            return false;
//        }
        int i = originX + this.getX();
        int j = i + 26;
        int k = originY + this.getY();
        int l = k + 26;
        return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
    }

    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }

	public List<SpreadsheetWidget> getChildren() {
		return Collections.unmodifiableList(children);
	}
}