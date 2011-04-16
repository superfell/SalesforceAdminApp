package com.pocketsoap.admin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.pocketsoap.admin.ApiAsyncTask.ActivityCallbacks;
import com.pocketsoap.salesforce.SalesforceApi;
import com.pocketsoap.salesforce.SalesforceApi.User;

/** Activity that is the user detail page, where they can do a reset password, toggle isActive etc */
public class UserDetailActivity extends Activity implements ActivityCallbacks {

	static final String EXTRA_USER_JSON = "user_json";

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		try {
			this.user = new ObjectMapper().readValue(getIntent().getStringExtra(EXTRA_USER_JSON), User.class);
			this.salesforce = new SalesforceApi(getIntent());
		} catch (IOException e) {
			showError(e);
		} catch (URISyntaxException e) {
			showError(e);
		}
		
		setContentView(R.layout.user_detail);
		setText(R.id.detail_name, user.Name);
		setText(R.id.detail_username, getString(R.string.username, user.Username));
		setText(R.id.detail_title, getString(R.string.title, user.Title == null ? "" : user.Title));
		
		resetPasswordButton = (Button)findViewById(R.id.detail_reset_pwd);
		isActive = (CheckBox)findViewById(R.id.detail_enabled);
		isActive.setChecked(user.IsActive);
		isActive.setOnClickListener(new ToggleActive());
	}
	
	private Button resetPasswordButton;
	private CheckBox isActive;
	private SalesforceApi salesforce;
	private User user;

	private void setText(int textId, String txt) {
		TextView tv = (TextView)findViewById(textId);
		tv.setText(txt);
	}
    
	public void showError(Exception ex) {
        Toast.makeText(
                this, 
                getString(R.string.api_failed, ex.getMessage()),
                Toast.LENGTH_LONG ).show();
	}
	
	public void setBusy(boolean b) {
		setProgressBarIndeterminateVisibility(b);
	}

	/** called when the user taps the reset password button */
	public void resetPasswordClicked(View v) {
		ResetPasswordTask t = new ResetPasswordTask(this);
		t.execute(user.Id);
	}

	/** called when the user taps the IsActive checkbox */
	private class ToggleActive implements OnClickListener {
		public void onClick(View v) {
			SetActiveTask t = new SetActiveTask(UserDetailActivity.this);
			t.execute(!user.IsActive);
		}
	}

	/** background task to toggle the IsActive flag on the User */
	private class SetActiveTask extends ApiAsyncTask<Boolean, Void> {

		SetActiveTask(ActivityCallbacks activity) {
			super(activity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isActive.setEnabled(false);
		}
		
		@Override
		protected Void doApiCall(Boolean... params) throws Exception {
			Map<String, Object> req = new HashMap<String, Object>();
			req.put("IsActive", params[0]);
			salesforce.patchSObjectJson("user", user.Id, req);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			isActive.setEnabled(true);
			super.onPostExecute(result);
		}

		@Override
		protected void handleResult(Void result) {
			isActive.setChecked(!user.IsActive);
			user.IsActive = !user.IsActive;
			Toast.makeText(UserDetailActivity.this, getString(R.string.active_updated), Toast.LENGTH_LONG).show();
		}
	}
	
	/** background task to call the ResetPassword API */
	private class ResetPasswordTask extends ApiAsyncTask<String, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			resetPasswordButton.setEnabled(false);
		}

		ResetPasswordTask(ActivityCallbacks activity) {
			super(activity);
		}

		@Override
		protected Void doApiCall(String... params) throws Exception {
			salesforce.resetPassword(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			resetPasswordButton.setEnabled(true);
			super.onPostExecute(result);
		}

		@Override
		protected void handleResult(Void result) {
			Toast.makeText(UserDetailActivity.this, getString(R.string.password_was_reset), Toast.LENGTH_LONG).show();
		}
	}
}
