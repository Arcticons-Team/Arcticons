package candybar.lib.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.fragments.AboutFragment;
import candybar.lib.fragments.ApplyFragment;
import candybar.lib.fragments.FAQsFragment;
import candybar.lib.fragments.HomeFragment;
import candybar.lib.fragments.IconsBaseFragment;
import candybar.lib.fragments.PresetsFragment;
import candybar.lib.fragments.RequestFragment;
import candybar.lib.fragments.SettingsFragment;
import candybar.lib.fragments.WallpapersFragment;
import candybar.lib.fragments.dialog.ChangelogFragment;
import candybar.lib.fragments.dialog.IntentChooserFragment;
import candybar.lib.helpers.ConfigurationHelper;
import candybar.lib.helpers.IntentHelper;
import candybar.lib.helpers.JsonHelper;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.NavigationViewHelper;
import candybar.lib.helpers.PlayStoreCheckHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.helpers.ThemeHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.items.Home;
import candybar.lib.items.Icon;
import candybar.lib.items.InAppBilling;
import candybar.lib.items.Request;
import candybar.lib.items.Wallpaper;
import candybar.lib.preferences.Preferences;
import candybar.lib.services.CandyBarService;
import candybar.lib.tasks.IconRequestTask;
import candybar.lib.tasks.IconsLoaderTask;
import candybar.lib.utils.Extras;
import candybar.lib.utils.listeners.RequestListener;
import candybar.lib.utils.listeners.SearchListener;
import candybar.lib.utils.listeners.WallpapersListener;
import candybar.lib.utils.views.HeaderView;

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

public abstract class CandyBarMainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, RequestListener,
        SearchListener, WallpapersListener {

    private TextView mToolbarTitle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private Extras.Tag mFragmentTag;
    private int mPosition, mLastPosition;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragManager;

    private boolean mIsMenuVisible = true;
    private boolean prevIsDarkTheme;

    public static List<Request> sMissedApps;
    public static List<Icon> sSections;
    public static Home sHomeIcon;
    public static int sInstalledAppsCount;
    public static int sIconsCount;

    private ActivityConfiguration mConfig;

    @NonNull
    public abstract ActivityConfiguration onInit();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        final boolean isDarkTheme = prevIsDarkTheme = ThemeHelper.isDarkTheme(this);

        final int nightMode;
        switch (Preferences.get(this).getTheme()) {
            case LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);

        LocaleHelper.setLocale(this);
        super.setTheme(R.style.CandyBar_Theme_App);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mToolbarTitle = findViewById(R.id.toolbar_title);

        toolbar.setPopupTheme(R.style.CandyBar_Theme_App);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mFragManager = getSupportFragmentManager();

        initNavigationView(toolbar);
        initNavigationViewHeader();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigationBar));
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
            int visibilityFlags = 0;
            if (ColorHelper.isLightColor(ContextCompat.getColor(this, R.color.colorPrimaryDark)) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                visibilityFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (ColorHelper.isLightColor(ContextCompat.getColor(this, R.color.navigationBar)) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                visibilityFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(visibilityFlags);
        }

        try {
            startService(new Intent(this, CandyBarService.class));
        } catch (IllegalStateException e) {
            LogUtil.e("Unable to start CandyBarService. App is probably running in background.");
        }

        //Todo: wait until google fix the issue, then enable wallpaper crop again on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Preferences.get(this).setCropWallpaper(false);
        }

        mConfig = onInit();

        mPosition = mLastPosition = 0;
        if (savedInstanceState != null) {
            mPosition = mLastPosition = savedInstanceState.getInt(Extras.EXTRA_POSITION, 0);
            onSearchExpanded(false);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int position = bundle.getInt(Extras.EXTRA_POSITION, -1);
            if (position >= 0 && position < 6) {
                mPosition = mLastPosition = position;
            }
        }

        IntentHelper.sAction = IntentHelper.getAction(getIntent());
        if (IntentHelper.sAction == IntentHelper.ACTION_DEFAULT) {
            setFragment(getFragment(mPosition));
        } else {
            setFragment(getActionFragment(IntentHelper.sAction));
        }

        checkWallpapers();
        new IconRequestTask(this).executeOnThreadPool();
        new IconsLoaderTask(this).execute();

        /*
        The below code does this
        #1. If new version - set `firstRun` to `true`
        #2. If `firstRun` equals `true`, run the following steps
            #X. Play store check
                - Enabled: Run check, when completed run #Y
                - Disabled: Run #Y
            #Y. License check
                - Enabled: Run check, when completed run #Z
                - Disabled: Run #Z
            #Z. Reset icon request limit, clear cache and show changelog
        */

        if (Preferences.get(this).isNewVersion()) {
            // Check licenses on new version
            Preferences.get(this).setFirstRun(true);
        }

        final Runnable onNewVersion = () -> {
            ChangelogFragment.showChangelog(mFragManager);
            File cache = getCacheDir();
            FileHelper.clearDirectory(cache);
        };

        if (Preferences.get(this).isFirstRun()) {
            final Runnable checkLicenseIfEnabled = () -> {
                final Runnable onAllChecksCompleted = () -> {
                    Preferences.get(this).setFirstRun(false);
                    onNewVersion.run();
                };

                    onAllChecksCompleted.run();
            };
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (prevIsDarkTheme != ThemeHelper.isDarkTheme(this)) {
            recreate();
            return;
        }
        LocaleHelper.setLocale(this);
        if (mIsMenuVisible) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int action = IntentHelper.getAction(intent);
        if (action != IntentHelper.ACTION_DEFAULT)
            setFragment(getActionFragment(action));
        super.onNewIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extras.EXTRA_POSITION, mPosition);
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            clearBackStack();
            return;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (mFragmentTag != Extras.Tag.HOME) {
            mPosition = mLastPosition = 0;
            setFragment(getFragment(mPosition));
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
                return;
            }
            Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestSelected(int count) {
        if (mFragmentTag == Extras.Tag.REQUEST) {
            String title = getResources().getString(R.string.navigation_view_request);
            if (count > 0) title += " (" + count + ")";
            mToolbarTitle.setText(title);
        }
    }


    @Override
    public void onRequestBuilt(Intent intent, int type) {
        if (type == IntentChooserFragment.ICON_REQUEST) {
            if (RequestFragment.sSelectedRequests == null)
                return;

            if (Preferences.get(this).isPremiumRequest()) {
                int count = Preferences.get(this).getPremiumRequestCount() - RequestFragment.sSelectedRequests.size();
                Preferences.get(this).setPremiumRequestCount(count);
                if (count == 0) {
                }
            } else {
                if (getResources().getBoolean(R.bool.enable_icon_request_limit)) {
                    int used = Preferences.get(this).getRegularRequestUsed();
                    Preferences.get(this).setRegularRequestUsed((used + RequestFragment.sSelectedRequests.size()));
                }
            }

            if (mFragmentTag == Extras.Tag.REQUEST) {
                RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(Extras.Tag.REQUEST.value);
                if (fragment != null) fragment.refreshIconRequest();
            }
        }

        if (intent != null) {
            try {
                startActivity(intent);
            } catch (IllegalArgumentException e) {
                startActivity(Intent.createChooser(intent,
                        getResources().getString(R.string.app_client)));
            }
        }

        CandyBarApplication.sRequestProperty = null;
        CandyBarApplication.sZipPath = null;
    }


    @Override
    public void onWallpapersChecked(int wallpaperCount) {
        Preferences.get(this).setAvailableWallpapersCount(wallpaperCount);

        if (mFragmentTag == Extras.Tag.HOME) {
            HomeFragment fragment = (HomeFragment) mFragManager.findFragmentByTag(Extras.Tag.HOME.value);
            if (fragment != null) fragment.resetWallpapersCount();
        }
    }

    @Override
    public void onSearchExpanded(boolean expand) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mIsMenuVisible = !expand;

        if (expand) {
            int color = ContextCompat.getColor(this, R.color.toolbarIcon);
            toolbar.setNavigationIcon(DrawableHelper.getTintedDrawable(
                    this, R.drawable.ic_toolbar_back, color));
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        } else {
            SoftKeyboardHelper.closeKeyboard(this);
            ColorHelper.setStatusBarColor(this, Color.TRANSPARENT, true);
            if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
                mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(this));
            } else {
                toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                        CandyBarApplication.getConfiguration().getNavigationIcon()));
            }

            toolbar.setNavigationOnClickListener(view ->
                    mDrawerLayout.openDrawer(GravityCompat.START));
        }

        mDrawerLayout.setDrawerLockMode(expand ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                DrawerLayout.LOCK_MODE_UNLOCKED);
        supportInvalidateOptionsMenu();
    }


    private void initNavigationView(Toolbar toolbar) {
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.txt_open, R.string.txt_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                SoftKeyboardHelper.closeKeyboard(CandyBarMainActivity.this);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                selectPosition(mPosition);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                CandyBarApplication.getConfiguration().getNavigationIcon()));
        toolbar.setNavigationOnClickListener(view ->
                mDrawerLayout.openDrawer(GravityCompat.START));

        if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
            DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
            drawerArrowDrawable.setColor(ContextCompat.getColor(this, R.color.toolbarIcon));
            drawerArrowDrawable.setSpinEnabled(true);
            mDrawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationViewHelper.initApply(mNavigationView);
        NavigationViewHelper.initIconRequest(mNavigationView);
        NavigationViewHelper.initWallpapers(mNavigationView);
        NavigationViewHelper.initPresets(mNavigationView);

        ColorStateList itemStateList = ContextCompat.getColorStateList(this,
                ThemeHelper.isDarkTheme(this) ?
                        R.color.navigation_view_item_highlight_dark :
                        R.color.navigation_view_item_highlight);
        mNavigationView.setItemTextColor(itemStateList);
        mNavigationView.setItemIconTintList(itemStateList);
        Drawable background = ContextCompat.getDrawable(this,
                ThemeHelper.isDarkTheme(this) ?
                        R.drawable.navigation_view_item_background_dark :
                        R.drawable.navigation_view_item_background);
        mNavigationView.setItemBackground(background);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_view_home) mPosition = Extras.Tag.HOME.idx;
            else if (id == R.id.navigation_view_apply) mPosition = Extras.Tag.APPLY.idx;
            else if (id == R.id.navigation_view_icons) mPosition = Extras.Tag.ICONS.idx;
            else if (id == R.id.navigation_view_request) mPosition = Extras.Tag.REQUEST.idx;
            else if (id == R.id.navigation_view_wallpapers) mPosition = Extras.Tag.WALLPAPERS.idx;
            else if (id == R.id.navigation_view_presets) mPosition = Extras.Tag.PRESETS.idx;
            else if (id == R.id.navigation_view_settings) mPosition = Extras.Tag.SETTINGS.idx;
            else if (id == R.id.navigation_view_faqs) mPosition = Extras.Tag.FAQS.idx;
            else if (id == R.id.navigation_view_about) mPosition = Extras.Tag.ABOUT.idx;

            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void initNavigationViewHeader() {
        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.NONE) {
            mNavigationView.removeHeaderView(mNavigationView.getHeaderView(0));
            return;
        }

        String imageUrl = getResources().getString(R.string.navigation_view_header);
        String titleText = getResources().getString(R.string.navigation_view_header_title);
        View header = mNavigationView.getHeaderView(0);
        HeaderView image = header.findViewById(R.id.header_image);
        LinearLayout container = header.findViewById(R.id.header_title_container);
        TextView title = header.findViewById(R.id.header_title);
        TextView version = header.findViewById(R.id.header_version);

        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.MINI) {
            image.setRatio(16, 9);
        }

        if (titleText.length() == 0) {
            container.setVisibility(View.GONE);
        } else {
            title.setText(titleText);
            try {
                String versionText = "v" + getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
                version.setText(versionText);
            } catch (Exception ignored) {
            }
        }

        if (ColorHelper.isValidColor(imageUrl)) {
            image.setBackgroundColor(Color.parseColor(imageUrl));
            return;
        }

        if (!URLUtil.isValidUrl(imageUrl)) {
            imageUrl = "drawable://" + DrawableHelper.getResourceId(this, imageUrl);
        }

        Glide.with(this)
                .load(imageUrl)
                .override(720)
                .optionalCenterInside()
                .diskCacheStrategy(imageUrl.contains("drawable://")
                        ? DiskCacheStrategy.NONE
                        : DiskCacheStrategy.RESOURCE)
                .into(image);
    }

    private void checkWallpapers() {
        if (Preferences.get(this).isConnectedToNetwork()) {
            new Thread(() -> {
                try {
                    if (WallpaperHelper.getWallpaperType(this) != WallpaperHelper.CLOUD_WALLPAPERS)
                        return;

                    String wallpaperUrl = getResources().getString(R.string.wallpaper_json);
                    URL url = new URL(wallpaperUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream stream = connection.getInputStream();
                        List<?> list = JsonHelper.parseList(stream);
                        if (list == null) return;

                        List<Wallpaper> wallpapers = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                            Wallpaper wallpaper = JsonHelper.getWallpaper(list.get(i));
                            if (wallpaper != null) {
                                if (!wallpapers.contains(wallpaper)) {
                                    wallpapers.add(wallpaper);
                                } else {
                                    LogUtil.e("Duplicate wallpaper found: " + wallpaper.getURL());
                                }
                            }
                        }

                        this.runOnUiThread(() -> onWallpapersChecked(wallpapers.size()));
                    }
                } catch (IOException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }).start();
        }

        int size = Preferences.get(this).getAvailableWallpapersCount();
        if (size > 0) {
            onWallpapersChecked(size);
        }
    }

    private void clearBackStack() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            onSearchExpanded(false);
        }
    }

    public void selectPosition(int position) {
        if (position == 3) {
            if (!getResources().getBoolean(R.bool.enable_icon_request) &&
                    getResources().getBoolean(R.bool.enable_premium_request)) {
                if (!Preferences.get(this).isPremiumRequestEnabled())
                    return;

            }
        }

        if (position == 4) {
            if (WallpaperHelper.getWallpaperType(this)
                    == WallpaperHelper.EXTERNAL_APP) {
                mPosition = mLastPosition;
                mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                WallpaperHelper.launchExternalApp(CandyBarMainActivity.this);
                return;
            }
        }

        if (position != mLastPosition) {
            mLastPosition = mPosition = position;
            setFragment(getFragment(position));
        }
    }

    private void setFragment(Fragment fragment) {
        clearBackStack();

        FragmentTransaction ft = mFragManager.beginTransaction()
                .replace(R.id.container, fragment, mFragmentTag.value);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }

        Menu menu = mNavigationView.getMenu();
        menu.getItem(mPosition).setChecked(true);
        mToolbarTitle.setText(menu.getItem(mPosition).getTitle());
    }

    private Fragment getFragment(int position) {
        mFragmentTag = Extras.Tag.HOME;
        if (position == Extras.Tag.HOME.idx) {
            mFragmentTag = Extras.Tag.HOME;
            return new HomeFragment();
        } else if (position == Extras.Tag.APPLY.idx) {
            mFragmentTag = Extras.Tag.APPLY;
            return new ApplyFragment();
        } else if (position == Extras.Tag.ICONS.idx) {
            mFragmentTag = Extras.Tag.ICONS;
            return new IconsBaseFragment();
        } else if (position == Extras.Tag.REQUEST.idx) {
            mFragmentTag = Extras.Tag.REQUEST;
            return new RequestFragment();
        } else if (position == Extras.Tag.WALLPAPERS.idx) {
            mFragmentTag = Extras.Tag.WALLPAPERS;
            return new WallpapersFragment();
        } else if (position == Extras.Tag.PRESETS.idx) {
            mFragmentTag = Extras.Tag.PRESETS;
            return new PresetsFragment();
        } else if (position == Extras.Tag.SETTINGS.idx) {
            mFragmentTag = Extras.Tag.SETTINGS;
            return new SettingsFragment();
        } else if (position == Extras.Tag.FAQS.idx) {
            mFragmentTag = Extras.Tag.FAQS;
            return new FAQsFragment();
        } else if (position == Extras.Tag.ABOUT.idx) {
            mFragmentTag = Extras.Tag.ABOUT;
            return new AboutFragment();
        }
        return new HomeFragment();
    }

    private Fragment getActionFragment(int action) {
        switch (action) {
            case IntentHelper.ICON_PICKER:
            case IntentHelper.IMAGE_PICKER:
                mPosition = mLastPosition = (mFragmentTag = Extras.Tag.ICONS).idx;
                return new IconsBaseFragment();
            case IntentHelper.WALLPAPER_PICKER:
                if (WallpaperHelper.getWallpaperType(this) == WallpaperHelper.CLOUD_WALLPAPERS) {
                    mPosition = mLastPosition = (mFragmentTag = Extras.Tag.WALLPAPERS).idx;
                    return new WallpapersFragment();
                }
            default:
                mPosition = mLastPosition = (mFragmentTag = Extras.Tag.HOME).idx;
                return new HomeFragment();
        }
    }

    public static class ActivityConfiguration {

        private boolean mIsLicenseCheckerEnabled;
        private byte[] mRandomString;
        private String mLicenseKey;
        private String[] mDonationProductsId;
        private String[] mPremiumRequestProductsId;
        private int[] mPremiumRequestProductsCount;

        public ActivityConfiguration setLicenseCheckerEnabled(boolean enabled) {
            mIsLicenseCheckerEnabled = enabled;
            return this;
        }

        public ActivityConfiguration setRandomString(@NonNull byte[] randomString) {
            mRandomString = randomString;
            return this;
        }

        public ActivityConfiguration setLicenseKey(@NonNull String licenseKey) {
            mLicenseKey = licenseKey;
            return this;
        }

        public ActivityConfiguration setDonationProductsId(@NonNull String[] productsId) {
            mDonationProductsId = productsId;
            return this;
        }

        public ActivityConfiguration setPremiumRequestProducts(@NonNull String[] ids, @NonNull int[] counts) {
            mPremiumRequestProductsId = ids;
            mPremiumRequestProductsCount = counts;
            return this;
        }

        public boolean isLicenseCheckerEnabled() {
            return mIsLicenseCheckerEnabled;
        }

        public byte[] getRandomString() {
            return mRandomString;
        }

        public String getLicenseKey() {
            return mLicenseKey;
        }

        public String[] getDonationProductsId() {
            return mDonationProductsId;
        }

        public String[] getPremiumRequestProductsId() {
            return mPremiumRequestProductsId;
        }

        public int[] getPremiumRequestProductsCount() {
            return mPremiumRequestProductsCount;
        }
    }

}
