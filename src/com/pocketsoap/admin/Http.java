package com.pocketsoap.admin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.map.ObjectMapper;

public class Http {

	public Http() {
		client = getHttpClient();
		mapper = new ObjectMapper();
	}
	
	private HttpClient client;
	private ObjectMapper mapper;
	
	public <T> T postWithJsonResponse(URI uri, List<NameValuePair> params, Class<T> responseClz) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(uri);
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		HttpResponse resp = client.execute(post);
		InputStream respStream = resp.getEntity().getContent();
		try {
			return mapper.readValue(respStream, responseClz);
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
