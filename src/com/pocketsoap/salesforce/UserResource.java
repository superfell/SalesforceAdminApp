package com.pocketsoap.salesforce;

import java.util.List;
import java.util.Map;

/** the result of fetching the main user resource */
public class UserResource {
	public Map<String, Object> objectDescribe;
	public List<UserBasic> recentItems;
}