package candybar.lib.items;

import android.content.pm.ResolveInfo;

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

public class IntentChooser {

    private final ResolveInfo mApp;
    private final int mType;

    public static final int TYPE_SUPPORTED = 0;
    public static final int TYPE_RECOMMENDED = 1;
    public static final int TYPE_NOT_SUPPORTED = 2;

    public IntentChooser(ResolveInfo app, int type) {
        mApp = app;
        mType = type;
    }

    public ResolveInfo getApp() {
        return mApp;
    }

    public int getType() {
        return mType;
    }

}
