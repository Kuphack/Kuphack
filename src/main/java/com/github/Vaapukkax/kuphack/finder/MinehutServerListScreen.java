package com.github.Vaapukkax.kuphack.finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.glfw.GLFW;

import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.Servers;
import com.github.Vaapukkax.minehut.Category;
import com.github.Vaapukkax.minehut.Minehut;
import com.github.Vaapukkax.minehut.NetworkStatistics;
import com.github.Vaapukkax.minehut.Server;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(value = EnvType.CLIENT)
public class MinehutServerListScreen extends Screen {

	private ArrayList<Server> entries = new ArrayList<>();
	
	protected List<Text> tooltipQueue;
	protected CategoryDropdownWidget categoryWidget;
    protected MinehutServerListWidget serverListWidget;
    private ButtonWidget buttonJoin, refreshButton, sortButton;
    private TextFieldWidget textField;
    
    private final Screen parent;
    private boolean initialized;
    private String error;
    
    final AtomicReference<Category> category = new AtomicReference<>();
    private final TreeMap<String, Integer> trending = new TreeMap<>();
    private SortType sortType = SortType.ACTIVITY;
    private int playerCount, serverCount;
    
    public MinehutServerListScreen(Screen parent) {
        super(Text.literal("Minehut Server List"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.client.keyboard.setRepeatEvents(true);
        
        boolean wasInitialized = this.initialized;
        if (this.initialized) {
            this.serverListWidget.updateSize(this.width, this.height, 32, this.height - 64);
        } else {
            this.initialized = true;
            this.serverListWidget = new MinehutServerListWidget(this, this.client, this.width, this.height, 32, this.height - 64, 36);
        }
        
        this.addSelectableChild(this.serverListWidget);
        
        this.addDrawableChild(this.sortButton = new ButtonWidget( // Sort
    		this.width / 2 - 154, this.height - 52, 100, 20,
    		Text.of("Sort: "+this.sortType),
    		button -> {
    			this.sortType = sortType.next();
    			sort(entries);
    			button.setMessage(Text.of("Sort: "+sortType));
    			serverListWidget.updateServers();
    			serverListWidget.setScrollAmount(0);
    		}
    	));
        
        this.buttonJoin = this.addDrawableChild( // Join
        	new ButtonWidget(this.width / 2 - 154, this.height - 28, 100, 20, Text.translatable("selectServer.select"), button -> this.connect())
        );
        
        this.refreshButton = this.addDrawableChild( // Refresh
        	new ButtonWidget(this.width / 2 - 50, this.height - 28, 100, 20, Text.translatable("selectServer.refresh"), button -> this.refresh())
        );
        this.addDrawableChild( // Back
        	new ButtonWidget(this.width / 2 + 4 + 50, this.height - 28, 100, 20, ScreenTexts.BACK, button -> this.client.setScreen(this.parent))
        );

        this.categoryWidget = new CategoryDropdownWidget(
        	this, textRenderer, this.width / 2 - 49, this.height - 52, 97
        );
        this.addSelectableChild(categoryWidget);
        
        String oldSearch = textField != null ? textField.getText() : "";
        this.textField = new TextFieldWidget(
        	this.textRenderer,
        	this.width / 2 + 4 + 50, this.height - 52,
        	99, 20,
        	Text.of("Search")
        );
        this.textField.setText(oldSearch);
        
        this.textField.setChangedListener(search -> {
        	serverListWidget.updateServers();
        });
        this.addSelectableChild(this.textField);
        this.textField.setTextFieldFocused(true);
        this.setInitialFocus(this.textField);
        this.updateJoinButtonState();
        
        if (!wasInitialized) refresh();
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
    
    public Map<String, Integer> getTrending() {
    	return this.trending;
    }
    
	public List<Server> getAllEntries() {
		return Collections.unmodifiableList(entries);
	}
	
	public List<Server> getEntries() {
		ArrayList<Server> list = new ArrayList<>();
		for (Server entry : entries) {
			if (!isInvalid(entry)) list.add(entry);
		}
		return Collections.unmodifiableList(list);
	}
    
    public void refresh() {    	
    	this.error = null;
    	this.refreshButton.active = false;
    	
    	new Thread(() -> {
    		ArrayList<Server> entries = new ArrayList<>();
	    	
	        try {
				long start = System.currentTimeMillis();
				Minehut minehut = Kuphack.get().getMinehut();
				List<Server> servers = minehut.getOnlineServers(category.get());
				Kuphack.LOGGER.info("Minehut servers loaded in "+(System.currentTimeMillis()-start)+"ms");

				NetworkStatistics stats = minehut.getStatistics();
                this.playerCount = stats.getPlayerCount();
                this.serverCount = stats.getServerCount();

                HashMap<String, Integer> trending = new HashMap<>();
                for (Server entry : servers) {
                	entries.add(entry);
                	
                	for (String word : entry.getMOTD().split("\\s")) {
                		word = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                		if (word.length() > 2 && !Arrays.asList("minehut", "server", "and", "the", "for", "join").contains(word)) {
                			trending.put(word, (trending.containsKey(word) ? trending.get(word) : 0)+1);
                		}
                	}
                }
                
                this.trending.clear();
                for (Map.Entry<String, Integer> entry : entriesSortedByValues(trending)) {
                	this.trending.put(entry.getKey(), entry.getValue());
                }
                
                this.sort(entries);
	        } catch (Exception e) {
				if (!(e instanceof IllegalStateException || e.getMessage().equals("Socket closed")))
					e.printStackTrace();
				
				error = e.toString();
				StackTraceElement[] traces = e.getStackTrace();
				if (traces.length > 0) {
					error += "\nClass["+traces[0].getClassName()+"] Line["+traces[0].getLineNumber()+"]";
				}
				
				entries = new ArrayList<>();
			}
	
	        synchronized (entries) {
	        	this.entries = entries;
	        	serverListWidget.updateServers();
	        }
	        refreshButton.active = true;
    	}).start();
    }
    
    public void sort(List<Server> entries) {
    	if (sortType == SortType.SHUFFLE) {
    		Collections.shuffle(entries);
    	} else Collections.sort(entries, sortType.getComparator(this));
    }
    
    public boolean isShown(Server entry) {
    	if (isInvalid(entry)) return false;
    	if (category.get() != null && !entry.getCategories().contains(category.get())) return false;
    	
    	String search = textField.getText().toLowerCase();
    	if (search.isBlank()) return true;
 
    	String motd = Kuphack.stripColor(entry.getMOTD().toLowerCase()).replaceAll("\n", " ");
    	
    	int motdi = 0;
    	for (String word : search.split("\\s")) {
    		if (motd.contains(word)) motdi++;
    	}
    	
    	return entry.getName().toLowerCase().contains(search) || motdi == search.split("\\s").length;
    }

    @Override
    public void tick() {
        this.textField.tick();
        super.tick();
    }
    
    @Override
    public void removed() {
        this.client.keyboard.setRepeatEvents(false);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F5) {
            this.refresh();
            return true;
        }
        if (this.serverListWidget.getSelectedOrNull() != null && !this.textField.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.connect();
                return true;
            }
            return this.serverListWidget.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    	this.renderBackground(matrices);
    	this.serverListWidget.render(matrices, mouseX, mouseY, delta);
        
        if (error != null) {
        	drawCenteredText(matrices, this.textRenderer, "Wild error appeard!", this.width / 2, this.height / 2 - textRenderer.fontHeight, 0xFF0000);
        	
        	int i = 0;
        	List<Text> lines = split(this.error);
        	for (Text line : lines) {
        		drawCenteredText(matrices, this.textRenderer, line, this.width / 2, this.height / 2 + textRenderer.fontHeight * i, 0xFFFFFF);
        		i++;
        	}
        } else if (!refreshButton.active) {
        	Text text = Text.of("Refreshing...");
        	drawTextWithShadow(matrices, this.textRenderer, text, client.getWindow().getScaledWidth() - this.textRenderer.getWidth(text) - 5, 5, 0xFFFFFF);
        }
        
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        drawCenteredText(matrices, this.textRenderer, Text.of("Servers: "+serverListWidget.getServerCount()+" ("+serverCount+") | Players: "+serverListWidget.getPlayerCount()+" ("+playerCount+")"), this.width / 2, 20 - this.textRenderer.fontHeight, 0xFFFFFF);
        
        this.categoryWidget.render(matrices, mouseX, mouseY, delta);
        this.textField.render(matrices, mouseX, mouseY, delta);
        
        if (this.sortButton.isHovered()) this.renderTooltip(matrices, split(sortType.getDescription()), mouseX, mouseY);
        
        if (this.tooltipQueue != null) {
        	this.renderTooltip(matrices, this.tooltipQueue, mouseX, mouseY);
        	this.tooltipQueue = null;
        }
        
        super.render(matrices, mouseX, mouseY, delta);
    }
    
    public void connect() {
    	
        MinehutServerListWidget.Entry entry = (MinehutServerListWidget.Entry)this.serverListWidget.getSelectedOrNull();
        if (entry instanceof MinehutServerListWidget.ServerEntry) {
        	Server serverEntry = ((MinehutServerListWidget.ServerEntry)entry).getServer();

	    	if (Kuphack.getServer() == Servers.LOBBY && client.player != null) {
	    		client.player.sendCommand("join "+serverEntry.getName());
	    	} else {
	    		if (client.world != null) {
	    			client.world.disconnect();
	    			this.client.disconnect();
	    		}

	        	ServerInfo info = new ServerInfo(serverEntry.toString(), serverEntry.getAddress(), false);
				ConnectScreen.connect(this, this.client, ServerAddress.parse(info.address), info);
	        }
    	}
    }

    public void select(MinehutServerListWidget.Entry entry) {
        this.serverListWidget.setSelected(entry);
        this.updateJoinButtonState();
    }

    protected void updateJoinButtonState() {
        this.buttonJoin.active = this.serverListWidget.getSelectedOrNull() != null;
    }
    
    private boolean isInvalid(Server entry) {
    	return entry.isLobby() || entry.isSubserver() || !entry.isOnline();
    }
    
    private static List<Text> split(String text) {
    	List<Text> lines = new ArrayList<>();
    	for (String line : text.split("\n")) {
    		lines.add(Text.of(line));
    	}
    	return lines;
    }
    
    private static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}

