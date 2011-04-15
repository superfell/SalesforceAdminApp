package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		public String username;
		public String firstName;
		public String lastName;
		public String email;
		public String profileId;
	}
	
	public static class UserResource {
		public Map<String, Object> objectDescribe;
		public List<UserBasic> recentItems;
	}
	
	public UserResource getUserResource() throws IOException {
		return getJson(instance.resolve("/services/data/v21.0/sobjects/user"), UserResource.class);
	}
	
	protected <T> T getJson(URI uri, Class<T> responseClz) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		headers.put("Authorization", "OAuth " + sessionId);
		return this.getJson(uri, headers, responseClz);
	}
}
