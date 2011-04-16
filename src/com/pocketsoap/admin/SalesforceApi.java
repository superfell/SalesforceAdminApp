package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;

/**
 * This class exposes all the API calls we want to make to Salesforce.com
 */
public class SalesforceApi extends Http {

	static final String EXTRA_SERVER = "SVR";
	static final String EXTRA_SID = "SID";

	SalesforceApi(Intent i) throws URISyntaxException {
		this(i.getStringExtra(EXTRA_SID), new URI(i.getStringExtra(EXTRA_SERVER)));
	}
	
	SalesforceApi(String sid, URI instance) {
		this.sessionId = sid;
		this.instance = instance;
		this.restRoot = instance.resolve("/services/data/v21.0/");
	}
	
	private final String sessionId;
	private final URI instance;
	private final URI restRoot;
	
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
		public String Title;
		public String MobilePhone;
		public String SmallPhotoUrl;
		public boolean IsActive;
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
		return getJson(restRoot.resolve("sobjects/user"), UserResource.class);
	}
	
	private static final String USER_QUERY = "select id,name,username,email,profileId,title,mobilePhone,smallPhotoUrl,isActive from user ";

	/** @return User details about the recently accessed users, or a default list if there are no recents */
	public List<User> getRecentUsers() throws IOException {
		// there's only Id & Name in the recents list, so we need to get that, and then use the Ids in a query.
		UserResource ur = getUserResource();
		StringBuilder soql = new StringBuilder(USER_QUERY);
		if (ur.recentItems.size() == 0) {
			soql.append("limit 20");	// nothing recent, just grab the first 20
		} else {
			soql.append("where id in (");
			for (UserBasic b : ur.recentItems)
				soql.append("'").append(b.Id).append("',");
			soql.deleteCharAt(soql.length()-1);
			soql.append(")");
		}
		return userSoqlQuery(soql.toString());
	}
	
	public List<User> userSearch(String searchTerm, int limit) throws IOException {
		String soql = USER_QUERY + "where username like '%" + searchTerm + "%' limit " + limit;
		return userSoqlQuery(soql);
	}
	
	private List<User> userSoqlQuery(String soql) throws IOException {
		URI q = restRoot.resolve("query?q=" + Uri.encode(soql));
		return getJson(q, UserQueryResult.class).records;
	}
	
	protected <T> T getJson(URI uri, Class<T> responseClz) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		headers.put("Authorization", "OAuth " + sessionId);
		return this.getJson(uri, headers, responseClz);
	}
}
