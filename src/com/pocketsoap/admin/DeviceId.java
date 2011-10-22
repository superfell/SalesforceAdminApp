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

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.content.*;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

/**
 * Our per device/install Id. this is encrypted with a static key, which isn't great
 * but better than nothing. perhaps in a future version we'll allow the user to set
 * a pin, and use that as part of the key.
 * 
 */
public class DeviceId {

	public static final String getDeviceId(Context ctx) {
		try {
			SharedPreferences sp = ctx.getApplicationContext().getSharedPreferences("did", Context.MODE_PRIVATE);
			EncryptionHelper e = new EncryptionHelper(new String(Base64.decode("RmdpWXdicFVETnJLU0k1OUJ3V3U5cHBqRy90dDZac2FoSHI0aEZyUkNHTFF3LzhGU1hNbUI4MGYyOUc3NXN2Wg==", Base64.DEFAULT), "UTF-8"));
			String did = sp.getString("id", null);
			if (did != null)
				return e.decrypt(did);
	
			did = UUID.randomUUID().toString();
			Editor ed = sp.edit();
			ed.putString("id", e.encrypt(did));
			ed.commit();
			return did;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}
}
