package candybar.lib.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Icon;
import candybar.lib.tasks.ReportBugsTask;

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

public class ReportBugsHelper {

    public static final String REPORT_BUGS = "reportbugs.zip";
    private static final String BROKEN_APPFILTER = "broken_appfilter.xml";
    private static final String BROKEN_DRAWABLES = "broken_drawables.xml";
    private static final String ACTIVITY_LIST = "activity_list.xml";
    private static final String CRASHLOG = "crashlog.txt";
    private static String UTF8 = "UTF8";

    public static void prepareReportBugs(@NonNull Context context) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_report_bugs, true)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .positiveText(R.string.report_bugs_send)
                .negativeText(R.string.close)
                .build();

        EditText editText = (EditText) dialog.findViewById(R.id.input_desc);
        TextInputLayout inputLayout = (TextInputLayout) dialog.findViewById(R.id.input_layout);

        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(view -> {
            if (editText.getText().length() > 0) {
                inputLayout.setErrorEnabled(false);
                new ReportBugsTask(context, editText.getText().toString()).executeOnThreadPool();
                dialog.dismiss();
                return;
            }

            inputLayout.setError(context.getResources().getString(R.string.report_bugs_desc_empty));
        });
        dialog.show();
    }

    @Nullable
    public static File buildBrokenAppFilter(@NonNull Context context) {
        try {
            HashMap<String, String> activities = RequestHelper.getAppFilter(context, RequestHelper.Key.ACTIVITY);
            File brokenAppFilter = new File(context.getCacheDir(), BROKEN_APPFILTER);
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(brokenAppFilter), UTF8));

            boolean first = true;
            for (Map.Entry<String, String> entry : activities.entrySet()) {
                if (first) {
                    first = false;
                    writer.append("<!-- BROKEN APPFILTER -->")
                            .append("\r\n").append("<!-- Broken appfilter will check for activities that included in appfilter but doesn't have a drawable")
                            .append("\r\n").append("* ").append("The reason could because misnamed drawable or the drawable not copied to the project -->")
                            .append("\r\n\r\n\r\n");
                }

                int drawable = context.getResources().getIdentifier(
                        entry.getValue(), "drawable", context.getPackageName());
                if (drawable == 0) {
                    writer.append("Activity: ").append(entry.getKey())
                            .append("\r\n")
                            .append("Drawable: ").append(entry.getValue()).append(".png")
                            .append("\r\n\r\n");
                }
            }

            writer.flush();
            writer.close();
            return brokenAppFilter;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return null;
        }
    }

    @Nullable
    public static File buildBrokenDrawables(@NonNull Context context) {
        try {
            HashMap<String, String> drawables = RequestHelper.getAppFilter(context, RequestHelper.Key.DRAWABLE);
            List<Icon> iconList = IconsHelper.getIconsList(context);
            List<Icon> icons = new ArrayList<>();

            File brokenDrawables = new File(context.getCacheDir(), BROKEN_DRAWABLES);
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(brokenDrawables), UTF8));

            for (Icon icon : iconList) {
                if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                    if (!icon.getTitle().equals(CandyBarApplication.getConfiguration().getTabAllIconsTitle())) {
                        icons.addAll(icon.getIcons());
                    }
                } else {
                    icons.addAll(icon.getIcons());
                }
            }

            IconsHelper.computeTitles(context, icons);
            HashSet<String> addedIcons = new HashSet<>();
            boolean first = true;
            for (Icon icon : icons) {
                if (first) {
                    first = false;
                    writer.append("<!-- BROKEN DRAWABLES -->")
                            .append("\r\n").append("<!-- Broken drawables will read drawables that listed in drawable.xml")
                            .append("\r\n").append("* ").append("and try to match them with drawables that used in appfilter.xml")
                            .append("\r\n").append("* ").append("The reason could be drawable copied to the project but not used in appfilter.xml -->")
                            .append("\r\n\r\n\r\n");
                }

                String drawable = drawables.get(icon.getDrawableName());
                if ((drawable == null || drawable.length() == 0) && !addedIcons.contains(icon.getDrawableName())) {
                    addedIcons.add(icon.getDrawableName());
                    writer.append("Drawable: ").append(icon.getDrawableName()).append(".png")
                            .append("\r\n\r\n");
                }
            }

            writer.flush();
            writer.close();
            return brokenDrawables;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return null;
        }
    }

    @Nullable
    public static File buildActivityList(@NonNull Context context) {
        try {
            File activityList = new File(context.getCacheDir(), ACTIVITY_LIST);
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(activityList), UTF8));

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> appList = context.getPackageManager().queryIntentActivities(
                    intent, PackageManager.GET_RESOLVED_FILTER);
            try {
                Collections.sort(appList, new ResolveInfo.DisplayNameComparator(context.getPackageManager()));
            } catch (Exception ignored) {
            }

            boolean first = true;
            for (ResolveInfo app : appList) {

                if (first) {
                    first = false;
                    out.append("<!-- ACTIVITY LIST -->")
                            .append("\r\n").append("<!-- Activity list is a list that contains all activity from installed apps -->")
                            .append("\r\n\r\n\r\n");
                }

                String name = app.activityInfo.loadLabel(context.getPackageManager()).toString();
                String activity = app.activityInfo.packageName + "/" + app.activityInfo.name;
                out.append("<!-- ").append(name).append(" -->");
                out.append("\r\n").append(activity);
                out.append("\r\n\r\n");
            }

            out.flush();
            out.close();
            return activityList;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return null;
        }
    }

    @Nullable
    public static File buildCrashLog(@NonNull Context context, @NonNull String stackTrace) {
        try {
            if (stackTrace.length() == 0) return null;

            File crashLog = new File(context.getCacheDir(), CRASHLOG);
            String deviceInfo = DeviceHelper.getDeviceInfoForCrashReport(context);
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(crashLog), UTF8));
            out.append(deviceInfo).append(stackTrace);
            out.flush();
            out.close();
            return crashLog;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return null;
        }
    }
}
