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

import com.pocketsoap.salesforce.SalesforceApi;
import com.pocketsoap.salesforce.OAuth2.TokenResponse;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/** 
 * Common stuff that we need to do from multiple activities. 
 *
 * The Activity class hierarchy (Activity/ListActivity) make it impossible to put this in a base class
 */
public class ActivityHelper implements ApiAsyncTask.ActivityCallbacks {
	
	public ActivityHelper(Activity owner) {
		this(owner, R.string.api_failed);
	}
	
	public ActivityHelper(Activity owner, int errorTextId) {
		this.activity = owner;
		this.errorTextId = errorTextId;
	}
	
	private final Activity activity;
	private final int errorTextId;
	
	public void setBusy(boolean b) {
		activity.setProgressBarIndeterminateVisibility(b);
	}

	public void showError(Exception ex) {
		// Our session expired out from under us. go back to the start.
		if (ex instanceof SalesforceApi.MissingSessionException) {
			Intent i = new Intent(activity, Boot.class);
			i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivity(i);
			activity.finish();
		}

		Toast.makeText(activity, 
		                activity.getString(errorTextId, ex.getMessage()),
		                Toast.LENGTH_LONG ).show();
    }

	public void startLoginActivity() {
    	Intent i = new Intent(activity, Login.class);
    	activity.startActivity(i);
    	activity.finish();
	}

	public void startUserListActivity(TokenResponse result) {
		startUserListActivity(result.access_token, result.instance_url);
	}
	
	public void startUserListActivity(String sid, String instanceUrl) {
		Intent i = new Intent(activity, UserListActivity.class);
		i.putExtra(SalesforceApi.EXTRA_SID, sid);
		i.putExtra(SalesforceApi.EXTRA_SERVER, instanceUrl);
		activity.startActivity(i);
		activity.finish();
	}
}
