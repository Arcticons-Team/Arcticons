package candybar.lib.helpers;

import android.content.Context;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import candybar.lib.R;
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

public class NavigationViewHelper {

    public static void initApply(NavigationView navigationView) {
        Context context = ContextHelper.getBaseContext(navigationView);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_view_apply);
        if (menuItem == null) return;

        menuItem.setVisible(context.getResources().getBoolean(R.bool.enable_apply));
    }

    public static void initIconRequest(NavigationView navigationView) {
        Context context = ContextHelper.getBaseContext(navigationView);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_view_request);
        if (menuItem == null) return;

        menuItem.setVisible(context.getResources().getBoolean(R.bool.enable_icon_request) ||
                Preferences.get(context).isPremiumRequestEnabled());
    }

    public static void initWallpapers(NavigationView navigationView) {
        Context context = ContextHelper.getBaseContext(navigationView);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_view_wallpapers);
        if (menuItem == null) return;

        if (WallpaperHelper.getWallpaperType(context) == WallpaperHelper.UNKNOWN)
            menuItem.setVisible(false);
    }

    public static void initPresets(NavigationView navigationView) {
        Context context = ContextHelper.getBaseContext(navigationView);

        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_view_presets);
        if (menuItem == null) return;

        if (PresetsHelper.getPresetsCount(context) == 0) {
            menuItem.setVisible(false);
        }
    }
}
