// Copyright (c) 2011 Simon Fell
//
// Permission is hereby granted, free of charge, to any person obtaining a 
// copy of this software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included 
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
// THE SOFTWARE.
//

package com.pocketsoap.salesforce;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.pocketsoap.admin.Login;

/** OAuth2 programatic calls, we only need the call to refresh the access token */
public class OAuth2 extends Http {

	public TokenResponse refreshToken(String token, String authHost) throws IOException, URISyntaxException {
		URI tkn = new URI(authHost).resolve("/services/oauth2/token"); 
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		params.add(new BasicNameValuePair("grant_type", "refresh_token"));
		params.add(new BasicNameValuePair("refresh_token", token));
		params.add(new BasicNameValuePair("format", "json"));
		params.add(new BasicNameValuePair("client_id", Login.CLIENT_ID));
		return postWithJsonResponse(tkn, params, null, TokenResponse.class);
	}
	
    @JsonIgnoreProperties(ignoreUnknown=true)
	public static class TokenResponse {
    	public String error;
    	public String error_description;
        public String refresh_token;
        public String access_token;
        public String instance_url;
        public String id;
	}
}
