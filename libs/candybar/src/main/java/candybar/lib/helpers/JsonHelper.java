package candybar.lib.helpers;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Wallpaper;
import candybar.lib.utils.JsonStructure;

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

public class JsonHelper {

    @SuppressLint("Raw")
    @Nullable
    public static List<?> parseList(@NonNull InputStream stream) {
        List<?> list = null;
        JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();

        try {
            if (jsonStructure.getArrayName() == null) {
                list = LoganSquare.parseList(stream, Map.class);
            } else {
                Map<String, List> map = LoganSquare.parseMap(stream, List.class);
                list = map.get(jsonStructure.getArrayName());
            }
        } catch (IOException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return list;
    }

    @Nullable
    public static Wallpaper getWallpaper(@NonNull Object object) {
        if (object instanceof Map) {
            JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();
            Map<?, ?> map = (Map<?, ?>) object;
            return Wallpaper.Builder()
                    .name((String) map.get(jsonStructure.getName()))
                    .author(getAuthor(map))
                    .url((String) map.get(jsonStructure.getUrl()))
                    .thumbUrl(getThumbUrl(map))
                    .build();
        }
        return null;
    }

    public static String getThumbUrl(@NonNull Map<?, ?> map) {
        JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();
        String url = (String) map.get(jsonStructure.getUrl());
        if (jsonStructure.getThumbUrl() == null) return url;
        String[] thumbUrlSelectors = {"thumb", "thumbnail", "thumbUrl", "url-thumb", "urlThumb"};

        String thumbUrl = (String) map.get(jsonStructure.getThumbUrl());
        for (String selector : thumbUrlSelectors) {
            if (thumbUrl == null) thumbUrl = (String) map.get(selector);
        }
        if (thumbUrl == null) return url;
        return thumbUrl;
    }

    private static String getAuthor(@NonNull Map<?, ?> map) {
        JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();
        String defaultAuthorName = "";

        String authorName = (String) map.get(jsonStructure.getAuthor());
        if (authorName == null) return defaultAuthorName;
        return authorName;
    }
}
