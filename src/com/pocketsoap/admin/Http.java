package com.pocketsoap.admin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

/** 
 * Wrapper for basic HTTP/JSON operations.
 */
public class Http {

	public Http() {
		client = getHttpClient();
		mapper = new ObjectMapper();
	}
	
	protected HttpClient client;
	protected ObjectMapper mapper;
	
	public <T> T postWithJsonResponse(URI uri, List<NameValuePair> params, Class<T> responseClz) throws IOException {
		HttpPost post = new HttpPost(uri);
		HttpEntity e = new UrlEncodedFormEntity(params, "UTF-8");
		String r = EntityUtils.toString(e);
		Log.i("POST", r);
		post.setEntity(e);
		return parse(post, responseClz);
	}
	
	public <T> T getJson(URI uri, Map<String, String> httpHeaders, Class<T> responseClz) throws IOException {
		HttpGet get = new HttpGet(uri);
		if (httpHeaders != null) {
			for (Map.Entry<String, String> hdr : httpHeaders.entrySet())
				get.addHeader(hdr.getKey(), hdr.getValue());
		}
		return parse(get, responseClz);
	}

	protected <T> T parse(HttpRequestBase request, Class<T> responseClz) throws IOException {
		HttpResponse resp = client.execute(request);
		try {
			String x = EntityUtils.toString(resp.getEntity());
			Log.i("json", x);
			return mapper.readValue(x, responseClz);
		} finally {
			resp.getEntity().consumeContent();
		}
	}

	private static DefaultHttpClient getHttpClient() {
	    HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
        HttpConnectionParams.setSoTimeout(httpParameters, 30000);
        return new DefaultHttpClient(new ThreadSafeClientConnManager(httpParameters, getRegistry()), httpParameters);	    
	}
	
	private static SchemeRegistry getRegistry() {
        SchemeRegistry reg = new SchemeRegistry();
        reg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        reg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        return reg;
	}
	
}
