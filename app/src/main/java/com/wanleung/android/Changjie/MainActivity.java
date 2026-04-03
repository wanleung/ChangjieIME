/*
    Changjie Chinese Input Method for Android
    Copyright (C) 2012 Wanleung's Workshop

    Author: Wan Leung Wong (info@wanleung.com)

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

package com.wanleung.android.Changjie;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Entry point activity for the Changjie IME application.
 *
 * <p>Shows the current IME enable status and provides guided setup steps:
 * (1) enable in system settings, (2) select as default. Also provides
 * links to the app settings, privacy policy, and about screen.</p>
 */
public class MainActivity extends AppCompatActivity {

    private static final String IME_PACKAGE = "com.wanleung.android.Changjie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusBanner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            openSettings(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // -------------------------------------------------------------------------
    // Status
    // -------------------------------------------------------------------------

    /** Updates the status banner to reflect whether the IME is currently enabled. */
    private void updateStatusBanner() {
        TextView statusText = findViewById(R.id.status_text);
        if (statusText == null) return;

        if (isImeEnabled()) {
            statusText.setText(getString(R.string.activity_status_enabled));
            statusText.setBackgroundColor(0xFFC8E6C9);
            statusText.setTextColor(0xFF1B5E20);
        } else {
            statusText.setText(getString(R.string.activity_status_disabled));
            statusText.setBackgroundColor(0xFFFFECB3);
            statusText.setTextColor(0xFF4E342E);
        }
    }

    /** Returns true if the Changjie IME is listed as an enabled input method. */
    private boolean isImeEnabled() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) return false;
        List<InputMethodInfo> enabled = imm.getEnabledInputMethodList();
        for (InputMethodInfo info : enabled) {
            if (info.getPackageName().equals(IME_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Button callbacks (referenced by android:onClick in layout)
    // -------------------------------------------------------------------------

    /**
     * Opens the system input method settings so the user can enable the IME (step 1).
     *
     * @param view the button that was clicked
     */
    public void openLanguageSetting(View view) {
        startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

    /**
     * Shows the system input method picker so the user can select Changjie as default (step 2).
     *
     * @param view the button that was clicked
     */
    public void selectInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
    }

    /**
     * Opens the in-app settings screen.
     *
     * @param view the button that was clicked (may be null when called from menu)
     */
    public void openSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Shows the Privacy Policy in a WebView dialog.
     *
     * @param view the button that was clicked
     */
    public void showPrivacyPolicy(View view) {
        showWebViewDialog(
                getString(R.string.action_privacy_policy),
                "file:///android_asset/privacy_policy.html");
    }

    /**
     * Shows the About screen in a WebView dialog.
     *
     * @param view the button that was clicked
     */
    public void showAboutBox(View view) {
        showWebViewDialog(
                getString(R.string.ime_name) + " v" + getString(R.string.ime_version),
                "file:///android_asset/about.html");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void showWebViewDialog(String title, String url) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.setting_about, null);
        WebView webView = dialogView.findViewWithTag("webview");
        if (webView != null) {
            webView.loadUrl(url);
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }
}

