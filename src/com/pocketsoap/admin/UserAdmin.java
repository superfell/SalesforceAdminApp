package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.pocketsoap.admin.SalesforceApi.UserResource;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class UserAdmin extends ListActivity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.user_admin);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		String sid = getIntent().getStringExtra("SID");
		String svr = getIntent().getStringExtra("SVR");
		
		try {
			salesforce = new SalesforceApi(sid, new URI(svr));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		UserListTask t = new UserListTask();
		t.execute(null);
	}
	
	private SalesforceApi salesforce;
	
	private class UserListTask extends AsyncTask<Void, Void, SalesforceApi.UserResource> {

		@Override
		protected UserResource doInBackground(Void... params) {
			try {
				return salesforce.getUserResource();
			} catch (IOException ex) {
				exception = ex;
				Log.i("ouch", ex.getMessage());
			}
			return null;
		}
		
		private IOException exception;

		@Override
		protected void onPostExecute(UserResource result) {
			for (SalesforceApi.UserBasic u : result.recentItems)
				Log.i("f", u.Name + "\t " + u.Id);
		}
	}
}
