package candybar.lib.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Request;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.listeners.RequestListener;

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

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Request> mRequests;
    private SparseBooleanArray mSelectedItems;

    private final int mTextColorSecondary;
    private final int mTextColorAccent;
    private boolean mSelectedAll = false;

    private final boolean mShowShadow;
    private final boolean mShowPremiumRequest;
    private final boolean mShowRegularRequest;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTENT = 1;
    private static final int TYPE_FOOTER = 2;

    public RequestAdapter(@NonNull Context context, @NonNull List<Request> requests, int spanCount) {
        mContext = context;
        mRequests = requests;
        mTextColorSecondary = ColorHelper.getAttributeColor(mContext,
                android.R.attr.textColorSecondary);
        mTextColorAccent = ColorHelper.getAttributeColor(mContext, R.attr.colorSecondary);
        mSelectedItems = new SparseBooleanArray();

        mShowShadow = (spanCount == 1);
        mShowPremiumRequest = Preferences.get(mContext).isPremiumRequestEnabled();
        mShowRegularRequest = Preferences.get(mContext).isRegularRequestLimit();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_header, parent, false);

            StaggeredGridLayoutManager.LayoutParams params = getLayoutParams(view);
            if (params != null) params.setFullSpan(false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_request_item_list, parent, false);

            StaggeredGridLayoutManager.LayoutParams params = getLayoutParams(view);
            if (params != null) params.setFullSpan(false);
            return new ContentViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_request_item_footer, parent, false);

        StaggeredGridLayoutManager.LayoutParams params = getLayoutParams(view);
        if (params != null) params.setFullSpan(true);
        return new FooterViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            contentViewHolder.content.setTextColor(mTextColorSecondary);

            if (mShowShadow) {
                contentViewHolder.divider.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder HeaderViewHolder = (HeaderViewHolder) holder;
            if (Preferences.get(mContext).isPremiumRequestEnabled()) {
                if (Preferences.get(mContext).isPremiumRequest()) {
                    HeaderViewHolder.button.setVisibility(View.GONE);
                    HeaderViewHolder.premContent.setVisibility(View.GONE);
                    HeaderViewHolder.premContainer.setVisibility(View.VISIBLE);

                    int total = Preferences.get(mContext).getPremiumRequestTotal();
                    int available = Preferences.get(mContext).getPremiumRequestCount();

                    HeaderViewHolder.premTotal.setText(mContext.getResources().getString(
                            R.string.premium_request_count, total));
                    HeaderViewHolder.premAvailable.setText(mContext.getResources().getString(
                            R.string.premium_request_available, available));
                    HeaderViewHolder.premUsed.setText(mContext.getResources().getString(
                            R.string.premium_request_used, (total - available)));

                    HeaderViewHolder.premProgress.setMax(total);
                    HeaderViewHolder.premProgress.setProgress(available);
                } else {
                    HeaderViewHolder.button.setVisibility(View.VISIBLE);
                    HeaderViewHolder.premContent.setVisibility(View.VISIBLE);
                    HeaderViewHolder.premContainer.setVisibility(View.GONE);
                }
            } else {
                HeaderViewHolder.premWholeContainer.setVisibility(View.GONE);
            }

            if (Preferences.get(mContext).isRegularRequestLimit()) {
                int total = mContext.getResources().getInteger(R.integer.icon_request_limit);
                int used = Preferences.get(mContext).getRegularRequestUsed();
                int available = total - used;

                HeaderViewHolder.regTotal.setText(mContext.getResources().getString(
                        R.string.regular_request_count, total));
                HeaderViewHolder.regAvailable.setText(mContext.getResources().getString(
                        R.string.regular_request_available, available));
                HeaderViewHolder.regUsed.setText(mContext.getResources().getString(
                        R.string.regular_request_used, used));

                HeaderViewHolder.regProgress.setMax(total);
                HeaderViewHolder.regProgress.setProgress(available);
            } else {
                HeaderViewHolder.regWholeContainer.setVisibility(View.GONE);
            }

            if (!mContext.getResources().getBoolean(R.bool.enable_icon_request)) {
                HeaderViewHolder.regWholeContainer.setVisibility(View.GONE);
            }
        } else if (holder.getItemViewType() == TYPE_CONTENT) {
            int finalPosition = mShowPremiumRequest ? position - 1 : position;
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;

            Glide.with(mContext)
                    .load("package://" + mRequests.get(finalPosition).getActivity())
                    .override(272)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(contentViewHolder.icon);

            contentViewHolder.title.setText(mRequests.get(finalPosition).getName());

            if (mRequests.get(finalPosition).isRequested()) {
                contentViewHolder.content.setTextColor(mTextColorAccent);
                contentViewHolder.content.setText(mContext.getResources().getString(
                        R.string.request_already_requested));
            } else {
                contentViewHolder.content.setText(mContext.getResources().getString(
                        R.string.request_not_requested));
            }

            contentViewHolder.checkbox.setChecked(mSelectedItems.get(finalPosition, false));

            if (finalPosition == (mRequests.size() - 1) && mShowShadow) {
                contentViewHolder.divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = mRequests == null ? 0 : mRequests.size();
        if (mShowShadow) count += 1;
        if (mShowPremiumRequest) count += 1;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && (mShowPremiumRequest || mShowRegularRequest)) return TYPE_HEADER;
        if (position == (getItemCount() - 1) && mShowShadow) return TYPE_FOOTER;
        return TYPE_CONTENT;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView premContent;
        private final TextView premTotal;
        private final TextView premAvailable;
        private final TextView premUsed;
        private final Button button;
        private final LinearLayout premContainer;
        private final LinearLayout premWholeContainer;
        private final ProgressBar premProgress;

        private final TextView regTotal;
        private final TextView regAvailable;
        private final TextView regUsed;
        private final LinearLayout regWholeContainer;
        private final ProgressBar regProgress;

        HeaderViewHolder(View itemView) {
            super(itemView);
            TextView premTitle = itemView.findViewById(R.id.premium_request_title);
            premContent = itemView.findViewById(R.id.premium_request_content);
            button = itemView.findViewById(R.id.buy);

            premWholeContainer = itemView.findViewById(R.id.premium_request_container);
            premContainer = itemView.findViewById(R.id.premium_request);
            premTotal = itemView.findViewById(R.id.premium_request_total);
            premAvailable = itemView.findViewById(R.id.premium_request_available);
            premUsed = itemView.findViewById(R.id.premium_request_used);
            premProgress = itemView.findViewById(R.id.premium_request_progress);


            TextView regTitle = itemView.findViewById(R.id.regular_request_title);
            TextView regContent = itemView.findViewById(R.id.regular_request_content);
            regWholeContainer = itemView.findViewById(R.id.regular_request_container);
            LinearLayout regContainer = itemView.findViewById(R.id.regular_request);
            regTotal = itemView.findViewById(R.id.regular_request_total);
            regAvailable = itemView.findViewById(R.id.regular_request_available);
            regUsed = itemView.findViewById(R.id.regular_request_used);
            regProgress = itemView.findViewById(R.id.regular_request_progress);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getRequestStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
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

            if (!Preferences.get(mContext).isCardShadowEnabled() && card != null) {
                card.setCardElevation(0);
            }

            int padding = mContext.getResources().getDimensionPixelSize(R.dimen.content_margin) + mContext.getResources().getDimensionPixelSize(R.dimen.icon_size_small);
            premContent.setPadding(padding, 0, 0, 0);
            premContainer.setPadding(padding, 0, padding, 0);

            regContent.setPadding(padding, 0, 0, 0);
            regContainer.setPadding(padding, 0, padding, 0);

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
            premTitle.setCompoundDrawablesWithIntrinsicBounds(
                    DrawableHelper.getTintedDrawable(mContext,
                            R.drawable.ic_toolbar_premium_request, color),
                    null, null, null);

            regTitle.setCompoundDrawablesWithIntrinsicBounds(
                    DrawableHelper.getTintedDrawable(mContext,
                            R.drawable.ic_toolbar_icon_request, color),
                    null, null, null);

            int primary = ColorHelper.getAttributeColor(mContext, R.attr.colorPrimary);
            int accent = ColorHelper.getAttributeColor(mContext, R.attr.colorSecondary);
            button.setTextColor(ColorHelper.getTitleTextColor(primary));

            premProgress.getProgressDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);
            regProgress.getProgressDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);

            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.buy) {
                RequestListener listener = (RequestListener) mContext;
                listener.onBuyPremiumRequest();
            }
        }
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView title;
        private final TextView content;
        private final ImageView icon;
        private final CheckBox checkbox;
        private final View divider;

        ContentViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name);
            content = itemView.findViewById(R.id.requested);
            icon = itemView.findViewById(R.id.icon);
            checkbox = itemView.findViewById(R.id.checkbox);
            LinearLayout container = itemView.findViewById(R.id.container);
            divider = itemView.findViewById(R.id.divider);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getRequestStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
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

            container.setOnClickListener(this);
            container.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = mShowPremiumRequest ?
                        getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                if (toggleSelection(position)) {
                    checkbox.toggle();
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = mShowPremiumRequest ?
                        getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                if (toggleSelection(position)) {
                    checkbox.toggle();
                    return true;
                }
            }
            return false;
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View itemView) {
            super(itemView);
            View shadow = itemView.findViewById(R.id.shadow);
            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                shadow.setVisibility(View.GONE);
            }
        }
    }

    @Nullable
    private StaggeredGridLayoutManager.LayoutParams getLayoutParams(@Nullable View view) {
        if (view != null) {
            try {
                return (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            } catch (Exception e) {
                LogUtil.d(Log.getStackTraceString(e));
            }
        }
        return null;
    }

    private boolean toggleSelection(int position) {
        if (position >= 0 && position < mRequests.size()) {
            if (mSelectedItems.get(position, false))
                mSelectedItems.delete(position);
            else mSelectedItems.put(position, true);
            try {
                RequestListener listener = (RequestListener) mContext;
                listener.onRequestSelected(getSelectedItemsSize());
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public boolean selectAll() {
        if (mSelectedAll) {
            mSelectedAll = false;
            resetSelectedItems();
            return false;
        }

        mSelectedItems.clear();
        for (int i = 0; i < mRequests.size(); i++) {
            if (!mRequests.get(i).isRequested())
                mSelectedItems.put(i, true);
        }
        mSelectedAll = mSelectedItems.size() > 0;
        notifyDataSetChanged();

        try {
            RequestListener listener = (RequestListener) mContext;
            listener.onRequestSelected(getSelectedItemsSize());
        } catch (Exception ignored) {
        }
        return mSelectedAll;
    }

    public void setRequested(int position, boolean requested) {
        mRequests.get(position).setRequested(requested);
    }

    public int getSelectedItemsSize() {
        return mSelectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < mSelectedItems.size(); i++) {
            selected.add(mSelectedItems.keyAt(i));
        }
        return selected;
    }

    public SparseBooleanArray getSelectedItemsArray() {
        return mSelectedItems;
    }

    public void setSelectedItemsArray(SparseBooleanArray selectedItems) {
        mSelectedItems = selectedItems;
        notifyDataSetChanged();
    }

    public void resetSelectedItems() {
        mSelectedAll = false;
        mSelectedItems.clear();
        try {
            RequestListener listener = (RequestListener) mContext;
            listener.onRequestSelected(getSelectedItemsSize());
        } catch (Exception ignored) {
        }
        notifyDataSetChanged();
    }

    public List<Request> getSelectedApps() {
        List<Request> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            int position = mSelectedItems.keyAt(i);
            if (position >= 0 && position < mRequests.size()) {
                Request request = mRequests.get(mSelectedItems.keyAt(i));
                items.add(request);
            }
        }
        return items;
    }

    public boolean isContainsRequested() {
        List<Request> requests = getSelectedApps();
        boolean requested = false;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).isRequested()) {
                requested = true;
                break;
            }
        }
        return requested;
    }
}
