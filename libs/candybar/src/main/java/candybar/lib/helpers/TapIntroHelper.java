package candybar.lib.helpers;

import static com.danimahardhika.android.helpers.core.UnitHelper.toDp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;

import candybar.lib.R;
import candybar.lib.adapters.HomeAdapter;
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

public class TapIntroHelper {

    @SuppressLint("StringFormatInvalid")
    public static void showHomeIntros(@NonNull Context context, @Nullable RecyclerView recyclerView,
                                      @Nullable StaggeredGridLayoutManager manager, int position) {
        if (Preferences.get(context).isTimeToShowHomeIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            Toolbar toolbar = activity.findViewById(R.id.toolbar);

            new Handler().postDelayed(() -> {
                try {
                    int primary = ContextCompat.getColor(context, R.color.toolbarIcon);
                    int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                    TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                    tapTargetSequence.continueOnCancel(true);

                    Typeface title = TypefaceHelper.getMedium(context);
                    //Todo:
                    //Typeface description = TypefaceHelper.getRegular(context);

                    if (toolbar != null) {
                        TapTarget tapTarget = TapTarget.forToolbarNavigationIcon(toolbar,
                                context.getResources().getString(R.string.tap_intro_home_navigation),
                                context.getResources().getString(R.string.tap_intro_home_navigation_desc))
                                .titleTextColorInt(primary)
                                .descriptionTextColorInt(secondary)
                                .targetCircleColorInt(primary)
                                .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                        if (title != null) {
                            tapTarget.textTypeface(title);
                        }

                        //Todo:
                        //if (description != null) {
                        //tapTarget.descriptionTypeface(description);
                        //}

                        tapTargetSequence.target(tapTarget);
                    }

                    if (recyclerView != null) {
                        HomeAdapter adapter = (HomeAdapter) recyclerView.getAdapter();
                        if (adapter != null) {
                            if (context.getResources().getBoolean(R.bool.enable_apply)) {
                                if (position >= 0 && position < adapter.getItemCount()) {
                                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                                    if (holder != null) {
                                        View view = holder.itemView;
                                        float targetRadius = toDp(context, view.getMeasuredWidth()) - 20f;

                                        String desc = context.getResources().getString(R.string.tap_intro_home_apply_desc,
                                                context.getResources().getString(R.string.app_name));
                                        TapTarget tapTarget = TapTarget.forView(view,
                                                context.getResources().getString(R.string.tap_intro_home_apply),
                                                desc)
                                                .titleTextColorInt(primary)
                                                .descriptionTextColorInt(secondary)
                                                .targetCircleColorInt(primary)
                                                .targetRadius((int) targetRadius)
                                                .tintTarget(false)
                                                .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                                        if (title != null) {
                                            tapTarget.textTypeface(title);
                                        }

                                        //if (description != null) {
                                        //tapTarget.descriptionTypeface(description);
                                        //}

                                        tapTargetSequence.target(tapTarget);
                                    }
                                }
                            }
                        }
                    }

                    tapTargetSequence.listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            Preferences.get(context).setTimeToShowHomeIntro(false);
                        }

                        @Override
                        public void onSequenceStep(TapTarget tapTarget, boolean b) {
                            if (manager != null) {
                                if (position >= 0)
                                    manager.scrollToPosition(position);
                            }
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget tapTarget) {

                        }
                    });
                    tapTargetSequence.start();
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }, 100);
        }
    }

    public static void showIconsIntro(@NonNull Context context) {
        if (Preferences.get(context).isTimeToShowIconsIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar == null) return;

            new Handler().postDelayed(() -> {
                try {
                    int primary = ContextCompat.getColor(context, R.color.toolbarIcon);
                    int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                    Typeface title = TypefaceHelper.getMedium(context);

                    TapTarget tapTarget = TapTarget.forToolbarMenuItem(toolbar, R.id.menu_search,
                            context.getResources().getString(R.string.tap_intro_icons_search),
                            context.getResources().getString(R.string.tap_intro_icons_search_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                    if (title != null) {
                        tapTarget.textTypeface(title);
                    }

                    //if (description != null) {
                    //tapTarget.descriptionTypeface(description);
                    //}

                    TapTargetView.showFor(activity, tapTarget,
                            new TapTargetView.Listener() {

                                @Override
                                public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                                    super.onTargetDismissed(view, userInitiated);
                                    Preferences.get(context).setTimeToShowIconsIntro(false);
                                }
                            });
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }, 100);
        }
    }

    public static void showRequestIntro(@NonNull Context context, @Nullable RecyclerView recyclerView) {
        if (Preferences.get(context).isTimeToShowRequestIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            int requestOrientation = context.getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_PORTRAIT ?
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            activity.setRequestedOrientation(requestOrientation);

            Toolbar toolbar = activity.findViewById(R.id.toolbar);

            new Handler().postDelayed(() -> {
                try {
                    int primary = ContextCompat.getColor(context, R.color.toolbarIcon);
                    int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                    TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                    tapTargetSequence.continueOnCancel(true);

                    Typeface title = TypefaceHelper.getMedium(context);

                    if (recyclerView != null) {
                        int position = 0;
                        if (Preferences.get(context).isPremiumRequestEnabled())
                            position += 1;

                        if (recyclerView.getAdapter() != null) {
                            if (position < recyclerView.getAdapter().getItemCount()) {
                                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

                                if (holder != null) {
                                    View view = holder.itemView.findViewById(R.id.checkbox);
                                    if (view != null) {
                                        TapTarget tapTarget = TapTarget.forView(view,
                                                context.getResources().getString(R.string.tap_intro_request_select),
                                                context.getResources().getString(R.string.tap_intro_request_select_desc))
                                                .titleTextColorInt(primary)
                                                .descriptionTextColorInt(secondary)
                                                .targetCircleColorInt(primary)
                                                .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                                        if (title != null) {
                                            tapTarget.textTypeface(title);
                                        }

                                        //if (description != null) {
                                        //tapTarget.descriptionTypeface(description);
                                        //}

                                        tapTargetSequence.target(tapTarget);
                                    }
                                }
                            }
                        }
                    }

                    if (toolbar != null) {
                        TapTarget tapTarget = TapTarget.forToolbarMenuItem(toolbar, R.id.menu_select_all,
                                context.getResources().getString(R.string.tap_intro_request_select_all),
                                context.getResources().getString(R.string.tap_intro_request_select_all_desc))
                                .titleTextColorInt(primary)
                                .descriptionTextColorInt(secondary)
                                .targetCircleColorInt(primary)
                                .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                        if (title != null) {
                            tapTarget.textTypeface(title);
                        }

                        //if (description != null) {
                        //tapTarget.descriptionTypeface(description);
                        //}

                        tapTargetSequence.target(tapTarget);
                    }

                    View fab = activity.findViewById(R.id.fab);
                    if (fab != null) {
                        TapTarget tapTarget = TapTarget.forView(fab,
                                context.getResources().getString(R.string.tap_intro_request_send),
                                context.getResources().getString(R.string.tap_intro_request_send_desc))
                                .titleTextColorInt(primary)
                                .descriptionTextColorInt(secondary)
                                .targetCircleColorInt(primary)
                                .tintTarget(false)
                                .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                        if (title != null) {
                            tapTarget.textTypeface(title);
                        }

                        //if (description != null) {
                        //tapTarget.descriptionTypeface(description);
                        //}

                        tapTargetSequence.target(tapTarget);
                    }

                    if (Preferences.get(context).isPremiumRequestEnabled()) {
                        if (!Preferences.get(context).isPremiumRequest()) {
                            if (recyclerView != null) {
                                int position = 0;

                                if (recyclerView.getAdapter() != null) {
                                    if (position < recyclerView.getAdapter().getItemCount()) {
                                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

                                        if (holder != null) {
                                            View view = holder.itemView.findViewById(R.id.buy);
                                            if (view != null) {
                                                float targetRadius = toDp(context, view.getMeasuredWidth()) - 10f;

                                                TapTarget tapTarget = TapTarget.forView(view,
                                                        context.getResources().getString(R.string.tap_intro_request_premium),
                                                        context.getResources().getString(R.string.tap_intro_request_premium_desc))
                                                        .titleTextColorInt(primary)
                                                        .descriptionTextColorInt(secondary)
                                                        .targetCircleColorInt(primary)
                                                        .targetRadius((int) targetRadius)
                                                        .tintTarget(false)
                                                        .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                                                if (title != null) {
                                                    tapTarget.textTypeface(title);
                                                }

                                                //if (description != null) {
                                                //tapTarget.descriptionTypeface(description);
                                                //}

                                                tapTargetSequence.target(tapTarget);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    tapTargetSequence.listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                            Preferences.get(context).setTimeToShowRequestIntro(false);
                        }

                        @Override
                        public void onSequenceStep(TapTarget tapTarget, boolean b) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget tapTarget) {

                        }
                    });
                    tapTargetSequence.start();
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }, 100);
        }
    }

    @SuppressLint("StringFormatInvalid")
    public static void showWallpapersIntro(@NonNull Context context, @Nullable RecyclerView recyclerView) {
        if (Preferences.get(context).isTimeToShowWallpapersIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            new Handler().postDelayed(() -> {
                int primary = ContextCompat.getColor(context, R.color.toolbarIcon);
                int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                if (recyclerView != null) {
                    TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                    tapTargetSequence.continueOnCancel(true);

                    int position = 0;

                    if (recyclerView.getAdapter() == null)
                        return;

                    if (position < recyclerView.getAdapter().getItemCount()) {
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder == null) return;

                        View view = holder.itemView.findViewById(R.id.image);
                        if (view != null) {
                            float targetRadius = toDp(context, view.getMeasuredWidth()) - 10f;

                            Typeface title = TypefaceHelper.getMedium(context);

                            String desc = context.getResources().getString(R.string.tap_intro_wallpapers_option_desc,
                                    context.getResources().getBoolean(R.bool.enable_wallpaper_download) ?
                                            context.getResources().getString(R.string.tap_intro_wallpapers_option_desc_download) : "");

                            TapTarget tapTarget = TapTarget.forView(view,
                                    context.getResources().getString(R.string.tap_intro_wallpapers_option),
                                    desc)
                                    .titleTextColorInt(primary)
                                    .descriptionTextColorInt(secondary)
                                    .targetCircleColorInt(primary)
                                    .targetRadius((int) targetRadius)
                                    .tintTarget(false)
                                    .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                            TapTarget tapTarget1 = TapTarget.forView(view,
                                    context.getResources().getString(R.string.tap_intro_wallpapers_preview),
                                    context.getResources().getString(R.string.tap_intro_wallpapers_preview_desc))
                                    .titleTextColorInt(primary)
                                    .descriptionTextColorInt(secondary)
                                    .targetCircleColorInt(primary)
                                    .targetRadius((int) targetRadius)
                                    .tintTarget(false)
                                    .drawShadow(Preferences.get(context).isTapIntroShadowEnabled());

                            if (title != null) {
                                tapTarget.textTypeface(title);
                                tapTarget1.textTypeface(title);
                            }

                            //if (description != null) {
                            //tapTarget.descriptionTypeface(description);
                            //tapTarget1.descriptionTypeface(description);
                            //}

                            tapTargetSequence.target(tapTarget);
                            tapTargetSequence.target(tapTarget1);

                            tapTargetSequence.listener(new TapTargetSequence.Listener() {
                                @Override
                                public void onSequenceFinish() {
                                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                                    Preferences.get(context).setTimeToShowWallpapersIntro(false);
                                }

                                @Override
                                public void onSequenceStep(TapTarget tapTarget, boolean b) {

                                }

                                @Override
                                public void onSequenceCanceled(TapTarget tapTarget) {

                                }
                            });
                            tapTargetSequence.start();
                        }
                    }
                }
            }, 200);
        }
    }

    public static void showWallpaperPreviewIntro(@NonNull Context context, @ColorInt int color) {
        if (Preferences.get(context).isTimeToShowWallpaperPreviewIntro()) {
            AppCompatActivity activity = (AppCompatActivity) context;

            View rootView = activity.findViewById(R.id.rootview);
            if (rootView == null) return;

            new Handler().postDelayed(() -> {
                try {
                    int baseColor = color;
                    if (baseColor == 0) {
                        baseColor = ColorHelper.getAttributeColor(context, R.attr.colorSecondary);
                    }

                    int primary = ColorHelper.getTitleTextColor(baseColor);
                    int secondary = ColorHelper.setColorAlpha(primary, 0.7f);

                    TapTargetSequence tapTargetSequence = new TapTargetSequence(activity);
                    tapTargetSequence.continueOnCancel(true);

                    Typeface title = TypefaceHelper.getMedium(context);
                    //Todo:
                    //Typeface description = TypefaceHelper.getRegular(context);

                    View apply = rootView.findViewById(R.id.menu_apply);
                    View save = rootView.findViewById(R.id.menu_save);

                    TapTarget tapTarget = TapTarget.forView(apply,
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_apply),
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_apply_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .outerCircleColorInt(baseColor)
                            .drawShadow(true);

                    TapTarget tapTarget1 = TapTarget.forView(save,
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_save),
                            context.getResources().getString(R.string.tap_intro_wallpaper_preview_save_desc))
                            .titleTextColorInt(primary)
                            .descriptionTextColorInt(secondary)
                            .targetCircleColorInt(primary)
                            .outerCircleColorInt(baseColor)
                            .drawShadow(true);

                    if (title != null) {
                        //Todo:
                        //tapTarget.titleTypeface(title);
                        //tapTarget1.titleTypeface(title);
                        //tapTarget2.titleTypeface(title);
                        tapTarget.textTypeface(title);
                        tapTarget1.textTypeface(title);
                    }

                    //if (description != null) {
                    //Todo:
                    //tapTarget.descriptionTypeface(description);
                    //tapTarget1.descriptionTypeface(description);
                    //tapTarget2.descriptionTypeface(description);
                    //}

                    tapTargetSequence.target(tapTarget);
                    if (context.getResources().getBoolean(R.bool.enable_wallpaper_download)) {
                        tapTargetSequence.target(tapTarget1);
                    }

                    tapTargetSequence.listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            Preferences.get(context).setTimeToShowWallpaperPreviewIntro(false);
                        }

                        @Override
                        public void onSequenceStep(TapTarget tapTarget, boolean b) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget tapTarget) {

                        }
                    });
                    tapTargetSequence.start();
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }, 100);
        }
    }
}
