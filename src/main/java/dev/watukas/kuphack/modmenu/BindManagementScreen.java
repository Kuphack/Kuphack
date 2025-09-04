package dev.watukas.kuphack.modmenu;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.flagclash.QuickBindFeature;
import dev.watukas.kuphack.flagclash.QuickBindFeature.QuickBind;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class BindManagementScreen extends Screen {

	private ButtonList buttonList;
	
	private QuickBind target;
	
	private final QuickBindFeature feature = Kuphack.get().getFeature(QuickBindFeature.class);
	private final Screen parent;
	private boolean initialized;
	
	public BindManagementScreen(Screen parent) {
		super(Text.of("Quick Bind Management"));
		this.parent = parent;
	}
	
    @Override
    protected void init() {
    	if (this.initialized) {
    		this.clearChildren();
    	}
    	
    	this.buttonList = new ButtonList(this.client, this, 64);
    	List<Item> items = new ArrayList<>();
    	for (QuickBind bind : feature.binds()) {
    		this.buttonList.addWidget(button(bind));
    		items.add(bind.item());
    	}
    	if (client.player != null) {
    		for (ItemStack stack : client.player.getInventory()) {
    			Item item = stack.getItem();
    			if (item == Items.AIR || items.contains(item))
    				continue;
    			this.buttonList.addWidget(button(item));
    			items.add(item);
    		}
    	}
        this.addSelectableChild(this.buttonList);
        
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close())
        	.position(this.width / 2 - 100, this.height - 28)
        	.width(150).build()
        );
        
        this.addDrawableChild(ButtonWidget.builder(Text.of("[" + feature.getTextState() + "]"), button -> {
        	this.feature.toggle();
        	button.setMessage(Text.of("[" + this.feature.getTextState() + "]"));
        	this.init();
        }).position(this.width / 2 + 50, this.height - 28).width(50).build());
        
        this.initialized = true;
    }
    
    private PressableWidget button(Item item) {
    	return button(this.feature.new QuickBind(item));
    }
    
    private PressableWidget button(QuickBind bind) {
    	Text text =
	    	bind.equals(this.target)
	    		? Text.literal("> ").append(bind.name()).append(" <")
	    	: feature.binds().contains(bind) ?
	    		bind.name().copy().append(" [").append(bind.bindedKey()).append("]")
			: Text.literal("Add ").append(bind.name());
		return new Widget(bind, width / 2 - ButtonWidget.DEFAULT_WIDTH / 2, text);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    	if (this.target != null) {
    		this.feature.binds().remove(target);
    		if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
    			this.target.set(keyCode);
    			this.feature.binds().add(0, this.target);
    		}
    		this.target = null;
    		this.executor.execute(this::clearAndInit);
    		return true;
    	}
    	return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    	super.render(context, mouseX, mouseY, delta);    	
        this.buttonList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFFFF);

        if (this.buttonList.children().isEmpty()) {
        	context.drawCenteredTextWithShadow(this.textRenderer, "You have no quick bind options", this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
    }

    @Override
    public void close() {
    	this.client.setScreen(parent);
    }
    
    public class Widget extends PressableWidget {

    	private final QuickBind bind;
    	
		public Widget(QuickBind bind, int x, Text text) {
			super(x, 0, ButtonWidget.DEFAULT_WIDTH, ButtonWidget.DEFAULT_HEIGHT, text);
			this.bind = bind;
		}
		
		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			super.renderWidget(context, mouseX, mouseY, deltaTicks);
			
			context.drawItem(new ItemStack(bind.item()), getX() - 18, getY() + 2);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
			appendDefaultNarrations(builder);
		}

		@Override
		public void onPress() {
			if (BindManagementScreen.this.target != null)
				BindManagementScreen.this.target = null;
			else
				BindManagementScreen.this.target = this.bind;
			executor.execute(BindManagementScreen.this::clearAndInit);
		}
    	
    }

}