package candybar.lib.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.ThemeHelper;
import candybar.lib.items.Language;
import candybar.lib.items.Theme;

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

public class Preferences {

    private final Context mContext;

    private static final String PREFERENCES_NAME = "candybar_preferences";

    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_THEME = "theme";
    private static final String KEY_ICON_SHAPE = "icon_shape";
    private static final String KEY_APP_VERSION = "app_version";
    private static final String KEY_WIFI_ONLY = "wifi_only";
    private static final String KEY_WALLS_DIRECTORY = "wallpaper_directory";
    private static final String KEY_PREMIUM_REQUEST = "premium_request";
    private static final String KEY_PREMIUM_REQUEST_PRODUCT = "premium_request_product";
    private static final String KEY_PREMIUM_REQUEST_COUNT = "premium_request_count";
    private static final String KEY_PREMIUM_REQUEST_TOTAL = "premium_request_total";
    private static final String KEY_REGULAR_REQUEST_USED = "regular_request_used";
    private static final String KEY_INAPP_BILLING_TYPE = "inapp_billing_type";
    private static final String KEY_LATEST_CRASHLOG = "last_crashlog";
    private static final String KEY_PREMIUM_REQUEST_ENABLED = "premium_request_enabled";
    private static final String KEY_AVAILABLE_WALLPAPERS_COUNT = "available_wallpapers_count";
    private static final String KEY_CROP_WALLPAPER = "crop_wallpaper";
    private static final String KEY_HOME_INTRO = "home_intro";
    private static final String KEY_ICONS_INTRO = "icons_intro";
    private static final String KEY_REQUEST_INTRO = "request_intro";
    private static final String KEY_WALLPAPERS_INTRO = "wallpapers_intro";
    private static final String KEY_WALLPAPER_PREVIEW_INTRO = "wallpaper_preview_intro";

    private static final String KEY_LANGUAGE_PREFERENCE = "language_preference";
    private static final String KEY_CURRENT_LOCALE = "current_locale";

    private static WeakReference<Preferences> mPreferences;

    @NonNull
    public static Preferences get(@NonNull Context context) {
        if (mPreferences == null || mPreferences.get() == null) {
            mPreferences = new WeakReference<>(new Preferences(context));
        }
        return mPreferences.get();
    }

    private Preferences(Context context) {
        mContext = context;
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void clearPreferences() {
        getSharedPreferences().edit().clear().apply();
        
    }

    public boolean isFirstRun() {
        return getSharedPreferences().getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRun(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_FIRST_RUN, bool).apply();
    }

    public void setIconShape(int shape) {
        getSharedPreferences().edit().putInt(KEY_ICON_SHAPE, shape).apply();
    }

    public int getIconShape() {
        return getSharedPreferences().getInt(KEY_ICON_SHAPE, -1); // -1 is System default
    }

    public boolean isTimeToShowHomeIntro() {
        return getSharedPreferences().getBoolean(KEY_HOME_INTRO, true);
    }

    public void setTimeToShowHomeIntro(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_HOME_INTRO, bool).apply();
    }

    public boolean isTimeToShowIconsIntro() {
        return getSharedPreferences().getBoolean(KEY_ICONS_INTRO, true);
    }

    public void setTimeToShowIconsIntro(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_ICONS_INTRO, bool).apply();
    }

    public boolean isTimeToShowRequestIntro() {
        return getSharedPreferences().getBoolean(KEY_REQUEST_INTRO, true);
    }

    public void setTimeToShowRequestIntro(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_REQUEST_INTRO, bool).apply();
    }

    public boolean isTimeToShowWallpapersIntro() {
        return getSharedPreferences().getBoolean(KEY_WALLPAPERS_INTRO, true);
    }

    public void setTimeToShowWallpapersIntro(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WALLPAPERS_INTRO, bool).apply();
    }

    public boolean isTimeToShowWallpaperPreviewIntro() {
        return getSharedPreferences().getBoolean(KEY_WALLPAPER_PREVIEW_INTRO, true);
    }

    public void setTimeToShowWallpaperPreviewIntro(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_WALLPAPER_PREVIEW_INTRO, bool).apply();
    }

    public Theme getTheme() {
        return Theme.values()[getSharedPreferences().getInt(KEY_THEME, ThemeHelper.getDefaultTheme(mContext).ordinal())];
    }

    public void setTheme(Theme theme) {
        getSharedPreferences().edit().putInt(KEY_THEME, theme.ordinal()).apply();
    }

    public boolean isToolbarShadowEnabled() {
        return CandyBarApplication.getConfiguration().getShadowOptions().isToolbarEnabled();
    }

    public boolean isCardShadowEnabled() {
        return CandyBarApplication.getConfiguration().getShadowOptions().isCardEnabled();
    }

    public boolean isFabShadowEnabled() {
        return CandyBarApplication.getConfiguration().getShadowOptions().isFabEnabled();
    }

    public boolean isTapIntroShadowEnabled() {
        return CandyBarApplication.getConfiguration().getShadowOptions().isTapIntroEnabled();
    }

    public boolean isWifiOnly() {
        return getSharedPreferences().getBoolean(KEY_WIFI_ONLY, false);
    }

    public String getWallsDirectory() {
        return getSharedPreferences().getString(KEY_WALLS_DIRECTORY, "");
    }

    public boolean isPremiumRequestEnabled() {
        return getSharedPreferences().getBoolean(KEY_PREMIUM_REQUEST_ENABLED,
                mContext.getResources().getBoolean(R.bool.enable_premium_request));
    }

    public void setPremiumRequestEnabled(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_PREMIUM_REQUEST_ENABLED, bool).apply();
    }

    public boolean isPremiumRequest() {
        return getSharedPreferences().getBoolean(KEY_PREMIUM_REQUEST, false);
    }

    public void setPremiumRequest(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_PREMIUM_REQUEST, bool).apply();
    }

    public String getPremiumRequestProductId() {
        return getSharedPreferences().getString(KEY_PREMIUM_REQUEST_PRODUCT, "");
    }

    public void setPremiumRequestProductId(String productId) {
        getSharedPreferences().edit().putString(KEY_PREMIUM_REQUEST_PRODUCT, productId).apply();
    }

    public int getPremiumRequestCount() {
        return getSharedPreferences().getInt(KEY_PREMIUM_REQUEST_COUNT, 0);
    }

    public void setPremiumRequestCount(int count) {
        getSharedPreferences().edit().putInt(KEY_PREMIUM_REQUEST_COUNT, count).apply();
    }

    public int getPremiumRequestTotal() {
        int count = getPremiumRequestCount();
        return getSharedPreferences().getInt(KEY_PREMIUM_REQUEST_TOTAL, count);
    }

    public void setPremiumRequestTotal(int count) {
        getSharedPreferences().edit().putInt(KEY_PREMIUM_REQUEST_TOTAL, count).apply();
    }

    public int getRegularRequestUsed() {
        return getSharedPreferences().getInt(KEY_REGULAR_REQUEST_USED, 0);
    }

    public boolean isRegularRequestLimit() {
        return mContext.getResources().getBoolean(R.bool.enable_icon_request_limit);
    }

    public void setRegularRequestUsed(int used) {
        getSharedPreferences().edit().putInt(KEY_REGULAR_REQUEST_USED, used).apply();
    }

    public int getInAppBillingType() {
        return getSharedPreferences().getInt(KEY_INAPP_BILLING_TYPE, -1);
    }

    public void setInAppBillingType(int type) {
        getSharedPreferences().edit().putInt(KEY_INAPP_BILLING_TYPE, type).apply();
    }

    public boolean isCropWallpaper() {
        return getSharedPreferences().getBoolean(KEY_CROP_WALLPAPER, false);
    }

    public void setCropWallpaper(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_CROP_WALLPAPER, bool).apply();
    }

    public String getLatestCrashLog() {
        return getSharedPreferences().getString(KEY_LATEST_CRASHLOG, "");
    }

    public void setLatestCrashLog(String string) {
        getSharedPreferences().edit().putString(KEY_LATEST_CRASHLOG, string).apply();
    }

    public int getAvailableWallpapersCount() {
        return getSharedPreferences().getInt(KEY_AVAILABLE_WALLPAPERS_COUNT, 0);
    }

    public void setAvailableWallpapersCount(int count) {
        getSharedPreferences().edit().putInt(KEY_AVAILABLE_WALLPAPERS_COUNT, count).apply();
    }

    public boolean isPlayStoreCheckEnabled() {
        return mContext.getResources().getBoolean(R.bool.playstore_check_enabled);
    }

    private int getVersion() {
        return getSharedPreferences().getInt(KEY_APP_VERSION, 0);
    }

    private void setVersion(int version) {
        getSharedPreferences().edit().putInt(KEY_APP_VERSION, version).apply();
    }

    public boolean isNewVersion() {
        int version = 0;
        try {
            version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        if (version > getVersion()) {
            boolean resetLimit = mContext.getResources().getBoolean(R.bool.reset_icon_request_limit);
            if (resetLimit) setRegularRequestUsed(0);
            setVersion(version);
            return true;
        } else {
            return false;
        }
    }

    public Locale getCurrentLocale() {
        String code = getSharedPreferences().getString(KEY_CURRENT_LOCALE, "en_US");
        return LocaleHelper.getLocale(code);
    }

    public void setCurrentLocale(String code) {
        getSharedPreferences().edit().putString(KEY_CURRENT_LOCALE, code).apply();
    }

    public boolean isTimeToSetLanguagePreference() {
        return getSharedPreferences().getBoolean(KEY_LANGUAGE_PREFERENCE, true);
    }

    private void setTimeToSetLanguagePreference(boolean bool) {
        getSharedPreferences().edit().putBoolean(KEY_LANGUAGE_PREFERENCE, bool).apply();
    }

    public void setLanguagePreference() {
        Locale locale = Locale.getDefault();
        List<Language> languages = LocaleHelper.getAvailableLanguages(mContext);

        Locale currentLocale = null;
        for (Language language : languages) {
            Locale l = language.getLocale();
            if (locale.toString().equals(l.toString())) {
                currentLocale = l;
                break;
            }
        }

        if (currentLocale == null) {
            for (Language language : languages) {
                Locale l = language.getLocale();
                if (locale.getLanguage().equals(l.getLanguage())) {
                    currentLocale = l;
                    break;
                }
            }
        }

        if (currentLocale != null) {
            setCurrentLocale(currentLocale.toString());
            LocaleHelper.setLocale(mContext);
            setTimeToSetLanguagePreference(false);
        }
    }

    public boolean isConnectedToNetwork() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connectivityManager != null;
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnectedAsPreferred() {
        try {
            if (isWifiOnly()) {
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                assert connectivityManager != null;
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                assert activeNetworkInfo != null;
                return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                        activeNetworkInfo.isConnected();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
