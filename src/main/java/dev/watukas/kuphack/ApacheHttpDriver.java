package dev.watukas.kuphack;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.github.vaapukkax.minehut.drivers.HttpDriver;

public class ApacheHttpDriver implements HttpDriver {

	private final CloseableHttpClient connection;
	private Map<String, String> headers = new HashMap<>();
	
	public ApacheHttpDriver(CloseableHttpClient connection) {
		this.connection = connection;
	}
	
	@Override
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	
	@Override
	public String get(String path) throws IOException {
		HttpGet request = new HttpGet(path);
		this.headers.forEach((key, value) -> request.addHeader(key, value));
		
		try (CloseableHttpResponse response = connection.execute(request)) {
			HttpEntity entity = response.getEntity();
			if (entity != null)
				return EntityUtils.toString(entity);
			return "{error:\"" + response.getStatusLine().getReasonPhrase() + "\"}";
		}
	}

	@Override
	public String post(String path, Map<String, String> body) throws IOException {
		HttpPost request = new HttpPost(path);
		this.headers.forEach((key, value) -> request.addHeader(key, value));
		
		ArrayList<NameValuePair> parameters = new ArrayList<>();
		body.forEach((key, value) -> parameters.add(new BasicNameValuePair(key, value)));
		request.setEntity(new UrlEncodedFormEntity(parameters));
		
		try (CloseableHttpResponse response = connection.execute(request)) {
			HttpEntity entity = response.getEntity();
			if (entity != null)
				return EntityUtils.toString(entity);
			return "{error:\""+response.getStatusLine().getReasonPhrase()+"\"}";
		}
	}
	
	@Override
	public InputStream openStream(String url) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
