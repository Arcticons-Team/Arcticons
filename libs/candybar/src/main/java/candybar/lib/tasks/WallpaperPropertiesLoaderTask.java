package candybar.lib.tasks;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import candybar.lib.databases.Database;
import candybar.lib.items.ImageSize;
import candybar.lib.items.Wallpaper;
import candybar.lib.utils.AsyncTaskBase;

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

public class WallpaperPropertiesLoaderTask extends AsyncTaskBase {

    private final Wallpaper mWallpaper;
    private final WeakReference<Callback> mCallback;
    private final WeakReference<Context> mContext;

    public WallpaperPropertiesLoaderTask(Context context, Wallpaper wallpaper, @Nullable Callback callback) {
        mContext = new WeakReference<>(context);
        mWallpaper = wallpaper;
        mCallback = new WeakReference<>(callback);
    }

    @Override
    protected boolean run() {
        if (!isCancelled()) {
            try {
                Thread.sleep(1);
                if (mWallpaper == null) return false;

                if (mWallpaper.getDimensions() != null &&
                        mWallpaper.getMimeType() != null &&
                        mWallpaper.getSize() > 0) {
                    return false;
                }

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                URL url = new URL(mWallpaper.getURL());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(15000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream stream = connection.getInputStream();
                    BitmapFactory.decodeStream(stream, null, options);

                    ImageSize imageSize = new ImageSize(options.outWidth, options.outHeight);
                    mWallpaper.setDimensions(imageSize);
                    mWallpaper.setMimeType(options.outMimeType);

                    int contentLength = connection.getContentLength();
                    if (contentLength > 0) {
                        mWallpaper.setSize(contentLength);
                    }

                    Database.get(mContext.get()).updateWallpaper(mWallpaper);
                    stream.close();
                    return true;
                }
                return false;
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void postRun(boolean ok) {
        if (ok && mContext.get() != null && !((AppCompatActivity) mContext.get()).isFinishing()) {
            if (mWallpaper.getSize() <= 0) {
                try {
                    File target = Glide.with(mContext.get())
                            .asFile()
                            .load(mWallpaper.getURL())
                            .onlyRetrieveFromCache(true)
                            .submit()
                            .get();
                    if (target != null && target.exists()) {
                        mWallpaper.setSize((int) target.length());
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (mCallback.get() != null) {
            mCallback.get().onPropertiesReceived(mWallpaper);
        }
    }

    public interface Callback {
        void onPropertiesReceived(Wallpaper wallpaper);
    }
}
