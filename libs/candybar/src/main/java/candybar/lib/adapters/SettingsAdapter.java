package candybar.lib.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.databases.Database;
import candybar.lib.fragments.SettingsFragment;
import candybar.lib.fragments.dialog.ChangelogFragment;
import candybar.lib.fragments.dialog.LanguagesFragment;
import candybar.lib.fragments.dialog.ThemeChooserFragment;
import candybar.lib.helpers.ReportBugsHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.Setting;
import candybar.lib.preferences.Preferences;
import candybar.lib.tasks.IconRequestTask;
import candybar.lib.utils.listeners.InAppBillingListener;

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

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Setting> mSettings;

    private static final int TYPE_CONTENT = 0;
    private static final int TYPE_FOOTER = 1;

    public SettingsAdapter(@NonNull Context context, @NonNull List<Setting> settings) {
        mContext = context;
        mSettings = settings;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CONTENT) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_settings_item_list, parent, false);
            return new ContentViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_settings_item_footer, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_CONTENT) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            Setting setting = mSettings.get(position);

            if (setting.getTitle().length() == 0) {
                contentViewHolder.title.setVisibility(View.GONE);
                contentViewHolder.divider.setVisibility(View.GONE);
                contentViewHolder.container.setVisibility(View.VISIBLE);

                contentViewHolder.subtitle.setText(setting.getSubtitle());

                if (setting.getContent().length() == 0) {
                    contentViewHolder.content.setVisibility(View.GONE);
                } else {
                    contentViewHolder.content.setText(setting.getContent());
                    contentViewHolder.content.setVisibility(View.VISIBLE);
                }

                if (setting.getFooter().length() == 0) {
                    contentViewHolder.footer.setVisibility(View.GONE);
                } else {
                    contentViewHolder.footer.setText(setting.getFooter());
                    contentViewHolder.footer.setVisibility(View.VISIBLE);
                }
            } else {
                contentViewHolder.container.setVisibility(View.GONE);
                contentViewHolder.title.setVisibility(View.VISIBLE);
                contentViewHolder.title.setText(setting.getTitle());

                if (position > 0) {
                    contentViewHolder.divider.setVisibility(View.VISIBLE);
                } else {
                    contentViewHolder.divider.setVisibility(View.GONE);
                }

                if (setting.getIcon() != -1) {
                    int color = ColorHelper.getAttributeColor(mContext, android.R.attr.textColorPrimary);
                    contentViewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                            DrawableHelper.getTintedDrawable(mContext, setting.getIcon(), color), null, null, null);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSettings.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) return TYPE_FOOTER;
        return TYPE_CONTENT;
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final TextView subtitle;
        private final TextView content;
        private final TextView footer;
        private final LinearLayout container;
        private final View divider;

        ContentViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            content = itemView.findViewById(R.id.content);
            footer = itemView.findViewById(R.id.footer);
            divider = itemView.findViewById(R.id.divider);
            container = itemView.findViewById(R.id.container);

            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.container) {
                int position = getBindingAdapterPosition();

                if (position < 0 || position > mSettings.size()) return;

                Setting setting = mSettings.get(position);
                switch (setting.getType()) {
                    case CACHE:
                        new MaterialDialog.Builder(mContext)
                                .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                                .content(R.string.pref_data_cache_clear_dialog)
                                .positiveText(R.string.clear)
                                .negativeText(android.R.string.cancel)
                                .onPositive((dialog, which) -> {
                                    try {
                                        File cache = mContext.getCacheDir();
                                        FileHelper.clearDirectory(cache);

                                        double size = (double) FileHelper.getDirectorySize(cache) / FileHelper.MB;
                                        NumberFormat formatter = new DecimalFormat("#0.00");

                                        setting.setFooter(mContext.getResources().getString(
                                                R.string.pref_data_cache_size, formatter.format(size) + " MB"));
                                        notifyItemChanged(position);

                                        Toast.makeText(mContext, R.string.pref_data_cache_cleared,
                                                Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        LogUtil.e(Log.getStackTraceString(e));
                                    }
                                })
                                .show();
                        break;
                    case ICON_REQUEST:
                        new MaterialDialog.Builder(mContext)
                                .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                                .content(R.string.pref_data_request_clear_dialog)
                                .positiveText(R.string.clear)
                                .negativeText(android.R.string.cancel)
                                .onPositive((dialog, which) -> {
                                    Database.get(mContext).deleteIconRequestData();

                                    CandyBarMainActivity.sMissedApps = null;
                                    new IconRequestTask(mContext).executeOnThreadPool();

                                    Toast.makeText(mContext, R.string.pref_data_request_cleared,
                                            Toast.LENGTH_LONG).show();
                                })
                                .show();
                        break;
                    case RESTORE:
                        try {
                            InAppBillingListener listener = (InAppBillingListener) mContext;
                            listener.onRestorePurchases();
                        } catch (Exception ignored) {
                        }
                        break;
                    case PREMIUM_REQUEST:
                        FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                        if (fm == null) return;

                        Fragment fragment = fm.findFragmentByTag("settings");
                        if (fragment == null) return;

                        if (fragment instanceof SettingsFragment) {
                            ((SettingsFragment) fragment).rebuildPremiumRequest();
                        }
                        break;
                    case THEME:
                        ThemeChooserFragment.showThemeChooser(((AppCompatActivity) mContext).getSupportFragmentManager());
                        break;
                    case LANGUAGE:
                        LanguagesFragment.showLanguageChooser(((AppCompatActivity) mContext).getSupportFragmentManager());
                        break;
                    case REPORT_BUGS:
                        ReportBugsHelper.prepareReportBugs(mContext);
                        break;
                    case CHANGELOG:
                        ChangelogFragment.showChangelog(((AppCompatActivity) mContext).getSupportFragmentManager());
                        break;
                    case RESET_TUTORIAL:
                        Preferences.get(mContext).setTimeToShowHomeIntro(true);
                        Preferences.get(mContext).setTimeToShowIconsIntro(true);
                        Preferences.get(mContext).setTimeToShowRequestIntro(true);
                        Preferences.get(mContext).setTimeToShowWallpapersIntro(true);
                        Preferences.get(mContext).setTimeToShowWallpaperPreviewIntro(true);

                        Toast.makeText(mContext, R.string.pref_others_reset_tutorial_reset, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View itemView) {
            super(itemView);
            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                View shadow = itemView.findViewById(R.id.shadow);
                shadow.setVisibility(View.GONE);
            }
        }
    }
}
