package candybar.lib.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.databases.Database;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.items.Request;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.Extras;
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

public class IconRequestTask extends AsyncTaskBase {

    private final WeakReference<Context> mContext;
    private Extras.Error mError;

    public IconRequestTask(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected boolean run() {
        if (!isCancelled()) {
            try {
                Thread.sleep(1);
                if (mContext.get().getResources().getBoolean(R.bool.enable_icon_request) ||
                        mContext.get().getResources().getBoolean(R.bool.enable_premium_request)) {
                    List<Request> requests = new ArrayList<>();
                    HashMap<String, String> appFilter = RequestHelper.getAppFilter(mContext.get(), RequestHelper.Key.ACTIVITY);
                    if (appFilter.size() == 0) {
                        mError = Extras.Error.APPFILTER_NULL;
                        return false;
                    }

                    PackageManager packageManager = mContext.get().getPackageManager();

                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                            intent, PackageManager.GET_RESOLVED_FILTER);
                    if (installedApps.size() == 0) {
                        mError = Extras.Error.INSTALLED_APPS_NULL;
                        return false;
                    }

                    CandyBarMainActivity.sInstalledAppsCount = installedApps.size();

                    try {
                        Collections.sort(installedApps,
                                new ResolveInfo.DisplayNameComparator(packageManager));
                    } catch (Exception ignored) {
                    }

                    for (ResolveInfo app : installedApps) {
                        String packageName = app.activityInfo.packageName;
                        String activity = packageName + "/" + app.activityInfo.name;

                        String value = appFilter.get(activity);

                        if (value == null) {
                            String name = LocaleHelper.getOtherAppLocaleName(mContext.get(), new Locale("en"), activity);
                            if (name == null) {
                                name = app.activityInfo.loadLabel(packageManager).toString();
                            }

                            boolean requested = Database.get(mContext.get()).isRequested(activity);
                            Request request = Request.Builder()
                                    .name(name)
                                    .packageName(app.activityInfo.packageName)
                                    .activity(activity)
                                    .requested(requested)
                                    .build();

                            requests.add(request);
                        }
                    }

                    CandyBarMainActivity.sMissedApps = requests;
                }
                return true;
            } catch (Exception e) {
                CandyBarMainActivity.sMissedApps = null;
                mError = Extras.Error.DATABASE_ERROR;
                LogUtil.e(Log.getStackTraceString(e));
                return false;
            }
        }
        return false;
    }

    @Override
    protected void postRun(boolean ok) {
        if (mContext.get() == null) return;
        if (((AppCompatActivity) mContext.get()).isFinishing()) return;

        if (ok) {
            FragmentManager fm = ((AppCompatActivity) mContext.get()).getSupportFragmentManager();
            if (fm == null) return;

            Fragment fragment = fm.findFragmentByTag("home");
            if (fragment == null) return;

            HomeListener listener = (HomeListener) fragment;
            listener.onHomeDataUpdated(null);
        } else {
            if (mError != null) {
                mError.showToast(mContext.get());
            }
        }
    }
}
