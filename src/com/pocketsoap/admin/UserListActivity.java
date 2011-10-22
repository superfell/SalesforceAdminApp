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

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import com.pocketsoap.salesforce.User;

/** the user list, this defaults to showing the recent users, and allows for a search */
public class UserListActivity extends BaseFragmentActivity {

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.user_list_a);
		detail = (UserDetailFragment)getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
	}
	
	private UserDetailFragment detail;
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.menu_logout:
    			new RefreshTokenStore(this).clearSavedData();
    			getActivityHelper().startLoginActivity();
    			finish();
    			return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    // The list fragment will call this when the user selects a row.
    void onUserItemClick(User u) {
    	// there's a detail fragment on this layout, just pass it the user
    	if (detail != null) {
    		detail.bindUser(u);
    		return;
    	}
    	
		// the user tapped a row in the list, serialize up the data for that row, and star the detail page activity
		Intent d = new Intent(this, UserDetailActivity.class);
		d.putExtras(getIntent());
		try {
			d.putExtra(UserDetailFragment.EXTRA_USER_JSON, new ObjectMapper().writeValueAsString(u));
		} catch (IOException e) {
			e.printStackTrace();
		}
		startActivity(d);

    }
}
