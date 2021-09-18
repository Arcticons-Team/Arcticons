package candybar.lib.items;

import android.content.Context;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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

public class PopupItem {

    private final String mTitle;
    private int mIcon;
    private boolean mShowCheckbox;
    private boolean mCheckboxValue;
    private boolean mIsSelected;
    private Type mType;

    public PopupItem(String title) {
        mTitle = title;
        mShowCheckbox = false;
        mCheckboxValue = false;
        mIsSelected = false;
    }

    public PopupItem setIcon(@DrawableRes int icon) {
        mIcon = icon;
        return this;
    }

    public PopupItem setShowCheckbox(boolean showCheckbox) {
        mShowCheckbox = showCheckbox;
        return this;
    }

    public PopupItem setCheckboxValue(boolean checkboxValue) {
        mCheckboxValue = checkboxValue;
        return this;
    }

    public PopupItem setSelected(boolean selected) {
        mIsSelected = selected;
        return this;
    }

    public PopupItem setType(Type type) {
        mType = type;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public boolean isShowCheckbox() {
        return mShowCheckbox;
    }

    public boolean getCheckboxValue() {
        return mCheckboxValue;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public Type getType() {
        return mType;
    }

    public enum Type {
        WALLPAPER_CROP,
        HOMESCREEN,
        LOCKSCREEN,
        HOMESCREEN_LOCKSCREEN,
        DOWNLOAD
    }

    public static List<PopupItem> getApplyItems(@NonNull Context context) {
        List<PopupItem> items = new ArrayList<>();

        //Todo: wait until google fix the issue, then enable wallpaper crop again on API 26+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            items.add(new PopupItem(context.getResources().getString(R.string.menu_wallpaper_crop))
                    .setType(Type.WALLPAPER_CROP)
                    .setCheckboxValue(Preferences.get(context).isCropWallpaper())
                    .setShowCheckbox(true));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            items.add(new PopupItem(context.getResources().getString(R.string.menu_apply_lockscreen))
                    .setType(Type.LOCKSCREEN)
                    .setIcon(R.drawable.ic_toolbar_lockscreen));
        }

        items.add(new PopupItem(context.getResources().getString(R.string.menu_apply_homescreen))
                .setType(Type.HOMESCREEN)
                .setIcon(R.drawable.ic_toolbar_homescreen));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            items.add(new PopupItem(context.getResources().getString(R.string.menu_apply_homescreen_lockscreen))
                    .setType(Type.HOMESCREEN_LOCKSCREEN)
                    .setIcon(R.drawable.ic_toolbar_homescreen_lockscreen));
        }

        if (context.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
            items.add(new PopupItem(context.getResources().getString(R.string.menu_save))
                    .setType(Type.DOWNLOAD)
                    .setIcon(R.drawable.ic_toolbar_download));
        }
        return items;
    }
}
