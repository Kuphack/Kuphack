package com.github.vaapukkax.kuphack.updater;

import java.io.File;
import java.io.IOException;

import com.github.vaapukkax.kuphack.Kuphack;
import com.github.vaapukkax.kuphack.modmenu.SettingsKuphackScreen;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class UpdateChecker {

	private static UpdateStatus status;
	private static Runnable download;
	
	private static boolean notified;
	
	public static UpdateStatus checkAndDownload() throws Exception {
		UpdateStatus status = null;
		GithubRelease latestRelease = GithubRelease.retrieveLatest();
		if (!getModVersion().equalsIgnoreCase(latestRelease.getVersion())) {
			@SuppressWarnings("resource")
			boolean settingsScreen = MinecraftClient.getInstance().currentScreen instanceof SettingsKuphackScreen;
			if (Kuphack.get().updateOption == CheckOption.CHECK_AND_DOWNLOAD)
				download(latestRelease);
			else if (settingsScreen)
				status = new UpdateStatus(latestRelease, "New release was found", true);
			else status = new UpdateStatus(latestRelease, null, false);
			notified = false;
		}
		return UpdateChecker.status = status;
	}

	public static String getModVersion() {
		return FabricLoader.getInstance().getModContainer("kuphack").get()
			.getMetadata().getVersion().getFriendlyString();
	}

	/**
	 * Does all kinds of trickery to download the file. May show a pop-up instead of downloading from the link.
	 * If the project is in a development environment the method won't even try downloading and instead it'll only inform you of the versions
	 * @param url the link to download from
	 */
	protected static void download(GithubRelease release) throws IOException {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			status = new UpdateStatus(release, "Development Environment (" + getModVersion() + ")", true);
			return;
		}
		if (Kuphack.isFeather()) {
			Kuphack.LOGGER.info("[Kuphack.cc] Manual update for the new Kuphack release is required on Feather");
			status = new UpdateStatus(release, "Manual download of the update is required on Feather", true);
			return;
		}
		
		File kuphackFile = getModFile();
		if (kuphackFile != null) {
			final File finalFile = kuphackFile;

			download = () -> release.tryDownload(finalFile);
			Kuphack.LOGGER.info("[Kuphack.cc] New version of kuphack.cc will be downloaded to " + kuphackFile.getAbsolutePath());
			status = new UpdateStatus(release, "Update will be downloaded automatically on close", false);
		} else {
			// accept that we didn't succeed and show a pop-up
			Kuphack.LOGGER.warn("[Kuphack.cc] Couldn't find mod file for updating automatically");
			status = new UpdateStatus(release, "Manual download of the update is required", true);
		}
	}
	
	protected static File getModFile() {
		try {
			// try getting the mod file with the Fabric API
			return FabricLoader.getInstance().getModContainer("kuphack").get().getOrigin().getPaths().iterator().next().toFile();
		} catch (Throwable e) {
			Kuphack.LOGGER.warn("Failed to get mod location thru mod container: " + e.getClass().getSimpleName() + " " + e.getMessage());
			MinecraftClient client = MinecraftClient.getInstance();
			
			// try entering the mod directory from the run directory and finding the file from there
			File modFolder = new File(client.runDirectory, "mods");
			if (!modFolder.exists() || !modFolder.isDirectory()) return null;
			
			File[] modFiles = modFolder.listFiles(file -> 
				!file.isDirectory() && file.getName().toLowerCase().contains("kuphack")
			);
			if (modFiles.length > 0) return modFiles[0];
		}
		return null;
	}
	
	public static void continueDownload() {
		if (download == null) return;
		download.run();
		download = null;
	}

	public static void sendCheckerStatus() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) // whether the player is in a world and the message can be sent
			return;
		if (status == null || notified) // whether the player can be notified
			return;
		MutableText text = Text.literal("§6[Kuphack.cc] §e| §fnew Kuphack release! " + getModVersion() + " -> " + status.release().getVersion());
		text.append(" (").append(Text.literal("Open Release").styled(style -> style
			.withUnderline(true)
			.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click Here!")))
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, status.release().getURL()))))
		.append(")");
		text.append("\n" + status.release().getBody().replace("\r", ""));
		if (status.additional() != null) text.append("\n§6[Kuphack.cc] §e| §f" + status.additional());
		
		client.inGameHud.getChatHud().addMessage(text);
		notified = true;
	}

}