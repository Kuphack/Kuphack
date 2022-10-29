package com.github.Vaapukkax.kuphack.updater;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.github.Vaapukkax.kuphack.Kuphack;
import com.github.Vaapukkax.kuphack.modmenu.SettingsKuphackScreen;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class UpdateChecker {

	private static UpdateStatus status;
	private static Runnable download;
	
	public static void checkAndDownload() throws Exception {
		String latestRelease = getLatestRelease();
		String latestVersionTag = getLatestVersion(latestRelease);
		if (!getCurrentTag().equalsIgnoreCase(latestVersionTag)) {
			if (Kuphack.get().autoUpdate || MinecraftClient.getInstance().currentScreen instanceof SettingsKuphackScreen)
				download(latestRelease.replace("tag", "download") + "/kuphack.jar");
			else status = new UpdateStatus("New version of Kuphack is available: "+latestVersionTag, null);
		}
	}

	public static String getCurrentTag() {
		return FabricLoader.getInstance().getModContainer("kuphack").get()
			.getMetadata().getVersion().getFriendlyString();
	}
	
	/**
	 * @return e.g. "https://github.com/Kuphack/Kuphack/releases/tag/6.9.2" -> "6.9.2"
	 */
	public static String getLatestVersion(String release) {
		String version = release.substring(release.lastIndexOf("/") + 1, release.length());
		return version;
	}

	/**
	 * @return e.g. "https://github.com/Kuphack/Kuphack/releases/tag/6.9.2"
	 * @throws IOException if the request failed
	 */
	public static String getLatestRelease() throws IOException {
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet("https://api.github.com/repos/kuphack/kuphack/releases");
			request.addHeader("content-type", "application/json");
			try (CloseableHttpResponse result = httpClient.execute(request)) {
				String jsonString = EntityUtils.toString(result.getEntity(), "UTF-8");

				return JsonParser.parseString(jsonString).getAsJsonArray()
					.get(0).getAsJsonObject()
					.get("html_url").getAsString();
			}
		}
	}

	/**
	 * Does all kinds of trickery to download the file. May show a pop-up instead for the link instead.
	 * If the project is in a development environment the method won't even try downloading and instead it'll just print out a nice message
	 * @param url the url to download from
	 */
	public static void download(String url) throws IOException {
		MinecraftClient c = MinecraftClient.getInstance();
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			status = new UpdateStatus("Dev: v"+getCurrentTag()+" Latest: v"+getLatestVersion(getLatestRelease()), () -> {
				if (c.world == null) open(url);
			});
			return;
		}
		
		if (Kuphack.isFeather()) {
			Kuphack.LOGGER.info("[Kuphack.cc] can't download on feather");
			status = new UpdateStatus("Can't auto update on Feather, please download it automatically", () -> open(url));
			return;
		}
		
		File kuphackFile = null;
		try {
			// Try getting the mod file with the Fabric API
			kuphackFile = FabricLoader.getInstance().getModContainer("kuphack").get().getOrigin().getPaths().iterator().next().toFile();
		} catch (Throwable e) {
			// Try getting the mod file from the run directory and entering the mods directory from there
			e.printStackTrace();
			File modFolder = new File(c.runDirectory, "mods");
			if (!modFolder.exists() || !modFolder.isDirectory()) {
				Kuphack.LOGGER.info("[Kuphack.cc] Couldn't find mods folder for updating wat??");
				status = new UpdateStatus("Couldn't download the new version of kuphack automatically", () -> open(url));
				return;
			}
			
			File[] modFiles = modFolder.listFiles(file -> {
				return !file.isDirectory() && file.getName().toLowerCase().contains("kuphack");
			});
			
			kuphackFile = modFiles.length > 0 ? modFiles[0] : null;
		}
		
		if (kuphackFile != null) {
			// Download the mod like a boss
			final File finalFile = kuphackFile;

			download = () -> {
				try {
					FileUtils.copyURLToFile(new URL(url), finalFile);
					Kuphack.LOGGER.info("[Kuphack.cc] Downloaded the new version of kuphack successfully");
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
			Kuphack.LOGGER.info("[Kuphack.cc] New version of kuphack.cc will be downloaded to "+kuphackFile.getAbsolutePath());
			status = new UpdateStatus("New version of kuphack has been released! Automatically downloading after closing", null);
		} else {
			// Accept that the mod didn't succeed and show a pop-up
			Kuphack.LOGGER.info("[Kuphack.cc] couldn't find mod file for updating automatically");
			status = new UpdateStatus("Mod couldn't be updated since the mod couldnt find itself? lmao", () -> open(url));
		}
	}
	
	// Open a pop-up to ask confirmation for opening a link
	private static void open(String url) {
		MinecraftClient c = MinecraftClient.getInstance();
		Screen oldScreen = c.currentScreen;
		try {
			c.setScreen(new ConfirmLinkScreen((bl) -> {
	            if (bl)
	                Util.getOperatingSystem().open(url);
	            c.setScreen(oldScreen);
	        }, url, true));
		} catch (NoClassDefFoundError e) {
			Util.getOperatingSystem().open(url);
		}
	}
	
	public static void continueDownload() {
		if (download != null) {
			download.run();
			download = null;
		}
	}
	
	public static UpdateStatus takeCheckerStatus() {
		UpdateStatus saved = status;
		status = null;
		return saved;
	}
	
	public static void sendCheckerStatus() {
		MinecraftClient c = MinecraftClient.getInstance();
		if (status != null && c.player != null) {
			c.player.sendMessage(Text.of(" \n§6[Kuphack.cc] §e| §f"+status.text()+"\n "), false);
			if (status.runnable() != null) status.runnable().run();
			status = null;
		}
	}

}