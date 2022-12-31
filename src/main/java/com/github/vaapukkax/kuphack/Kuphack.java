package com.github.vaapukkax.kuphack;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.vaapukkax.kuphack.Event.EventHolder;
import com.github.vaapukkax.kuphack.Event.EventMention;
import com.github.vaapukkax.kuphack.events.ChatEvent;
import com.github.vaapukkax.kuphack.events.ServerJoinEvent;
import com.github.vaapukkax.kuphack.finder.MinehutButtonState;
import com.github.vaapukkax.kuphack.flagclash.BlockRadiusFeature;
import com.github.vaapukkax.kuphack.flagclash.FlagBreakTimeFeature;
import com.github.vaapukkax.kuphack.flagclash.FlagClash;
import com.github.vaapukkax.kuphack.flagclash.FlagLocation;
import com.github.vaapukkax.kuphack.flagclash.FriendFeature;
import com.github.vaapukkax.kuphack.flagclash.HookshotHelperFeature;
import com.github.vaapukkax.kuphack.flagclash.ItemEntityInfoFeature;
import com.github.vaapukkax.kuphack.flagclash.StablePipeFeature;
import com.github.vaapukkax.kuphack.flagclash.StariteTracerFeature;
import com.github.vaapukkax.kuphack.flagclash.UltraSignalProgressFeature;
import com.github.vaapukkax.kuphack.updater.CheckOption;
import com.github.vaapukkax.kuphack.updater.UpdateChecker;
import com.github.vaapukkax.minehut.Minehut;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class Kuphack implements ModInitializer, EventHolder {

	public static final Logger LOGGER = LoggerFactory.getLogger("kuphack");
	private static Kuphack instance;
	
	private CloseableHttpClient httpClient;
	private Minehut minehut;
	private final ArrayList<Feature> features = new ArrayList<>();
	
	public MinehutButtonState serverListButton = isFeather() ? MinehutButtonState.LEFT_CORNER : MinehutButtonState.RIGHT_CORNER;
	public CheckOption updateOption = CheckOption.LOOKUP;
	
	private SupportedServer server;
	private long customCheckTimeout = -1;
	
	@Override
	public void onInitialize() {
		Kuphack.instance = this;
	
		this.httpClient = HttpClients.createDefault();
		this.minehut = new Minehut.Builder()
			.driver(new ApacheHttpDriver(this.httpClient))
			.build();
		
		Event.register(this);

		// LOBBY
		features.add(new AdBlockFeature());
		features.add(new ServerListReplacement());
		
		// FLAGCLASH
		features.add(new FriendFeature());
		features.add(new FlagBreakTimeFeature());
		features.add(new FlagLocation());
		features.add(new BlockRadiusFeature());
		features.add(new StablePipeFeature());
		features.add(new HookshotHelperFeature());
		features.add(new UltraSignalProgressFeature());
		features.add(new StariteTracerFeature());

		// FLAGCLASH & OVERCOOKED
		features.add(new ItemEntityInfoFeature());
		
		Event.register(new FlagClash());
		
		// Multiplayer Button Setting
		JsonObject object = this.readDataFile();
		if (object.has("mhButtonState")) 
			this.serverListButton = MinehutButtonState.valueOf(object.get("mhButtonState").getAsString());
		if (object.has("auto-update"))
			this.updateOption = CheckOption.of(object.get("auto-update"));
		
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			this.minehut.close(); // closes httpClient
			UpdateChecker.continueDownload();
		});
		
		ClientTickEvents.START_CLIENT_TICK.register(new StartTick() {
			
			private ServerInfo info;
			
			public void onStartTick(MinecraftClient client) {
				boolean debug = FabricLoader.getInstance().isDevelopmentEnvironment();
				if (debug && getServer() != SupportedServer.FLAGCLASH)
					Event.call(new ServerJoinEvent(new ServerInfo("FlagClash", "flagclash.minehut.gg", false)));
				if (client.isIntegratedServerRunning() && !debug) setServer(null);

				// TODO recode since there are better ways to do this
				if (System.currentTimeMillis() - customCheckTimeout > 1500) {
					for (SupportedServer server : SupportedServer.values()) {
						if (getServer() == server || !server.test(client)) continue;
						setServer(server);
						break;
					}
				}
				
				ServerInfo info = client.getCurrentServerEntry();
				if (this.info != info) {
					if (info != null)
						Event.call(new ServerJoinEvent(info));
					this.info = info;
				}
				UpdateChecker.sendCheckerStatus();
			}
		});
		if (isFeather()) { // replacement for the extra info on the FlagClash sidebar
			MinecraftClient client = MinecraftClient.getInstance();
			HudRenderCallback.EVENT.register((matrices, delta) -> {
				if (client.player == null || getServer() != SupportedServer.FLAGCLASH) return;
				boolean flag = this.getFeature(FlagLocation.class).isFlagDown();
				if (!flag && FlagClash.isUpgradeCostUnsure()) return;
				
				Text text = Text.literal(flag ?
					"Upgrade Time: "  + FlagClash.timeAsString(FlagClash.getUpgradeTime())
					: "Upgrade Cost: "+ FlagClash.toVisualValue(FlagClash.getUpgradeCost()));
				client.textRenderer.drawWithShadow(matrices, text,
					client.getWindow().getScaledWidth() - client.textRenderer.getWidth(text) - 5,
					client.getWindow().getScaledHeight() - 30 + client.textRenderer.fontHeight
				, Formatting.GOLD.getColorValue());
			});
		}
		new Thread(() -> {
			try {
				if (updateOption != CheckOption.OFF) UpdateChecker.checkAndDownload();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public Minehut getMinehut() {
		return this.minehut;
	}
	
	public CloseableHttpClient getHttpClient() {
		return this.httpClient;
	}
	
	private void setServer(SupportedServer server) {
		if (this.server == server) return;
		this.customCheckTimeout = System.currentTimeMillis();
		
		for (Feature feature : features) {
			if (feature.isOnServer() && !feature.isDisabled()) feature.onDeactivate();
		}
		
		this.server = server;
		
		for (Feature feature : features) {
			if (feature.isOnServer() && !feature.isDisabled()) feature.onActivate();
		}
	}

	public JsonObject readDataFile() {
		try (BufferedReader reader = Files.newBufferedReader(getDataFile(), Charset.defaultCharset())) {
			return Objects.requireNonNullElseGet(
				new Gson().fromJson(reader.lines().collect(Collectors.joining("\n")), JsonObject.class),
				JsonObject::new
			);
		} catch (IOException | JsonParseException e) {
			e.printStackTrace();
		}
		return new JsonObject();
	}
	
	public Path getDataFile() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("kuphack.json");
		if (Files.exists(path)) return path;
		
		try {	
			Files.createFile(path);
		} catch (IOException e) {
			new IOException("Couldn't create Kuphack settings file", e).printStackTrace();
		}
		return path;
	}
	
	public void register(Feature feature) {
		if (!features.contains(feature)) features.add(feature);
	}
	
	public <T extends Feature> T getFeature(Class<T> clazz) {
		for (Feature feature : features) {
			if (feature.getClass().isAssignableFrom(clazz)) return clazz.cast(feature);
		}
		return null;
	}
	
	public List<Feature> getFeatures() {
		return this.features.stream().filter(feature -> !feature.getClass().equals(FlagLocation.class))
			.collect(Collectors.toUnmodifiableList());
	}
	
	@EventMention
	public void onEvent(ServerJoinEvent e) {
		if (e.getInfo().address.toLowerCase().endsWith(".minehut.gg")) {
			String address = e.getInfo().address.toUpperCase();
			String name = address.substring(0, address.indexOf("."));
			
			SupportedServer server = null;
			try {
				server = SupportedServer.valueOf(name);
			} catch (IllegalArgumentException ex) {}
			setServer(server);
			if (server != null) return;
		} else if (e.getInfo().address.toLowerCase().startsWith("minehut.com")) {
			setServer(SupportedServer.LOBBY);
			return;
		}
		setServer(null);
	}
	
	@EventMention
	public void onEvent(ChatEvent e) {
		if (!isOnMinehut()) return;
		if (e.getText().getString().equals("§3Sending you to the lobby!")) {
			setServer(SupportedServer.LOBBY);
		} else if (getServer() == SupportedServer.LOBBY) {
			String message = e.getText().getString();
			if (message.startsWith("Sending you to ") && message.endsWith("!")) {
				String name = e.getText().getString().substring(15, e.getText().getString().length()-1);
				try {
					setServer(SupportedServer.valueOf(name.toUpperCase()));
				} catch (IllegalArgumentException exc) {
					LOGGER.info("Kuphack.cc doesn't support: "+name.toUpperCase());
					setServer(null);
				}
			}
		}
	}
	
	public static boolean isFeather() {
		return FabricLoader.getInstance().getModContainer("feather").isPresent();
	}
	
	public static Kuphack get() {
		return Kuphack.instance;
	}
	
	private static boolean isOnMinehut(ServerInfo info) {
		return info != null && (info.address.toLowerCase().contains("minehut") || info.address.startsWith("172.65.244.181"));
	}
	
	public static boolean isOnMinehut() {
		MinecraftClient client = MinecraftClient.getInstance();
		return isOnMinehut(client.getCurrentServerEntry());
	}
	
	public static String round(double value) {
		DecimalFormat df = new DecimalFormat("0.#");
		df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		return df.format(value);
	}
	
	public static SupportedServer getServer() {
		if (get() == null) return null;
		return get().server;
	}
	
	public static ItemStack getHolding(PlayerEntity player) {
		PlayerInventory inv = player.getInventory();
		return inv.getStack(inv.selectedSlot);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static List<Text> getScoreboard() {
		ArrayList<Text> texts = new ArrayList<>();
		MinecraftClient m = MinecraftClient.getInstance();
		Scoreboard scoreboard = m.world.getScoreboard();
		if (scoreboard == null) return texts;

		Iterator<ScoreboardObjective> os = scoreboard.getObjectives().iterator();
		if (!os.hasNext()) return texts;
		
		ScoreboardObjective objective = os.next();
		Collection<ScoreboardPlayerScore> collectionf = scoreboard.getAllPlayerScores(objective);
		List<ScoreboardPlayerScore> list = (List<ScoreboardPlayerScore>) collectionf.stream().filter((score) -> {
			return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
		}).collect(Collectors.toList());

		for (Iterator var11 = list.iterator(); var11.hasNext();) {
			ScoreboardPlayerScore score = (ScoreboardPlayerScore) var11.next();
			Team team = scoreboard.getPlayerTeam(score.getPlayerName());
			texts.add(Team.decorateName(team, Text.literal(score.getPlayerName())));
		}
		return texts;
	}
	
	protected static List<Text> getModifiedSidebar() {
		List<Text> lines = Kuphack.getScoreboard();
		if (getServer() == SupportedServer.FLAGCLASH) {
	        if (Kuphack.get().getFeature(FlagLocation.class).isFlagDown()) {
	        	double time = FlagClash.getUpgradeTime();
	        	if (time != -1) lines.add(0, Text.literal(
	        		FlagClash.isMushroomArc() ?
	        			(FlagClash.isUpgradeCostUnsure() ? " §k" : " §f") + FlagClash.timeAsString(time)
	        		: " §fUpgrade Time: §a§l" + (FlagClash.isUpgradeCostUnsure() ? "Unsure" : FlagClash.timeAsString(time))
	        	));
	        } else if (!FlagClash.isUpgradeCostUnsure()) lines.add(0, (FlagClash.isMushroomArc() ? Text.literal(" ") : Text.literal(" Upgrade Cost: ")).append(
	        	Text.literal(FlagClash.toVisualValue(FlagClash.getUpgradeCost()))
	        		.styled(style -> style.withColor(Formatting.GREEN).withBold(true))
	        ));
		}
		return lines;
	}
	
    public static void renderSidebar(MatrixStack matrices, ScoreboardObjective objective) {
    	MinecraftClient client = MinecraftClient.getInstance();
    	TextRenderer textRenderer = client.textRenderer;
		int scaledWidth = client.getWindow().getScaledWidth(),
			scaledHeight= client.getWindow().getScaledHeight();
    	List<Text> lines = Kuphack.getModifiedSidebar();

        Text title = objective.getDisplayName();
        final int titleWidth = textRenderer.getWidth(title);
        int width = titleWidth;

        for (Text line : lines) width = Math.max(width, textRenderer.getWidth(line));

        int footerHeight = lines.size() * textRenderer.fontHeight;
        int bottom = scaledHeight / 2 + footerHeight / 3;

        int textX = scaledWidth - width - 1;
        int footerAlpha = client.options.getTextBackgroundColor(0.3f),
        	tabAlpha = client.options.getTextBackgroundColor(0.4f);
        int i = 0;
        for (Text line : lines) {
            int y = bottom - ++i * textRenderer.fontHeight;
            int right = scaledWidth - 1;
            InGameHud.fill(matrices, textX - 2, y, right, y + textRenderer.fontHeight, footerAlpha);
            textRenderer.draw(matrices, line, (float) textX, (float)y, -1);

            if (i != lines.size()) continue;
            InGameHud.fill(matrices, textX - 2, y - textRenderer.fontHeight - 1, right, y - 1, tabAlpha);
            InGameHud.fill(matrices, textX - 2, y - 1, right, y, footerAlpha);
            textRenderer.draw(matrices, title, (float)(textX + width / 2 - titleWidth / 2), (float)(y - textRenderer.fontHeight), -1);
        }
	}
	
	public static void renderText(Text text, MatrixStack matrix, VertexConsumerProvider consumer) {
		final int light = 255;

		MinecraftClient client = MinecraftClient.getInstance();

		matrix.push();
		matrix.multiply(client.gameRenderer.getCamera().getRotation());
		matrix.scale(-0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = matrix.peek().getPositionMatrix();
		float g = client.options.getTextBackgroundOpacity(0.25F);
		int j = (int) (g * 255.0F) << 24;
		TextRenderer textRenderer = client.textRenderer;
		float h = (float) (-textRenderer.getWidth((StringVisitable) text) / 2);
		textRenderer.draw((Text) text, h, 0, -1, false, matrix4f, consumer, false, j, light);

		matrix.pop();
	}
	
	private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");
	
	public static String stripColor(String message) {
		if (message.isBlank()) return message;
		return COLOR_PATTERN.matcher(message).replaceAll("");
	}
	
	public static String stripColor(Text message) {
		return stripColor(message.getString());
	}
	
	public static String translateColor(String message) {
		if (message.isBlank()) return message;
		return message.replaceAll("(?i)&(?=[0-9A-FK-ORX])", "§");
	}
	
	public static <T extends MutableText> T color(T text, Color color) {
		text.setStyle(text.getStyle().withColor(color.getRGB()));
		return text;
	}
	
	public static void translateCamera(WorldRenderContext context) {
		MatrixStack matrices = context.matrixStack();
		Vec3d pos = context.camera().getPos();

		matrices.translate(-(pos.x), -(pos.y), -(pos.z));
	}

	public static String formatTime(double seconds) {
		return (int)(seconds/60/60)+"h "+(int)(seconds/60%60)+"min "+(int)(seconds%60d)+"s";
	}

	public static void error(Throwable throwable) {
		MinecraftClient c = MinecraftClient.getInstance();
		SupportedServer server = Kuphack.getServer();
		if (c.player != null) c.player.sendMessage(Text.of(
			"§c[Kuphack] Error occured " + (
			server != null ? "maybe relating to " + server
			: "outside of any server"
			) + " (Printed to console)"
		), true);
		throwable.printStackTrace();
	}
	
	public static void addToast(String title, String description) {
		MinecraftClient client = MinecraftClient.getInstance();
		MutableText titleText = Text.literal("Kuphack");
		if (title != null) titleText.append(" | " + title);
		
		SystemToast toast = SystemToast.create(client, SystemToast.Type.PERIODIC_NOTIFICATION,
			Text.of(title), Text.of(description.replace('\n', ' '))
		);
		client.getToastManager().add(toast);
	}
	
	/**
	 * Gets the lore of an item and strips the whitespace from the sides and color
	 * @param stack the stack
	 */
	public static List<String> getStripLore(ItemStack stack) {
		return getLore(stack).stream().map(line -> stripColor(line).strip()).toList();
	}
	
	/**
	 * Gets the lore of an item and strips the whitespace from the sides and color
	 * @param stack the stack
	 */
	public static List<Text> getLore(ItemStack stack) {
		if (!stack.hasNbt() || !stack.getNbt().contains(ItemStack.DISPLAY_KEY, NbtElement.COMPOUND_TYPE))
			return Collections.emptyList();
		NbtCompound display = stack.getNbt().getCompound(ItemStack.DISPLAY_KEY);
        if (display.getType(ItemStack.LORE_KEY) == NbtElement.LIST_TYPE) {
        	List<Text> list = new ArrayList<>();
        	NbtList lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
            for (int i = 0; i < lore.size(); i++) {
                String nbt = lore.getString(i);
                MutableText text = Text.Serializer.fromJson(nbt);
                if (text == null) continue;
                list.add(text);
            }
            return Collections.unmodifiableList(list);
        } else return Collections.emptyList();
	}
	
}