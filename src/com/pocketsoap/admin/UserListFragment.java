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
import java.net.URISyntaxException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.*;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;

import com.pocketsoap.salesforce.*;

/**
 * The fragment for the user list / search bar.
 */
public class UserListFragment extends ListFragment implements OnEditorActionListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.user_list, container);
		search = (EditText)view.findViewById(R.id.search_text);
		search.setOnEditorActionListener(this);
		emptyText = (TextView)view.findViewById(android.R.id.empty);
		footerText = (TextView)view.findViewById(R.id.footer_text);
		
		return view;
	}

	private SalesforceApi salesforce;
	private EditText search;
	private TextView emptyText, footerText;
	private TextView listHeaderText;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setRetainInstance(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		addHeaderIfNeeded();
		try {
			salesforce = new SalesforceApi(getActivity().getIntent());
			emptyText.setText(getString(R.string.loading));
			startFetchUsers(search.getText().toString());
		} catch (URISyntaxException e) {
			getActivityHelper().showError(e);
		}
		footerText.setText(salesforce.getInstanceUri().getHost());
	}

	private void addHeaderIfNeeded() {
		if (getListView().getHeaderViewsCount() > 0) return;
		LayoutInflater inf = getActivity().getLayoutInflater();
		// inflate the view for the header view, and add it to the list view
		View header = inf.inflate(R.layout.list_header, null);
		listHeaderText = (TextView)header.findViewById(R.id.list_header_text);
		getListView().addHeaderView(header, null, false);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// the user tapped a row in the list, serialize up the data for that row, and star the detail page activity
		Intent d = new Intent(getActivity(), UserDetailActivity.class);
		d.putExtras(getActivity().getIntent());
		try {
			d.putExtra(UserDetailFragment.EXTRA_USER_JSON, new ObjectMapper().writeValueAsString(v.getTag()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		startActivity(d);
	}

	// build an adapter for this list of users, so they can be rendered in the list view.
	private <T> void bindUserList(List<User> users) {
		UserListAdapter a = new UserListAdapter(getActivity(), R.layout.user_row, users);
		this.setListAdapter(a);
		if (users.size() == 0)
			emptyText.setText(getString(R.string.no_results));
	}
	
	// Adapter/Binder that renders the list view rows.
	private class UserListAdapter extends ArrayAdapter<User> {

		public UserListAdapter(Context context, int textViewResourceId, List<User> objects) {
			super(context, textViewResourceId, objects);
			inf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		private final LayoutInflater inf;
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = inf.inflate(R.layout.user_row, null);
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
	
	/** returns true if we should do a search, or false if we should show the recent list */
	private boolean shouldDoSearch(String searchTerm) {
		return searchTerm != null && searchTerm.trim().length() > 0;
	}
	
	/** background task to run the search query, and bind the results to the UI */
	private class UserSearchTask extends ApiAsyncTask<String, List<User>> {

		UserSearchTask(ActivityCallbacks activity) {
			super(activity);
		}
		
		@Override
		protected List<User> doApiCall(String... params) throws Exception {
			String search = params[0];
			if (shouldDoSearch(search)) 
				return salesforce.userSearch(search, 25);
			return salesforce.getRecentUsers();
		}

		@Override
		protected void handleResult(List<User> result) {
			bindUserList(result);
		}
	}
	
	protected ActivityHelper getActivityHelper() {
		return ((BaseFragmentActivity)getActivity()).getActivityHelper();
	}
	
	protected void startFetchUsers(String searchTerm) {
		int txtId = shouldDoSearch(searchTerm) ? R.string.list_search_results : R.string.list_recent;
		listHeaderText.setText(getString(txtId));
		UserSearchTask t = new UserSearchTask(getActivityHelper());
		t.execute(searchTerm);
	}
	
	/** called when the user clicks the search button or enter on the keyboard */
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		startFetchUsers(v.getText().toString());
		return true;
	}
}
