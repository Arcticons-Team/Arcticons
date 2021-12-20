package candybar.lib.items;

import androidx.annotation.NonNull;

import java.util.List;

import candybar.lib.utils.AlphanumComparator;

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

public class Icon {

    private String mDrawableName;
    private String mCustomName;
    private String mTitle;
    private int mRes;
    private String mPackageName;
    private List<Icon> mIcons;

    public Icon(String drawableName, String customName, int res) {
        mDrawableName = drawableName;
        mCustomName = customName;
        mRes = res;
    }

    public Icon(String title, int res, String packageName) {
        mTitle = title;
        mRes = res;
        mPackageName = packageName;
    }

    public Icon(String title, @NonNull List<Icon> icons) {
        mTitle = title;
        mIcons = icons;
    }

    public String getDrawableName() {
        return mDrawableName;
    }

    public String getCustomName() {
        return mCustomName;
    }

    public Icon setTitle(String title) {
        mTitle = title;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getRes() {
        return mRes;
    }

    public String getPackageName() {
        return mPackageName;
    }

    @NonNull
    public List<Icon> getIcons() {
        return mIcons;
    }

    public void setIcons(List<Icon> icons) {
        mIcons = icons;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    @Override
    public boolean equals(Object object) {
        boolean res = false;
        boolean title = false;
        if (object instanceof Icon) {
            res = mRes == ((Icon) object).getRes();
            title = mTitle.equals(((Icon) object).getTitle());
        }
        return res && title;
    }

    public static final AlphanumComparator TitleComparator = new AlphanumComparator() {
        @Override
        public int compare(Object o1, Object o2) {
            String s1 = ((Icon) o1).getTitle();
            String s2 = ((Icon) o2).getTitle();
            return super.compare(s1, s2);
        }
    };
}
