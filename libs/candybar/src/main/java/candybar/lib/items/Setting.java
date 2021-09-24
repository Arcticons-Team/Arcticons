package candybar.lib.items;

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

public class Setting {

    private final int mIcon;
    private final String mTitle;
    private final String mSubtitle;
    private final String mContent;
    private String mFooter;
    private final Setting.Type mType;

    public Setting(@DrawableRes int icon, String title, String subtitle, String content, String footer,
                   @NonNull Setting.Type type) {
        mIcon = icon;
        mTitle = title;
        mSubtitle = subtitle;
        mContent = content;
        mFooter = footer;
        mType = type;
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

    public String getContent() {
        return mContent;
    }

    public String getFooter() {
        return mFooter;
    }

    public Setting.Type getType() {
        return mType;
    }

    public void setFooter(String footer) {
        mFooter = footer;
    }

    public enum Type {
        HEADER,
        CACHE,
        ICON_REQUEST,
        RESTORE,
        PREMIUM_REQUEST,
        THEME,
        LANGUAGE,
        REPORT_BUGS,
        CHANGELOG,
        RESET_TUTORIAL
    }
}
