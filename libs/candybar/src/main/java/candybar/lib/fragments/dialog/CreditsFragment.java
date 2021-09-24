package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.dialog.CreditsAdapter;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.Credit;
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

public class CreditsFragment extends DialogFragment {

    private ListView mListView;
    private AsyncTaskBase mAsyncTask;
    private int mType;

    private static final String TAG = "candybar.dialog.credits";
    private static final String TYPE = "type";

    public static final int TYPE_ICON_PACK_CONTRIBUTORS = 0;
    public static final int TYPE_DASHBOARD_CONTRIBUTORS = 1;
    public static final int TYPE_DASHBOARD_TRANSLATOR = 2;

    private static CreditsFragment newInstance(int type) {
        CreditsFragment fragment = new CreditsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showCreditsDialog(FragmentManager fm, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = CreditsFragment.newInstance(type);
            dialog.show(ft, TAG);
        } catch (IllegalStateException | IllegalArgumentException ignored) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .customView(R.layout.fragment_credits, false)
                .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                .title(getTitle(mType))
                .positiveText(R.string.close)
                .build();
        dialog.show();
        mListView = (ListView) dialog.findViewById(R.id.listview);
        mAsyncTask = new CreditsLoader().executeOnThreadPool();

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
        }
    }

    @Override
    public void onDestroyView() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroyView();
    }

    @NonNull
    private String getTitle(int type) {
        if (getActivity() == null) return "";
        switch (type) {
            case TYPE_ICON_PACK_CONTRIBUTORS:
                return getActivity().getResources().getString(R.string.about_contributors_title);
            case TYPE_DASHBOARD_CONTRIBUTORS:
                return getActivity().getResources().getString(R.string.about_dashboard_contributors);
            case TYPE_DASHBOARD_TRANSLATOR:
                return getActivity().getResources().getString(R.string.about_dashboard_translator);
            default:
                return "";
        }
    }

    private int getResource(int type) {
        switch (type) {
            case TYPE_ICON_PACK_CONTRIBUTORS:
                return R.xml.contributors;
            case TYPE_DASHBOARD_CONTRIBUTORS:
                return R.xml.dashboard_contributors;
            case TYPE_DASHBOARD_TRANSLATOR:
                return R.xml.dashboard_translator;
            default:
                return -1;
        }
    }

    private class CreditsLoader extends AsyncTaskBase {

        private List<Credit> credits;

        @Override
        protected void preRun() {
            credits = new ArrayList<>();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    int res = getResource(mType);

                    XmlPullParser xpp = getResources().getXml(res);

                    while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("contributor")) {
                                Credit credit = new Credit(
                                        xpp.getAttributeValue(null, "name"),
                                        xpp.getAttributeValue(null, "contribution"),
                                        xpp.getAttributeValue(null, "image"),
                                        xpp.getAttributeValue(null, "link"));
                                credits.add(credit);
                            }
                        }
                        xpp.next();
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
                mListView.setAdapter(new CreditsAdapter(getActivity(), credits));
            } else {
                dismiss();
            }
        }
    }
}
