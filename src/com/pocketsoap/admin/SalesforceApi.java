package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.net.Uri;

/**
 * This class exposes all the API calls we want to make to Salesforce.com
 */
public class SalesforceApi extends Http {

	SalesforceApi(String sid, URI instance) {
		this.sessionId = sid;
		this.instance = instance;
	}
	
	private final String sessionId;
	private final URI instance;
	
	public static class SObjectAttributes {
		public String type;
		public String url;
	}
	
	public static class UserBasic {
		public SObjectAttributes attributes;
		public String Id;
		public String Name;
	}
	
	public static class User extends UserBasic {
		public String Username;
		public String Email;
		public String ProfileId;
	}
	
	public static class UserResource {
		public Map<String, Object> objectDescribe;
		public List<UserBasic> recentItems;
	}
	
	public static class UserQueryResult {
		public int totalSize;
		public boolean done;
		public List<User> records;
	}
	
	public UserResource getUserResource() throws IOException {
		return getJson(instance.resolve("/services/data/v21.0/sobjects/user"), UserResource.class);
	}
	
	public List<User> userSearch(String searchTerm, int limit) throws IOException {
		String soql = "select id,name,username,email,profileId from user where username like '%" + searchTerm + "%' limit " + limit;
		URI q = instance.resolve("/services/data/v21.0/query?q=" + Uri.encode(soql));
		return getJson(q, UserQueryResult.class).records;
	}
	
	protected <T> T getJson(URI uri, Class<T> responseClz) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		headers.put("Authorization", "OAuth " + sessionId);
		return this.getJson(uri, headers, responseClz);
	}
}
