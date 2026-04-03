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
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper that opens the bundled {@code changjie.db} SQLite database.
 *
 * <p>On the first run the database is copied from the app's assets directory to
 * {@link Context#getDatabasePath(String)} so it is accessible by
 * {@link SQLiteDatabase#openDatabase}.</p>
 *
 * <p>This replaces the old {@link android.database.sqlite.SQLiteOpenHelper} approach
 * with a simpler, direct file-copy pattern.</p>
 */
public class ChangjieDatabaseHelper {

    private static final String DB_NAME = "changjie.db";

    private SQLiteDatabase database;
    private final Context context;

    /**
     * Creates a new helper bound to the given context.
     *
     * @param context any context; the application context is stored internally
     */
    public ChangjieDatabaseHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Returns an open, read-only reference to the database.
     * If the database file does not exist it is first copied from assets.
     *
     * @return open SQLiteDatabase
     * @throws RuntimeException if the asset cannot be copied
     */
    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            File dbFile = context.getDatabasePath(DB_NAME);
            if (!dbFile.exists() || dbFile.length() == 0) {
                copyFromAssets(dbFile);
            }
            database = SQLiteDatabase.openDatabase(
                    dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        }
        return database;
    }

    /**
     * Closes the database connection if it is open.
     */
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
            database = null;
        }
    }

    /**
     * Copies {@code changjie.db} from the app assets to the given destination file.
     * Parent directories are created if necessary.
     *
     * @param dest destination file
     * @throws RuntimeException wrapping any {@link IOException}
     */
    private void copyFromAssets(File dest) {
        if (dest.getParentFile() != null) {
            dest.getParentFile().mkdirs();
        }
        try (InputStream in = context.getAssets().open(DB_NAME);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to copy changjie.db from assets: " + e.getMessage(), e);
        }
    }
}
