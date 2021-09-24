package candybar.lib.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.material.card.MaterialCardView;
import com.mikhaellopez.circularimageview.CircularImageView;

import candybar.lib.BuildConfig;
import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.fragments.dialog.CreditsFragment;
import candybar.lib.fragments.dialog.LicensesFragment;
import candybar.lib.helpers.ConfigurationHelper;
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

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;

    private int mItemCount;

    private final boolean mShowExtraInfo;

    private final boolean mShowContributors;
    private final boolean mShowPrivacyPolicy;
    private final boolean mShowTerms;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EXTRA_INFO = 1;
    private static final int TYPE_FOOTER = 2;
    private static final int TYPE_BOTTOM_SHADOW = 3;

    public AboutAdapter(@NonNull Context context, int spanCount) {
        mContext = context;

        mItemCount = 2;
        boolean cardMode = (spanCount > 1);
        if (!cardMode) {
            mItemCount += 1;
        }

        mShowContributors = mContext.getResources().getBoolean(R.bool.show_contributors_dialog);

        mShowPrivacyPolicy = mContext.getResources().getString(R.string.privacy_policy_link).length() > 0;

        mShowTerms = mContext.getResources().getString(R.string.terms_and_conditions_link).length() > 0;

        mShowExtraInfo = mShowContributors || mShowPrivacyPolicy || mShowTerms;

        if (mShowExtraInfo) {
            mItemCount += 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_EXTRA_INFO) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_sub, parent, false);
            return new ExtraInfoViewHolder(view);
        }

        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_footer, parent, false);
            if (CandyBarApplication.getConfiguration().getSocialIconColor() == CandyBarApplication.IconColor.ACCENT) {
                view = LayoutInflater.from(mContext).inflate(
                        R.layout.fragment_about_item_footer_accent, parent, false);
            }
            return new FooterViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_settings_item_footer, parent, false);
        return new ShadowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            String imageUri = mContext.getString(R.string.about_image);

            if (ColorHelper.isValidColor(imageUri)) {
                headerViewHolder.image.setBackgroundColor(Color.parseColor(imageUri));
            } else {
                if (!URLUtil.isValidUrl(imageUri)) {
                    imageUri = "drawable://" + DrawableHelper.getResourceId(mContext, imageUri);
                }

                Glide.with(mContext)
                        .load(imageUri)
                        .transition(DrawableTransitionOptions.withCrossFade(300))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(imageUri.contains("drawable://")
                                ? DiskCacheStrategy.NONE
                                : DiskCacheStrategy.RESOURCE)
                        .into(headerViewHolder.image);
            }

            String profileUri = mContext.getResources().getString(R.string.about_profile_image);
            if (!URLUtil.isValidUrl(profileUri)) {
                profileUri = "drawable://" + DrawableHelper.getResourceId(mContext, profileUri);
            }

            Glide.with(mContext)
                    .load(profileUri)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(profileUri.contains("drawable://")
                            ? DiskCacheStrategy.NONE
                            : DiskCacheStrategy.RESOURCE)
                    .into(headerViewHolder.profile);
        }
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) {
            if (mShowExtraInfo) return TYPE_EXTRA_INFO;
            else return TYPE_FOOTER;
        }

        if (position == 2 && mShowExtraInfo) return TYPE_FOOTER;
        return TYPE_BOTTOM_SHADOW;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final CircularImageView profile;

        HeaderViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            profile = itemView.findViewById(R.id.profile);
            TextView subtitle = itemView.findViewById(R.id.subtitle);
            RecyclerView recyclerView = itemView.findViewById(R.id.recyclerview);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, true));
            recyclerView.setHasFixedSize(true);

            String[] urls = mContext.getResources().getStringArray(R.array.about_social_links);
            if (urls.length == 0) {
                recyclerView.setVisibility(View.GONE);

                subtitle.setPadding(
                        subtitle.getPaddingLeft(),
                        subtitle.getPaddingTop(),
                        subtitle.getPaddingRight(),
                        subtitle.getPaddingBottom() + mContext.getResources().getDimensionPixelSize(R.dimen.content_margin));
            } else {
                if (recyclerView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
                    if (urls.length < 7) {
                        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                        params.gravity = Gravity.CENTER_HORIZONTAL;
                        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    }
                }
                recyclerView.setAdapter(new AboutSocialAdapter(mContext, urls));
            }

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getAboutStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
                    card != null) {
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

            if (mContext.getResources().getBoolean(R.bool.use_flat_card) && card != null) {
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
                if (card != null) card.setCardElevation(0);

                profile.setShadowRadius(0f);
                profile.setShadowColor(Color.TRANSPARENT);
            }

            subtitle.setText(HtmlCompat.fromHtml(
                    mContext.getResources().getString(R.string.about_desc), HtmlCompat.FROM_HTML_MODE_COMPACT));
            subtitle.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private class ExtraInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ExtraInfoViewHolder(View itemView) {
            super(itemView);
            LinearLayout contributorsHolder = itemView.findViewById(R.id.contributors);
            TextView contributorsTitle = itemView.findViewById(R.id.contributors_title);
            LinearLayout privacyPolicyHolder = itemView.findViewById(R.id.privacy_policy);
            TextView privacyPolicyTitle = itemView.findViewById(R.id.privacy_policy_title);
            LinearLayout termsHolder = itemView.findViewById(R.id.terms);
            TextView termsTitle = itemView.findViewById(R.id.terms_title);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getAboutStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
                    card != null) {
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

            if (mContext.getResources().getBoolean(R.bool.use_flat_card) && card != null) {
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
                if (card != null) card.setCardElevation(0);
            }


            if (!mShowContributors)
                contributorsHolder.setVisibility(View.GONE);
            if (!mShowPrivacyPolicy)
                privacyPolicyHolder.setVisibility(View.GONE);
            if (!mShowTerms)
                termsHolder.setVisibility(View.GONE);

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);

            contributorsTitle.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_people, color), null, null, null);
            contributorsTitle.setText(mContext.getResources().getString(R.string.about_contributors_title));

            privacyPolicyTitle.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_link, color), null, null, null);
            privacyPolicyTitle.setText(mContext.getResources().getString(R.string.about_privacy_policy_title));

            termsTitle.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_link, color), null, null, null);
            termsTitle.setText(mContext.getResources().getString(R.string.about_terms_and_conditions_title));

            contributorsTitle.setOnClickListener(this);
            privacyPolicyTitle.setOnClickListener(this);
            termsTitle.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.contributors_title) {
                CreditsFragment.showCreditsDialog(((AppCompatActivity) mContext).getSupportFragmentManager(),
                        CreditsFragment.TYPE_ICON_PACK_CONTRIBUTORS);
            } else if (id == R.id.privacy_policy_title) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getResources().getString(R.string.privacy_policy_link)));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            } else if (id == R.id.terms_title) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getResources().getString(R.string.terms_and_conditions_link)));
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                mContext.startActivity(intent);
            }
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        FooterViewHolder(View itemView) {
            super(itemView);
            ImageView github = itemView.findViewById(R.id.about_dashboard_github);
            TextView title = itemView.findViewById(R.id.about_dashboard_title);
            TextView licenses = itemView.findViewById(R.id.about_dashboard_licenses);
            TextView contributors = itemView.findViewById(R.id.about_dashboard_contributors);
            TextView translator = itemView.findViewById(R.id.about_dashboard_translator);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getAboutStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
                    card != null) {
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

            if (mContext.getResources().getBoolean(R.bool.use_flat_card) && card != null) {
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
                if (card != null) card.setCardElevation(0);
            }

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            title.setCompoundDrawablesWithIntrinsicBounds(DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_dashboard, color), null, null, null);
            title.append(" v" + BuildConfig.VERSION_NAME);

            color = ConfigurationHelper.getSocialIconColor(mContext,
                    CandyBarApplication.getConfiguration().getSocialIconColor());
            github.setImageDrawable(DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_toolbar_github, color));

            github.setOnClickListener(this);
            licenses.setOnClickListener(this);
            contributors.setOnClickListener(this);
            translator.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.about_dashboard_licenses) {
                LicensesFragment.showLicensesDialog(((AppCompatActivity) mContext).getSupportFragmentManager());
                return;
            }

            if (id == R.id.about_dashboard_contributors) {
                CreditsFragment.showCreditsDialog(((AppCompatActivity) mContext).getSupportFragmentManager(),
                        CreditsFragment.TYPE_DASHBOARD_CONTRIBUTORS);
                return;
            }

            if (id == R.id.about_dashboard_translator) {
                CreditsFragment.showCreditsDialog(((AppCompatActivity) mContext).getSupportFragmentManager(),
                        CreditsFragment.TYPE_DASHBOARD_TRANSLATOR);
                return;
            }

            Intent intent = null;
            if (id == R.id.about_dashboard_github) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext
                        .getResources().getString(R.string.about_dashboard_github_url)));
            }

            try {
                mContext.startActivity(intent);
            } catch (NullPointerException | ActivityNotFoundException e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        }
    }

    private class ShadowViewHolder extends RecyclerView.ViewHolder {

        ShadowViewHolder(View itemView) {
            super(itemView);
            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                View shadow = itemView.findViewById(R.id.shadow);
                shadow.setVisibility(View.GONE);
            }
        }
    }
}
