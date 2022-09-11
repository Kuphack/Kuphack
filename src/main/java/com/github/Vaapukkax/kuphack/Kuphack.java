package com.github.Vaapukkax.kuphack;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.Vaapukkax.kuphack.events.ChatEvent;
import com.github.Vaapukkax.kuphack.events.ServerJoinEvent;
import com.github.Vaapukkax.kuphack.finder.MinehutButtonState;
import com.github.Vaapukkax.kuphack.flagclash.FlagBreakTime;
import com.github.Vaapukkax.kuphack.flagclash.FlagLocation;
import com.github.Vaapukkax.kuphack.flagclash.FriendFeature;
import com.github.Vaapukkax.kuphack.updater.UpdateChecker;
import com.github.Vaapukkax.minehut.Minehut;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.icu.text.DecimalFormat;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class Kuphack implements ModInitializer, EventListener {

	public static final Logger LOGGER = LoggerFactory.getLogger("kuphack");
	private static Kuphack instance;
	
	private Minehut minehut;
	private final ArrayList<Feature> features = new ArrayList<>();
	
	public MinehutButtonState mhButtonState = isFeather() ? MinehutButtonState.LEFT_CORNER : MinehutButtonState.RIGHT_CORNER;
	public boolean autoUpdate = false;
	
	private Servers server;
	private long customCheckTimeout = -1;
	
	@Override
	public void onInitialize() {
		Kuphack.instance = this;
		minehut = new Minehut();
		minehut.setHttpDriver(new ApacheHttpDriver());
		
		Event.register(this);
		
		// FLAGCLASH
		features.add(new FlagBreakTime());
		features.add(new FlagLocation());
		features.add(new FriendFeature());
		
		// LOBBY
		features.add(new AdBlockFeature());
		features.add(new ServerListReplacement());

		// Multiplayer Button Setting
		Gson gson = new Gson();
		JsonObject object = new JsonObject();
		try {
			object = gson.fromJson(Kuphack.get().readDataFile(), JsonObject.class);
		} catch (Exception e) {}
		if (object != null) {
			if (object.has("mhButtonState"))
				mhButtonState = MinehutButtonState.valueOf(object.get("mhButtonState").getAsString());
			if (object.has("auto-update"))
				autoUpdate = object.get("auto-update").getAsBoolean();
		}
		
		ClientTickEvents.START_CLIENT_TICK.register(new StartTick() {

			ServerInfo info = null;
			
			@Override
			public void onStartTick(MinecraftClient client) {
				boolean debug = FabricLoader.getInstance().isDevelopmentEnvironment();
				if (debug && getServer() != Servers.FLAGCLASH)
					onEvent(new ServerJoinEvent(new ServerInfo("FlagClash", "fungify.minehut.gg", false)));

				if (client.isInSingleplayer() && !debug) setServer(null);
				
				if (System.currentTimeMillis()-customCheckTimeout > 3000) {
					for (Servers server : Servers.values()) {
						if (getServer() != server && server.test(client)) {
							setServer(server);
							break;
						}
					}
				}

				ServerInfo info = client.getCurrentServerEntry();
				if (this.info != info) {
					if (info != null) {
						ServerJoinEvent event = new ServerJoinEvent(info);
						Event.call(event);
					}
					this.info = info;
				}
				
				UpdateChecker.sendCheckerStatus();
			}
			
		});
		new Thread(() -> {
			try {
				if (autoUpdate) UpdateChecker.checkAndDownload();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public Minehut getMinehut() {
		return this.minehut;
	}
	
	private void setServer(Servers server) {
		if (this.server == server) return;
		customCheckTimeout = System.currentTimeMillis();
		
		for (Feature feature : features) {
			if (feature.isOnServer()) feature.onDisable();
		}
		
		this.server = server;
		
		for (Feature feature : features) {
			if (feature.isOnServer()) feature.onEnable();
		}
	}

	public String readDataFile() {
		try (BufferedReader reader = Files.newBufferedReader(getDataFile(), Charset.defaultCharset())) {
			return reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public Path getDataFile() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("kuphack.json");
		
		try {
			if (!Files.exists(path)) {
				Files.createFile(path);
				Files.setAttribute(path, "dos:hidden", true);
			}
		} catch (IOException e) {
			e.printStackTrace();
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
	
	public void onEvent(ServerJoinEvent e) {
		if (e.getInfo().address.toLowerCase().endsWith(".minehut.gg")) {
			String address = e.getInfo().address.toUpperCase();
			String name = address.substring(0, address.indexOf("."));
			
			try {
				setServer(Servers.valueOf(name));
				return;
			} catch (IllegalArgumentException ex) {}
		} else if (e.getInfo().address.toLowerCase().startsWith("minehut.com")) {
			setServer(Servers.LOBBY);
			return;
		}
		setServer(null);
		
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			minehut.close();
			UpdateChecker.continueDownload();
		});
	}
	
	public void onEvent(ChatEvent e) {
		if (isOnMinehut()) {
			if (e.getMessage().getString().equals("\u00a73Sending you to the lobby!")) {
				setServer(Servers.LOBBY);
			} else if (getServer() == Servers.LOBBY) {
				String message = e.getMessage().getString();
				if (message.startsWith("Sending you to ") && message.endsWith("!")) {
					String name = e.getMessage().getString().substring(15, e.getMessage().getString().length()-1);
					try {
						setServer(Servers.valueOf(name.toUpperCase()));
					} catch (IllegalArgumentException exc) {
						LOGGER.info("Kuphack.cc doesn't support: "+name.toUpperCase());
						setServer(null);
					}
				}
			}
		}
	}
	
	public static boolean isFeather() {
		return FabricLoader.getInstance().getModContainer("feather").isPresent();
	}
	
	public static Kuphack get() {
		return instance;
	}
	
	private static boolean isOnMinehut(ServerInfo info) {
		return info != null && (info.address.toLowerCase().contains("minehut") || info.address.startsWith("172.65.244.181"));
	}
	
	public static boolean isOnMinehut() {
		MinecraftClient client = MinecraftClient.getInstance();
		return isOnMinehut(client.getCurrentServerEntry());
	}
	
	public static String round(double value) {
		DecimalFormat df = new DecimalFormat("#.#");
//		df.setRoundingMode(RoundingMode.DOWN);
		return df.format(value).replace(",", ".");
	}
	
	public static Servers getServer() {
		if (get() == null) return null;
		return get().server;
	}
	
	public static ItemStack getHolding(PlayerEntity player) {
		PlayerInventory inv = player.getInventory();
		return inv.getStack(inv.selectedSlot);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Text> getScoreboard() {
		ArrayList<Text> texts = new ArrayList<>();
		MinecraftClient m = MinecraftClient.getInstance();
		Scoreboard scoreboard = m.world.getScoreboard();
		if (scoreboard == null) return texts;
		
		Iterator<ScoreboardObjective> os = scoreboard.getObjectives().iterator();
		if (!os.hasNext()) return texts;
		ScoreboardObjective objective = os.next();

	      Collection<ScoreboardPlayerScore> collectionf = scoreboard.getAllPlayerScores(objective);
	      List<ScoreboardPlayerScore> list = (List<ScoreboardPlayerScore>)collectionf.stream().filter((score) -> {
	         return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
	      }).collect(Collectors.toList());
	      Object collection;
	      if (list.size() > 15) {
	         collection = Lists.newArrayList(Iterables.skip(list, collectionf.size() - 15));
	      } else {
	         collection = list;
	      }

	      List<Pair<ScoreboardPlayerScore, Text>> list2 = Lists.newArrayListWithCapacity(((Collection)collection).size());

	      ScoreboardPlayerScore scoreboardPlayerScore;
	      MutableText text2;
	      for(Iterator var11 = ((Collection)collection).iterator(); var11.hasNext();) {
	         scoreboardPlayerScore = (ScoreboardPlayerScore)var11.next();
	         Team team = scoreboard.getPlayerTeam(scoreboardPlayerScore.getPlayerName());
	         text2 = Team.decorateName(team, Text.literal(scoreboardPlayerScore.getPlayerName()));
	         list2.add(Pair.of(scoreboardPlayerScore, text2));
	      }
	      Iterator var18 = list2.iterator();

	      while(var18.hasNext()) {
	         Pair<ScoreboardPlayerScore, Text> pair = (Pair)var18.next();
	         Text text3 = (Text)pair.getSecond();
	         texts.add(text3);
	      }
	      return texts;
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
	
	public static String stripColor(String message, char color) {
		if (message.isBlank()) return message;
		return message.replaceAll("(?i)"+color+"[0-9A-FK-ORX]", "");
	}
	
	public static String translateColor(String message) {
		if (message.isBlank()) return message;
		return message.replaceAll("&(?=([a-fA-F0-9]|k|l|m|n|o|r))", "\u00a7");
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
		return (int)(seconds/60/60)+"h "+(int)(seconds/60%60)+"m "+(int)(seconds%60d)+"s";
	}
	
}