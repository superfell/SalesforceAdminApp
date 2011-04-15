package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.pocketsoap.admin.SalesforceApi.UserBasic;
import com.pocketsoap.admin.SalesforceApi.UserResource;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
		t.execute();
	}
	
	private void bindUserList(List<UserBasic> users) {
		UserAdapter a = new UserAdapter(this, R.layout.user_row, users);
		this.setListAdapter(a);
	}
	
	private class UserAdapter extends ArrayAdapter<UserBasic> {

		public UserAdapter(Context context, int textViewResourceId, List<UserBasic> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.user_row, null);
			UserBasic u = getItem(position);
			TextView tv = (TextView)convertView.findViewById(R.id.user_name);
			tv.setText(u.Name);
			return convertView;
		}
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
			if (result != null)
				bindUserList(result.recentItems);
			// else	
				// show error
		}
	}
}
