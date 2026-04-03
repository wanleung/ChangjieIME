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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles Changjie character lookups and phrase dictionary loading.
 *
 * <p>The phrase dictionary ({@code tsin.ser}) is loaded asynchronously using
 * an {@link ExecutorService} instead of the deprecated {@link android.os.AsyncTask}.</p>
 *
 * <p>Character lookups are performed against the bundled {@code changjie.db} SQLite
 * database via {@link ChangjieDatabaseHelper}.</p>
 */
public class WordProcessor {

    /**
     * Cangjie radical characters for keys a–z (index 0 = 'a' = 日, etc.).
     */
    static final String[] cangjie_radicals = {
            "日", // a
            "月", // b
            "金", // c
            "木", // d
            "水", // e
            "火", // f
            "土", // g
            "竹", // h
            "戈", // i
            "十", // j
            "大", // k
            "中", // l
            "一", // m
            "弓", // n
            "人", // o
            "心", // p
            "手", // q
            "口", // r
            "尸", // s
            "廿", // t
            "山", // u
            "女", // v
            "田", // w
            "難", // x
            "卜", // y
            "Ｚ", // z
    };

    private final Context ctx;

    private ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>> chinesePhraseDict;

    private final ChangjieDatabaseHelper dbHelper;
    private SQLiteDatabase changjieDB;

    private final SharedPreferences sharedPrefs;

    private final String defaultChangjieFilter =
            "big5 = 1 OR hkscs = 1 OR punct = 1 OR zhuyin = 1 OR katakana = 1 OR hiragana = 1 OR symbol = 1";

    private final String defaultChangjieVersion = "3";

    /**
     * Creates a new WordProcessor.
     *
     * @param ctx context used for database and preference access
     */
    public WordProcessor(Context ctx) {
        this.ctx = ctx;
        chinesePhraseDict = new ConcurrentSkipListMap<>();
        dbHelper = new ChangjieDatabaseHelper(ctx);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    /**
     * Initialises the processor by loading the phrase dictionary in the background
     * and opening the character database.
     */
    public void init() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(this::loading);
        executor.shutdown();
        changjieDB = dbHelper.getDatabase();
    }

    /**
     * Loads the serialised phrase dictionary from raw resources.
     * Called on a background thread by {@link #init()}.
     */
    @SuppressWarnings("unchecked")
    public void loading() {
        Object loaded = importFile(R.raw.tsin);
        if (loaded instanceof ConcurrentSkipListMap) {
            chinesePhraseDict =
                    (ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>) loaded;
        }
    }

    /**
     * Looks up Chinese characters matching the given Changjie code prefix.
     *
     * <p>The query respects the user's version, quick-mode, traditional-mode, and
     * simplify-Chinese preferences.</p>
     *
     * @param key the typed Changjie code (one or more letters)
     * @return ordered list of matching Chinese characters (at most 100)
     */
    public ArrayList<String> getChineseWordDictArrayList(String key) {
        String filter = defaultChangjieFilter;
        String order = "code, frequency DESC ";

        if (sharedPrefs.getBoolean("setting_filter_simplify", false)) {
            filter += " OR kanji = 1 ";
        }
        String version = defaultChangjieVersion;
        if (sharedPrefs.getBoolean("setting_version_5", false)) {
            version = "5";
        }

        String searchKey = key + "*";

        if (sharedPrefs.getBoolean("setting_quick", false)) {
            order = "frequency DESC ";
            if (key.length() < 2) {
                searchKey = key + "*";
            } else {
                searchKey = key.charAt(0) + "*" + key.charAt(1);
            }
        } else {
            if (sharedPrefs.getBoolean("setting_changjie_tradition", false)) {
                searchKey = key;
                order = " frequency DESC ";
            }
        }

        ArrayList<String> result = new ArrayList<>();
        String[] args = {version, searchKey};
        String[] searchColumns = {"chchar", "code", "frequency"};

        Cursor cursor = changjieDB.query(true,
                "chars INNER JOIN codes on chars._id=codes._id",
                searchColumns,
                "version = ? AND code GLOB ? AND ( " + filter + " )",
                args, null, null, order, "100");
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String ch = cursor.getString(cursor.getColumnIndexOrThrow("chchar"));
                result.add(ch);
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * Returns the phrase suggestions for the given word from the preloaded dictionary.
     *
     * @param key the word to look up
     * @return list of suggested follow-up words, or null if no entry exists
     */
    public CopyOnWriteArrayList<String> getChinesePhraseDictLinkedHashMap(String key) {
        if (chinesePhraseDict.containsKey(key)) {
            return chinesePhraseDict.get(key);
        } else {
            return null;
        }
    }

    /**
     * Deserialises a Java object from a raw resource.
     *
     * @param resourceId raw resource identifier
     * @return the deserialised object, or null on failure
     */
    private Object importFile(int resourceId) {
        Object obj = null;
        try {
            InputStream inputStream = ctx.getResources().openRawResource(resourceId);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            obj = in.readObject();
            in.close();
            inputStream.close();
        } catch (Exception e) {
            // Phrase dictionary loading is best-effort; failure is non-fatal.
        }
        return obj;
    }

    /**
     * Translates a typed Changjie code string (letters a–z) into the corresponding
     * Cangjie radical characters for display in the composing text area.
     *
     * @param code typed code string (only a–z characters are translated)
     * @return string of Cangjie radical characters
     */
    public static String translateToChangjieCode(String code) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            int index = code.charAt(i) - 'a';
            if (index >= 0 && index < cangjie_radicals.length) {
                result.append(cangjie_radicals[index]);
            }
        }
        return result.toString();
    }
}
