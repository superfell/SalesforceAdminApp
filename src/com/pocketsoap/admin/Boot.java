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

package com.pocketsoap.admin;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;

import com.pocketsoap.salesforce.*;
import com.pocketsoap.salesforce.OAuth2.TokenResponse;

/** This is the Boot/Loader activity it, checks for a saved refresh token, generates a sid, or if that fails, starts the oauth flow */
public class Boot extends Activity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		helper = new BootActivityHelper(this);
        tokenStore = new RefreshTokenStore(this);
        if (!tokenStore.hasSavedToken()) {
        	// no refresh token stored, go straight to login.
        	helper.startLoginActivity();
        }
        // otherwise show the boot screen while we validate the refresh token
        setContentView(R.layout.boot);
    }

	private ActivityHelper helper;
    private RefreshTokenStore tokenStore;
	
	@Override
	public void onResume() {
		super.onResume();
		// start validating the cached ref token.
		TokenRefresherTask tr = new TokenRefresherTask(helper);
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
			if (result.error != null) {
				activity.showError(new IOException(result.error_description));
			} else {
				helper.startUserListActivity(result);
			}
		}
	}

	private static class BootActivityHelper extends ActivityHelper {

		BootActivityHelper(Activity a) {
			super(a, R.string.auth_failed);
		}

		@Override
		public void showError(Exception ex) {
			super.showError(ex);
			startLoginActivity();
		}
	}
}
