package candybar.lib.items;

import android.graphics.Point;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

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

public class Home {

    private final int mIcon;
    private String mTitle;
    private final String mSubtitle;
    private final Home.Type mType;
    private boolean mLoading;

    public Home(@DrawableRes int icon, String title, String subtitle, @NonNull Home.Type type, boolean loading) {
        mIcon = icon;
        mTitle = title;
        mSubtitle = subtitle;
        mType = type;
        mLoading = loading;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public Home.Type getType() {
        return mType;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
    }

    public enum Type {
        APPLY,
        DONATE,
        ICONS,
        DIMENSION
    }

    public static class Style {

        private final Point mPoint;
        private final Home.Style.Type mType;

        public Style(@NonNull Point point, @NonNull Home.Style.Type type) {
            mPoint = point;
            mType = type;
        }

        public Point getPoint() {
            return mPoint;
        }

        public Type getType() {
            return mType;
        }

        public enum Type {
            CARD_SQUARE,
            CARD_LANDSCAPE,
            SQUARE,
            LANDSCAPE
        }
    }
}
