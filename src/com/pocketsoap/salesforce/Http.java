package com.pocketsoap.salesforce;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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

	/** make a GET request, and parse the response JSON payload */
	public <T> T getWithJsonResponse(URI uri, Map<String, String> httpHeaders, Class<T> responseClz) throws IOException {
		HttpGet get = new HttpGet(uri);
		return addHeadersAndExecute(get, httpHeaders, responseClz);
	}
	
	/** make a POST request with a set of form params, and parse the response JSON payload */
	public <T> T postWithJsonResponse(URI uri, List<NameValuePair> params, Map<String, String> httpHeaders, Class<T> responseClz) throws IOException {
		HttpPost post = new HttpPost(uri);
		HttpEntity e = new UrlEncodedFormEntity(params, "UTF-8");
		post.setEntity(e);
		return addHeadersAndExecute(post, httpHeaders, responseClz);
	}

	/** make a PATCH request with a request body, and parse the JSON response */
	public <T> T patchWithJsonResponse(URI uri, HttpEntity requestBody, Map<String, String> httpHeaders, Class<T> responseClz) throws IOException {
		HttpPatch patch = new HttpPatch(uri);
		patch.setEntity(requestBody);
		return addHeadersAndExecute(patch, httpHeaders, responseClz);
	}
	
	protected <T> T addHeadersAndExecute(HttpRequestBase request, Map<String, String> httpHeaders, Class<T> responseClz) throws IOException {
		if (httpHeaders != null) {
			for (Map.Entry<String, String> hdr : httpHeaders.entrySet())
				request.addHeader(hdr.getKey(), hdr.getValue());
		}
		return executeAndParse(request, responseClz);
	}

	protected <T> T executeAndParse(HttpRequestBase request, Class<T> responseClz) throws IOException {
		HttpResponse resp = client.execute(request);
		int sc = resp.getStatusLine().getStatusCode();
		try {
			if (sc == HttpStatus.SC_NO_CONTENT) return null;
			if (sc >= 400) handleErrorResponse(resp);
			String x = EntityUtils.toString(resp.getEntity());
			Log.i("json", x);
			return mapper.readValue(x, responseClz);
		} finally {
			if (resp.getEntity() != null)
				resp.getEntity().consumeContent();
		}
	}

	protected void handleErrorResponse(HttpResponse resp) throws IOException {
		
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