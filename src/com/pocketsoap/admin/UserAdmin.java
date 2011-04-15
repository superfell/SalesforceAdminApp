package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.pocketsoap.admin.SalesforceApi.User;
import com.pocketsoap.admin.SalesforceApi.UserBasic;
import com.pocketsoap.admin.SalesforceApi.UserResource;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class UserAdmin extends ListActivity implements OnEditorActionListener {

	private SalesforceApi salesforce;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.user_admin);
		EditText search = (EditText)findViewById(R.id.search_text);
		search.setOnEditorActionListener(this);
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
		RecentUserListTask t = new RecentUserListTask();
		t.execute();
	}
	
	private void showError(Exception ex) {
        Toast.makeText(
                this, 
                "api request failed: " + ex.getMessage(), 
                Toast.LENGTH_LONG ).show();
	}
	
	private <T> void bindUserList(List<? extends UserBasic> users) {
		UserAdapter a = new UserAdapter(this, R.layout.user_row, users);
		this.setListAdapter(a);
	}
	
	private class UserAdapter<T extends UserBasic> extends ArrayAdapter<T> {

		public UserAdapter(Context context, int textViewResourceId, List<T> objects) {
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
	
	private abstract class ApiAsyncTask<ParamType, ResultType> extends AsyncTask<ParamType, Void, ResultType> {

		private Exception exception;
		
		@Override
		protected void onPreExecute() {
			setProgress(100);
		}
		
		@Override
		protected final ResultType doInBackground(ParamType... params) {
			try {
				return doApiCall(params);
			} catch (Exception ex) {
				exception = ex;
			}
			return null;
		}

		protected abstract ResultType doApiCall(ParamType ... params) throws Exception;
		protected abstract void handleResult(ResultType result);
		
		@Override
		protected final void onPostExecute(ResultType result) {
			setProgress(0);
			if (result != null)
				handleResult(result);
			else	
				showError(exception);
		}
	}
	
	private class RecentUserListTask extends ApiAsyncTask<Void, SalesforceApi.UserResource> {

		@Override
		protected SalesforceApi.UserResource doApiCall(Void ... params) throws IOException {
			return salesforce.getUserResource();
		}
		
		@Override
		protected void handleResult(SalesforceApi.UserResource result) {
			bindUserList(result.recentItems);
		}
	}

	private class UserSearchTask extends ApiAsyncTask<String, List<User>> {

		@Override
		protected List<User> doApiCall(String... params) throws Exception {
			return salesforce.userSearch(params[0], 25);
		}

		@Override
		protected void handleResult(List<User> result) {
			bindUserList(result);
		}
	}
	
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		UserSearchTask t = new UserSearchTask();
		t.execute(v.getText().toString());
		return true;
	}
}
