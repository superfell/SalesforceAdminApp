package com.pocketsoap.salesforce;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.*;
import org.codehaus.jackson.type.TypeReference;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.content.Intent;
import android.net.Uri;
import android.util.Xml;


/**
 * This class exposes all the API calls we want to make to Salesforce.com
 */
public class SalesforceApi extends Http {

	public static final String EXTRA_SERVER = "SVR";
	public static final String EXTRA_SID = "SID";

	public SalesforceApi(Intent i) throws URISyntaxException {
		this(i.getStringExtra(EXTRA_SID), new URI(i.getStringExtra(EXTRA_SERVER)));
	}
	
	public SalesforceApi(String sid, URI instance) {
		this.sessionId = sid;
		this.instance = instance;
		this.restRoot = instance.resolve("/services/data/v21.0/");
	}
	
	private final String sessionId;
	private final URI instance;
	private final URI restRoot;
	
	/** @returns the User SObjects primary resource from the REST API */
	public UserResource getUserResource() throws IOException {
		return getJson(restRoot.resolve("sobjects/user"), UserResource.class);
	}
	
	private static final String USER_QUERY = "select id,name,username,email,title,mobilePhone,phone,smallPhotoUrl,isActive from user ";

	/** @return User details about the recently accessed users, or a default list if there are no recents */
	public List<User> getRecentUsers() throws IOException {
		// there's only Id & Name in the recents list, so we need to get that, and then use the Ids in a query.
		UserResource ur = getUserResource();
		StringBuilder soql = new StringBuilder(USER_QUERY);
		if (ur.recentItems == null || ur.recentItems.size() == 0) {
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

	/** @return a list of users that have the searchTerm in their name */
	public List<User> userSearch(String searchTerm, int limit) throws IOException {
		String soql = USER_QUERY + "where name like '%" + searchTerm + "%' limit " + limit;
		return userSoqlQuery(soql);
	}

	/** makes a PATCH request to update an SObject */
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

	/** performs a pasword reset on the specified userId (using the SOAP API) */
	public void resetPassword(final String userId) throws IOException {
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
	
	protected void handleErrorResponse(HttpResponse resp) throws IOException {
		List<Error> errors = mapper.readValue(resp.getEntity().getContent(), new TypeReference<List<Error>>() {});
		throw new IOException(errors.get(0).errorMessage);
	}
}
