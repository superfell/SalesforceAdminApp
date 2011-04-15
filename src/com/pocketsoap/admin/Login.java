package com.pocketsoap.admin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

public class Login extends Activity {
	
	public static final String CLIENT_ID = "3MVG99OxTyEMCQ3hP1_9.Mh8dF0RyAiHUybfddL9XzlPIAkLZtbHUJmz7HNHvhQSOgwsl5Ivb8uF0FU_R0nob";
	public static final String CALLBACK_URI = "adminApp://oauth/";
	
	public static final String DEFAULT_AUTH_HOST = "https://login.saleforce.com";
	
	private static final String OAUTH_AUTH_PATH = "/services/oauth2/authorize";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        webview = (WebView)findViewById(R.id.web_view);
    }

    @Override
    public void onResume() {
    	String url = DEFAULT_AUTH_HOST + OAUTH_AUTH_PATH +
    				"?display=mobile" + 
			    	"&response_type=token" +
			    	"&client_id=" + Uri.encode(CLIENT_ID) + 
			    	"&redirect_uri=" + Uri.encode(CALLBACK_URI);
    	
    	webview.loadUrl(url);
    }

    private WebView webview;
    private SharedPreferences prefs;
    
    private static final String REF_TOKEN = "refTKn", AUTH_SERVER = "authServer";
}