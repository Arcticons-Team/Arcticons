package candybar.lib.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;

import com.danimahardhika.android.helpers.core.ColorHelper;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;

import static com.danimahardhika.android.helpers.core.DrawableHelper.get;

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

public class ConfigurationHelper {

    public static Drawable getNavigationIcon(@NonNull Context context, @NonNull CandyBarApplication.NavigationIcon navigationIcon) {
        switch (navigationIcon) {
            case DEFAULT:
                return new DrawerArrowDrawable(context);
            case STYLE_1:
                return get(context, R.drawable.ic_toolbar_navigation);
            case STYLE_2:
                return get(context, R.drawable.ic_toolbar_navigation_2);
            case STYLE_3:
                return get(context, R.drawable.ic_toolbar_navigation_3);
            case STYLE_4:
                return get(context, R.drawable.ic_toolbar_navigation_4);
            default:
                return get(context, R.drawable.ic_toolbar_navigation);
        }
    }

    public static int getSocialIconColor(@NonNull Context context, @NonNull CandyBarApplication.IconColor iconColor) {
        if (iconColor == CandyBarApplication.IconColor.ACCENT) {
            return ColorHelper.getAttributeColor(context, R.attr.colorSecondary);
        }
        return ColorHelper.getAttributeColor(context, android.R.attr.textColorPrimary);
    }
}
