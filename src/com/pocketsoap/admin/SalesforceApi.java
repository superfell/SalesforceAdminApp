package com.pocketsoap.admin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.codehaus.jackson.type.TypeReference;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Intent;
import android.net.Uri;
import android.util.Xml;

import com.pocketsoap.http.Http;

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
	
	public static class Error {
		public String errorCode;
		public String errorMessage;
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
			soql.append("limit 10");	// nothing recent, just grab the first 10
		} else {
			soql.append("where id in (");
			for (UserBasic b : ur.recentItems)
				soql.append("'").append(b.Id).append("',");
			soql.deleteCharAt(soql.length()-1);
			soql.append(") order by lastname,firstname");
		}
		return userSoqlQuery(soql.toString());
	}
	
	public List<User> usernameSearch(String searchTerm, int limit) throws IOException {
		String soql = USER_QUERY + "where username like '%" + searchTerm + "%' limit " + limit;
		return userSoqlQuery(soql);
	}
	
	private List<User> userSoqlQuery(String soql) throws IOException {
		URI q = restRoot.resolve("query?q=" + Uri.encode(soql));
		return getJson(q, UserQueryResult.class).records;
	}
	
	private Map<String, String> getStandardHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/json");
		headers.put("Authorization", "OAuth " + sessionId);
		return headers;
	}
	
	protected <T> T getJson(URI uri, Class<T> responseClz) throws IOException {
		return this.getWithJsonResponse(uri, getStandardHeaders(), responseClz);
	}
	
	public void patchSObjectJson(String type, String id, final Map<String, Object> props) throws IOException {
		ContentProducer json = new ContentProducer() {
			public void writeTo(OutputStream os) throws IOException {
				mapper.writeValue(os, props);
			}
		};
		URI uri = restRoot.resolve("sobjects/" + type + "/" + id);
		EntityTemplate jsonEntity = new EntityTemplate(json);
		jsonEntity.setContentType("application/json");
		this.patchWithJsonResponse(uri, jsonEntity, getStandardHeaders(), User.class);
	}
	
	protected void handleErrorResponse(HttpResponse resp) throws IOException {
		List<Error> errors = mapper.readValue(resp.getEntity().getContent(), new TypeReference<List<Error>>() {});
		throw new IOException(errors.get(0).errorMessage);
	}
	
	public void resetPassword(final String userId) throws IOException {
		// can only do this via soap
		URI soapUri = instance.resolve("/services/Soap/u/21.0");
		SoapProducer rp = new SoapProducer(sessionId) {
			@Override
			protected void writeBody(XmlSerializer x) throws IOException {
				x.startTag(PARTNER_NS, "resetPassword");
				writeElem(x, PARTNER_NS, "userId", userId);
				x.endTag(PARTNER_NS, "resetPassword");
			}
		};
		EntityTemplate resetPwdRequestBody = new EntityTemplate(rp);
		resetPwdRequestBody.setContentType("text/xml; charset=UTF-8");
		HttpPost post = new HttpPost(soapUri);
		post.setEntity(resetPwdRequestBody);
		post.addHeader("SOAPAction", "\"\"");
		HttpResponse res = client.execute(post);
		InputStreamReader rdr = new InputStreamReader(res.getEntity().getContent(), "UTF-8");
		try {
			SoapFaultHandler handler = new SoapFaultHandler();
			Xml.parse(rdr, handler);
			if (handler.hasSeenSoapFault())
				throw new IOException(handler.getFaultString());
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		} finally {
			res.getEntity().consumeContent();
		}
	}
}
