package candybar.lib.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.WindowHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.kogitune.activitytransition.ActivityTransition;
import com.kogitune.activitytransition.ExitActivityTransition;

import candybar.lib.R;
import candybar.lib.adapters.WallpapersAdapter;
import candybar.lib.databases.Database;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.TapIntroHelper;
import candybar.lib.helpers.ThemeHelper;
import candybar.lib.items.PopupItem;
import candybar.lib.items.Wallpaper;
import candybar.lib.preferences.Preferences;
import candybar.lib.tasks.WallpaperApplyTask;
import candybar.lib.tasks.WallpaperPropertiesLoaderTask;
import candybar.lib.utils.Extras;
import candybar.lib.utils.Popup;
import candybar.lib.utils.WallpaperDownloader;
import uk.co.senab.photoview.PhotoViewAttacher;

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

public class CandyBarWallpaperActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, WallpaperPropertiesLoaderTask.Callback {

    private ImageView mImageView;
    private ProgressBar mProgress;
    private LinearLayout mBottomBar;
    private TextView mName;
    private TextView mAuthor;
    private ImageView mBack;
    private ImageView mMenuApply;
    private ImageView mMenuSave;

    private boolean mIsEnter;
    private boolean mIsResumed = false;

    private Wallpaper mWallpaper;
    private Runnable mRunnable;
    private Handler mHandler;
    private PhotoViewAttacher mAttacher;
    private ExitActivityTransition mExitTransition;

    private boolean prevIsDarkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        prevIsDarkTheme = ThemeHelper.isDarkTheme(this);
        super.setTheme(R.style.CandyBar_Theme_Wallpaper);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        mIsEnter = true;

        mImageView = findViewById(R.id.wallpaper);
        mProgress = findViewById(R.id.progress);
        mBottomBar = findViewById(R.id.bottom_bar);
        mName = findViewById(R.id.name);
        mAuthor = findViewById(R.id.author);
        mBack = findViewById(R.id.back);
        mMenuApply = findViewById(R.id.menu_apply);
        mMenuSave = findViewById(R.id.menu_save);

        mProgress.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("#CCFFFFFF"), PorterDuff.Mode.SRC_IN);
        mBack.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_back, Color.WHITE));
        mBack.setOnClickListener(this);

        String url = "";
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(Extras.EXTRA_URL);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString(Extras.EXTRA_URL);
        }

        mWallpaper = Database.get(this.getApplicationContext()).getWallpaper(url);
        if (mWallpaper == null) {
            finish();
            return;
        }

        initBottomBar();
        resetBottomBarPadding();

        if (!mIsResumed) {
            mExitTransition = ActivityTransition
                    .with(getIntent())
                    .to(this, mImageView, Extras.EXTRA_IMAGE)
                    .duration(300)
                    .start(savedInstanceState);
        }

        if (mImageView.getDrawable() == null) {
            int color = mWallpaper.getColor();
            if (color == 0) {
                color = ContextCompat.getColor(this, R.color.cardBackground);
            }

            AnimationHelper.setBackgroundColor(findViewById(R.id.rootview), Color.TRANSPARENT, color).start();
            mProgress.getIndeterminateDrawable().setColorFilter(
                    ColorHelper.setColorAlpha(ColorHelper.getTitleTextColor(color), 0.7f),
                    PorterDuff.Mode.SRC_IN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && savedInstanceState == null) {
            Transition transition = getWindow().getSharedElementEnterTransition();

            if (transition != null) {
                transition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        if (mIsEnter) {
                            mIsEnter = false;

                            AnimationHelper.fade(mBottomBar).duration(400).start();
                            loadWallpaper();
                        }
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {
                    }

                    @Override
                    public void onTransitionPause(Transition transition) {
                    }

                    @Override
                    public void onTransitionResume(Transition transition) {
                    }
                });

                return;
            }
        }

        mRunnable = () -> {
            AnimationHelper.fade(mBottomBar).duration(400).start();
            loadWallpaper();

            mRunnable = null;
            mHandler = null;
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 700);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (prevIsDarkTheme != ThemeHelper.isDarkTheme(this)) {
            recreate();
            return;
        }
        LocaleHelper.setLocale(this);
        resetBottomBarPadding();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (mWallpaper != null) {
            outState.putString(Extras.EXTRA_URL, mWallpaper.getURL());
        }

        outState.putBoolean(Extras.EXTRA_RESUMED, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (Preferences.get(this).isCropWallpaper()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
        Glide.get(this).clearMemory();
        if (mAttacher != null) mAttacher.cleanup();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        WallpapersAdapter.sIsClickable = true;
        if (mHandler != null && mRunnable != null)
            mHandler.removeCallbacks(mRunnable);

        if (mExitTransition != null) {
            mExitTransition.exit(this);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back) {
            onBackPressed();
        } else if (id == R.id.menu_apply) {
            Popup popup = Popup.Builder(this)
                    .to(mMenuApply)
                    .list(PopupItem.getApplyItems(this))
                    .callback((p, position) -> {
                        PopupItem item = p.getItems().get(position);
                        if (item.getType() == PopupItem.Type.WALLPAPER_CROP) {
                            Preferences.get(this).setCropWallpaper(!item.getCheckboxValue());
                            item.setCheckboxValue(Preferences.get(this).isCropWallpaper());

                            p.updateItem(position, item);
                            if (Preferences.get(this).isCropWallpaper()) {
                                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                }
                                return;
                            }

                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            return;
                        } else {
                            RectF rectF = null;
                            if (Preferences.get(this).isCropWallpaper()) {
                                if (mAttacher != null)
                                    rectF = mAttacher.getDisplayRect();
                            }

                            WallpaperApplyTask task = new WallpaperApplyTask(this, mWallpaper)
                                    .crop(rectF);

                            if (item.getType() == PopupItem.Type.LOCKSCREEN) {
                                task.to(WallpaperApplyTask.Apply.LOCKSCREEN);
                            } else if (item.getType() == PopupItem.Type.HOMESCREEN) {
                                task.to(WallpaperApplyTask.Apply.HOMESCREEN);
                            } else if (item.getType() == PopupItem.Type.HOMESCREEN_LOCKSCREEN) {
                                task.to(WallpaperApplyTask.Apply.HOMESCREEN_LOCKSCREEN);
                            }

                            task.executeOnThreadPool();
                        }

                        p.dismiss();
                    })
                    .build();

            if (getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                popup.removeItem(popup.getItems().size() - 1);
            }
            popup.show();
        } else if (id == R.id.menu_save) {
            WallpaperDownloader.prepare(this)
                    .wallpaper(mWallpaper)
                    .start();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();
        int res = 0;
        if (id == R.id.menu_apply) {
            res = R.string.wallpaper_apply;
        } else if (id == R.id.menu_save) {
            res = R.string.wallpaper_save_to_device;
        }

        if (res == 0) return false;

        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                WallpaperDownloader.prepare(this).wallpaper(mWallpaper).start();
            } else {
                Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPropertiesReceived(Wallpaper wallpaper) {
        if (wallpaper == null) return;

        mWallpaper.setDimensions(wallpaper.getDimensions());
        mWallpaper.setSize(wallpaper.getSize());
        mWallpaper.setMimeType(wallpaper.getMimeType());
    }

    private void initBottomBar() {
        mName.setText(mWallpaper.getName());
        mName.setTextColor(Color.WHITE);
        mAuthor.setText(mWallpaper.getAuthor());
        mAuthor.setTextColor(ColorHelper.setColorAlpha(Color.WHITE, 0.7f));
        mMenuSave.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_download, Color.WHITE));
        mMenuApply.setImageDrawable(DrawableHelper.getTintedDrawable(
                this, R.drawable.ic_toolbar_apply_options, Color.WHITE));

        if (getResources().getBoolean(R.bool.enable_wallpaper_download)) {
            mMenuSave.setVisibility(View.VISIBLE);
        }

        mMenuApply.setOnClickListener(this);
        mMenuSave.setOnClickListener(this);

        mMenuApply.setOnLongClickListener(this);
        mMenuSave.setOnLongClickListener(this);
    }

    private void resetBottomBarPadding() {
        LinearLayout container = findViewById(R.id.bottom_bar_container);
        int height = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
        int bottom = 0;
        int right = WindowHelper.getNavigationBarHeight(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBack.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mBack.getLayoutParams();
                params.topMargin = WindowHelper.getStatusBarHeight(this);
            }

            boolean tabletMode = getResources().getBoolean(R.bool.android_helpers_tablet_mode);
            if (tabletMode || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                bottom = right;
                right = 0;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (isInMultiWindowMode()) {
                    bottom = right = 0;
                }
            }
        }

        container.setPadding(0, 0, right, bottom);

        if (container.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
            params.height = height + bottom;
        }
    }

    private void loadWallpaper() {
        if (mAttacher != null) {
            mAttacher.cleanup();
            mAttacher = null;
        }

        new WallpaperPropertiesLoaderTask(this, mWallpaper, this)
                .executeOnThreadPool();

        Glide.with(this)
                .asBitmap()
                .load(mWallpaper.getURL())
                .override(2000)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .timeout(10000)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (mWallpaper.getColor() == 0) {
                            mWallpaper.setColor(ColorHelper.getAttributeColor(
                                    CandyBarWallpaperActivity.this, R.attr.colorSecondary));
                        }

                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap loadedImage, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (loadedImage != null && mWallpaper.getColor() == 0) {
                            Palette.from(loadedImage).generate(palette -> {
                                if (palette != null) {
                                    int accent = ColorHelper.getAttributeColor(
                                            CandyBarWallpaperActivity.this, R.attr.colorSecondary);
                                    int color = palette.getVibrantColor(accent);
                                    if (color == accent)
                                        color = palette.getMutedColor(accent);

                                    mWallpaper.setColor(color);
                                    Database.get(CandyBarWallpaperActivity.this).updateWallpaper(mWallpaper);
                                }

                                onWallpaperLoaded();
                            });
                        } else {
                            onWallpaperLoaded();
                        }

                        return false;
                    }
                })
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                        mImageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { /* Do nothing */ }
                });

        if (Preferences.get(CandyBarWallpaperActivity.this).isCropWallpaper()) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        AnimationHelper.fade(mProgress).start();
    }

    private void onWallpaperLoaded() {
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);

        AnimationHelper.fade(mProgress).start();
        mRunnable = null;
        mHandler = null;
        mIsResumed = false;

        if (this.getResources().getBoolean(R.bool.show_intro)) {
            TapIntroHelper.showWallpaperPreviewIntro(this, mWallpaper.getColor());
        }
    }
}
