package candybar.lib.helpers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
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

public class MuzeiHelper {

    @Nullable
    public static Wallpaper getRandomWallpaper(@NonNull Context context) throws Exception {
        if (Database.get(context).getWallpapersCount() == 0) {
            URL url = new URL(context.getString(R.string.wallpaper_json));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                List<?> list = JsonHelper.parseList(stream);
                if (list == null) {
                    JsonStructure jsonStructure = CandyBarApplication.getConfiguration().getWallpaperJsonStructure();
                    LogUtil.e("Muzei: Json error: wallpaper array with name "
                            + jsonStructure.getArrayName() + " not found");
                    return null;
                }

                if (list.size() > 0) {
                    int position = getRandomInt(list.size());
                    Wallpaper wallpaper = JsonHelper.getWallpaper(list.get(position));
                    if (wallpaper != null) {
                        if (wallpaper.getName() == null) {
                            wallpaper.setName("Wallpaper");
                        }
                    }
                    return wallpaper;
                }
            }
            return null;
        } else {
            return Database.get(context).getRandomWallpaper();
        }
    }


    private static int getRandomInt(int size) {
        try {
            Random random = new Random();
            return random.nextInt(size);
        } catch (Exception e) {
            return 0;
        }
    }
}
