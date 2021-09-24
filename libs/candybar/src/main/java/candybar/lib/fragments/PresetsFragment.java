package candybar.lib.fragments;

import static candybar.lib.helpers.ViewHelper.setFastScrollColor;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import candybar.lib.R;
import candybar.lib.adapters.PresetsAdapter;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.Preset;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.AsyncTaskBase;

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

public class PresetsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgress;
    private RecyclerFastScroller mFastScroll;

    private AsyncTaskBase mAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_presets, container, false);
        mRecyclerView = view.findViewById(R.id.presets_grid);
        mProgress = view.findViewById(R.id.progress);
        mFastScroll = view.findViewById(R.id.fastscroll);

        if (!Preferences.get(requireActivity()).isToolbarShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorSecondary),
                PorterDuff.Mode.SRC_IN);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                requireActivity().getResources().getInteger(R.integer.presets_column_count)));

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        CountDownLatch permissionDone = new CountDownLatch(1);

        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    CafeBar.builder(requireActivity())
                            .theme(CafeBarTheme.Custom(ContextCompat.getColor(requireActivity(), R.color.cardBackground)))
                            .floating(true)
                            .fitSystemWindow()
                            .duration(CafeBar.Duration.MEDIUM)
                            .typeface(TypefaceHelper.getRegular(requireActivity()), TypefaceHelper.getBold(requireActivity()))
                            .content(R.string.presets_storage_permission)
                            .show();
                }

                mAsyncTask = new PresetsLoader().execute();
            }).launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            mAsyncTask = new PresetsLoader().execute();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetSpanSizeLookUp();
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) mAsyncTask.cancel(true);
        Activity activity = getActivity();
        if (activity != null) Glide.get(activity).clearMemory();
        setHasOptionsMenu(false);
        super.onDestroy();
    }

    private void resetSpanSizeLookUp() {
        int column = requireActivity().getResources().getInteger(R.integer.presets_column_count);
        PresetsAdapter adapter = (PresetsAdapter) mRecyclerView.getAdapter();
        GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();

        try {
            manager.setSpanCount(column);

            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter.isHeader(position)) return column;
                    return 1;
                }
            });
        } catch (Exception ignored) {
        }
    }

    private class PresetsLoader extends AsyncTaskBase {

        private final List<Preset> presets = new ArrayList<>();

        private List<Preset> loadPresets(String sectionName, String directory) throws IOException {
            List<Preset> presets = new ArrayList<>();
            presets.add(new Preset("", sectionName));
            for (String item : requireActivity().getAssets().list(directory)) {
                presets.add(new Preset(directory + "/" + item, null));
            }
            if (presets.size() == 1) presets.clear();
            return presets;
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    presets.addAll(loadPresets("Komponents", "komponents"));
                    presets.addAll(loadPresets("Lockscreens", "lockscreens"));
                    presets.addAll(loadPresets("Wallpapers", "wallpapers"));
                    presets.addAll(loadPresets("Widgets", "widgets"));

                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void postRun(boolean ok) {
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            mProgress.setVisibility(View.GONE);

            if (ok) {
                mRecyclerView.setAdapter(new PresetsAdapter(requireActivity(), presets));
            } else {
                Toast.makeText(getActivity(), R.string.presets_load_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
