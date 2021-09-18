package candybar.lib.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.adapters.IconsAdapter;
import candybar.lib.databases.Database;
import candybar.lib.items.Icon;

import static candybar.lib.helpers.ViewHelper.setFastScrollColor;

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

public class IconsFragment extends Fragment {

    private View mNoBookmarksFoundView;
    private RecyclerView mRecyclerView;
    private RecyclerFastScroller mFastScroll;
    private IconsAdapter mAdapter;

    private List<Icon> mIcons;
    private boolean isBookmarksFragment;
    private boolean prevIsEmpty = false;

    private static final String INDEX = "index";

    private static final List<WeakReference<IconsAdapter>> iconsAdapters = new ArrayList<>();
    private static WeakReference<IconsFragment> bookmarksIconFragment = new WeakReference<>(null);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons, container, false);
        mNoBookmarksFoundView = view.findViewById(R.id.no_bookmarks_found_container);
        mRecyclerView = view.findViewById(R.id.icons_grid);
        mFastScroll = view.findViewById(R.id.fastscroll);
        return view;
    }

    public static IconsFragment newInstance(int index) {
        IconsFragment fragment = new IconsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIcons = new ArrayList<>();
        int index = requireArguments().getInt(INDEX);
        if (index == -1) {
            mIcons = Database.get(requireActivity()).getBookmarkedIcons(requireActivity());
            bookmarksIconFragment = new WeakReference<>(this);
            isBookmarksFragment = true;
            prevIsEmpty = mIcons.size() == 0;
        } else if (CandyBarMainActivity.sSections != null) {
            mIcons = CandyBarMainActivity.sSections.get(index).getIcons();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewVisibility();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                requireActivity().getResources().getInteger(R.integer.icons_column_count)));

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        ((ImageView) mNoBookmarksFoundView.findViewById(R.id.bookmark_image))
                .setImageDrawable(DrawableHelper.getTintedDrawable(requireActivity(), R.drawable.ic_bookmark,
                        ColorHelper.getAttributeColor(requireActivity(), android.R.attr.textColorSecondary)));

        mAdapter = new IconsAdapter(requireActivity(), mIcons, this, isBookmarksFragment);
        mRecyclerView.setAdapter(mAdapter);
        iconsAdapters.add(new WeakReference<>(mAdapter));
    }

    private void setupViewVisibility() {
        if (isBookmarksFragment && mIcons.size() == 0) {
            mNoBookmarksFoundView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mNoBookmarksFoundView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView,
                requireActivity().getResources().getInteger(R.integer.icons_column_count));
    }

    public void refreshBookmarks() {
        if (isBookmarksFragment) {
            mIcons = Database.get(requireActivity()).getBookmarkedIcons(requireActivity());
            mAdapter.setIcons(mIcons);
            setupViewVisibility();
        }
    }

    public static void reloadIcons() {
        for (WeakReference<IconsAdapter> adapterRef : iconsAdapters) {
            if (adapterRef.get() != null) adapterRef.get().reloadIcons();
        }
    }

    public static void reloadBookmarks() {
        if (bookmarksIconFragment.get() != null) {
            bookmarksIconFragment.get().refreshBookmarks();
        }
    }
}
