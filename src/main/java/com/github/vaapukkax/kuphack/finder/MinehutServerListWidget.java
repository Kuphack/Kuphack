package com.github.vaapukkax.kuphack.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.minehut.PredefinedCategory;
import com.github.vaapukkax.minehut.Server;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MinehutServerListWidget extends AlwaysSelectedEntryListWidget<MinehutServerListWidget.Entry> {
	
    private static final Identifier UNKNOWN_SERVER_TEXTURE = new Identifier("textures/misc/unknown_server.png");
    private static final Identifier SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
    private static final Identifier ICONS_TEXTURE = new Identifier("textures/gui/icons.png");
    
    private final MinehutServerListScreen screen;
    private final List<ServerEntry> servers = new ArrayList<>();
    
    public MinehutServerListWidget(MinehutServerListScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
        super(client, width, height, top, bottom, entryHeight);
        this.screen = screen;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
    	if (screen.categoryWidget.isFocused()) return false;
    	return super.isMouseOver(mouseX, mouseY);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    	super.render(context, mouseX, mouseY, delta);
    	if (screen.categoryWidget.isMouseOver(mouseX, mouseY)) return;
    	
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
        		lines.add(Text.of("§cInactivity: "+Kuphack.formatTime((System.currentTimeMillis()-server.getInactivityTime())/1000d)));
        	}
        	
        	screen.setTooltip(lines.stream().map(Text::asOrderedText).toList());
    	} catch (Exception e) {
    		screen.setTooltip(Arrays.asList(Text.of("§cError rendering tooltip").asOrderedText()));
    		e.printStackTrace();
    	}
    }
    
    private void updateEntries() {
        this.clearEntries();
        this.servers.forEach(server -> this.addEntry(server));
        this.setSelected(null);
        this.setScrollAmount(Math.min(this.getMaxScroll(), this.getScrollAmount()));
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
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 30;
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
            Item item = Registries.ITEM.get(new Identifier("minecraft", server.getItemIcon().toLowerCase()));
            this.icon = new ItemStack(item);
            if (server.isUsingCosmetics()) this.icon.addEnchantment(Enchantments.UNBREAKING, 1);
            
            this.motd = Kuphack.translateColor(this.server.getMOTD());
        }

		@Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        	if (screen.categoryWidget.isMouseOver(mouseX, mouseY)) hovered = false;
        	
            context.drawTextWithShadow(client.textRenderer, this.server.getName(), x + 32 + 3, y + 1, 0xFFFFFF);
            List<OrderedText> motdList = this.client.textRenderer.wrapLines(motd, entryWidth - 32 - 2);
            for (int i = 0; i < Math.min(motdList.size(), 2); ++i) {
                context.drawTextWithShadow(client.textRenderer, motdList.get(i), x + 32 + 3, y + 12 + this.client.textRenderer.fontHeight * i, 0x808080);
            }
            Text playerCountText = Text.of(updating ? "Updating..."
            	: this.server.getPlayerCount() + "/" + this.server.getMaxPlayerCount()
            );
            int width = this.client.textRenderer.getWidth(playerCountText);
            context.drawTextWithShadow(client.textRenderer, playerCountText, x + entryWidth - width - 15 - 2, y + 1, 0x808080);

            context.drawTexture(ICONS_TEXTURE, x + entryWidth - 15, y, 0, 176, 10, 8, 256, 256);

            if (this.icon != null) {
            	int iconX = x;
            	float scale = 1f;
            	if (!(this.client.options.getTouchscreen().getValue() || hovered)) {
            		iconX += 8;
            		scale += 0.5f;
            	}
            	
                renderGuiItem(this.icon, iconX, y+8, scale);
            } else {
                context.drawTexture(UNKNOWN_SERVER_TEXTURE, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            }

            if (this.client.options.getTouchscreen().getValue() || hovered) {
                context.fill(x, y, x + 32, y + 32, -1601138544);
                int mx = mouseX - x;
                if (this.canConnect()) {
                    if (mx < 32 && mx > 16) {
                        context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 0.0f, 32.0f, 32, 32, 256, 256);
                    } else {
                    	context.drawTexture(SERVER_SELECTION_TEXTURE, x, y, 0.0f, 0.0f, 32, 32, 256, 256);
                    }
                }
            }
        }

        
        /*
         *  Modified from ItemRenderer to support scaling
         */
        
        @SuppressWarnings("deprecation")
		protected void renderGuiItem(ItemStack stack, int x, int y, float scale) {
        	BakedModel model = client.getItemRenderer().getModel(stack, null, null, 0);

            client.getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            MatrixStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.push();
            matrixStack.translate(x, y, 100.0f + ItemRenderer.field_41120);
            matrixStack.translate(8.0, 8.0, 0.0);
            matrixStack.scale(1.0f, -1.0f, 1.0f);
            matrixStack.scale(16.0f, 16.0f, 16.0f);
            RenderSystem.applyModelViewMatrix();
            MatrixStack matrixStack2 = new MatrixStack();
            matrixStack2.scale(scale, scale, 1);
            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            boolean bl = !model.isSideLit();
            if (bl) {
                DiffuseLighting.disableGuiDepthLighting();
            }
            client.getItemRenderer().renderItem(stack, ModelTransformationMode.GUI, false, matrixStack2, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, model);
            immediate.draw();
            RenderSystem.enableDepthTest();
            if (bl) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();
        }
        
        private boolean canConnect() {
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
        	if (screen.categoryWidget.isMouseOver(mouseX, mouseY)) return false;
            double mx = mouseX - (double)MinehutServerListWidget.this.getRowLeft();
//          double my = mouseY - (double)getRowTop(children().indexOf(this));
            if (mx < 32.0 && mx > 16.0 && this.canConnect()) {
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