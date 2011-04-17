package com.pocketsoap.admin;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.pocketsoap.admin.ApiAsyncTask.ActivityCallbacks;
import com.pocketsoap.salesforce.*;
import com.pocketsoap.salesforce.OAuth2.TokenResponse;

/** This is the Boot/Loader activity it, checks for a saved refresh token, generates a sid, or if that fails, starts the oauth flow */
public class Boot extends Activity implements ActivityCallbacks {

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
		TokenRefresherTask tr = new TokenRefresherTask(this);
		tr.execute(tokenStore.getRefreshToken(), tokenStore.getAuthServer());
	}

	/** background task that calls the OAuth Token service to get a new access token using the refresh token */
	private class TokenRefresherTask extends ApiAsyncTask<String, TokenResponse> {

		TokenRefresherTask(ActivityCallbacks activity) {
			super(activity);
		}

		@Override
		protected TokenResponse doApiCall(String... params) throws Exception {
			return new OAuth2().refreshToken(params[0], params[1]);
		}

		@Override
		protected void handleResult(TokenResponse result) {
			if (result.error != null)
				showTokenError(result, null);
			else
				startUserListActivity(result);
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

	public void setBusy(boolean b) {
		// we already have a big spinner, don't need anything else
	}

	public void showError(Exception ex) {
		showTokenError(null, ex);
	}
}
