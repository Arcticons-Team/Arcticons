package candybar.lib.adapters.dialog;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.util.List;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.fragments.dialog.IntentChooserFragment;
import candybar.lib.helpers.DrawableHelper;
import candybar.lib.items.IntentChooser;
import candybar.lib.items.Request;
import candybar.lib.tasks.IconRequestBuilderTask;
import candybar.lib.tasks.PremiumRequestBuilderTask;
import candybar.lib.utils.AsyncTaskBase;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

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

public class IntentAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<IntentChooser> mApps;
    private final int mType;
    private AsyncTaskBase mAsyncTask;

    public IntentAdapter(@NonNull Context context, @NonNull List<IntentChooser> apps, int type) {
        mContext = context;
        mApps = apps;
        mType = type;
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public IntentChooser getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = View.inflate(mContext, R.layout.fragment_intent_chooser_item_list, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.icon.setImageDrawable(DrawableHelper.getAppIcon(mContext, mApps.get(position).getApp()));
        holder.name.setText(mApps.get(position).getApp().loadLabel(mContext.getPackageManager()).toString());

        if (mApps.get(position).getType() == IntentChooser.TYPE_SUPPORTED) {
            holder.type.setTextColor(ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary));
            holder.type.setText(mContext.getResources().getString(R.string.intent_email_supported));
        } else if (mApps.get(position).getType() == IntentChooser.TYPE_RECOMMENDED) {
            holder.type.setTextColor(ColorHelper.getAttributeColor(mContext, R.attr.colorSecondary));
            holder.type.setText(mContext.getResources().getString(R.string.intent_email_recommended));
        } else {
            holder.type.setTextColor(Color.parseColor("#F44336"));
            holder.type.setText(mContext.getResources().getString(R.string.intent_email_not_supported));
        }

        holder.container.setOnClickListener(v -> {
            ActivityInfo app = mApps.get(position).getApp().activityInfo;
            if (mApps.get(position).getType() == IntentChooser.TYPE_RECOMMENDED ||
                    mApps.get(position).getType() == IntentChooser.TYPE_SUPPORTED) {
                if (mAsyncTask != null) return;

                holder.icon.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);

                if (CandyBarApplication.sRequestProperty == null) {
                    CandyBarApplication.sRequestProperty = new Request.Property(null, null, null);
                }
                CandyBarApplication.sRequestProperty.setComponentName(
                        new ComponentName(app.applicationInfo.packageName, app.name));

                if (mType == IntentChooserFragment.ICON_REQUEST) {
                    mAsyncTask = new IconRequestBuilderTask(mContext, () -> {
                        mAsyncTask = null;
                        FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                        if (fm != null) {
                            DialogFragment dialog = (DialogFragment) fm.findFragmentByTag(
                                    IntentChooserFragment.TAG);
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }).executeOnThreadPool();
                } else if (mType == IntentChooserFragment.REBUILD_ICON_REQUEST) {
                    mAsyncTask = new PremiumRequestBuilderTask(mContext, () -> {
                        mAsyncTask = null;
                        FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                        if (fm != null) {
                            DialogFragment dialog = (DialogFragment) fm.findFragmentByTag(
                                    IntentChooserFragment.TAG);
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }).executeOnThreadPool();
                } else {
                    LogUtil.e("Intent chooser type unknown: " + mType);
                }
                return;
            }

            Toast.makeText(mContext, R.string.intent_email_not_supported_message,
                    Toast.LENGTH_LONG).show();
        });

        return view;
    }

    public boolean isAsyncTaskRunning() {
        return mAsyncTask != null;
    }

    private static class ViewHolder {

        private final TextView name;
        private final TextView type;
        private final ImageView icon;
        private final LinearLayout container;
        private final MaterialProgressBar progressBar;

        ViewHolder(View view) {
            name = view.findViewById(R.id.name);
            type = view.findViewById(R.id.type);
            icon = view.findViewById(R.id.icon);
            container = view.findViewById(R.id.container);
            progressBar = view.findViewById(R.id.progress);
        }
    }
}
