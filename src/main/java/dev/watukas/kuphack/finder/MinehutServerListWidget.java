package dev.watukas.kuphack.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.github.vaapukkax.minehut.PredefinedCategory;
import com.github.vaapukkax.minehut.Server;

import dev.watukas.kuphack.Kuphack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MinehutServerListWidget extends AlwaysSelectedEntryListWidget<MinehutServerListWidget.Entry> {
	
    static final Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("server_list/join_highlighted");
    static final Identifier JOIN_TEXTURE = Identifier.ofVanilla("server_list/join");
    
    private final MinehutServerListScreen screen;
    private final List<ServerEntry> servers = new ArrayList<>();
    
    public MinehutServerListWidget(MinehutServerListScreen screen, MinecraftClient client, int width, int height) {
        super(client, width, height - 96, 32, 32);
        this.screen = screen;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
    	if (screen.categoryWidget.isFocused()) return false;
    	return super.isMouseOver(mouseX, mouseY);
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    	super.renderWidget(context, mouseX, mouseY, delta);
    	if (screen.categoryWidget.isMouseOver(mouseX, mouseY))
    		return;
    	
    	try {
    		if (this.client.options.getTouchscreen().getValue() || !this.isMouseOver(mouseX, mouseY))
    			return;
    		ServerEntry entry = (ServerEntry) (this.getFocused() != null ? this.getFocused()
    			: getEntryAtPosition(mouseX, mouseY));
    		if (entry == null) return;
        	
    		final Server server = entry.getServer();
        	ArrayList<Text> lines = new ArrayList<>();
        	// Category Text
        	StringBuilder builder = new StringBuilder();
        	Iterator<PredefinedCategory> categories = server.getPredefinedCategories().iterator();
        	while (categories.hasNext()) {
        		builder.append(categories.next().toString());
        		if (categories.hasNext()) builder.append(", ");
        	}
        	if (builder.isEmpty()) builder.append("[None]");
        	lines.add(Text.of("§2Categories: "+builder));
        	// Rank Text
        	lines.add(Text.of("§aRank: #"+(screen.getEntries().indexOf(server)+1)));
        	// Plan Text
        	lines.add(Text.of("§6Plan: "+server.getPlan()));        	
        	// Up time text
        	double uptime = (System.currentTimeMillis()-server.getLastOnline())/1000d;
        	lines.add(Text.of("§eUptime: "+Kuphack.formatTime(uptime)));
        	// Inactivity Text
        	if (server.getInactivityTime() != 0 && server.getPlayerCount() == 0) {
        		lines.add(Text.of("§cInactivity: " + Kuphack.formatTime((System.currentTimeMillis()-server.getInactivityTime())/1000d)));
        	}
        	
        	context.drawTooltip(lines.stream().map(Text::asOrderedText).toList(), mouseX, mouseY);
    	} catch (Exception e) {
    		context.drawTooltip(Arrays.asList(Text.of("§cError rendering tooltip").asOrderedText()), mouseY, mouseY);
    		e.printStackTrace();
    	}
    }
    
    private void updateEntries() {
        this.clearEntries();
        this.servers.forEach(server -> this.addEntry(server));
        this.setSelected(null);
        this.setScrollY(Math.min(this.getMaxScrollY(), this.getScrollY()));
    }

    @Override
    public void setSelected(@Nullable MinehutServerListWidget.Entry entry) {
        super.setSelected(entry);
        this.screen.updateJoinButtonState();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    	if (keyCode == GLFW.GLFW_KEY_R && this.getHoveredEntry() instanceof ServerEntry) {
    		ServerEntry entry = (ServerEntry) this.getHoveredEntry();
    		if (!entry.updating) {
	    		new Thread(() -> {
	        		entry.refresh();
	        	}).start();
    		}
    	}
        MinehutServerListWidget.Entry entry = (MinehutServerListWidget.Entry) this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void updateServers() {
    	synchronized (this.servers) {
	        this.servers.clear();
	        for (int i = 0; i < screen.getAllEntries().size(); ++i) {
	        	Server serverListEntry = screen.getAllEntries().get(i);
	            if (screen.isShown(serverListEntry)) {
	            	this.servers.add(new ServerEntry(this.screen, serverListEntry));
	            }
	        }
	        this.updateEntries();
    	}
    }
    
    public int getServerCount() {
    	return this.servers.size();
    }
    
    public int getPlayerCount() {
	    int i = 0;
	    synchronized (this.servers) {
		   	for (ServerEntry serverEntry : this.servers) {
		   		if (serverEntry != null) i += serverEntry.server.getPlayerCount();
		   	}
	    }
	    return i;
    }

    @Override
    protected int getScrollbarX() {
        return super.getScrollbarX() + 30;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    @Override
	public boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    @Environment(value=EnvType.CLIENT) protected static abstract class Entry extends AlwaysSelectedEntryListWidget.Entry<MinehutServerListWidget.Entry> {}

    @Environment(value=EnvType.CLIENT)
    public class ServerEntry extends MinehutServerListWidget.Entry {

        private final MinehutServerListScreen screen;
        private final MinecraftClient client;
        private final Server server;
        private ItemStack icon;
        private Text motd;
        
        private long time;
        private boolean updating;
        
        protected ServerEntry(MinehutServerListScreen screen, Server server) {
            this.screen = screen;
            this.server = server;
            this.client = MinecraftClient.getInstance();
            
            this.refreshCache();
        }
        
        public void refresh() {
        	this.updating = true;
        	try {
        		this.server.update();
				this.refreshCache();
        	} finally {
        		this.updating = false;
        	}
		}
        
        private void refreshCache() {
            Item item = Registries.ITEM.get(Identifier.ofVanilla(server.getItemIcon().toLowerCase()));
            this.icon = new ItemStack(item);
            if (server.isUsingCosmetics()) {
            	this.icon.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
            
            this.motd = Kuphack.translateColor(this.server.getMOTD());
        }

		@Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        	if (screen.categoryWidget.isMouseOver(mouseX, mouseY))
        		hovered = false;
        	
            context.drawTextWithShadow(client.textRenderer, this.server.getName(), x + 32 + 3, y + 1, 0xFFFFFFFF);
            List<OrderedText> motdList = this.client.textRenderer.wrapLines(motd, entryWidth - 32 - 2);
            for (int i = 0; i < Math.min(motdList.size(), 2); ++i) {
                context.drawTextWithShadow(client.textRenderer, motdList.get(i), x + 32 + 3, y + 12 + this.client.textRenderer.fontHeight * i, 0xFF808080);
            }
            Text playerCountText = Text.of(updating ? "Updating..."
            	: this.server.getPlayerCount() + "/" + this.server.getMaxPlayerCount()
            );
            int width = this.client.textRenderer.getWidth(playerCountText);
            context.drawTextWithShadow(client.textRenderer, playerCountText, x + entryWidth - width - 4, y + 1, 0xFF808080);

            if (this.icon != null) {
            	int iconX = x;
            	float scale = 1f;
            	if (!(this.client.options.getTouchscreen().getValue() || hovered)) {
            		iconX += 8;
            		scale += 0.5f;
            	}
            	
                renderGuiItem(context, this.icon, iconX, y + 8, scale);
            }
            
            if (this.client.options.getTouchscreen().getValue() || hovered) {
                context.fill(x, y, x + 32, y + 32, -1601138544);
                int mx = mouseX - x;

                if (mx < 32 && mx > 16) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, JOIN_HIGHLIGHTED_TEXTURE, x, y, 32, 32);
                } else {
                	context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, JOIN_TEXTURE, x, y, 32, 32);
                }

            }
        }

		protected void renderGuiItem(DrawContext context, ItemStack stack, int x, int y, float scale) {
			var matrices = context.getMatrices();
			matrices.pushMatrix();
			matrices.scaleAround(scale, x + 8, y + 8);
        	context.drawItem(stack, x, y);
        	matrices.popMatrix();
        }
		
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
        	if (screen.categoryWidget.isMouseOver(mouseX, mouseY)) return false;
            double mx = mouseX - (double)MinehutServerListWidget.this.getRowLeft();
            if (mx < 32.0 && mx > 16.0) {
            	setSelected(this);
            	this.screen.connect();
            	return true;
            }
            setSelected(this);
            
            if (Util.getMeasuringTimeMs() - this.time < 250L)
                this.screen.connect();
            this.time = Util.getMeasuringTimeMs();
            return false;
        }

        public Server getServer() {
            return this.server;
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", this.server.getName());
        }
    }

}