package candybar.lib.applications;

import android.content.Intent;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.activities.CandyBarCrashReport;
import candybar.lib.databases.Database;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.items.Request;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.JsonStructure;

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

public abstract class CandyBarApplication extends MultiDexApplication {

    private static Configuration mConfiguration;
    private Thread.UncaughtExceptionHandler mHandler;

    public static Request.Property sRequestProperty;
    public static String sZipPath = null;

    @NonNull
    public abstract Configuration onInit();

    public static Configuration getConfiguration() {
        if (mConfiguration == null) {
            mConfiguration = new Configuration();
        }
        return mConfiguration;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Database.get(this).openDatabase();

        // Enable or disable logging
        LogUtil.setLoggingTag(getString(R.string.app_name));
        LogUtil.setLoggingEnabled(true);

        mConfiguration = onInit();

        if (mConfiguration.mIsCrashReportEnabled) {
            mHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
        }

        if (Preferences.get(this).isTimeToSetLanguagePreference()) {
            Preferences.get(this).setLanguagePreference();
            return;
        }

        LocaleHelper.setLocale(this);
    }

    private void handleUncaughtException(Thread thread, Throwable throwable) {
        try {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateTime = dateFormat.format(new Date());
            sb.append("Crash Time : ").append(dateTime).append("\r\n");
            sb.append("Class Name : ").append(throwable.getClass().getName()).append("\r\n");
            sb.append("Caused By : ").append(throwable.toString()).append("\r\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\r\n");
                sb.append(element.toString());
            }

            Preferences.get(this).setLatestCrashLog(sb.toString());

            Intent intent = new Intent(this, CandyBarCrashReport.class);
            intent.putExtra(CandyBarCrashReport.EXTRA_STACKTRACE, sb.toString());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } catch (Exception e) {
            if (mHandler != null) {
                mHandler.uncaughtException(thread, throwable);
                return;
            }
        }
        System.exit(1);
    }

    public static class Configuration {

        public interface EmailBodyGenerator {
            String generate(List<Request> requests);
        }

        private EmailBodyGenerator mEmailBodyGenerator;

        private NavigationIcon mNavigationIcon = NavigationIcon.STYLE_1;
        private NavigationViewHeader mNavigationViewHeader = NavigationViewHeader.NORMAL;

        private GridStyle mHomeGrid = GridStyle.CARD;
        private GridStyle mApplyGrid = GridStyle.CARD;
        private Style mRequestStyle = Style.PORTRAIT_FLAT_LANDSCAPE_CARD;
        private GridStyle mWallpapersGrid = GridStyle.CARD;
        private Style mAboutStyle = Style.PORTRAIT_FLAT_LANDSCAPE_CARD;
        private IconColor mIconColor = IconColor.PRIMARY_TEXT;
        private List<OtherApp> mOtherApps = null;
        private List<DonationLink> mDonationLinks = null;

        private boolean mIsHighQualityPreviewEnabled = false;
        private boolean mIsColoredApplyCard = true;
        private boolean mIsAutomaticIconsCountEnabled = true;
        private int mCustomIconsCount = 0;
        private boolean mIsShowTabIconsCount = false;
        private boolean mIsShowTabAllIcons = false;
        private String mTabAllIconsTitle = "All Icons";
        private String[] mCategoryForTabAllIcons = null;

        private ShadowOptions mShadowOptions = new ShadowOptions();
        private boolean mIsDashboardThemingEnabled = true;
        private int mWallpaperGridPreviewQuality = 4;

        private boolean mIsGenerateAppFilter = true;
        private boolean mIsGenerateAppMap = false;
        private boolean mIsGenerateThemeResources = false;
        private boolean mIsIncludeIconRequestToEmailBody = true;

        private boolean mIsCrashReportEnabled = true;
        private JsonStructure mWallpaperJsonStructure = new JsonStructure.Builder(null).build();

        public Configuration setEmailBodyGenerator(EmailBodyGenerator emailBodyGenerator) {
            mEmailBodyGenerator = emailBodyGenerator;
            return this;
        }

        public Configuration setDonationLinks(@NonNull DonationLink[] donationLinks) {
            mDonationLinks = Arrays.asList(donationLinks);
            return this;
        }

        public Configuration setNavigationIcon(@NonNull NavigationIcon navigationIcon) {
            mNavigationIcon = navigationIcon;
            return this;
        }

        public Configuration setNavigationViewHeaderStyle(@NonNull NavigationViewHeader navigationViewHeader) {
            mNavigationViewHeader = navigationViewHeader;
            return this;
        }

        public Configuration setAutomaticIconsCountEnabled(boolean automaticIconsCountEnabled) {
            mIsAutomaticIconsCountEnabled = automaticIconsCountEnabled;
            return this;
        }

        public Configuration setHomeGridStyle(@NonNull GridStyle gridStyle) {
            mHomeGrid = gridStyle;
            return this;
        }

        public Configuration setApplyGridStyle(@NonNull GridStyle gridStyle) {
            mApplyGrid = gridStyle;
            return this;
        }

        public Configuration setRequestStyle(@NonNull Style style) {
            mRequestStyle = style;
            return this;
        }

        public Configuration setWallpapersGridStyle(@NonNull GridStyle gridStyle) {
            mWallpapersGrid = gridStyle;
            return this;
        }

        public Configuration setAboutStyle(@NonNull Style style) {
            mAboutStyle = style;
            return this;
        }

        public Configuration setSocialIconColor(@NonNull IconColor iconColor) {
            mIconColor = iconColor;
            return this;
        }

        public Configuration setColoredApplyCard(boolean coloredApplyCard) {
            mIsColoredApplyCard = coloredApplyCard;
            return this;
        }

        public Configuration setCustomIconsCount(int customIconsCount) {
            mCustomIconsCount = customIconsCount;
            return this;
        }

        public Configuration setShowTabIconsCount(boolean showTabIconsCount) {
            mIsShowTabIconsCount = showTabIconsCount;
            return this;
        }

        public Configuration setShowTabAllIcons(boolean showTabAllIcons) {
            mIsShowTabAllIcons = showTabAllIcons;
            return this;
        }

        public Configuration setTabAllIconsTitle(@NonNull String title) {
            mTabAllIconsTitle = title;
            if (mTabAllIconsTitle.length() == 0) mTabAllIconsTitle = "All Icons";
            return this;
        }

        public Configuration setCategoryForTabAllIcons(@NonNull String[] categories) {
            mCategoryForTabAllIcons = categories;
            return this;
        }

        public Configuration setShadowEnabled(boolean shadowEnabled) {
            mShadowOptions = new ShadowOptions(shadowEnabled);
            return this;
        }

        public Configuration setShadowEnabled(@NonNull ShadowOptions shadowOptions) {
            mShadowOptions = shadowOptions;
            return this;
        }

        public Configuration setDashboardThemingEnabled(boolean dashboardThemingEnabled) {
            mIsDashboardThemingEnabled = dashboardThemingEnabled;
            return this;
        }

        public Configuration setWallpaperGridPreviewQuality(@IntRange(from = 1, to = 10) int quality) {
            mWallpaperGridPreviewQuality = quality;
            return this;
        }

        public Configuration setGenerateAppFilter(boolean generateAppFilter) {
            mIsGenerateAppFilter = generateAppFilter;
            return this;
        }

        public Configuration setGenerateAppMap(boolean generateAppMap) {
            mIsGenerateAppMap = generateAppMap;
            return this;
        }

        public Configuration setGenerateThemeResources(boolean generateThemeResources) {
            mIsGenerateThemeResources = generateThemeResources;
            return this;
        }

        public Configuration setIncludeIconRequestToEmailBody(boolean includeIconRequestToEmailBody) {
            mIsIncludeIconRequestToEmailBody = includeIconRequestToEmailBody;
            return this;
        }

        public Configuration setCrashReportEnabled(boolean crashReportEnabled) {
            mIsCrashReportEnabled = crashReportEnabled;
            return this;
        }

        public Configuration setWallpaperJsonStructure(@NonNull JsonStructure jsonStructure) {
            mWallpaperJsonStructure = jsonStructure;
            return this;
        }

        public Configuration setOtherApps(@NonNull OtherApp[] otherApps) {
            mOtherApps = Arrays.asList(otherApps);
            return this;
        }

        public Configuration setHighQualityPreviewEnabled(boolean highQualityPreviewEnabled) {
            mIsHighQualityPreviewEnabled = highQualityPreviewEnabled;
            return this;
        }

        public EmailBodyGenerator getEmailBodyGenerator() {
            return mEmailBodyGenerator;
        }

        public List<DonationLink> getDonationLinks() {
            return mDonationLinks;
        }

        public NavigationIcon getNavigationIcon() {
            return mNavigationIcon;
        }

        public NavigationViewHeader getNavigationViewHeader() {
            return mNavigationViewHeader;
        }

        public GridStyle getHomeGrid() {
            return mHomeGrid;
        }

        public GridStyle getApplyGrid() {
            return mApplyGrid;
        }

        public Style getRequestStyle() {
            return mRequestStyle;
        }

        public GridStyle getWallpapersGrid() {
            return mWallpapersGrid;
        }

        public Style getAboutStyle() {
            return mAboutStyle;
        }

        public IconColor getSocialIconColor() {
            return mIconColor;
        }

        public boolean isColoredApplyCard() {
            return mIsColoredApplyCard;
        }

        public boolean isAutomaticIconsCountEnabled() {
            return mIsAutomaticIconsCountEnabled;
        }

        public int getCustomIconsCount() {
            return mCustomIconsCount;
        }

        public boolean isShowTabIconsCount() {
            return mIsShowTabIconsCount;
        }

        public boolean isShowTabAllIcons() {
            return mIsShowTabAllIcons;
        }

        public String getTabAllIconsTitle() {
            return mTabAllIconsTitle;
        }

        public String[] getCategoryForTabAllIcons() {
            return mCategoryForTabAllIcons;
        }

        @NonNull
        public ShadowOptions getShadowOptions() {
            return mShadowOptions;
        }

        public boolean isDashboardThemingEnabled() {
            return mIsDashboardThemingEnabled;
        }

        public int getWallpaperGridPreviewQuality() {
            return mWallpaperGridPreviewQuality;
        }

        public boolean isGenerateAppFilter() {
            return mIsGenerateAppFilter;
        }

        public boolean isGenerateAppMap() {
            return mIsGenerateAppMap;
        }

        public boolean isGenerateThemeResources() {
            return mIsGenerateThemeResources;
        }

        public boolean isIncludeIconRequestToEmailBody() {
            return mIsIncludeIconRequestToEmailBody;
        }

        public boolean isHighQualityPreviewEnabled() {
            return mIsHighQualityPreviewEnabled;
        }

        public JsonStructure getWallpaperJsonStructure() {
            return mWallpaperJsonStructure;
        }

        @Nullable
        public List<OtherApp> getOtherApps() {
            return mOtherApps;
        }
    }

    public enum NavigationIcon {
        DEFAULT,
        STYLE_1,
        STYLE_2,
        STYLE_3,
        STYLE_4
    }

    public enum NavigationViewHeader {
        NORMAL,
        MINI,
        NONE
    }

    public enum GridStyle {
        CARD,
        FLAT
    }

    public enum Style {
        PORTRAIT_FLAT_LANDSCAPE_CARD,
        PORTRAIT_FLAT_LANDSCAPE_FLAT
    }

    public enum IconColor {
        PRIMARY_TEXT,
        ACCENT
    }

    public static class ShadowOptions {

        private boolean mIsToolbarEnabled;
        private boolean mIsCardEnabled;
        private boolean mIsFabEnabled;
        private boolean mIsTapIntroEnabled;

        public ShadowOptions() {
            mIsToolbarEnabled = mIsCardEnabled = mIsFabEnabled = mIsTapIntroEnabled = true;
        }

        public ShadowOptions(boolean shadowEnabled) {
            mIsToolbarEnabled = mIsCardEnabled = mIsFabEnabled = mIsTapIntroEnabled = shadowEnabled;
        }

        public ShadowOptions setToolbarEnabled(boolean toolbarEnabled) {
            mIsToolbarEnabled = toolbarEnabled;
            return this;
        }

        public ShadowOptions setCardEnabled(boolean cardEnabled) {
            mIsCardEnabled = cardEnabled;
            return this;
        }

        public ShadowOptions setFabEnabled(boolean fabEnabled) {
            mIsFabEnabled = fabEnabled;
            return this;
        }

        public ShadowOptions setTapIntroEnabled(boolean tapIntroEnabled) {
            mIsTapIntroEnabled = tapIntroEnabled;
            return this;
        }

        public boolean isToolbarEnabled() {
            return mIsToolbarEnabled;
        }

        public boolean isCardEnabled() {
            return mIsCardEnabled;
        }

        public boolean isFabEnabled() {
            return mIsFabEnabled;
        }

        public boolean isTapIntroEnabled() {
            return mIsTapIntroEnabled;
        }
    }

    public static class OtherApp {

        private final String mIcon;
        private final String mTitle;
        private final String mDescription;
        private final String mUrl;

        public OtherApp(String icon, String title, String description, String url) {
            mIcon = icon;
            mTitle = title;
            mDescription = description;
            mUrl = url;
        }

        public String getIcon() {
            return mIcon;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public String getUrl() {
            return mUrl;
        }
    }

    public static class DonationLink extends OtherApp {
        public DonationLink(String icon, String title, String description, String url) {
            super(icon, title, description, url);
        }
    }

}
