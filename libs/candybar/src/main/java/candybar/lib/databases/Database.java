package candybar.lib.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.TimeHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import candybar.lib.helpers.JsonHelper;
import candybar.lib.items.Icon;
import candybar.lib.items.ImageSize;
import candybar.lib.items.Request;
import candybar.lib.items.Wallpaper;
import candybar.lib.preferences.Preferences;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "candybar_database";
    private static final int DATABASE_VERSION = 11;

    private static final String TABLE_REQUEST = "icon_request";
    private static final String TABLE_PREMIUM_REQUEST = "premium_request";
    private static final String TABLE_WALLPAPERS = "wallpapers";
    private static final String TABLE_BOOKMARKED_ICONS = "bookmarked_icons";

    private static final String KEY_ID = "id";

    private static final String KEY_ORDER_ID = "order_id";
    private static final String KEY_PRODUCT_ID = "product_id";

    private static final String KEY_NAME = "name";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_REQUESTED_ON = "requested_on";

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_URL = "url";
    private static final String KEY_MIME_TYPE = "mimeType";
    private static final String KEY_COLOR = "color";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_SIZE = "size";

    private static final String KEY_TITLE = "title";

    private final Context mContext;

    private static WeakReference<Database> mDatabase;
    private SQLiteDatabase mSQLiteDatabase;

    public static Database get(@NonNull Context context) {
        if (mDatabase == null || mDatabase.get() == null) {
            mDatabase = new WeakReference<>(new Database(context));
        }
        return mDatabase.get();
    }

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_REQUEST = "CREATE TABLE IF NOT EXISTS " + TABLE_REQUEST + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_ACTIVITY + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (" + KEY_ACTIVITY + ") ON CONFLICT REPLACE)";
        String CREATE_TABLE_PREMIUM_REQUEST = "CREATE TABLE IF NOT EXISTS " + TABLE_PREMIUM_REQUEST + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_ORDER_ID + " TEXT NOT NULL, " +
                KEY_PRODUCT_ID + " TEXT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_ACTIVITY + " TEXT NOT NULL, " +
                KEY_REQUESTED_ON + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (" + KEY_ACTIVITY + ") ON CONFLICT REPLACE)";
        String CREATE_TABLE_WALLPAPER = "CREATE TABLE IF NOT EXISTS " + TABLE_WALLPAPERS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_AUTHOR + " TEXT NOT NULL, " +
                KEY_URL + " TEXT NOT NULL, " +
                KEY_THUMB_URL + " TEXT NOT NULL, " +
                KEY_MIME_TYPE + " TEXT, " +
                KEY_SIZE + " INTEGER DEFAULT 0, " +
                KEY_COLOR + " INTEGER DEFAULT 0, " +
                KEY_WIDTH + " INTEGER DEFAULT 0, " +
                KEY_HEIGHT + " INTEGER DEFAULT 0, " +
                "UNIQUE (" + KEY_URL + "))";
        String CREATE_TABLE_BOOKMARKED_ICONS = "CREATE TABLE IF NOT EXISTS " + TABLE_BOOKMARKED_ICONS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                KEY_NAME + " TEXT NOT NULL, " +
                KEY_TITLE + " TEXT NOT NULL, " +
                "UNIQUE (" + KEY_NAME + ") ON CONFLICT IGNORE)";
        db.execSQL(CREATE_TABLE_REQUEST);
        db.execSQL(CREATE_TABLE_PREMIUM_REQUEST);
        db.execSQL(CREATE_TABLE_WALLPAPER);
        db.execSQL(CREATE_TABLE_BOOKMARKED_ICONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Need to clear shared preferences with version 3.4.0
        if (newVersion == 9) {
            Preferences.get(mContext).clearPreferences();
        }
        resetDatabase(db, oldVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        resetDatabase(db, oldVersion);
    }

    private void resetDatabase(SQLiteDatabase db, int oldVersion) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                tables.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        List<Request> requests = getRequestedApps(db);
        List<Request> premiumRequest = getPremiumRequest(db);
        List<Wallpaper> wallpapers = getWallpapers(db);

        for (int i = 0; i < tables.size(); i++) {
            try {
                String dropQuery = "DROP TABLE IF EXISTS " + tables.get(i);
                if (!tables.get(i).equalsIgnoreCase("SQLITE_SEQUENCE"))
                    db.execSQL(dropQuery);
            } catch (Exception ignored) {
            }
        }
        onCreate(db);

        for (Request request : requests) {
            addRequest(db, request);
        }

        addWallpapers(db, wallpapers);

        if (oldVersion <= 3) {
            return;
        }

        for (Request premium : premiumRequest) {
            Request r = Request.Builder()
                    .name(premium.getName())
                    .activity(premium.getActivity())
                    .orderId(premium.getOrderId())
                    .productId(premium.getProductId())
                    .requestedOn(premium.getRequestedOn())
                    .build();
            addPremiumRequest(db, r);
        }
    }

    public boolean openDatabase() {
        try {
            if (mDatabase == null || mDatabase.get() == null) {
                LogUtil.e("Database error: openDatabase() database instance is null");
                return false;
            }

            if (mDatabase.get().mSQLiteDatabase == null) {
                mDatabase.get().mSQLiteDatabase = mDatabase.get().getWritableDatabase();
            }

            if (!mDatabase.get().mSQLiteDatabase.isOpen()) {
                LogUtil.e("Database error: database openable false, trying to open the database again");
                mDatabase.get().mSQLiteDatabase = mDatabase.get().getWritableDatabase();
            }
            return mDatabase.get().mSQLiteDatabase.isOpen();
        } catch (SQLiteException | NullPointerException e) {
            LogUtil.e(Log.getStackTraceString(e));
            return false;
        }
    }

    public void closeDatabase() {
        try {
            if (mDatabase == null || mDatabase.get() == null) {
                LogUtil.e("Database error: closeDatabase() database instance is null");
                return;
            }

            if (mDatabase.get().mSQLiteDatabase == null) {
                LogUtil.e("Database error: trying to close database which is not opened");
                return;
            }
            mDatabase.get().mSQLiteDatabase.close();
        } catch (SQLiteException | NullPointerException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public void addRequest(@Nullable SQLiteDatabase db, Request request) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: addRequest() failed to open database");
                return;
            }

            database = mDatabase.get().mSQLiteDatabase;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, request.getName());
        values.put(KEY_ACTIVITY, request.getActivity());

        String requestedOn = request.getRequestedOn();
        if (requestedOn == null) requestedOn = TimeHelper.getLongDateTime();
        values.put(KEY_REQUESTED_ON, requestedOn);

        database.insert(TABLE_REQUEST, null, values);
    }

    public boolean isRequested(String activity) {
        if (!openDatabase()) {
            LogUtil.e("Database error: isRequested() failed to open database");
            return false;
        }

        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_REQUEST, null, KEY_ACTIVITY + " = ?",
                new String[]{activity}, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        return rowCount > 0;
    }

    private List<Request> getRequestedApps(@Nullable SQLiteDatabase db) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: getRequestedApps() failed to open database");
                return new ArrayList<>();
            }

            database = mDatabase.get().mSQLiteDatabase;
        }

        List<Request> requests = new ArrayList<>();
        Cursor cursor = database.query(TABLE_REQUEST, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Request request = Request.Builder()
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .activity(cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY)))
                        .requestedOn(cursor.getString(cursor.getColumnIndex(KEY_REQUESTED_ON)))
                        .requested(true)
                        .build();

                requests.add(request);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return requests;
    }

    public void addPremiumRequest(@Nullable SQLiteDatabase db, Request request) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: addPremiumRequest() failed to open database");
                return;
            }

            database = mDatabase.get().mSQLiteDatabase;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_ORDER_ID, request.getOrderId());
        values.put(KEY_PRODUCT_ID, request.getProductId());
        values.put(KEY_NAME, request.getName());
        values.put(KEY_ACTIVITY, request.getActivity());

        String requestedOn = request.getRequestedOn();
        if (requestedOn == null) requestedOn = TimeHelper.getLongDateTime();
        values.put(KEY_REQUESTED_ON, requestedOn);

        database.insert(TABLE_PREMIUM_REQUEST, null, values);
    }

    public List<Request> getPremiumRequest(@Nullable SQLiteDatabase db) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: getPremiumRequest() failed to open database");
                return new ArrayList<>();
            }

            database = mDatabase.get().mSQLiteDatabase;
        }

        List<Request> requests = new ArrayList<>();

        Cursor cursor = database.query(TABLE_PREMIUM_REQUEST,
                null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Request request = Request.Builder()
                        .name(cursor.getString(cursor.getColumnIndex(KEY_NAME)))
                        .activity(cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY)))
                        .orderId(cursor.getString(cursor.getColumnIndex(KEY_ORDER_ID)))
                        .productId(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_ID)))
                        .build();
                requests.add(request);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return requests;
    }

    public void addWallpapers(@Nullable SQLiteDatabase db, List<?> list) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: addWallpapers() failed to open database");
                return;
            }

            database = mDatabase.get().mSQLiteDatabase;
        }

        String query = "INSERT OR IGNORE INTO " + TABLE_WALLPAPERS + " (" + KEY_NAME + "," + KEY_AUTHOR + "," + KEY_URL + ","
                + KEY_THUMB_URL + ") VALUES (?,?,?,?);";
        SQLiteStatement statement = database.compileStatement(query);
        database.beginTransaction();

        for (int i = 0; i < list.size(); i++) {
            statement.clearBindings();

            Wallpaper wallpaper;
            if (list.get(i) instanceof Wallpaper) {
                wallpaper = (Wallpaper) list.get(i);
            } else {
                wallpaper = JsonHelper.getWallpaper(list.get(i));
            }

            if (wallpaper != null) {
                if (wallpaper.getURL() != null) {
                    String name = wallpaper.getName();
                    if (name == null) name = "";

                    statement.bindString(1, name);

                    if (wallpaper.getAuthor() != null) {
                        statement.bindString(2, wallpaper.getAuthor());
                    } else {
                        statement.bindNull(2);
                    }

                    statement.bindString(3, wallpaper.getURL());
                    statement.bindString(4, wallpaper.getThumbUrl());
                    statement.execute();
                }
            }
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void updateWallpaper(Wallpaper wallpaper) {
        if (!openDatabase()) {
            LogUtil.e("Database error: updateWallpaper() failed to open database");
            return;
        }

        if (wallpaper == null) return;

        ContentValues values = new ContentValues();
        if (wallpaper.getSize() > 0) {
            values.put(KEY_SIZE, wallpaper.getSize());
        }

        if (wallpaper.getMimeType() != null) {
            values.put(KEY_MIME_TYPE, wallpaper.getMimeType());
        }

        if (wallpaper.getDimensions() != null) {
            values.put(KEY_WIDTH, wallpaper.getDimensions().width);
            values.put(KEY_HEIGHT, wallpaper.getDimensions().height);
        }

        if (wallpaper.getColor() != 0) {
            values.put(KEY_COLOR, wallpaper.getColor());
        }

        if (values.size() > 0) {
            mDatabase.get().mSQLiteDatabase.update(TABLE_WALLPAPERS,
                    values, KEY_URL + " = ?", new String[]{wallpaper.getURL()});
        }
    }

    public int getWallpapersCount() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpapersCount() failed to open database");
            return 0;
        }

        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, null, null, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        return rowCount;
    }

    @Nullable
    public Wallpaper getWallpaper(String url) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getWallpaper() failed to open database");
            return null;
        }

        Wallpaper wallpaper = null;
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, KEY_URL + " = ?", new String[]{url}, null, null, null, "1");
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper " + id;
                }

                wallpaper = Wallpaper.Builder()
                        .name(name)
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .dimensions(dimensions)
                        .mimeType(cursor.getString(cursor.getColumnIndex(KEY_MIME_TYPE)))
                        .size(cursor.getInt(cursor.getColumnIndex(KEY_SIZE)))
                        .color(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)))
                        .build();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpaper;
    }

    public List<Wallpaper> getWallpapers(@Nullable SQLiteDatabase db) {
        SQLiteDatabase database = db;
        if (database == null) {
            if (!openDatabase()) {
                LogUtil.e("Database error: getWallpapers() failed to open database");
                return new ArrayList<>();
            }

            database = mDatabase.get().mSQLiteDatabase;
        }

        List<Wallpaper> wallpapers = new ArrayList<>();
        Cursor cursor = database.query(TABLE_WALLPAPERS,
                null, null, null, null, null, KEY_ID + " ASC");
        if (cursor.moveToFirst()) {
            do {
                int width = cursor.getInt(cursor.getColumnIndex(KEY_WIDTH));
                int height = cursor.getInt(cursor.getColumnIndex(KEY_HEIGHT));
                ImageSize dimensions = null;
                if (width > 0 && height > 0) {
                    dimensions = new ImageSize(width, height);
                }

                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper " + id;
                }

                Wallpaper wallpaper = Wallpaper.Builder()
                        .name(name)
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .color(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)))
                        .mimeType(cursor.getString(cursor.getColumnIndex(KEY_MIME_TYPE)))
                        .dimensions(dimensions)
                        .size(cursor.getInt(cursor.getColumnIndex(KEY_SIZE)))
                        .build();
                wallpapers.add(wallpaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpapers;
    }

    @Nullable
    public Wallpaper getRandomWallpaper() {
        if (!openDatabase()) {
            LogUtil.e("Database error: getRandomWallpaper() failed to open database");
            return null;
        }

        Wallpaper wallpaper = null;
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_WALLPAPERS,
                null, null, null, null, null, "RANDOM()", "1");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                if (name.length() == 0) {
                    name = "Wallpaper " + id;
                }

                wallpaper = Wallpaper.Builder()
                        .name(name)
                        .author(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)))
                        .url(cursor.getString(cursor.getColumnIndex(KEY_URL)))
                        .thumbUrl(cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)))
                        .build();
            } while (cursor.moveToNext());
        }
        cursor.close();
        return wallpaper;
    }

    public void addBookmarkedIcon(String drawableName, String title) {
        if (!openDatabase()) {
            LogUtil.e("Database error: addBookmarkedIcon() failed to open database");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, drawableName);
        values.put(KEY_TITLE, title);

        mDatabase.get().mSQLiteDatabase.insert(TABLE_BOOKMARKED_ICONS, null, values);
    }

    public void deleteBookmarkedIcon(String drawableName) {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteBookmarkedIcon() failed to open database");
            return;
        }

        mDatabase.get().mSQLiteDatabase
                .delete(TABLE_BOOKMARKED_ICONS, KEY_NAME + " = ?", new String[]{drawableName});
    }

    public void deleteBookmarkedIcons(List<String> drawableNames) {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteBookmarkedIcons() failed to open database");
            return;
        }

        final String inPart = "\"" + TextUtils.join("\", \"", drawableNames) + "\"";

        mDatabase.get().mSQLiteDatabase.execSQL("DELETE FROM " + TABLE_BOOKMARKED_ICONS +
                " WHERE " + KEY_NAME + " IN (" + inPart + ")");
    }

    public boolean isIconBookmarked(String drawableName) {
        if (!openDatabase()) {
            LogUtil.e("Database error: isIconBookmarked() failed to open database");
            return false;
        }

        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_BOOKMARKED_ICONS, null, KEY_NAME + " = ?",
                new String[]{drawableName}, null, null, null, null);
        int rowCount = cursor.getCount();
        cursor.close();
        return rowCount > 0;
    }

    public List<Icon> getBookmarkedIcons(Context context) {
        if (!openDatabase()) {
            LogUtil.e("Database error: getBookmarkedIcons() failed to open database");
            return new ArrayList<>();
        }

        List<Icon> icons = new ArrayList<>();
        Cursor cursor = mDatabase.get().mSQLiteDatabase.query(TABLE_BOOKMARKED_ICONS,
                null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String drawableName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
                int resId = DrawableHelper.getResourceId(context, drawableName);
                icons.add(new Icon(drawableName, null, resId).setTitle(title));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Collections.sort(icons, Icon.TitleComparator);

        return icons;
    }

    public void deleteIconRequestData() {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteIconRequestData() failed to open database");
            return;
        }

        mDatabase.get().mSQLiteDatabase.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_REQUEST});
        mDatabase.get().mSQLiteDatabase.delete(TABLE_REQUEST, null, null);
    }

    public void deleteWallpapers() {
        if (!openDatabase()) {
            LogUtil.e("Database error: deleteWallpapers() failed to open database");
            return;
        }

        mDatabase.get().mSQLiteDatabase.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{TABLE_WALLPAPERS});
        mDatabase.get().mSQLiteDatabase.delete(TABLE_WALLPAPERS, null, null);
    }
}
