package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.dialog.IntentAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.fragments.RequestFragment;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.IntentChooser;
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

public class IntentChooserFragment extends DialogFragment {

    private ListView mIntentList;
    private TextView mNoApp;

    private int mType;
    private IntentAdapter mAdapter;
    private AsyncTaskBase mAsyncTask;

    public static final int ICON_REQUEST = 0;
    public static final int REBUILD_ICON_REQUEST = 1;

    public static final String TAG = "candybar.dialog.intent.chooser";
    private static final String TYPE = "type";

    private static IntentChooserFragment newInstance(int type) {
        IntentChooserFragment fragment = new IntentChooserFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showIntentChooserDialog(@NonNull FragmentManager fm, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = IntentChooserFragment.newInstance(type);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TYPE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .customView(R.layout.fragment_intent_chooser, false)
                .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                .positiveText(android.R.string.cancel)
                .build();

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(view -> {
            if (mAdapter == null || mAdapter.isAsyncTaskRunning()) return;

            if (CandyBarApplication.sZipPath != null) {
                File file = new File(CandyBarApplication.sZipPath);
                if (file.exists()) {
                    if (file.delete()) {
                        LogUtil.e(String.format("Intent chooser cancel: %s deleted", file.getName()));
                    }
                }
            }

            RequestFragment.sSelectedRequests = null;
            CandyBarApplication.sRequestProperty = null;
            CandyBarApplication.sZipPath = null;
            dialog.dismiss();
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        setCancelable(false);

        mIntentList = (ListView) dialog.findViewById(R.id.intent_list);
        mNoApp = (TextView) dialog.findViewById(R.id.intent_noapp);
        mAsyncTask = new IntentChooserLoader().execute();

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDismiss(dialog);
    }

    private class IntentChooserLoader extends AsyncTaskBase {

        private List<IntentChooser> apps;

        @Override
        protected void preRun() {
            apps = new ArrayList<>();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    boolean nonMailingAppSend = getResources().getBoolean(R.bool.enable_non_mail_app_request);
                    Intent intent;

                    if (!nonMailingAppSend) {
                        intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                    } else {
                        intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("application/zip");
                    }

                    List<ResolveInfo> resolveInfos = requireActivity().getPackageManager()
                            .queryIntentActivities(intent, 0);
                    try {
                        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(
                                requireActivity().getPackageManager()));
                    } catch (Exception ignored) {
                    }

                    for (ResolveInfo resolveInfo : resolveInfos) {
                        switch (resolveInfo.activityInfo.packageName) {
                            case "com.google.android.gm":
                                apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_RECOMMENDED));
                                break;
                            case "com.google.android.apps.inbox":
                                try {
                                    ComponentName componentName = new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                                            "com.google.android.apps.bigtop.activities.MainActivity");
                                    Intent inbox = new Intent(Intent.ACTION_SEND);
                                    inbox.setComponent(componentName);

                                    List<ResolveInfo> list = requireActivity().getPackageManager().queryIntentActivities(
                                            inbox, PackageManager.MATCH_DEFAULT_ONLY);
                                    if (list.size() > 0) {
                                        apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_SUPPORTED));
                                        break;
                                    }
                                } catch (ActivityNotFoundException e) {
                                    LogUtil.e(Log.getStackTraceString(e));
                                }

                                apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_NOT_SUPPORTED));
                                break;
                            case "com.android.fallback":
                            case "com.paypal.android.p2pmobile":
                            case "com.lonelycatgames.Xplore":
                                break;
                            default:
                                apps.add(new IntentChooser(resolveInfo, IntentChooser.TYPE_SUPPORTED));
                                break;
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
            if (ok && apps != null) {
                mAdapter = new IntentAdapter(getActivity(), apps, mType);
                mIntentList.setAdapter(mAdapter);

                if (apps.size() == 0) {
                    mNoApp.setVisibility(View.VISIBLE);
                    setCancelable(true);
                }
            } else {
                dismiss();
                Toast.makeText(getActivity(), R.string.intent_email_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
