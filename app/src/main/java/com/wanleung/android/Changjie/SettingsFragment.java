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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Preference fragment that displays the Changjie IME settings.
 *
 * <p>Replaces the deprecated {@link android.preference.PreferenceActivity} pattern.
 * Uses {@link PreferenceFragmentCompat} from the AndroidX preference library.</p>
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref, rootKey);

        Preference about = findPreference("about");
        if (about != null) {
            about.setOnPreferenceClickListener(pref -> {
                showAboutDialog();
                return true;
            });
        }
    }

    /**
     * Displays the About dialog with an embedded {@link WebView} loading {@code about.html}.
     */
    private void showAboutDialog() {
        Context ctx = requireContext();
        View aboutView = LayoutInflater.from(ctx).inflate(R.layout.setting_about, null);
        WebView webView = aboutView.findViewWithTag("webview");
        if (webView != null) {
            webView.loadUrl("file:///android_asset/about.html");
        }
        new AlertDialog.Builder(ctx)
                .setTitle(getString(R.string.ime_name) + " v" + getString(R.string.ime_version))
                .setView(aboutView)
                .setPositiveButton("OK", null)
                .show();
    }
}
