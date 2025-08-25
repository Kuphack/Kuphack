package dev.watukas.kuphack;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.vaapukkax.minehut.Minehut;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;

import dev.watukas.kuphack.Event.EventHolder;
import dev.watukas.kuphack.Event.EventMention;
import dev.watukas.kuphack.events.ChatEvent;
import dev.watukas.kuphack.events.ServerJoinEvent;
import dev.watukas.kuphack.finder.MinehutButtonState;
import dev.watukas.kuphack.flagclash.FlagBreakTimeFeature;
import dev.watukas.kuphack.flagclash.FlagClash;
import dev.watukas.kuphack.flagclash.FlagLocation;
import dev.watukas.kuphack.flagclash.FriendFeature;
import dev.watukas.kuphack.flagclash.ItemEntityInfoFeature;
import dev.watukas.kuphack.flagclash.RevokerRadiusFeature;
import dev.watukas.kuphack.updater.CheckOption;
import dev.watukas.kuphack.updater.UpdateChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
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
		
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(5000)
			.setSocketTimeout(5000)
			.build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setValidateAfterInactivity(5000);
		
		this.httpClient = HttpClients.custom()
			.setDefaultRequestConfig(config)
			.setConnectionManager(cm)
			.build();
		
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
		features.add(new RevokerRadiusFeature());
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
					Event.call(new ServerJoinEvent(new ServerInfo("FlagClash", "flagclash.minehut.gg", ServerType.OTHER)));
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
				
				if (server != null)
					UpdateChecker.sendCheckerStatus();
			}
		});

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
					LOGGER.info("Kuphack.cc doesn't support: " + name.toUpperCase());
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
		return inv.getStack(inv.getSelectedSlot());
	}
	
    private static final Comparator<ScoreboardEntry> SCOREBOARD_ENTRY_COMPARATOR = Comparator.comparing(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);
	
	public static List<Text> getScoreboard() {
		ArrayList<Text> texts = new ArrayList<>();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null)
			return texts;
		Scoreboard scoreboard = client.world.getScoreboard();
		if (scoreboard == null) return texts;

		ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
		if (objective == null) return texts;
		
        @Environment(value=EnvType.CLIENT)
        record SidebarEntry(Text name, Text score, int scoreWidth) { }
        SidebarEntry[] sidebarEntries = (SidebarEntry[])scoreboard.getScoreboardEntries(objective).stream().filter(score -> !score.hidden()).sorted(SCOREBOARD_ENTRY_COMPARATOR).map(scoreboardEntry -> {
            Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
            Text text = scoreboardEntry.name();
            MutableText text2 = Team.decorateName(team, text);
            MutableText text3 = scoreboardEntry.formatted(BlankNumberFormat.INSTANCE);
            int i = client.textRenderer.getWidth(text3);
            return new SidebarEntry(text2, text3, i);
        }).toArray(size -> new SidebarEntry[size]);

		for (SidebarEntry entry : sidebarEntries) {
			texts.add(entry.name());
		}
		return texts;
	}
	
	protected static List<Text> getModifiedSidebar() {
		List<Text> lines = Kuphack.getScoreboard();
		
		try {
			
			if (getServer() == SupportedServer.FLAGCLASH) {
				
				long upgradeCost = FlagClash.getUpgradeCost();
				if (upgradeCost > 0) {
					MutableText content = Text.literal(FlagClash.toSmallText(" Upgrade: ")).withColor(0xDCD2B8)
						.append(
							Text.literal(FlagClash.toVisualValue(upgradeCost)).withColor(0x28d283)
						);
		
			        if (Kuphack.get().getFeature(FlagLocation.class).isFlagDown()) {
			        	
			        	double time = FlagClash.getUpgradeTime();
			        	if (time != -1) {
			        		if (time > 0) {
				        		content.append(Text.literal(
				        			" (" + FlagClash.timeAsString(time) + ")"
				        		).withColor(0x9bdece));
			        		} else {
			        			int color = Color.HSBtoRGB((System.currentTimeMillis() % 1000) / 1000.0f, 1.0f, 1.0f);
				        		content.append(Text.literal(
				        			" (Upgrade!)"
				        		).withColor(color));
			        		}
			        	}
			        }
		        	
			        lines.add(lines.size() - 2, content);
		        }		        
			}
			
			lines.replaceAll(text -> {
				if (text.getString().contains("flagclash"))
					return Text.literal(" kuphack.cc").withColor(0xFF545454);
				return text;
			});
			
		} catch (Exception e) {
			Kuphack.error(e);
		}
		
		return lines;
	}
	
    public static void renderSidebar(DrawContext context, ScoreboardObjective objective) {
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
        for (Text line : Lists.reverse(lines)) {
            int y = bottom - ++i * textRenderer.fontHeight;
            int right = scaledWidth - 1;
            context.fill(textX - 2, y, right, y + textRenderer.fontHeight, footerAlpha);
            context.drawText(textRenderer, line, textX, y, -1, false);

            if (i != lines.size()) continue;
            context.fill(textX - 2, y - textRenderer.fontHeight - 1, right, y - 1, tabAlpha);
            context.fill(textX - 2, y - 1, right, y, footerAlpha);
            context.drawText(textRenderer, title, (textX + width / 2 - titleWidth / 2), (y - textRenderer.fontHeight), -1, false);
        }
        
	}
	
	public static void renderText(List<Text> lines, MatrixStack matrices, VertexConsumerProvider consumer) {
		MinecraftClient client = MinecraftClient.getInstance();
		final int light = 255;
		
		matrices.push();
		matrices.scale(-0.025F, -0.025F, -0.025F);
		matrices.multiply(new Quaternionf().rotateY(client.gameRenderer.getCamera().getYaw() / -MathHelper.DEGREES_PER_RADIAN));
		
		Matrix4f matrix4f = matrices.peek().getPositionMatrix();
		TextRenderer textRenderer = client.textRenderer;
		for (int i = 0; i < lines.size(); i++) {
			OrderedText text = lines.get(i).asOrderedText();
			float h = (float) (-textRenderer.getWidth(text) / 2);
			textRenderer.drawWithOutline(text, h, (i - lines.size() + 1) * (textRenderer.fontHeight + 2), -1, 0xFF440000, matrix4f, consumer, light);
		}
		
		matrices.pop();
	}
	
	private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");
	
	public static String stripColor(String message) {
		if (message.isBlank()) return message;
		return COLOR_PATTERN.matcher(message).replaceAll("");
	}
	
	public static String stripColor(Text message) {
		return stripColor(message.getString());
	}
	
	public static String stripMiniColor(String message) {
		if (message.isBlank())
			return message;
		try {
			return MiniMessage.miniMessage().stripTags(stripColor(message));
		} catch (ParsingException e) {
			LOGGER.warn(e.getMessage());
			return "";
		}
	}
	
	public static Text translateColor(String message) {
		if (message.isBlank())
			return Text.empty();
		try {
			return MinecraftClientAudiences.of().asNative(
				MiniMessage.miniMessage().deserialize(message.replace('§', '&'))
			);
		} catch (ParsingException e) {
			LOGGER.warn(e.getMessage());
			return Text.empty();
		}
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
		return stack.get(DataComponentTypes.LORE).lines();
	}
	
}