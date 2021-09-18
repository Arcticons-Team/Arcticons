package candybar.lib.adapters.dialog;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.util.List;

import candybar.lib.R;
import candybar.lib.items.Credit;

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

public class CreditsAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Credit> mCredits;
    private final Drawable mPlaceholder;

    public CreditsAdapter(@NonNull Context context, @NonNull List<Credit> credits) {
        mContext = context;
        mCredits = credits;

        int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
        mPlaceholder = DrawableHelper.getTintedDrawable(
                mContext, R.drawable.ic_toolbar_default_profile, color);
    }

    @Override
    public int getCount() {
        return mCredits.size();
    }

    @Override
    public Credit getItem(int position) {
        return mCredits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_credits_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Credit credit = mCredits.get(position);
        holder.title.setText(credit.getName());
        holder.subtitle.setText(credit.getContribution());
        holder.container.setOnClickListener(view1 -> {
            String link = credit.getLink();
            if (URLUtil.isValidUrl(link)) {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                } catch (ActivityNotFoundException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            }
        });

        if (credit.getContribution().length() == 0) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            holder.subtitle.setVisibility(View.VISIBLE);
        }

        Glide.with(mContext)
                .load(credit.getImage())
                .override(144)
                .optionalCenterInside()
                .circleCrop()
                .placeholder(mPlaceholder)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.image);

        return view;
    }

    class ViewHolder {

        private final LinearLayout container;
        private final TextView title;
        private final TextView subtitle;
        private final ImageView image;

        ViewHolder(View view) {
            container = view.findViewById(R.id.container);
            title = view.findViewById(R.id.title);
            subtitle = view.findViewById(R.id.subtitle);
            image = view.findViewById(R.id.image);

            int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary);
            ViewCompat.setBackground(image, DrawableHelper.getTintedDrawable(
                    mContext, R.drawable.ic_toolbar_circle, ColorHelper.setColorAlpha(color, 0.4f)));
        }
    }
}
