package com.github.vaapukkax.kuphack.modmenu;

import java.util.Arrays;
import java.util.List;

import com.github.vaapukkax.kuphack.modmenu.ButtonList.ButtonEntry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

@Environment(value=EnvType.CLIENT)
public class ButtonList extends ElementListWidget<ButtonEntry> {
	
    public ButtonList(MinecraftClient client, Screen screen) {
        this(client, screen, 32);
    }
    
    public ButtonList(MinecraftClient client, Screen screen, int y) {
        super(client, screen.width, screen.height, y, screen.height - 32, 25);
        this.centerListVertically = false;
    }

    public <T extends ClickableWidget> T addWidget(T widget) {
        this.addEntry(new ButtonEntry(widget));
        return widget;
    }

    @Override
    public int getRowWidth() {
        return 400;
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 32;
    }

    @Environment(value=EnvType.CLIENT)
    protected static class ButtonEntry extends ElementListWidget.Entry<ButtonEntry> {
        
        final ClickableWidget widget;
        
        private ButtonEntry(ClickableWidget widget) {
            this.widget = widget;
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
            widget.y = y;
            widget.render(matrices, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends Element> children() {
            return Arrays.asList(widget);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Arrays.asList(widget);
        }
    }
}