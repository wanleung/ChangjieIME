/*
    Changjie Chinese Input Method for Android
    Copyright (C) 2012 LinkOmnia Ltd.

    Author: Wan Leung Wong (wanleung@linkomnia.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.linkomnia.android.Changjie;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry point activity for the Changjie IME application.
 *
 * <p>Provides buttons to open the system input method settings and an about dialog.
 * Rewritten to extend {@link AppCompatActivity} instead of the legacy {@link android.app.Activity}.</p>
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Opens the system input method settings screen so the user can enable the IME.
     *
     * @param view the button that was clicked
     */
    public void openLanguageSetting(View view) {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        startActivity(intent);
    }

    /**
     * Opens the in-app settings screen ({@link SettingsActivity}).
     *
     * @param view the button that was clicked
     */
    public void openSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Shows the About dialog with an embedded WebView loading {@code about.html}.
     *
     * @param view the button that was clicked
     */
    public void showAboutBox(View view) {
        View aboutView = LayoutInflater.from(this).inflate(R.layout.setting_about, null);
        WebView webView = aboutView.findViewWithTag("webview");
        if (webView != null) {
            webView.loadUrl("file:///android_asset/about.html");
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.ime_name) + " v" + getString(R.string.ime_version))
                .setView(aboutView)
                .setPositiveButton("OK", null)
                .show();
    }
}
