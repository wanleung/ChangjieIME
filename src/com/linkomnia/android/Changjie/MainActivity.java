package com.linkomnia.android.Changjie;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void openLanguageSetting(View view) {
	    // Do something in response to button 
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName("com.android.settings", "com.android.settings.LanguageSettings");
		startActivity(intent);
	}

    public void showAboutBox(View view) {
        View aboutView = View.inflate(this, R.layout.setting_about, null);
        WebView webView = (WebView) aboutView.findViewWithTag("webview");
        webView.loadUrl("file:///android_asset/about.html");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.ime_name)+" v"+getString(R.string.ime_version));
        dialog.setView(aboutView);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Setting.this.finish();
            }
        });
        dialog.show();
    }
}
