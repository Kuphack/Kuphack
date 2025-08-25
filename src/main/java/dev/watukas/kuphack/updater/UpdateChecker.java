package dev.watukas.kuphack.updater;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import dev.watukas.kuphack.Kuphack;
import dev.watukas.kuphack.modmenu.SettingsKuphackScreen;
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
		GithubRelease latestRelease = GithubRelease.retrieve().getFirst();
		if (!getModVersion().equalsIgnoreCase(latestRelease.version())) {
			boolean settingsScreen = MinecraftClient.getInstance().currentScreen instanceof SettingsKuphackScreen;
			if (isNewer(getModVersion(), latestRelease.version()))
				status = new UpdateStatus(null, Text.literal("Woah, a dev build!").withColor(0xFFFFAAFF), false);
			else if (Kuphack.get().updateOption == CheckOption.CHECK_AND_DOWNLOAD)
				download(latestRelease);
			else if (settingsScreen)
				status = new UpdateStatus(latestRelease, Text.literal("New release was found").withColor(0xFFAAFFAA), true);
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
			status = new UpdateStatus(release, Text.of("Development Environment (" + getModVersion() + ")"), true);
			return;
		}
		if (Kuphack.isFeather()) {
			Kuphack.LOGGER.info("[Kuphack.cc] Manual update for the new Kuphack release is required on Feather");
			status = new UpdateStatus(release, Text.literal("Manual download required on Feather").withColor(0xFFFFFFAA), true);
			return;
		}
		
		File kuphackFile = getModFile();
		if (kuphackFile != null) {
			final File finalFile = kuphackFile;

			download = () -> release.tryDownload(finalFile);
			Kuphack.LOGGER.info("[Kuphack.cc] New version of kuphack.cc will be downloaded to " + kuphackFile.getAbsolutePath());
			status = new UpdateStatus(release, Text.literal("Update will download automatically on close").withColor(0xFFAAFFAA), false);
		} else {
			// accept that we didn't succeed and show a pop-up
			Kuphack.LOGGER.warn("[Kuphack.cc] Couldn't find mod file for updating automatically");
			status = new UpdateStatus(release, Text.literal("Manual download of the update is required").withColor(0xFFFFCCAA), true);
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
		if (status == null || status.release() == null || notified) // whether the player can be notified
			return;
		MutableText text = Text.literal("§6[Kuphack.cc] §e| §fnew Kuphack release! " + getModVersion() + " -> " + status.release().tag());
		text.append(" (").append(Text.literal("Open Release").styled(style -> style
			.withUnderline(true)
			.withHoverEvent(new HoverEvent.ShowText(Text.of("Click Here!")))
			.withClickEvent(new ClickEvent.OpenUrl(URI.create(status.release().url())))))
		.append(")");
		text.append("\n" + status.release().body().replace("\r", ""));
		if (status.additional() != null) text.append("\n§6[Kuphack.cc] §e| §f" + status.additional());
		
		client.getToastManager().add(new UpdateToast(status));
		
		client.inGameHud.getChatHud().addMessage(text);
		notified = true;
	}

	protected static boolean isNewer(String v1, String v2) {
		v1 = v1.replaceAll("[^\\d.]", "");
		v2 = v2.replaceAll("[^\\d.]", "");

		String[] p1 = v1.split("\\.");
		String[] p2 = v2.split("\\.");

		int len = Math.max(p1.length, p2.length);
		for (int i = 0; i < len; i++) {
			int n1 = i < p1.length ? Integer.parseInt(p1[i]) : 0;
			int n2 = i < p2.length ? Integer.parseInt(p2[i]) : 0;

			if (n1 != n2) {
				return n1 > n2;
			}
		}
		return false;
	}

}