package candybar.lib.fragments;

import static candybar.lib.helpers.ViewHelper.setFastScrollColor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.FAQsAdapter;
import candybar.lib.items.FAQs;
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

public class FAQsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private TextView mSearchResult;
    private RecyclerFastScroller mFastScroll;

    private FAQsAdapter mAdapter;
    private AsyncTaskBase mAsyncTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faqs, container, false);
        mRecyclerView = view.findViewById(R.id.faqs_list);
        mSearchResult = view.findViewById(R.id.search_result);
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

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        mAsyncTask = new FAQsLoader().executeOnThreadPool();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);

        View searchView = search.getActionView();
        EditText searchInput = searchView.findViewById(R.id.search_input);
        View clearQueryButton = searchView.findViewById(R.id.clear_query_button);

        searchInput.setHint(requireActivity().getResources().getString(R.string.search_faqs));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                filterSearch(query);
                clearQueryButton.setVisibility(query.contentEquals("") ? View.GONE : View.VISIBLE);
            }
        });

        clearQueryButton.setOnClickListener(view -> searchInput.setText(""));

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                searchInput.requestFocus();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (getActivity() != null) {
                        SoftKeyboardHelper.openKeyboard(getActivity());
                    }
                }, 1000);

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                searchInput.setText("");
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        setHasOptionsMenu(false);
        super.onDestroy();
    }

    @SuppressLint("StringFormatInvalid")
    private void filterSearch(String query) {
        try {
            mAdapter.search(query);
            if (mAdapter.getFaqsCount() == 0) {
                String text = requireActivity().getResources().getString(R.string.search_noresult, query);
                mSearchResult.setText(text);
                mSearchResult.setVisibility(View.VISIBLE);
            } else mSearchResult.setVisibility(View.GONE);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    private class FAQsLoader extends AsyncTaskBase {

        private List<FAQs> faqs;
        private String[] questions;
        private String[] answers;

        @Override
        protected void preRun() {
            faqs = new ArrayList<>();
            questions = getResources().getStringArray(R.array.questions);
            answers = getResources().getStringArray(R.array.answers);
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    for (int i = 0; i < questions.length; i++) {
                        if (i < answers.length) {
                            FAQs faq = new FAQs(questions[i], answers[i]);
                            faqs.add(faq);
                        }
                    }
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
            if (ok) {
                setHasOptionsMenu(true);
                mAdapter = new FAQsAdapter(getActivity(), faqs);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }
}
