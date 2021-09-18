package candybar.lib.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import candybar.lib.R;
import candybar.lib.databases.Database;
import candybar.lib.fragments.IconsFragment;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.helpers.IntentHelper;
import candybar.lib.items.Icon;

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

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.ViewHolder> {

    private final Context mContext;
    private List<Icon> mIcons;
    private List<Icon> mIconsAll;
    private final Fragment mFragment;
    private final List<ViewHolder> mViewHolders;

    private List<Icon> mSelectedIcons = new ArrayList<>();

    private final boolean mIsShowIconName;
    private final boolean mIsBookmarkMode;

    private ActionMode actionMode;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            mode.getMenuInflater().inflate(R.menu.menu_bookmark_icons, menu);
            Activity activity = (Activity) mContext;
            TabLayout tabLayout = activity.findViewById(R.id.tab);
            View shadow = activity.findViewById(R.id.shadow);
            if (shadow != null) {
                shadow.animate().translationY(-tabLayout.getHeight()).setDuration(200).start();
            }
            tabLayout.animate().translationY(-tabLayout.getHeight()).setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            tabLayout.setVisibility(View.GONE);
                            if (shadow != null) {
                                shadow.setTranslationY(0);
                            }
                            tabLayout.animate().setListener(null);
                        }
                    }).start();
            ((ViewPager2) activity.findViewById(R.id.pager)).setUserInputEnabled(false);
            ((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                    .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            for (ViewHolder holder : mViewHolders) {
                holder.onActionModeChange();
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(mContext.getResources().getString(R.string.items_selected, mSelectedIcons.size()));
            menu.findItem(R.id.menu_select_all).setIcon(mSelectedIcons.size() == mIcons.size()
                    ? R.drawable.ic_toolbar_select_all_selected : R.drawable.ic_toolbar_select_all);
            menu.findItem(R.id.menu_delete).setVisible(mSelectedIcons.size() > 0);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_delete) {
                List<String> drawableNames = new ArrayList<>();
                for (Icon icon : mSelectedIcons) drawableNames.add(icon.getDrawableName());
                Database.get(mContext).deleteBookmarkedIcons(drawableNames);
                IconsFragment.reloadBookmarks();
                mode.finish();
                return true;
            } else if (itemId == R.id.menu_select_all) {
                if (mSelectedIcons.size() != mIcons.size()) {
                    for (ViewHolder holder : mViewHolders) {
                        holder.setChecked(true, true);
                    }
                    mSelectedIcons = new ArrayList<>(mIcons);
                } else {
                    for (ViewHolder holder : mViewHolders) {
                        holder.setChecked(false, true);
                    }
                    mSelectedIcons = new ArrayList<>();
                }
                actionMode.invalidate();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            mSelectedIcons = new ArrayList<>();
            Activity activity = (Activity) mContext;
            TabLayout tabLayout = activity.findViewById(R.id.tab);
            View shadow = activity.findViewById(R.id.shadow);
            if (shadow != null) {
                shadow.setTranslationY(-tabLayout.getHeight());
                shadow.animate().translationY(0).setDuration(200).start();
            }
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.animate().translationY(0).setDuration(200).start();
            ((ViewPager2) activity.findViewById(R.id.pager)).setUserInputEnabled(true);
            ((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                    .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            for (ViewHolder holder : mViewHolders) {
                holder.onActionModeChange();
            }
        }
    };

    public IconsAdapter(@NonNull Context context, @NonNull List<Icon> icons, Fragment fragment, boolean isBookmarkMode) {
        mContext = context;
        mFragment = fragment;
        mIcons = icons;
        mIsShowIconName = mContext.getResources().getBoolean(R.bool.show_icon_name);
        mViewHolders = new ArrayList<>();
        mIsBookmarkMode = isBookmarkMode;
    }

    public void setIcons(@NonNull List<Icon> icons) {
        mIcons = icons;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_icons_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Icon icon = mIcons.get(position);
        holder.name.setText(icon.getTitle());
        mViewHolders.add(holder);
        loadIconInto(holder.icon, position);
        if (mIsBookmarkMode) {
            holder.setCheckChangedListener(null);
            holder.setChecked(mSelectedIcons.contains(icon), false);
            holder.setCheckChangedListener(isChecked -> {
                if (isChecked) {
                    mSelectedIcons.add(icon);
                } else {
                    mSelectedIcons.remove(icon);
                }
                if (actionMode != null) actionMode.invalidate();
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        mViewHolders.remove(holder);
        super.onViewRecycled(holder);
    }

    private void loadIconInto(ImageView imageView, int position) {
        Glide.with(mFragment)
                .load("drawable://" + mIcons.get(position).getRes())
                .skipMemoryCache(true)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public void reloadIcons() {
        Glide.get(mContext).clearMemory();
        for (ViewHolder holder : mViewHolders) {
            int position = holder.getBindingAdapterPosition();
            if (position < 0 || position > getItemCount()) continue;
            loadIconInto(holder.icon, holder.getBindingAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        return mIcons.size();
    }

    interface CheckChangedListener {
        void onCheckChanged(boolean isChecked);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final ImageView icon;
        private final TextView name;

        private final View container;
        private final View innerContainer;
        private final View checkBackground;
        private boolean isChecked;
        private CheckChangedListener checkChangedListener;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            innerContainer = itemView.findViewById(R.id.inner_container);
            checkBackground = itemView.findViewById(R.id.check_background);

            container = itemView.findViewById(R.id.container);
            container.setOnClickListener(this);

            if (mIsBookmarkMode) {
                container.setOnLongClickListener(this);
                int color = ColorHelper.getAttributeColor(mContext, R.attr.colorSecondary);
                ((ImageView) checkBackground.findViewById(R.id.checkmark))
                        .setImageDrawable(DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_check_circle, color));
            }

            if (!mIsShowIconName) {
                name.setVisibility(View.GONE);
            }

            onActionModeChange();
        }

        private void onActionModeChange() {
            TypedValue outValue = new TypedValue();
            if (actionMode != null) {
                mContext.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
                container.setBackgroundResource(outValue.resourceId);
                innerContainer.setBackgroundResource(0);
            } else {
                mContext.getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true);
                container.setBackgroundResource(0);
                innerContainer.setBackgroundResource(outValue.resourceId);
                setChecked(false, true);
            }
        }

        private void setCheckChangedListener(CheckChangedListener checkChangedListener) {
            this.checkChangedListener = checkChangedListener;
        }

        private void setChecked(boolean isChecked, boolean animate) {
            this.isChecked = isChecked;
            float scale = isChecked ? (float) 0.6 : 1;
            if (animate) {
                checkBackground.animate().alpha(isChecked ? 1 : 0).setDuration(200).start();
                icon.animate().scaleX(scale).scaleY(scale).setDuration(200).start();
            } else {
                checkBackground.setAlpha(isChecked ? 1 : 0);
                icon.setScaleX(scale);
                icon.setScaleY(scale);
            }
            if (checkChangedListener != null) {
                checkChangedListener.onCheckChanged(isChecked);
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getBindingAdapterPosition();
            if (id == R.id.container) {
                if (position < 0 || position > mIcons.size()) return;
                if (actionMode != null) {
                    setChecked(!isChecked, true);
                } else {
                    SoftKeyboardHelper.closeKeyboard(mContext);
                    IconsHelper.selectIcon(mContext, IntentHelper.sAction, mIcons.get(position));
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (actionMode == null) {
                ((Activity) mContext).startActionMode(actionModeCallback);
            }
            setChecked(!isChecked, true);
            return true;
        }
    }

    public void search(String string) {
        // Initialize mIconsAll if not initialized
        // Also remove duplicates
        if (mIconsAll == null) {
            if (!mContext.getResources().getBoolean(R.bool.show_icon_name)) {
                // It means the title of icon is not yet computed, so compute it
                IconsHelper.computeTitles(mContext, mIcons);
            }

            mIconsAll = new ArrayList<>();
            Set<String> addedNames = new HashSet<>();
            Locale defaultLocale = Locale.getDefault();
            for (int i = 0; i < mIcons.size(); i++) {
                Icon icon = mIcons.get(i);
                String name = icon.getTitle();
                name = name.toLowerCase(defaultLocale);
                if (!addedNames.contains(name)) {
                    mIconsAll.add(icon);
                    addedNames.add(name);
                }
            }
        }

        String query = string.toLowerCase(Locale.getDefault()).trim();
        mIcons = new ArrayList<>();
        if (query.length() == 0) mIcons.addAll(mIconsAll);
        else {
            Locale defaultLocale = Locale.getDefault();
            for (int i = 0; i < mIconsAll.size(); i++) {
                Icon icon = mIconsAll.get(i);
                String name = icon.getTitle();
                name = name.toLowerCase(defaultLocale);
                if (name.contains(query)) {
                    mIcons.add(icon);
                }
            }
        }
        notifyDataSetChanged();
    }
}
