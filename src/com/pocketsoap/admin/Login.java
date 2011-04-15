package com.pocketsoap.admin;


import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Login extends Activity {
	
	private static final String TAG = "Login";
	
	public static final String CLIENT_ID = "3MVG99OxTyEMCQ3hP1_9.Mh8dF0RyAiHUybfddL9XzlPIAkLZtbHUJmz7HNHvhQSOgwsl5Ivb8uF0FU_R0nob";
	public static final String CALLBACK_URI = "adminApp://oauth/";
	
	public static final String DEFAULT_AUTH_HOST = "https://login.salesforce.com";
	
	private static final String OAUTH_AUTH_PATH = "/services/oauth2/authorize";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.webview);
        webview = (WebView)findViewById(R.id.web_view);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebChromeClient(new ProgressChromeClient());
        webview.setWebViewClient(new LoginWebViewClient());
    }

    @Override
    public void onResume() {
    	super.onResume();
    	String url = DEFAULT_AUTH_HOST + OAUTH_AUTH_PATH +
    				"?display=mobile" + 
			    	"&response_type=token" +
			    	"&client_id=" + Uri.encode(CLIENT_ID) + 
			    	"&redirect_uri=" + Uri.encode(CALLBACK_URI);

    	webview.loadUrl(url);
    }

    // called when the webview see's our callback_uri try to get loaded.
    private void authDone(Uri callbackUri) {
    	// parse info out of fragment
    	Log.i("auth", "oauth done" + callbackUri);
    	String frag = callbackUri.getEncodedFragment();
    	String [] params = frag.split("&");
    	Map<String, String> values = new HashMap<String,String>();
    	for (String p : params) {
    		String [] nv = p.split("=");
    		if (nv.length == 2)
    			values.put(nv[0], Uri.decode(nv[1]));
    	}
    	// save in prefs
    	SharedPreferences sp = this.getSharedPreferences("a", MODE_PRIVATE);
    	Editor spe = sp.edit();
    	spe.putString(REF_TOKEN, values.get("refresh_token"));
    	spe.putString(AUTH_SERVER, DEFAULT_AUTH_HOST);	// TODO
    	spe.commit();
    	
    	// launch admin activity
    	Intent i = new Intent(this, UserAdmin.class);
    	i.putExtra("SID", values.get("access_token"));
    	i.putExtra("SVR", values.get("instance_url"));
    	startActivity(i);
    	finish();
    }
    
	class ProgressChromeClient extends WebChromeClient {
	    
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d(TAG, "onProgressChanged : " + view + " : " + newProgress);
            setProgress(newProgress * 100);
        }
	}
	
	class LoginWebViewClient extends WebViewClient {
        
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, "onReceivedError :" + view + ": " + errorCode +":" + description + ":" + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
            boolean isDone = url.startsWith(CALLBACK_URI);
            if (isDone) authDone(Uri.parse(url));
            return isDone;
        }
	}
	
    private WebView webview;
    
    static final String REF_TOKEN = "refTKn";
	static final String AUTH_SERVER = "authServer";
}