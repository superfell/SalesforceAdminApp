package com.pocketsoap.admin;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pocketsoap.admin.SalesforceApi.User;

public class UserDetailActivity extends Activity {

	static final String EXTRA_USER_JSON = "user_json";

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		
		try {
			this.user = new ObjectMapper().readValue(getIntent().getStringExtra(EXTRA_USER_JSON), User.class);
		} catch (IOException e) {
			showError(e);
		}
		
		setContentView(R.layout.user_detail);
		setText(R.id.detail_name, user.Name);
		setText(R.id.detail_username, user.Username);
	}
	
	private void setText(int textId, String txt) {
		TextView tv = (TextView)findViewById(textId);
		tv.setText(txt);
	}
	
	public void resetPasswordClicked(View v) {
		Log.i("x", "do reset password");
	}
	
	private void showError(Exception ex) {
        Toast.makeText(
                this, 
                "api request failed: " + ex.getMessage(), 
                Toast.LENGTH_LONG ).show();
	}
	
	private User user;
}
