package candybar.lib.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.adapters.dialog.ChangelogAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.fragments.dialog.DonationLinksFragment;
import candybar.lib.fragments.dialog.IconPreviewFragment;
import candybar.lib.fragments.dialog.OtherAppsFragment;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.helpers.ViewHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.items.Home;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.views.HeaderView;
import me.grantland.widget.AutofitTextView;

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

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Home> mHomes;
    private final Home.Style mImageStyle;

    private int mItemsCount;
    private int mOrientation;
    private boolean mShowWallpapers;
    private boolean mShowIconRequest;
    private boolean mShowMoreApps;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_ICON_REQUEST = 2;
    private static final int TYPE_WALLPAPERS = 3;
    private static final int TYPE_GOOGLE_PLAY_DEV = 4;

    public HomeAdapter(@NonNull Context context, @NonNull List<Home> homes, int orientation) {
        mContext = context;
        mHomes = homes;
        mOrientation = orientation;

        String viewStyle = mContext.getResources().getString(R.string.home_image_style);
        mImageStyle = ViewHelper.getHomeImageViewStyle(viewStyle);

        mItemsCount = 1;
        if (WallpaperHelper.getWallpaperType(mContext) == WallpaperHelper.CLOUD_WALLPAPERS) {
            mItemsCount += 1;
            mShowWallpapers = true;
        }

        if (mContext.getResources().getBoolean(R.bool.enable_icon_request) ||
                mContext.getResources().getBoolean(R.bool.enable_premium_request)) {
            mItemsCount += 1;
            mShowIconRequest = true;
        }

        String link = mContext.getResources().getString(R.string.google_play_dev);
        if (link.length() > 0) {
            mItemsCount += 1;
            mShowMoreApps = true;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_header, parent, false);
            if (mImageStyle.getType() == Home.Style.Type.LANDSCAPE ||
                    mImageStyle.getType() == Home.Style.Type.SQUARE) {
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.fragment_home_item_header_alt, parent, false);
            }
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_content, parent, false);
            return new ContentViewHolder(view);
        } else if (viewType == TYPE_ICON_REQUEST) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_icon_request, parent, false);
            return new IconRequestViewHolder(view);
        } else if (viewType == TYPE_WALLPAPERS) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_home_item_wallpapers, parent, false);
            return new WallpapersViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_home_item_more_apps, parent, false);
        return new GooglePlayDevViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;

            contentViewHolder.title.setSingleLine(false);
            contentViewHolder.title.setMaxLines(10);
            contentViewHolder.title.setSizeToFit(false);
            contentViewHolder.title.setGravity(Gravity.CENTER_VERTICAL);
            contentViewHolder.title.setIncludeFontPadding(true);
            contentViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            contentViewHolder.subtitle.setVisibility(View.GONE);
            contentViewHolder.subtitle.setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder.itemView != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams)
                        holder.itemView.getLayoutParams();
                layoutParams.setFullSpan(isFullSpan(holder.getItemViewType()));
            }
        } catch (Exception e) {
            LogUtil.d(Log.getStackTraceString(e));
        }

        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            String home_title_text = mContext.getResources().getString(R.string.home_title);
            if (home_title_text.length() > 0) {
                headerViewHolder.title.setText(home_title_text);
            } else {
                headerViewHolder.title.setVisibility(View.GONE);
            }

            headerViewHolder.content.setText(HtmlCompat.fromHtml(
                    mContext.getResources().getString(R.string.home_description), HtmlCompat.FROM_HTML_MODE_COMPACT));
            headerViewHolder.content.setMovementMethod(LinkMovementMethod.getInstance());

            String uri = mContext.getResources().getString(R.string.home_image);
            if (ColorHelper.isValidColor(uri)) {
                headerViewHolder.image.setBackgroundColor(Color.parseColor(uri));
            } else {
                if (!URLUtil.isValidUrl(uri)) {
                    uri = "drawable://" + DrawableHelper.getResourceId(mContext, uri);
                }

                Glide.with(mContext)
                        .load(uri)
                        .transition(DrawableTransitionOptions.withCrossFade(300))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(uri.contains("drawable://")
                                ? DiskCacheStrategy.NONE
                                : DiskCacheStrategy.RESOURCE)
                        .into(headerViewHolder.image);
            }
        } else if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            int finalPosition = position - 1;

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            if (mHomes.get(finalPosition).getIcon() != -1) {
                if (mHomes.get(finalPosition).getType() == Home.Type.DIMENSION) {
                    Glide.with(mContext)
                            .asBitmap()
                            .load("drawable://" + mHomes.get(finalPosition).getIcon())
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap bitmap, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        // Using RoundedBitmapDrawable because BitmapDrawable is deprecated
                                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(
                                                mContext.getResources(), bitmap);
                                        drawable.setCornerRadius(0);
                                        contentViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                                                DrawableHelper.getResizedDrawable(mContext, drawable, 40),
                                                null, null, null);
                                    });
                                    return true;
                                }
                            })
                            .submit();
                } else {
                    contentViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                            mContext, mHomes.get(finalPosition).getIcon(), color), null, null, null);
                }
            }

            if (mHomes.get(finalPosition).getType() == Home.Type.ICONS) {
                if (mHomes.get(finalPosition).isLoading() && CandyBarMainActivity.sIconsCount == 0 && CandyBarApplication.getConfiguration().isAutomaticIconsCountEnabled()) {
                    contentViewHolder.progressBar.setVisibility(View.VISIBLE);
                    contentViewHolder.title.setVisibility(View.GONE);
                } else {
                    contentViewHolder.progressBar.setVisibility(View.GONE);
                    contentViewHolder.title.setVisibility(View.VISIBLE);
                }

                contentViewHolder.title.setSingleLine(true);
                contentViewHolder.title.setMaxLines(1);
                contentViewHolder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.text_max_size));
                contentViewHolder.title.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                contentViewHolder.title.setIncludeFontPadding(false);
                contentViewHolder.title.setSizeToFit(true);

                contentViewHolder.subtitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            } else {
                contentViewHolder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        mContext.getResources().getDimension(R.dimen.text_content_title));
            }

            contentViewHolder.title.setTypeface(TypefaceHelper.getMedium(mContext));
            contentViewHolder.title.setText(mHomes.get(finalPosition).getTitle());

            if (mHomes.get(finalPosition).getSubtitle().length() > 0) {
                contentViewHolder.subtitle.setText(mHomes.get(finalPosition).getSubtitle());
                contentViewHolder.subtitle.setVisibility(View.VISIBLE);
            }
        } else if (holder.getItemViewType() == TYPE_ICON_REQUEST) {
            IconRequestViewHolder iconRequestViewHolder = (IconRequestViewHolder) holder;
            if (mContext.getResources().getBoolean(R.bool.hide_missing_app_count)) {
                iconRequestViewHolder.dataContainer.setVisibility(View.GONE);
                iconRequestViewHolder.progressBar.setVisibility(View.GONE);
            } else if (CandyBarMainActivity.sMissedApps == null) {
                // Missing apps are not yet loaded, show the progressbar
                iconRequestViewHolder.dataContainer.setVisibility(View.GONE);
                iconRequestViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                iconRequestViewHolder.dataContainer.setVisibility(View.VISIBLE);
                iconRequestViewHolder.progressBar.setVisibility(View.GONE);
            }

            int installed = CandyBarMainActivity.sInstalledAppsCount;
            int missed = CandyBarMainActivity.sMissedApps == null ?
                    installed : CandyBarMainActivity.sMissedApps.size();
            int themed = installed - missed;

            iconRequestViewHolder.installedApps.setText(mContext.getResources().getString(
                    R.string.home_icon_request_installed_apps, installed));
            iconRequestViewHolder.missedApps.setText(mContext.getResources().getString(
                    R.string.home_icon_request_missed_apps, missed));
            iconRequestViewHolder.themedApps.setText(mContext.getResources().getString(
                    R.string.home_icon_request_themed_apps, themed));

            iconRequestViewHolder.progress.setMax(installed);
            iconRequestViewHolder.progress.setProgress(themed);
        } else if (holder.getItemViewType() == TYPE_WALLPAPERS) {
            WallpapersViewHolder wallpapersViewHolder = (WallpapersViewHolder) holder;

            wallpapersViewHolder.title.setText(mContext.getResources().getString(
                    R.string.home_loud_wallpapers, Preferences.get(mContext).getAvailableWallpapersCount()));
        }
    }

    @Override
    public int getItemCount() {
        return mHomes.size() + mItemsCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == (mHomes.size() + 1) && mShowIconRequest) return TYPE_ICON_REQUEST;

        if (position == (getItemCount() - 2) && mShowWallpapers && mShowMoreApps)
            return TYPE_WALLPAPERS;

        if (position == (getItemCount() - 1)) {
            if (mShowMoreApps) return TYPE_GOOGLE_PLAY_DEV;
            else if (mShowWallpapers) return TYPE_WALLPAPERS;
        }
        return TYPE_CONTENT;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final HeaderView image;
        private final TextView title;
        private final TextView content;

        HeaderViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.header_image);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            Button rate = itemView.findViewById(R.id.rate);
            ImageView share = itemView.findViewById(R.id.share);
            ImageView update = itemView.findViewById(R.id.update);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                } else if (card.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
                    if (mImageStyle.getType() == Home.Style.Type.LANDSCAPE ||
                            mImageStyle.getType() == Home.Style.Type.SQUARE) {
                        params.setMargins(margin,
                                mContext.getResources().getDimensionPixelSize(R.dimen.content_padding_reverse),
                                margin, margin);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (mContext.getResources().getBoolean(R.bool.use_flat_card)) {
                card.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                card.setCardElevation(0);
                card.setUseCompatPadding(false);
                int marginTop = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_top);
                int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_left);
                int marginRight = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_right);
                int marginBottom = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            }

            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                card.setCardElevation(0);
            }

            if (mContext.getResources().getString(R.string.rate_and_review_link).length() == 0) {
                rate.setVisibility(View.GONE);
            }

            if (mContext.getResources().getString(R.string.share_link).length() == 0) {
                share.setVisibility(View.GONE);
            }

            if ((!mContext.getResources().getBoolean(R.bool.enable_check_update)) || (mContext.getResources().getString(R.string.config_json).length() == 0)) {
                update.setVisibility(View.GONE);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
            rate.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_rate, color), null, null, null);
            share.setImageDrawable(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_share, color));
            update.setImageDrawable(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_update, color));

            image.setRatio(mImageStyle.getPoint().x, mImageStyle.getPoint().y);

            rate.setOnClickListener(this);
            share.setOnClickListener(this);
            update.setOnClickListener(this);
        }

        @Override
        @SuppressLint("StringFormatInvalid")
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.rate) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getResources().getString(R.string.rate_and_review_link)
                        .replaceAll("\\{\\{packageName\\}\\}", mContext.getPackageName())));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            } else if (id == R.id.share) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources().getString(
                        R.string.share_app_title, mContext.getResources().getString(R.string.app_name)));
                intent.putExtra(Intent.EXTRA_TEXT,
                        mContext.getResources().getString(R.string.share_app_body,
                                mContext.getResources().getString(R.string.app_name),
                                "\n" + mContext.getResources().getString(R.string.share_link)
                                        .replaceAll("\\{\\{packageName\\}\\}", mContext.getPackageName())));
                mContext.startActivity(Intent.createChooser(intent,
                        mContext.getResources().getString(R.string.app_client)));
            } else if (id == R.id.update) {
                new UpdateChecker().execute();
            }
        }
    }

    private class UpdateChecker extends AsyncTaskBase {

        private MaterialDialog loadingDialog;
        private String latestVersion;
        private String updateUrl;
        private String[] changelog;
        private boolean isUpdateAvailable;

        @Override
        protected void preRun() {
            loadingDialog = new MaterialDialog.Builder(mContext)
                    .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                    .content(R.string.checking_for_update)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .build();

            loadingDialog.show();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                boolean isSuccess = true;
                String configJsonUrl = mContext.getResources().getString(R.string.config_json);
                URLConnection urlConnection;
                BufferedReader bufferedReader = null;

                try {
                    urlConnection = new URL(configJsonUrl).openConnection();
                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    JSONObject configJson = new JSONObject(stringBuilder.toString());
                    latestVersion = configJson.getString("latestVersion");
                    updateUrl = configJson.getString("url");

                    PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    long latestVersionCode = configJson.getLong("latestVersionCode");
                    long appVersionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? packageInfo.getLongVersionCode() : packageInfo.versionCode;

                    if (latestVersionCode > appVersionCode) {
                        isUpdateAvailable = true;
                        JSONArray changelogArray = configJson.getJSONArray("releaseNotes");
                        changelog = new String[changelogArray.length()];
                        for (int i = 0; i < changelogArray.length(); i++) {
                            changelog[i] = changelogArray.getString(i);
                        }
                    }
                } catch (Exception ex) {
                    LogUtil.e("Error loading Configuration JSON " + Log.getStackTraceString(ex));
                    isSuccess = false;
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            LogUtil.e(Log.getStackTraceString(e));
                        }
                    }
                }

                return isSuccess;
            }
            return false;
        }

        @Override
        @SuppressLint("SetTextI18n")
        protected void postRun(boolean ok) {
            loadingDialog.dismiss();
            loadingDialog = null;

            if (ok) {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext)
                        .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                        .customView(R.layout.fragment_update, false);

                if (isUpdateAvailable) {
                    builder
                            .positiveText(R.string.update)
                            .negativeText(R.string.close)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
                                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                mContext.startActivity(intent);
                            });
                } else {
                    builder.positiveText(R.string.close);
                }

                MaterialDialog dialog = builder.build();

                TextView changelogVersion = (TextView) dialog.findViewById(R.id.changelog_version);
                ListView mChangelogList = (ListView) dialog.findViewById(R.id.changelog_list);

                if (isUpdateAvailable) {
                    changelogVersion.setText(
                            mContext.getResources().getString(R.string.update_available) + "\n" +
                                    mContext.getResources().getString(R.string.changelog_version) + " " +
                                    latestVersion);
                    mChangelogList.setAdapter(new ChangelogAdapter(mContext, changelog));
                } else {
                    changelogVersion.setText(mContext.getResources().getString(R.string.no_update_available));
                    mChangelogList.setVisibility(View.GONE);
                }

                dialog.show();
            } else {
                new MaterialDialog.Builder(mContext)
                        .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                        .content(R.string.unable_to_load_config)
                        .positiveText(R.string.close)
                        .build()
                        .show();
            }
        }
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView subtitle;
        private final AutofitTextView title;
        private final ProgressBar progressBar;

        ContentViewHolder(View itemView) {
            super(itemView);
            LinearLayout container = itemView.findViewById(R.id.container);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            progressBar = itemView.findViewById(R.id.progressBar);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (mContext.getResources().getBoolean(R.bool.use_flat_card)) {
                card.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                card.setCardElevation(0);
                card.setUseCompatPadding(false);
                int marginTop = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_top);
                int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_left);
                int marginRight = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_right);
                int marginBottom = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            }

            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                card.setCardElevation(0);
            }

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = getBindingAdapterPosition() - 1;
                if (position < 0 || position > mHomes.size()) return;

                switch (mHomes.get(position).getType()) {
                    case APPLY:
                        ((CandyBarMainActivity) mContext).selectPosition(1);
                        break;
                    case DONATE:
                        if (mContext instanceof CandyBarMainActivity) {
                            if (CandyBarApplication.getConfiguration().getDonationLinks() != null) {
                                DonationLinksFragment.showDonationLinksDialog(((AppCompatActivity) mContext).getSupportFragmentManager());
                                break;
                            }
                        }
                        break;
                    case ICONS:
                        ((CandyBarMainActivity) mContext).selectPosition(2);
                        break;
                    case DIMENSION:
                        Home home = mHomes.get(position);
                        IconPreviewFragment.showIconPreview(
                                ((AppCompatActivity) mContext).getSupportFragmentManager(),
                                home.getTitle(), home.getIcon(), null);
                        break;
                }
            }
        }
    }

    private class IconRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView installedApps;
        private final TextView themedApps;
        private final TextView missedApps;
        private final ProgressBar progress;
        private final ProgressBar progressBar;
        private final LinearLayout dataContainer;

        IconRequestViewHolder(View itemView) {
            super(itemView);
            TextView title = itemView.findViewById(R.id.title);
            installedApps = itemView.findViewById(R.id.installed_apps);
            missedApps = itemView.findViewById(R.id.missed_apps);
            themedApps = itemView.findViewById(R.id.themed_apps);
            progress = itemView.findViewById(R.id.progress);
            LinearLayout container = itemView.findViewById(R.id.container);
            progressBar = itemView.findViewById(R.id.progressBar);
            dataContainer = itemView.findViewById(R.id.dataContainer);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (mContext.getResources().getBoolean(R.bool.use_flat_card)) {
                card.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                card.setCardElevation(0);
                card.setUseCompatPadding(false);
                int marginTop = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_top);
                int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_left);
                int marginRight = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_right);
                int marginBottom = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            }

            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_icon_request, color), null, null, null);

            int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorSecondary);
            progress.getProgressDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                ((CandyBarMainActivity) mContext).selectPosition(3);
            }
        }
    }

    private class WallpapersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;

        WallpapersViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            TextView muzei = itemView.findViewById(R.id.muzei);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (mContext.getResources().getBoolean(R.bool.use_flat_card)) {
                card.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                card.setCardElevation(0);
                card.setUseCompatPadding(false);
                int marginTop = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_top);
                int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_left);
                int marginRight = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_right);
                int marginBottom = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            }

            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_wallpapers, color), null, null, null);

            muzei.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.get(
                    mContext, R.drawable.ic_home_app_muzei), null, null, null);

            title.setOnClickListener(this);
            muzei.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.title) {
                ((CandyBarMainActivity) mContext).selectPosition(4);
            } else if (id == R.id.muzei) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=net.nurik.roman.muzei"));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            }
        }
    }

    private class GooglePlayDevViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        GooglePlayDevViewHolder(View itemView) {
            super(itemView);
            LinearLayout container = itemView.findViewById(R.id.container);
            TextView title = itemView.findViewById(R.id.title);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
                if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                    card.setRadius(0f);
                    card.setUseCompatPadding(false);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                    StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                    params.setMargins(0, 0, margin, margin);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        params.setMarginEnd(margin);
                    }
                }
            }

            if (mContext.getResources().getBoolean(R.bool.use_flat_card)) {
                card.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                card.setCardElevation(0);
                card.setUseCompatPadding(false);
                int marginTop = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_top);
                int marginLeft = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_left);
                int marginRight = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_right);
                int marginBottom = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin_bottom);
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                params.setMargins(marginLeft, marginTop, marginRight, marginBottom);
            }

            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_google_play_more_apps, color), null, null, null);

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                if (CandyBarApplication.getConfiguration().getOtherApps() != null) {
                    OtherAppsFragment.showOtherAppsDialog(((AppCompatActivity) mContext).getSupportFragmentManager());
                    return;
                }

                String link = mContext.getResources().getString(R.string.google_play_dev);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            }
        }
    }

    public int getApplyIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_CONTENT) {
                int pos = i - 1;
                if (mHomes.get(pos).getType() == Home.Type.APPLY) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public Home getItem(int position) {
        return mHomes.get(position - 1);
    }

    public int getIconsIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_CONTENT) {
                int pos = i - 1;
                if (mHomes.get(pos).getType() == Home.Type.ICONS) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public int getDimensionsIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_CONTENT) {
                int pos = i - 1;
                if (mHomes.get(pos).getType() == Home.Type.DIMENSION) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public int getIconRequestIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_ICON_REQUEST) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getWallpapersIndex() {
        int index = -1;
        for (int i = 0; i < getItemCount(); i++) {
            int type = getItemViewType(i);
            if (type == TYPE_WALLPAPERS) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void addNewContent(@Nullable Home home) {
        if (home == null) return;

        mHomes.add(home);
        notifyItemInserted(mHomes.size());
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        notifyDataSetChanged();
    }

    private boolean isFullSpan(int viewType) {
        if (viewType == TYPE_HEADER) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                return true;
            } else return mImageStyle.getType() == Home.Style.Type.SQUARE ||
                    mImageStyle.getType() == Home.Style.Type.LANDSCAPE;
        }
        return false;
    }
}
