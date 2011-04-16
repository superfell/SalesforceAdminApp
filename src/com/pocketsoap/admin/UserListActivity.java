package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.pocketsoap.admin.SalesforceApi.User;

public class UserListActivity extends ListActivity implements OnEditorActionListener {

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
		try {
			salesforce = new SalesforceApi(getIntent());
			RecentUserListTask t = new RecentUserListTask();
			t.execute();
		} catch (URISyntaxException e) {
			showError(e);
		}
	}
	
	private void showError(Exception ex) {
        Toast.makeText(
                this, 
                "api request failed: " + ex.getMessage(), 
                Toast.LENGTH_LONG ).show();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent d = new Intent(this, UserDetailActivity.class);
		d.putExtras(getIntent());
		try {
			d.putExtra(UserDetailActivity.EXTRA_USER_JSON, new ObjectMapper().writeValueAsString(v.getTag()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		startActivity(d);
	}

	private <T> void bindUserList(List<User> users) {
		UserListAdapter a = new UserListAdapter(this, R.layout.user_row, users);
		this.setListAdapter(a);
	}
	
	private class UserListAdapter extends ArrayAdapter<User> {

		public UserListAdapter(Context context, int textViewResourceId, List<User> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.user_row, null);
			User u = getItem(position);
			setText(convertView, R.id.user_name, u.Name);
			setText(convertView, R.id.user_username, u.Username);
			setText(convertView, R.id.user_title, u.Title);
			convertView.setTag(u);
			return convertView;
		}
		
		private void setText(View v, int textViewId, String text) {
			TextView tv = (TextView)v.findViewById(textViewId);
			tv.setText(text);
		}
	}
	
	/** base class for doing background API calls, and updating the UI with results, standardized error handling */
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
	
	/** background task to fetch the recent users list, and then bind it to the UI */
	private class RecentUserListTask extends ApiAsyncTask<Void, List<User>> {

		@Override
		protected List<User> doApiCall(Void ... params) throws IOException {
			return salesforce.getRecentUsers();
		}
		
		@Override
		protected void handleResult(List<User> result) {
			bindUserList(result);
		}
	}

	/** background task to run the search query, and bind the results to the UI */
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
	
	/** called when the user clicks the search button or enter on the keyboard */
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		UserSearchTask t = new UserSearchTask();
		t.execute(v.getText().toString());
		return true;
	}
}
