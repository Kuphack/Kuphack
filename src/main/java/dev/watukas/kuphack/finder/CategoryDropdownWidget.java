/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package dev.watukas.kuphack.finder;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.vaapukkax.minehut.PredefinedCategory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class CategoryDropdownWidget extends ClickableWidget {

	private static final List<PredefinedCategory> options = Collections.unmodifiableList(Arrays.asList(PredefinedCategory.values()));
	private final MinehutServerListScreen screen;
	private final TextRenderer textRenderer;	
	private final List<PredefinedCategory> categories;
	
	private PredefinedCategory removed;
	
    public CategoryDropdownWidget(MinehutServerListScreen screen, TextRenderer textRenderer, int x, int y, int width) {
        super(x, y, width, 20, Text.of("Categories"));
        this.screen = screen;
        this.textRenderer = textRenderer;
        this.categories = screen.categories;
    }
    
    public List<PredefinedCategory> getCategories() {
    	return Collections.unmodifiableList(this.categories);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean over = isMouseOver(mouseX, mouseY), categoryClick = this.isFocused() && over && button == 0;
        boolean control = Screen.hasControlDown();
        if (categoryClick) {
        	for (PredefinedCategory category : options) {
        		Rectangle bounds = getBounds(category);
        		if (bounds.intersects(new Rectangle2D.Double(mouseX, mouseY, 1, 1))) {
        			boolean active = this.categories.contains(category);
        			if (!control) this.categories.clear();
        			if (active) {
        				this.categories.remove(category);
        				this.removed = category;
        			} else this.categories.add(category);
    	        	if (!control) this.setFocused(false);
    	        	break;
        		}
        	}
        }
        
        for (Element element : screen.children()) {
        	if (element instanceof ClickableWidget widget) {
        		if (widget.isFocused()) widget.setFocused(false);
        	}
        }
        if (over || this.isFocused())
        	this.screen.serverListWidget.updateServers();
        this.setFocused(over && !(categoryClick && !control));
        if (categoryClick)
        	return true;
        return false;
    }
    
    @Override
    public void setFocused(boolean b) {
    	super.setFocused(b);
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!this.visible)
        	return false;
        if (!(mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height))
        	return false;
        if (isFocused()) {
        	return mouseY >= this.getY() - this.height / 2 * (options.size()-1);
        } else return mouseY >= this.getY();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		int color = this.isFocused() ? -1 : -6250336;
		int height = this.height / 2 * (this.isFocused() ? (options.size()) : 2);
		if (this.isFocused()) height += 3;

		context.fill(this.getX() - 1, this.getY() - 1 - height + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, color);
		context.fill(this.getX(), this.getY() - height + this.height, this.getX() + this.width, this.getY() + this.height, -16777216);
		
		if (this.isFocused()) {
			int i = 0;
			for (PredefinedCategory category : options) {
				Rectangle rectangle = getBounds(category);

				boolean active = categories.contains(category);
				if (active || rectangle.intersects(new Rectangle(mouseX, mouseY, 1, 1))) {
					if (this.removed != category || active) context.fill(rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, Color.getHSBColor(i/17f, 1f, 1f).getRGB());
				} else if (this.removed == category) this.removed = null;
				context.drawTextWithShadow(textRenderer, category.toString(), rectangle.x + 2, rectangle.y + 1, Color.WHITE.getRGB());
				
				i++;
			}
		} else {
			String text = categories.isEmpty() ? "[All Categories]"
				: categories.size() == 1 ? categories.get(0).toString()
				: categories.size() + " Categories...";
			context.drawTextWithShadow(
				textRenderer, text, this.getX() + 4, this.getY() + (this.height - 8) / 2, Color.WHITE.getRGB()
			);
		}
    }
    
    private Rectangle getBounds(PredefinedCategory category) {
		int y = this.getY() + (this.height - 8) / 2 - options.indexOf(category) * (this.height / 2) + 3;
		Rectangle rectangle = new Rectangle(this.getX() + 2, y, this.width - 4, 10);
		return rectangle;
    }
	
	@Override protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    
}