package dev.watukas.kuphack.updater;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import dev.watukas.kuphack.Kuphack;

public record GithubRelease(
	@SerializedName("tag") String tag,
	@SerializedName("version") String version,
	@SerializedName("game_version") @Nullable String gameVersion,
		
	@SerializedName("name") String name,
	@SerializedName("body") String body,
	@SerializedName("url") String url,
	@SerializedName("download") String download
) {

	public boolean tryDownload(File destination) {
		try {
			FileUtils.copyURLToFile(URI.create(this.url).toURL(), destination);
			Kuphack.LOGGER.info("[Kuphack.cc] Downloaded v" + this.version() + " of Kuphack successfully");
			return true;
		} catch (IOException e) {
			Kuphack.LOGGER.error("[Kuphack.cc] Failed at downloading v" + this.version() + " of Kuphack (" + e.getMessage() + ")");
			return false;
		}
	}

	public static List<GithubRelease> retrieve() throws IOException {
		HttpGet request = new HttpGet("https://download.kuphack.cc/releases");
		request.addHeader("content-type", "application/json");
		try (CloseableHttpResponse result = Kuphack.get().getHttpClient().execute(request)) {
			String jsonString = EntityUtils.toString(result.getEntity(), "UTF-8");

			Gson gson = new Gson();
			return Arrays.asList(
				gson.fromJson(jsonString, GithubRelease[].class)
			);
		}
	}

}
