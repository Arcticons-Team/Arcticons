package candybar.lib.adapters;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.kogitune.activitytransition.ActivityTransitionLauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.activities.CandyBarWallpaperActivity;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.ViewHelper;
import candybar.lib.items.PopupItem;
import candybar.lib.items.Wallpaper;
import candybar.lib.preferences.Preferences;
import candybar.lib.tasks.WallpaperApplyTask;
import candybar.lib.utils.Extras;
import candybar.lib.utils.ImageConfig;
import candybar.lib.utils.Popup;
import candybar.lib.utils.WallpaperDownloader;
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

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Wallpaper> mWallpapers;
    private final List<Wallpaper> mWallpapersAll;

    public static boolean sIsClickable = true;
    private final boolean mIsShowName;

    public WallpapersAdapter(@NonNull Context context, @NonNull List<Wallpaper> wallpapers) {
        mContext = context;
        mWallpapers = wallpapers;
        mWallpapersAll = new ArrayList<>(wallpapers);
        mIsShowName = mContext.getResources().getBoolean(R.bool.wallpaper_show_name_author);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (mIsShowName) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_wallpapers_item_grid, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_wallpapers_item_grid_alt, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallpaper wallpaper = mWallpapers.get(position);
        if (mIsShowName) {
            holder.name.setText(wallpaper.getName());
            holder.author.setText(wallpaper.getAuthor());
        }

        Glide.with(mContext)
                .asBitmap()
                .load(wallpaper.getThumbUrl())
                .override(ImageConfig.getThumbnailSize())
                .transition(BitmapTransitionOptions.withCrossFade(300))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.thumbnailBitmap = resource;
                        return false;
                    }
                })
                .into(holder.image);
    }

    public void search(String string) {
        String query = string.toLowerCase(Locale.getDefault()).trim();
        mWallpapers.clear();
        if (query.length() == 0) mWallpapers.addAll(mWallpapersAll);
        else {
            for (int i = 0; i < mWallpapersAll.size(); i++) {
                Wallpaper wallpaper = mWallpapersAll.get(i);
                if (wallpaper.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    mWallpapers.add(wallpaper);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mWallpapers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private final HeaderView image;
        private TextView name;
        private TextView author;
        private Bitmap thumbnailBitmap;

        ViewHolder(View itemView) {
            super(itemView);
            String viewStyle = mContext.getResources().getString(
                    R.string.wallpaper_grid_preview_style);
            Point ratio = ViewHelper.getWallpaperViewRatio(viewStyle);

            image = itemView.findViewById(R.id.image);
            image.setRatio(ratio.x, ratio.y);

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (CandyBarApplication.getConfiguration().getWallpapersGrid() == CandyBarApplication.GridStyle.FLAT) {
                card.setCardElevation(0);
                card.setMaxCardElevation(0);
            }

            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                card.setCardElevation(0);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                StateListAnimator stateListAnimator = AnimatorInflater
                        .loadStateListAnimator(mContext, R.animator.card_lift);
                card.setStateListAnimator(stateListAnimator);
            }

            if (mIsShowName) {
                name = itemView.findViewById(R.id.name);
                author = itemView.findViewById(R.id.author);
            }

            card.setOnClickListener(this);
            card.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getBindingAdapterPosition();
            if (id == R.id.card) {
                if (sIsClickable) {
                    sIsClickable = false;
                    try {
                        final Intent intent = new Intent(mContext, CandyBarWallpaperActivity.class);
                        intent.putExtra(Extras.EXTRA_URL, mWallpapers.get(position).getURL());

                        ActivityTransitionLauncher.with((AppCompatActivity) mContext)
                                .from(image, Extras.EXTRA_IMAGE)
                                .image(thumbnailBitmap)
                                .launch(intent);
                    } catch (Exception e) {
                        sIsClickable = true;
                    }
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            int position = getBindingAdapterPosition();
            if (id == R.id.card) {
                if (position < 0 || position > mWallpapers.size()) {
                    return false;
                }

                Popup popup = Popup.Builder(mContext)
                        .to(name != null ? name : view)
                        .list(PopupItem.getApplyItems(mContext))
                        .callback((applyPopup, i) -> {
                            PopupItem item = applyPopup.getItems().get(i);
                            if (item.getType() == PopupItem.Type.WALLPAPER_CROP) {
                                Preferences.get(mContext).setCropWallpaper(!item.getCheckboxValue());
                                item.setCheckboxValue(Preferences.get(mContext).isCropWallpaper());

                                applyPopup.updateItem(i, item);
                                return;
                            } else if (item.getType() == PopupItem.Type.DOWNLOAD) {
                                WallpaperDownloader.prepare(mContext)
                                        .wallpaper(mWallpapers.get(position))
                                        .start();
                            } else {
                                WallpaperApplyTask task = new WallpaperApplyTask(mContext, mWallpapers.get(position));

                                if (item.getType() == PopupItem.Type.LOCKSCREEN) {
                                    task.to(WallpaperApplyTask.Apply.LOCKSCREEN);
                                } else if (item.getType() == PopupItem.Type.HOMESCREEN) {
                                    task.to(WallpaperApplyTask.Apply.HOMESCREEN);
                                } else if (item.getType() == PopupItem.Type.HOMESCREEN_LOCKSCREEN) {
                                    task.to(WallpaperApplyTask.Apply.HOMESCREEN_LOCKSCREEN);
                                }

                                task.executeOnThreadPool();
                            }
                            applyPopup.dismiss();
                        })
                        .build();

                popup.show();
                return true;
            }
            return false;
        }
    }
}
