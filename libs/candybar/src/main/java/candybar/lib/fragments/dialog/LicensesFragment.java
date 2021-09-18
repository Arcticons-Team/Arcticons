package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
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
import candybar.lib.adapters.dialog.LicensesAdapter;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.License;
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

public class LicensesFragment extends DialogFragment {

    private ListView mListView;
    private AsyncTaskBase mAsyncTask;

    private static final String TAG = "candybar.dialog.licenses";

    private static LicensesFragment newInstance() {
        return new LicensesFragment();
    }

    public static void showLicensesDialog(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = LicensesFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .customView(R.layout.fragment_licenses, false)
                .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                .title(R.string.about_open_source_licenses)
                .negativeText(R.string.close)
                .build();
        dialog.show();

        mListView = (ListView) dialog.findViewById(R.id.licenses_list);

        mAsyncTask = new LicensesLoader().executeOnThreadPool();

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDismiss(dialog);
    }

    private class LicensesLoader extends AsyncTaskBase {

        private List<License> licenses;

        @Override
        protected void preRun() {
            licenses = new ArrayList<>();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    XmlPullParser xpp = requireActivity().getResources().getXml(R.xml.dashboard_licenses);
                    String licenseName = "";
                    String licenseText = "";

                    while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                        switch (xpp.getEventType()) {
                            case XmlPullParser.START_TAG:
                                if (xpp.getName().equals("license")) {
                                    licenseName = xpp.getAttributeValue(null, "name");
                                }
                                break;

                            case XmlPullParser.TEXT:
                                String[] parts = xpp.getText().split("\n");
                                for (int i = 0; i < parts.length; i++) {
                                    licenseText += parts[i].trim() + "\n";
                                }
                                licenseText = licenseText.trim();
                                licenseText = licenseText.replaceAll("(.)\\n(.)", "$1 $2");
                                break;

                            case XmlPullParser.END_TAG:
                                if (xpp.getName().equals("license")) {
                                    licenses.add(new License(licenseName, licenseText));
                                    licenseName = licenseText = "";
                                }
                                break;
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
                mListView.setAdapter(new LicensesAdapter(getActivity(), licenses));
            } else {
                dismiss();
            }
        }
    }
}


