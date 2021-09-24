package candybar.lib.helpers;

import android.graphics.Point;

import androidx.annotation.Nullable;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.Locale;

import candybar.lib.R;
import candybar.lib.items.Home;

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

public class ViewHelper {

    public static void setFastScrollColor(@Nullable RecyclerFastScroller fastScroll) {
        if (fastScroll == null) return;

        int accent = ColorHelper.getAttributeColor(fastScroll.getContext(), R.attr.colorSecondary);
        fastScroll.setBarColor(ColorHelper.setColorAlpha(accent, 0.8f));
        fastScroll.setHandleNormalColor(accent);
        fastScroll.setHandlePressedColor(ColorHelper.getDarkerColor(accent, 0.7f));
    }

    public static Point getWallpaperViewRatio(String viewStyle) {
        switch (viewStyle.toLowerCase(Locale.getDefault())) {
            default:
            case "square":
                return new Point(1, 1);
            case "landscape":
                return new Point(16, 9);
            case "portrait":
                return new Point(4, 5);
        }
    }

    public static Home.Style getHomeImageViewStyle(String viewStyle) {
        switch (viewStyle.toLowerCase(Locale.getDefault())) {
            case "card_square":
                return new Home.Style(new Point(1, 1), Home.Style.Type.CARD_SQUARE);
            default:
            case "card_landscape":
                return new Home.Style(new Point(16, 9), Home.Style.Type.CARD_LANDSCAPE);
            case "square":
                return new Home.Style(new Point(1, 1), Home.Style.Type.SQUARE);
            case "landscape":
                return new Home.Style(new Point(16, 9), Home.Style.Type.LANDSCAPE);
        }
    }
}
