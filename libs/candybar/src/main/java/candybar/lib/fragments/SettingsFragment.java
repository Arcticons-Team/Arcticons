package candybar.lib.fragments;

import static candybar.lib.helpers.DrawableHelper.getReqIcon;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.SettingsAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.fragments.dialog.IntentChooserFragment;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.Language;
import candybar.lib.items.Request;
import candybar.lib.items.Setting;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.listeners.RequestListener;

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

public class SettingsFragment extends Fragment {

    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
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

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        initSettings();
    }

    public void restorePurchases(List<String> productsId, String[] premiumRequestProductsId,
                                 int[] premiumRequestProductsCount) {
        int index = -1;
        for (String productId : productsId) {
            for (int i = 0; i < premiumRequestProductsId.length; i++) {
                if (premiumRequestProductsId[i].equals(productId)) {
                    index = i;
                    break;
                }
            }
            if (index > -1 && index < premiumRequestProductsCount.length) {
                final Preferences preferences = Preferences.get(requireActivity());
                if (!preferences.isPremiumRequest()) {
                    preferences.setPremiumRequestProductId(productId);
                    preferences.setPremiumRequestCount(premiumRequestProductsCount[index]);
                    preferences.setPremiumRequestTotal(premiumRequestProductsCount[index]);
                    preferences.setPremiumRequest(true);
                }
            }
        }
        int message = index > -1 ?
                R.string.pref_premium_request_restore_success :
                R.string.pref_premium_request_restore_empty;
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void initSettings() {
        List<Setting> settings = new ArrayList<>();

        double cache = (double) FileHelper.getDirectorySize(requireActivity().getCacheDir()) / FileHelper.MB;
        NumberFormat formatter = new DecimalFormat("#0.00");
        final Resources resources = requireActivity().getResources();

        settings.add(new Setting(R.drawable.ic_toolbar_storage,
                resources.getString(R.string.pref_data_header),
                "", "", "", Setting.Type.HEADER));

        settings.add(new Setting(-1, "",
                resources.getString(R.string.pref_data_cache),
                resources.getString(R.string.pref_data_cache_desc),
                resources.getString(R.string.pref_data_cache_size,
                        formatter.format(cache) + " MB"),
                Setting.Type.CACHE));

        if (resources.getBoolean(R.bool.enable_icon_request) ||
                Preferences.get(requireActivity()).isPremiumRequestEnabled() &&
                        !resources.getBoolean(R.bool.enable_icon_request_limit)) {
            settings.add(new Setting(-1, "",
                    resources.getString(R.string.pref_data_request),
                    resources.getString(R.string.pref_data_request_desc),
                    "", Setting.Type.ICON_REQUEST));
        }

        if (Preferences.get(requireActivity()).isPremiumRequestEnabled()) {
            settings.add(new Setting(R.drawable.ic_toolbar_premium_request,
                    resources.getString(R.string.pref_premium_request_header),
                    "", "", "", Setting.Type.HEADER));

            settings.add(new Setting(-1, "",
                    resources.getString(R.string.pref_premium_request_restore),
                    resources.getString(R.string.pref_premium_request_restore_desc),
                    "", Setting.Type.RESTORE));

            settings.add(new Setting(-1, "",
                    resources.getString(R.string.pref_premium_request_rebuild),
                    resources.getString(R.string.pref_premium_request_rebuild_desc),
                    "", Setting.Type.PREMIUM_REQUEST));
        }

        if (CandyBarApplication.getConfiguration().isDashboardThemingEnabled()) {
            settings.add(new Setting(R.drawable.ic_toolbar_theme,
                    resources.getString(R.string.pref_theme_header),
                    "", "", "", Setting.Type.HEADER));

            settings.add(new Setting(-1, "",
                    Preferences.get(requireActivity()).getTheme().displayName(requireActivity()),
                    "", "", Setting.Type.THEME));
        }

        settings.add(new Setting(R.drawable.ic_toolbar_language,
                resources.getString(R.string.pref_language_header),
                "", "", "", Setting.Type.HEADER));

        Language language = LocaleHelper.getCurrentLanguage(requireActivity());
        settings.add(new Setting(-1, "",
                language.getName(),
                "", "", Setting.Type.LANGUAGE));

        settings.add(new Setting(R.drawable.ic_toolbar_others,
                resources.getString(R.string.pref_others_header),
                "", "", "", Setting.Type.HEADER));

        settings.add(new Setting(-1, "",
                resources.getString(R.string.pref_others_changelog),
                "", "", Setting.Type.CHANGELOG));

        if (resources.getBoolean(R.bool.enable_apply)) {
            settings.add(new Setting(-1, "",
                    resources.getString(R.string.pref_others_report_bugs),
                    "", "", Setting.Type.REPORT_BUGS));
        }

        if (resources.getBoolean(R.bool.show_intro)) {
            settings.add(new Setting(-1, "",
                    resources.getString(R.string.pref_others_reset_tutorial),
                    "", "", Setting.Type.RESET_TUTORIAL));
        }

        mRecyclerView.setAdapter(new SettingsAdapter(requireActivity(), settings));
    }

    public void rebuildPremiumRequest() {
        new PremiumRequestRebuilder().execute();
    }

    private class PremiumRequestRebuilder extends AsyncTaskBase {

        private MaterialDialog dialog;
        private boolean isArctic;
        private String arcticApiKey;
        private List<Request> requests;
        private String errorMessage = "";

        @Override
        protected void preRun() {
            isArctic = RequestHelper.isPremiumArcticEnabled(requireActivity());
            arcticApiKey = RequestHelper.getPremiumArcticApiKey(requireActivity());

            dialog = new MaterialDialog.Builder(requireActivity())
                    .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                    .content(R.string.premium_request_rebuilding)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .build();

            dialog.show();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    File directory = requireActivity().getCacheDir();
                    requests = Database.get(requireActivity()).getPremiumRequest(null);
                    if (requests.size() == 0) return true;

                    List<String> files = new ArrayList<>();

                    for (Request request : requests) {
                        Drawable drawable = getReqIcon(requireActivity(), request.getActivity());
                        String icon = IconsHelper.saveIcon(files, directory, drawable,
                                isArctic ? request.getPackageName() : RequestHelper.fixNameForRequest(request.getName()));
                        if (icon != null) files.add(icon);
                    }

                    if (isArctic) {
                        errorMessage = RequestHelper.sendArcticRequest(requests, files, directory, arcticApiKey);
                        return errorMessage == null;
                    } else {
                        File appFilter = RequestHelper.buildXml(requireActivity(), requests, RequestHelper.XmlType.APPFILTER);
                        File appMap = RequestHelper.buildXml(requireActivity(), requests, RequestHelper.XmlType.APPMAP);
                        File themeResources = RequestHelper.buildXml(requireActivity(), requests, RequestHelper.XmlType.THEME_RESOURCES);

                        if (appFilter != null) {
                            files.add(appFilter.toString());
                        }

                        if (appMap != null) {
                            files.add(appMap.toString());
                        }

                        if (themeResources != null) {
                            files.add(themeResources.toString());
                        }

                        CandyBarApplication.sZipPath = FileHelper.createZip(files, new File(directory.toString(),
                                RequestHelper.getGeneratedZipName(RequestHelper.REBUILD_ZIP)));
                    }
                    return true;
                } catch (Exception e) {
                    errorMessage = e.toString();
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

            dialog.dismiss();
            dialog = null;

            if (ok) {
                if (requests.size() == 0) {
                    Toast.makeText(getActivity(), R.string.premium_request_rebuilding_empty,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (isArctic) {
                    Toast.makeText(getActivity(), R.string.request_arctic_success, Toast.LENGTH_LONG).show();
                    ((RequestListener) getActivity()).onRequestBuilt(null, IntentChooserFragment.REBUILD_ICON_REQUEST);
                } else {
                    IntentChooserFragment.showIntentChooserDialog(
                            getActivity().getSupportFragmentManager(), IntentChooserFragment.REBUILD_ICON_REQUEST);
                }
            } else {
                Toast.makeText(getActivity(), "Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
