package candybar.lib.adapters.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.fragments.dialog.ThemeChooserFragment;
import candybar.lib.items.Theme;

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

public class ThemeAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<Theme> mThemes;
    private final int mSelectedIndex;
    private final List<ViewHolder> mHolders;

    public ThemeAdapter(@NonNull Context context, @NonNull List<Theme> themes, int selectedIndex) {
        mContext = context;
        mThemes = themes;
        mSelectedIndex = selectedIndex;
        mHolders = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mThemes.size();
    }

    @Override
    public Theme getItem(int position) {
        return mThemes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ThemeAdapter.ViewHolder holder;

        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_inapp_dialog_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
            mHolders.add(holder);
        } else {
            holder = (ThemeAdapter.ViewHolder) view.getTag();
        }

        holder.radio.setChecked(mSelectedIndex == position);
        holder.name.setText(mThemes.get(position).displayName(mContext));

        holder.container.setOnClickListener(v -> {
            for (ViewHolder aHolder : mHolders) {
                if (aHolder != holder) aHolder.radio.setChecked(false);
            }
            holder.radio.setChecked(true);

            FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
            if (fm == null) return;

            Fragment fragment = fm.findFragmentByTag(ThemeChooserFragment.TAG);
            if (fragment == null) return;

            if (fragment instanceof ThemeChooserFragment) {
                ((ThemeChooserFragment) fragment).setTheme(mThemes.get(position));
            }
        });

        return view;
    }

    private static class ViewHolder {
        private final RadioButton radio;
        private final TextView name;
        private final LinearLayout container;

        ViewHolder(View view) {
            radio = view.findViewById(R.id.radio);
            name = view.findViewById(R.id.name);
            container = view.findViewById(R.id.container);
        }
    }
}
