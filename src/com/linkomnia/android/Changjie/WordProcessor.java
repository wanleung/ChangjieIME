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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.linkomnia.android.Changjie.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;


public class WordProcessor {
	
	static final String [] cangjie_radicals = {
		    "日", // 日
		    "月", // 月
		    "金", // 金
		    "木", // 木
		    "水", // 水
		    "火", // 火
		    "土", // 土
		    "竹", // 竹
		    "戈", // 戈
		    "十", // 十
		    "大", // 大
		    "中", // 中
		    "一", // 一
		    "弓", // 弓
		    "人", // 人
		    "心", // 心
		    "手", // 手
		    "口", // 口
		    "尸", // 尸
		    "廿", // 廿
		    "山", // 山
		    "女", // 女
		    "田", // 田
		    "難", // 難
		    "卜", // 卜
		    "Ｚ", // Ｚ
	};
    
    private Context ctx;
    
    private ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>> chinesePhraseDict;
    
    private ChangjieDatabaseHelper dbh;
    private SQLiteDatabase changjieDB;
    
    private SharedPreferences sharedPrefs;
     
    private String defaultChangjieFilter = "big5 = 1 OR hkscs = 1 OR punct = 1 OR zhuyin = 1 OR katakana = 1 OR hiragana = 1 OR symbol = 1";

    private String defaultChangjieVersion = "3"; 
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public WordProcessor(Context ctx) {
        this.ctx = ctx;
        chinesePhraseDict = new ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>();
        
        dbh = new ChangjieDatabaseHelper(ctx);
        
        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(ctx);
    }
    
    public void init() {
        new ImportFilesTask().execute(this);
        dbh.getReadableDatabase();
        changjieDB = dbh.getDatabase();
    }
    
    @SuppressWarnings("unchecked")
    public void loading() {
        this.chinesePhraseDict = (ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>)this.importFile(R.raw.tsin);
    }
    
    public ArrayList<String> getChineseWordDictArrayList(String key) {
    	String filter = this.defaultChangjieFilter;
    	String order = "code, frequency DESC ";
    	
        if (sharedPrefs.getBoolean("setting_filter_simplify", false)) {
        	filter += " OR kanji = 1 ";
        } 
        String version = this.defaultChangjieVersion;
        if (sharedPrefs.getBoolean("setting_version_5", false)) {
        	version = "5";
        }
        
        String searchKey = key + "*";
        
        if (this.sharedPrefs.getBoolean("setting_quick", false)) {
        	order = "frequency DESC ";
        	if (key.length() < 2) {
        		searchKey = key + "*";
        	} else {
        		searchKey = key.charAt(0) + "*" + key.charAt(1);
        	}
        }
    	
    	ArrayList<String> result = new ArrayList<String>();
    	String[] args =  {version, searchKey};
    	//String order = "code, frequency DESC ";
    	String[] searchColumns = { "chchar", "code", "frequency" };
    	//SELECT chchar, code, frequency FROM chars INNER JOIN codes on chars.char_index=codes.char_index WHERE version=5 AND code GLOB "okr" ORDER BY frequency DESC;
    	//Cursor cursor = changjieDB.query("chars INNER JOIN codes on chars._id=codes._id",
    	//		searchColumns, "version=? AND code GLOB ? AND (big5 = 0 OR hkscs = 0 OR punct = 0 OR zh = 0 OR zhuyin = 0 OR kanji = 0 OR katakana = 0 OR hiragana = 0 OR symbol = 0 )", args, null, null, order );
    	Cursor cursor = changjieDB.query(true, "chars INNER JOIN codes on chars._id=codes._id", 
    			searchColumns, "version = ? AND code GLOB ? AND ( "+ filter + " )", 
    			args, null, null, order, "100");
    	
    	
    	cursor.moveToFirst();
    	while (!cursor.isAfterLast()) {
    		String ch = cursor.getString(cursor.getColumnIndex("chchar")) ;
    		result.add(ch);
    		//Log.d("WANLEUNG", ch);
    		cursor.moveToNext();
    	}
    	// Make sure to close the cursor
    	cursor.close();
    	return result;
    
    	
        //if (this.chineseWordDict.containsKey(key)) {
        //    return new ArrayList<String>(this.chineseWordDict.get(key));
        //} else {
        //    return new ArrayList<String>();
        //}
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public CopyOnWriteArrayList<String> getChinesePhraseDictLinkedHashMap(String key) {
        if (this.chinesePhraseDict.containsKey(key)) {
            return this.chinesePhraseDict.get(key);
        } else {
            return null;
        }
    }
    
    private Object importFile(int resourceId) {
        Object obj = null;
        try {
            InputStream inputStream = ctx.getResources().openRawResource(resourceId);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            obj = in.readObject();
            //Log.d("WANLEUNG", obj.toString());
            in.close();
            inputStream.close();
        } catch (Exception e) {
            //Log.w("WANLEUNG", e.toString());
        }
        return obj;
    }
    
    private class ImportFilesTask extends AsyncTask<WordProcessor, Void, Long> {
        protected Long doInBackground(WordProcessor... fims) {
            int count = fims.length;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                totalSize ++;
                fims[i].loading();
            }
            return totalSize;
        }

    }
    
    static public String translateToChangjieCode(String code) {
    	String result = "";
    	for (int i = 0; i < code.length(); i++) {
    		int index = code.charAt(i) - 'a';
    		result += cangjie_radicals[index];
    	}
    	return result;
    }
}
