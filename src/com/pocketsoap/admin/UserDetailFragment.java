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
import java.util.*;

import org.codehaus.jackson.map.ObjectMapper;

import android.graphics.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.pocketsoap.salesforce.*;

/** Fragment that is the user detail page, where they can do a reset password, toggle isActive etc */
public class UserDetailFragment extends Fragment {

	static final String EXTRA_USER_JSON = "user_json";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		theView = inflater.inflate(R.layout.user_detail, container);
		resetPasswordButton = (Button)theView.findViewById(R.id.detail_reset_pwd);
		isActive = (CheckBox)theView.findViewById(R.id.detail_enabled);
		resetPasswordButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				resetPasswordClicked(v);
			}
		});
		userPhoto = (ImageView)theView.findViewById(R.id.detail_photo);
		return theView;
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setRetainInstance(true);
		try {
			String userJson = getActivity().getIntent().getStringExtra(EXTRA_USER_JSON);
			if (userJson != null) 
				this.user = new ObjectMapper().readValue(userJson, User.class);
			
			this.salesforce = new SalesforceApi(getActivity().getIntent());
		} catch (IOException e) {
			getActivityHelper().showError(e);
		} catch (URISyntaxException e) {
			getActivityHelper().showError(e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (user != null)
			bindUi();
	}
	
	private View theView;
	private Button resetPasswordButton;
	private CheckBox isActive;
	private SalesforceApi salesforce;
	private User user;
	private ImageView userPhoto;

	void bindUser(User newUser) {
		this.user = newUser;
		bindUi();
	}
	
	protected ActivityHelper getActivityHelper() {
		return ((BaseFragmentActivity)getActivity()).getActivityHelper();
	}
	
	// take all the data from the User object, and bind into the relevant parts of the UI
	private void bindUi() {
		// header section
		setText(R.id.detail_name, user.Name);
		setText(R.id.detail_username, user.Username);
		setText(R.id.detail_title, user.Title);
		
		// contact section
		setText(R.id.contact_email, user.Email);
		setText(R.id.contact_phone, user.Phone);
		setText(R.id.contact_mobile, user.MobilePhone);

		// no auto link for SMS, so we need to build our own URLSpan for it.
		if (user.MobilePhone != null && user.MobilePhone.length() > 0) {
			SpannableStringBuilder b = new SpannableStringBuilder(user.MobilePhone);
			b.setSpan(new URLSpan("smsto:" + user.MobilePhone), 0, user.MobilePhone.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			setText(R.id.contact_mobile_text, b).setMovementMethod(LinkMovementMethod.getInstance());
		}
		
		//action section
		isActive.setChecked(user.IsActive);
		isActive.setOnClickListener(new ToggleActive());

		// user photo
		// the default person image is https://blah/.../005/T but we don't want to bother fetching that, we'll just use our local default instead.
		if (user.SmallPhotoUrl != null && user.SmallPhotoUrl.length() > 0 && !user.SmallPhotoUrl.endsWith("/005/T")) {
			PhotoLoaderTask photoLoader = new PhotoLoaderTask(getActivityHelper());
			photoLoader.execute(user.SmallPhotoUrl);
		}
	}
	
	private TextView setText(int textId, CharSequence txt) {
		TextView tv = (TextView)theView.findViewById(textId);
		tv.setText(txt);
		return tv;
	}
    
	/** called when the user taps the reset password button */
	public void resetPasswordClicked(View v) {
		ResetPasswordTask t = new ResetPasswordTask(getActivityHelper());
		t.execute(user.Id);
	}

	/** called when the user taps the IsActive checkbox */
	private class ToggleActive implements OnClickListener {
		public void onClick(View v) {
			SetActiveTask t = new SetActiveTask(getActivityHelper());
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
		protected void handleError(Exception exception) {
			// the Checkbox will of toggled its state automatically
			// but the API change didn't go through so we need to
			// put the checkbox back
			isActive.setChecked(user.IsActive);
			super.handleError(exception);
		}

		@Override
		protected void handleResult(Void result) {
			isActive.setChecked(!user.IsActive);
			user.IsActive = !user.IsActive;
			Toast.makeText(getActivity(), getString(R.string.active_updated), Toast.LENGTH_LONG).show();
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
			Toast.makeText(getActivity(), getString(R.string.password_was_reset), Toast.LENGTH_LONG).show();
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
			if (result != null) 
				userPhoto.setImageBitmap(result);
		}
	}
}
