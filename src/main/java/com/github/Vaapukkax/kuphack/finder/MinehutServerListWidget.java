package com.github.vaapukkax.kuphack.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.github.vaapukkax.minehut.Category;
import com.github.vaapukkax.minehut.Server;
import com.github.vaapukkax.kuphack.Kuphack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

@Environment(value=EnvType.CLIENT)
public class MinehutServerListWidget extends AlwaysSelectedEntryListWidget<MinehutServerListWidget.Entry> {
	
    private static final Identifier UNKNOWN_SERVER_TEXTURE = new Identifier("textures/misc/unknown_server.png");
    private static final Identifier SERVER_SELECTION_TEXTURE = new Identifier("textures/gui/server_selection.png");
    
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    	super.render(matrices, mouseX, mouseY, delta);
    	if (screen.categoryWidget.isMouseOver(mouseX, mouseY)) return;
    	
    	try {
	    	for (ServerEntry entry : servers) {
	    		if (Objects.equals(getEntryAtPosition(mouseX, mouseY), entry) && this.isMouseOver(mouseX, mouseY) && !this.client.options.getTouchscreen().getValue()) {
		        	final Server server = entry.getServer();

		        	ArrayList<Text> lines = new ArrayList<>();
		        	// Category Text
		        	StringBuilder builder = new StringBuilder();
		        	Iterator<Category> categories = server.getCategories().iterator();
		        	while (categories.hasNext()) {
		        		builder.append(categories.next().toString());
		        		if (categories.hasNext()) builder.append(", ");
		        	}
		        	if (builder.isEmpty()) builder.append("[None Assigned]");
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
		        	
		        	screen.tooltipQueue = lines;
		        }
	    	}
    	} catch (Exception e) {
    		screen.tooltipQueue = Arrays.asList(Text.of("§cError rendering tooltip"));
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
    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
        this.screen.updateJoinButtonState();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    	if (keyCode == GLFW.GLFW_KEY_R && this.getHoveredEntry() instanceof ServerEntry) {
    		ServerEntry entry = (ServerEntry) this.getHoveredEntry();
    		if (!entry.updating) {
	    		new Thread(() -> {
	        		try {
	        			entry.updating = true;
	        			entry.server.update();
	        		} finally {
	        			entry.updating = false;
	        		}
	        	}).start();
    		}
    	}
        Entry entry = (Entry)this.getSelectedOrNull();
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
    	try {
	    	int i = 0;
	    	synchronized (this.servers) {
		    	for (ServerEntry serverEntry : this.servers) {
		    		if (serverEntry != null) i += serverEntry.server.getPlayerCount();
		    	}
	    	}
	    	return i;
    	} catch (ConcurrentModificationException e) {
    		e.printStackTrace();
    		return 0;
    	}
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
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    @Environment(value=EnvType.CLIENT) protected static abstract class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {}

    @Environment(value=EnvType.CLIENT)
    public class ServerEntry extends Entry {

        private final MinehutServerListScreen screen;
        private final MinecraftClient client;
        private final Server server;
        private final ItemStack icon;
        
        private long time;
        private boolean updating;
        
        protected ServerEntry(MinehutServerListScreen screen, Server server) {
            this.screen = screen;
            this.server = server;
            this.client = MinecraftClient.getInstance();
            
            Item item = Registry.ITEM.get(new Identifier("minecraft:"+server.getItemIcon().toLowerCase()));
            this.icon = new ItemStack(item);
        }
        
        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        	if (screen.categoryWidget.isMouseOver(mouseX, mouseY)) hovered = false;
        	
            this.client.textRenderer.draw(matrices, this.server.getName(), (float)(x + 32 + 3), (float)(y + 1), 0xFFFFFF);
            Text motd = Text.of(Kuphack.translateColor(this.server.getMOTD()));
            List<OrderedText> motdList = this.client.textRenderer.wrapLines(motd, entryWidth - 32 - 2);
            for (int i = 0; i < Math.min(motdList.size(), 2); ++i) {
                this.client.textRenderer.draw(matrices, motdList.get(i), (float)(x + 32 + 3), (float)(y + 12 + this.client.textRenderer.fontHeight * i), 0x808080);
            }
            
            Text playerCountText = Text.of(updating ? "Updating..."
            	: this.server.getPlayerCount() + "/" + this.server.getMaxPlayerCount()
            );
            int width = this.client.textRenderer.getWidth(playerCountText);
            this.client.textRenderer.draw(matrices, playerCountText, (float)(x + entryWidth - width - 15 - 2), (float)(y + 1), 0x808080);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
            DrawableHelper.drawTexture(matrices, x + entryWidth - 15, y, 0, 176, 10, 8, 256, 256);

            if (this.icon != null) {
            	int iconX = x;
            	float scale = 1f;
            	if (!(this.client.options.getTouchscreen().getValue() || hovered)) {
            		iconX += 8;
            		scale += 0.5f;
            	}
            	
                renderGuiItem(this.icon, iconX, y+8, scale);
            } else {
                this.draw(matrices, x, y, UNKNOWN_SERVER_TEXTURE);
            }

            if (this.client.options.getTouchscreen().getValue() || hovered) {
                RenderSystem.setShaderTexture(0, SERVER_SELECTION_TEXTURE);
                DrawableHelper.fill(matrices, x, y, x + 32, y + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                int mx = mouseX - x;
//                int my = mouseY - y;
                if (this.canConnect()) {
                    if (mx < 32 && mx > 16) {
                        DrawableHelper.drawTexture(matrices, x, y, 0.0f, 32.0f, 32, 32, 256, 256);
                    } else {
                        DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, 32, 32, 256, 256);
                    }
                }
            }
        }

        
        /*
         *  Modified from ItemRenderer
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
            matrixStack.translate(x, y, 100.0f + client.getItemRenderer().zOffset);
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
            client.getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, false, matrixStack2, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, model);
            immediate.draw();
            RenderSystem.enableDepthTest();
            if (bl) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();
        }
        
        protected void draw(MatrixStack matrices, int x, int y, Identifier textureId) {
            RenderSystem.setShaderTexture(0, textureId);
            RenderSystem.enableBlend();
            DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        private boolean canConnect() {
            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (Screen.hasShiftDown()) {
                MinehutServerListWidget multiplayerServerListWidget = this.screen.serverListWidget;
                int i = multiplayerServerListWidget.children().indexOf(this);
                if (i == -1) return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
        	if (screen.categoryWidget.isMouseOver(mouseX, mouseY)) return false;
            double mx = mouseX - (double)MinehutServerListWidget.this.getRowLeft();
//            double my = mouseY - (double)MinehutMultiplayerServerListWidget.this.getRowTop(MinehutMultiplayerServerListWidget.this.children().indexOf(this));
            if (mx <= 32.0) {
                if (mx < 32.0 && mx > 16.0 && this.canConnect()) {
                    this.screen.select(this);
                    this.screen.connect();
                    return true;
                }
            }
            this.screen.select(this);
            if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.screen.connect();
            }
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