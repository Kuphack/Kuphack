/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package com.github.Vaapukkax.kuphack.finder;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.Vaapukkax.minehut.Category;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class CategoryDropdownWidget extends ClickableWidget {

	private static final List<Category> categories = Collections.unmodifiableList(Arrays.asList(Category.values()));
	private final MinehutServerListScreen screen;
	private final TextRenderer textRenderer;	
	private final AtomicReference<Category> category;
	
    public CategoryDropdownWidget(MinehutServerListScreen screen, TextRenderer textRenderer, int x, int y, int width) {
        super(x, y, width, 20, Text.of("Categories"));
        this.screen = screen;
        this.textRenderer = textRenderer;
        this.category = screen.category;
    }
    
    public Category getCategory() {
    	return this.category.get();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = isMouseOver(mouseX, mouseY);
        if (this.isFocused() && bl && button == 0) {
        	for (Category category : categories) {
        		Rectangle bounds = getBounds(category);
        		if (bounds.intersects(new Rectangle2D.Double(mouseX, mouseY, 1, 1))) {
        			if (this.getCategory() != category)
        				this.category.set(category);
        			else this.category.set(null);
    	        	this.setFocused(false);
    	        	this.screen.serverListWidget.updateServers();
    	        	return true;
        		}
        	}
            return true;
        }
        
        for (Element element : screen.children()) {
        	if (element instanceof ClickableWidget) {
        		ClickableWidget widget = (ClickableWidget)element;
        		if (widget.isFocused()) widget.changeFocus(false);
        	}
        }
        this.setFocused(bl);
        if (this.isFocused() && bl && button == 0)
        	return true;
        return false;
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!this.visible)
        	return false;
        if (!(mouseX >= this.x && mouseX < this.x + this.width && mouseY < this.y + this.height))
        	return false;
        if (isFocused()) {
        	return mouseY >= this.y - this.height / 2 * (categories.size()-1);
        } else return mouseY >= this.y;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int color = this.isFocused() ? -1 : -6250336;
		int height = this.height / 2 * (this.isFocused() ? (categories.size()) : 2);
		if (this.isFocused()) height += 3;
		
		TextFieldWidget.fill(matrices, this.x - 1, this.y - 1 - height + this.height, this.x + this.width + 1, this.y + this.height + 1, color);
		TextFieldWidget.fill(matrices, this.x, this.y - height + this.height, this.x + this.width, this.y + this.height, -16777216);
		
		if (this.isFocused()) {
			int i = 0;
			for (Category category : categories) {
				Rectangle rectangle = getBounds(category);

				if (category == this.getCategory() || rectangle.intersects(new Rectangle(mouseX, mouseY, 1, 1)))
					TextFieldWidget.fill(matrices, rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, Color.getHSBColor(i/17f, 1f, 1f).getRGB());
				textRenderer.drawWithShadow(matrices, category.toString(), rectangle.x + 2, rectangle.y + 1, Color.WHITE.getRGB());
				
				i++;
			}
		} else textRenderer.drawWithShadow(
			matrices, getCategory() != null ? getCategory().toString() : "[All Categories]", this.x + 4, this.y + (this.height - 8) / 2, Color.WHITE.getRGB()
		);
    }
    
    private Rectangle getBounds(Category category) {
		int y = this.y + (this.height - 8) / 2 - categories.indexOf(category) * (this.height / 2) + 3;
		Rectangle rectangle = new Rectangle(this.x + 2, y, this.width - 4, 10);
		return rectangle;
    }
	
	@Override public void appendNarrations(NarrationMessageBuilder var1) {}
    
}