package candybar.lib.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.items.Home;
import candybar.lib.items.Icon;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.listeners.HomeListener;

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

public class IconsLoaderTask extends AsyncTaskBase {

    private final WeakReference<Context> mContext;
    private Home mHome;

    public IconsLoaderTask(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    @SuppressLint("StringFormatInvalid")
    protected boolean run() {
        if (!isCancelled()) {
            try {
                Thread.sleep(1);
                if (CandyBarMainActivity.sSections == null) {
                    CandyBarMainActivity.sSections = IconsHelper.getIconsList(mContext.get());

                    for (int i = 0; i < CandyBarMainActivity.sSections.size(); i++) {
                        List<Icon> icons = CandyBarMainActivity.sSections.get(i).getIcons();

                        if (mContext.get().getResources().getBoolean(R.bool.show_icon_name) ||
                                mContext.get().getResources().getBoolean(R.bool.enable_icon_name_replacer)) {
                            IconsHelper.computeTitles(mContext.get(), icons);
                        }

                        if (mContext.get().getResources().getBoolean(R.bool.enable_icons_sort) ||
                                mContext.get().getResources().getBoolean(R.bool.enable_icon_name_replacer)) {
                            Collections.sort(icons, Icon.TitleComparator);
                            CandyBarMainActivity.sSections.get(i).setIcons(icons);
                        }
                    }

                    if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                        List<Icon> icons = IconsHelper.getTabAllIcons();
                        CandyBarMainActivity.sSections.add(new Icon(
                                CandyBarApplication.getConfiguration().getTabAllIconsTitle(), icons));
                    }
                }

                if (CandyBarMainActivity.sHomeIcon != null) return true;

                Random random = new Random();
                int index = random.nextInt(CandyBarMainActivity.sSections.size());
                List<Icon> icons = CandyBarMainActivity.sSections.get(index).getIcons();
                index = random.nextInt(icons.size());
                Icon icon = icons.get(index);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(mContext.get().getResources(),
                        icon.getRes(), options);

                if (!mContext.get().getResources().getBoolean(R.bool.show_icon_name)) {
                    // It means the title of icon is not yet computed, so compute it
                    IconsHelper.computeTitles(mContext.get(), Collections.singletonList(icon));
                }

                String iconDimension = "";

                if (options.outWidth > 0 && options.outHeight > 0) {
                    iconDimension = mContext.get().getResources().getString(R.string.home_icon_dimension,
                            options.outWidth + " x " + options.outHeight);
                }

                mHome = new Home(
                        icon.getRes(),
                        icon.getTitle(),
                        iconDimension,
                        Home.Type.DIMENSION,
                        false);
                CandyBarMainActivity.sHomeIcon = mHome;
                return true;
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void postRun(boolean ok) {
        if (ok) {
            if (mHome == null) return;
            if (mContext.get() == null) return;

            FragmentManager fm = ((AppCompatActivity) mContext.get()).getSupportFragmentManager();
            if (fm == null) return;

            Fragment fragment = fm.findFragmentByTag("home");
            if (fragment == null) return;

            HomeListener listener = (HomeListener) fragment;
            listener.onHomeDataUpdated(mHome);
        }
    }
}
