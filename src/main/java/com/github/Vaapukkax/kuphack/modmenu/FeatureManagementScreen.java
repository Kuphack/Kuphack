package com.github.Vaapukkax.kuphack.modmenu;

import java.util.HashMap;
import java.util.List;

import com.github.Vaapukkax.kuphack.Feature;
import com.github.Vaapukkax.kuphack.Kuphack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class FeatureManagementScreen extends Screen {

	private final HashMap<Feature, ButtonWidget> widgets = new HashMap<>();
	private ButtonListWidget buttonList;

	private final Screen parent;
	private boolean initialized;
	
	public FeatureManagementScreen(Screen parent) {
		super(Text.of("Feature Management"));
		this.parent = parent;
	}
	
    @Override
    protected void init() {
    	if (initialized) this.clearChildren();
    	
    	this.buttonList = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
    	
        int y = this.height / 6 - 3;

        for (Feature feature : Kuphack.get().getFeatures()) {
        	ButtonWidget widget = new ButtonWidget(
	        	this.width / 2 - 150 / 2, y, 150, 20,
	        	Text.of(feature.getName() + " ["+feature.getDisableState()+"]"),
	        	button -> {
	        		feature.toggle();
	        		button.setMessage(Text.of(feature.getName() + " ["+feature.getDisableState()+"]"));
	        	}
	        );
        	widget.active = client.getCurrentServerEntry() == null || feature.isOnServer();
	        this.widgets.put(feature, this.addDrawableChild(widget));
	        y += 24;
        }
        
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 28 /*y += 24*/, 200, 20, ScreenTexts.BACK, button -> this.client.setScreen(this.parent)));
        this.initialized = true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.buttonList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
        
        widgets.forEach((feature, widget) -> {
        	if (widget.isHovered()) {
        		List<OrderedText> list = this.textRenderer.wrapLines(Text.of(feature.getDescription()), 250);
        		renderOrderedTooltip(matrices, list, mouseX, mouseY);
        	}
        });
    }

    @Override
    public void close() {
    	this.client.setScreen(parent);
    }

}