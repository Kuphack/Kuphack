package dev.watukas.kuphack.modmenu;

import java.util.Arrays;
import java.util.List;

import dev.watukas.kuphack.modmenu.ButtonList.ButtonEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

@Environment(value=EnvType.CLIENT)
public class ButtonList extends ElementListWidget<ButtonEntry> {
    
    public ButtonList(MinecraftClient client, Screen screen, int y) {
        super(client, screen.width, screen.height - y - 32, y, 24);
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
    protected int getScrollbarX() {
        return super.getScrollbarX() + 32;
    }

    @Environment(value=EnvType.CLIENT)
    protected static class ButtonEntry extends ElementListWidget.Entry<ButtonEntry> {
        
        final ClickableWidget widget;
        
        private ButtonEntry(ClickableWidget widget) {
            this.widget = widget;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
            widget.setY(y);
            widget.render(context, mouseX, mouseY, delta);
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