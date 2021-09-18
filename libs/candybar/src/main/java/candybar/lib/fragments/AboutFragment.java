package candybar.lib.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.danimahardhika.android.helpers.core.ViewHelper;

import candybar.lib.R;
import candybar.lib.adapters.AboutAdapter;
import candybar.lib.applications.CandyBarApplication;
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

public class AboutFragment extends Fragment {

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);

        if (!Preferences.get(requireActivity()).isToolbarShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resetRecyclerViewPadding(requireActivity().getResources().getConfiguration().orientation);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        int spanCount = requireActivity().getResources().getInteger(R.integer.about_column_count);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                spanCount, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(new AboutAdapter(requireActivity(), spanCount));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        resetRecyclerViewPadding(newConfig.orientation);
        ViewHelper.resetSpanCount(mRecyclerView,
                requireActivity().getResources().getInteger(R.integer.about_column_count));

        StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
        assert manager != null;
        mRecyclerView.setAdapter(new AboutAdapter(requireActivity(), manager.getSpanCount()));
    }

    private void resetRecyclerViewPadding(int orientation) {
        if (mRecyclerView == null) return;

        int padding = 0;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            padding = requireActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);

            if (CandyBarApplication.getConfiguration().getAboutStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT) {
                padding = requireActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            }
        }

        mRecyclerView.setPadding(padding, padding, 0, 0);
    }
}
