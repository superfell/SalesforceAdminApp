package com.pocketsoap.admin;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.widget.Toast;

import com.pocketsoap.salesforce.*;

/** This is the Boot/Loader activity it, checks for a saved refresh token, generates a sid, or if that fails, starts the oauth flow */
public class Boot extends Activity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		
        tokenStore = new RefreshTokenStore(this);
        if (!tokenStore.hasSavedToken()) {
        	// no refresh token stored, go straight to login.
        	startLogin();
        }
        // otherwise show the boot screen while we validate the refresh token
        setContentView(R.layout.boot);
    }

    private RefreshTokenStore tokenStore;

	private void startLogin() {
    	Intent i = new Intent(this, Login.class);
    	startActivity(i);
    	finish();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// start validating the cached ref token.
		TokenRefresher tr = new TokenRefresher();
		tr.execute(tokenStore.getRefreshToken(), tokenStore.getAuthServer());
	}

	/** background task that calls the OAuth Token service to get a new access token using the refresh token */
	private class TokenRefresher extends AsyncTask<String, Void, TokenResponse> {

		@Override
		protected TokenResponse doInBackground(String... params) {
			try {
				return refreshToken(params[0], params[1]);
			} catch (Exception ex) {
				exception = ex;
			}
			return null;
		}
		
		private Exception exception;
		
		private TokenResponse refreshToken(String token, String authHost) throws URISyntaxException, ClientProtocolException, IOException {
			URI tkn = new URI(authHost).resolve("/services/oauth2/token"); 
			Http http = new Http();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("grant_type", "refresh_token"));
			params.add(new BasicNameValuePair("refresh_token", token));
			params.add(new BasicNameValuePair("format", "json"));
			params.add(new BasicNameValuePair("client_id", Login.CLIENT_ID));
			TokenResponse tr = http.postWithJsonResponse(tkn, params, null, TokenResponse.class);
			return tr;
		}
		
		protected void onPostExecute(TokenResponse result) {
			if (exception != null || result.error != null) {
				showTokenError(result, exception);
			} else {
				startUserListActivity(result);
			}
		}
	}

	private void showTokenError(TokenResponse res, Exception ex) {
        Toast.makeText(
                this, 
                getString(R.string.auth_failed, res != null ? res.error_description : ex.getMessage()), 
                Toast.LENGTH_LONG ).show();
        startLogin();
	}
	
	private void startUserListActivity(TokenResponse result) {
		Intent i = new Intent(this, UserListActivity.class);
		i.putExtra(SalesforceApi.EXTRA_SID, result.access_token);
		i.putExtra(SalesforceApi.EXTRA_SERVER, result.instance_url);
		startActivity(i);
		finish();
	}
	
    @JsonIgnoreProperties(ignoreUnknown=true)
	static class TokenResponse {
    	public String error;
    	public String error_description;
        public String refresh_token;
        public String access_token;
        public String instance_url;
        public String id;
	}
}
