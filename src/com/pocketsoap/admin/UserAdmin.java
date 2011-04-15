package com.pocketsoap.admin;

import android.app.ListActivity;
import android.os.Bundle;

public class UserAdmin extends ListActivity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.user_admin);
	}
}
