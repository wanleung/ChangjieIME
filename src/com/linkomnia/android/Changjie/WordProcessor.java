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
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;


public class WordProcessor {
    
    private Context ctx;
    
    private ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>> chineseWordDict;
    
    
    private ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>> chinesePhraseDict;
    
    private DatabaseHelper dbh;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public WordProcessor(Context ctx) {
        this.ctx = ctx;
        chineseWordDict = new ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>();
        chinesePhraseDict = new ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>();
        
        dbh = new DatabaseHelper(ctx);
    }
    
    public void init() {
        new ImportFilesTask().execute(this);
    }
    
    @SuppressWarnings("unchecked")
    public void loading() {
        this.chineseWordDict = (ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>)this.importFile(R.raw.stroke);
        this.chinesePhraseDict = (ConcurrentSkipListMap<String, CopyOnWriteArrayList<String>>)this.importFile(R.raw.tsin);
    }
    
    public ArrayList<String> getChineseWordDictArrayList(String key) {
        if (this.chineseWordDict.containsKey(key)) {
            return new ArrayList<String>(this.chineseWordDict.get(key));
        } else {
            return new ArrayList<String>();
        }
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
            Log.w("WANLEUNG", e.toString());
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
}
