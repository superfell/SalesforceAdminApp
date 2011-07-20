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

import android.content.*;
import android.content.SharedPreferences.Editor;

/** helper for reading/writing the refresh token from the preferences */
public class RefreshTokenStore {

	RefreshTokenStore(Context ctx) {
		this.pref = ctx.getSharedPreferences("a", Context.MODE_PRIVATE);
	}
	
	private final SharedPreferences pref;
	
	void saveToken(String refreshToken, String authServer) {
		Editor e = pref.edit();
		e.putString(REF_TOKEN, refreshToken);
		e.putString(AUTH_SERVER, authServer);
		e.commit();
	}
	
	void clearSavedData() {
		Editor e = pref.edit();
		e.clear();
		e.commit();
	}
	
	boolean hasSavedToken() {
		return pref.contains(REF_TOKEN);
	}
	
	String getRefreshToken() {
		return pref.getString(REF_TOKEN, null);
	}
	
	String getAuthServer() {
		return pref.getString(AUTH_SERVER, Login.PROD_AUTH_HOST);
	}
	
	
    private static final String REF_TOKEN = "refTKn";
	private static final String AUTH_SERVER = "authServer";
}
