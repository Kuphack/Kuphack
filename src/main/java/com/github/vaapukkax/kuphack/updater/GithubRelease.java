package com.github.vaapukkax.kuphack.updater;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.github.vaapukkax.kuphack.Kuphack;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class GithubRelease {

	@SerializedName("html_url")
	private String url;
	
	@SerializedName("tag_name")
	private String version;
	
	@SerializedName("name")
	private String title;

	@SerializedName("body")
	private String body;
	
	@SerializedName("assets")
	private List<GithubReleaseFile> assets;

	public String getTitle() {
		return this.title;
	}

	public String getBody() {
		return this.body;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public String getURL() {
		return this.url;
	}
	
	public List<GithubReleaseFile> getAssets() {
		return Collections.unmodifiableList(this.assets);
	}
	
	public GithubReleaseFile getJarFile() {
		return this.assets.stream()
			.filter(a -> a.getName().endsWith(".jar"))
			.findFirst().orElse(null);
	}
	
	public boolean tryDownload(File destination) {
		try {
			FileUtils.copyURLToFile(new URL(this.url), destination);
			Kuphack.LOGGER.info("[Kuphack.cc] Downloaded v" + this.getVersion() + " of Kuphack successfully");
			return true;
		} catch (IOException e) {
			Kuphack.LOGGER.error("[Kuphack.cc] Failed at downloading v" + this.getVersion() + " of Kuphack (" + e.getMessage() + ")");
			return false;
		}
	}
	
	public static GithubRelease retrieveLatest() throws IOException {
		HttpGet request = new HttpGet("https://api.github.com/repos/kuphack/kuphack/releases");
		request.addHeader("content-type", "application/json");
		try (CloseableHttpResponse result = Kuphack.get().getHttpClient().execute(request)) {
			String jsonString = EntityUtils.toString(result.getEntity(), "UTF-8");

			Gson gson = new Gson();
			return gson.fromJson(
				gson.fromJson(jsonString, JsonArray.class)
			.get(0).getAsJsonObject(), GithubRelease.class);
		}
	}
	
	public class GithubReleaseFile {
		
		@SerializedName("browser_download_url")
		private String download;
		
		@SerializedName("name")
		private String name;

		public String getName() {
			return this.name;
		}
		
		public String getDownloadURL() {
			return this.download;
		}
		
	}
	
}
