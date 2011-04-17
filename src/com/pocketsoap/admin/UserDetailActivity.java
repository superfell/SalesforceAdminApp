package com.pocketsoap.admin;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.graphics.*;
import android.os.*;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.pocketsoap.admin.ApiAsyncTask.ActivityCallbacks;
import com.pocketsoap.salesforce.*;

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
		setText(R.id.detail_username, user.Username);
		setText(R.id.detail_title, user.Title);
		
		setText(R.id.contact_email, user.Email);
		setText(R.id.contact_phone, user.Phone);
		setText(R.id.contact_mobile, user.MobilePhone);
		if (user.MobilePhone != null && user.MobilePhone.length() > 0) {
			SpannableStringBuilder b = new SpannableStringBuilder(user.MobilePhone);
			b.setSpan(new URLSpan("smsto:" + user.MobilePhone), 0, user.MobilePhone.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			setText(R.id.contact_mobile_text, b).setMovementMethod(LinkMovementMethod.getInstance());
		}
		resetPasswordButton = (Button)findViewById(R.id.detail_reset_pwd);
		isActive = (CheckBox)findViewById(R.id.detail_enabled);
		isActive.setChecked(user.IsActive);
		isActive.setOnClickListener(new ToggleActive());
		
		// sigh, Android 2.1's SSL handling is not compatible with the way the SSL certs are setup on *.content.force.com
		// so, we can't load the user photo's if we're on 2.1
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
			// the default person image is https://blah/.../005/T but we don't want to bother fetching that, we'll just use our local default instead.
			if (user.SmallPhotoUrl != null && user.SmallPhotoUrl.length() > 0 && !user.SmallPhotoUrl.endsWith("/005/T")) {
				PhotoLoaderTask photoLoader = new PhotoLoaderTask(this);
				photoLoader.execute(user.SmallPhotoUrl);
			}
		}
	}
	
	private Button resetPasswordButton;
	private CheckBox isActive;
	private SalesforceApi salesforce;
	private User user;

	private TextView setText(int textId, CharSequence txt) {
		TextView tv = (TextView)findViewById(textId);
		tv.setText(txt);
		return tv;
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
	
	private class PhotoLoaderTask extends ApiAsyncTask<String, Bitmap> {

		PhotoLoaderTask(ActivityCallbacks activity) {
			super(activity);
		}

		@Override
		protected Bitmap doApiCall(String... params) throws Exception {
			byte [] img = salesforce.getBinaryData(params[0]);
			return BitmapFactory.decodeByteArray(img, 0, img.length);
		}

		@Override
		protected void handleResult(Bitmap result) {
			if (result != null) {
				ImageView v = (ImageView)findViewById(R.id.detail_photo);
				v.setImageBitmap(result);
			}
		}
	}
}
