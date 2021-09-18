package candybar.lib.tasks;

import static com.danimahardhika.android.helpers.core.FileHelper.getUriFromFile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.helpers.DeviceHelper;
import candybar.lib.helpers.ReportBugsHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.helpers.TypefaceHelper;
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

public class ReportBugsTask extends AsyncTaskBase {

    private final WeakReference<Context> mContext;
    private final String mDescription;
    private String mZipPath = null;
    private StringBuilder mStringBuilder;
    private MaterialDialog mDialog;

    public ReportBugsTask(Context context, String description) {
        mContext = new WeakReference<>(context);
        mDescription = description;
    }

    @Override
    protected void preRun() {
        mDialog = new MaterialDialog.Builder(mContext.get())
                .typeface(TypefaceHelper.getMedium(mContext.get()), TypefaceHelper.getRegular(mContext.get()))
                .content(R.string.report_bugs_building)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build();
        mDialog.show();
        mStringBuilder = new StringBuilder();
    }

    @Override
    protected boolean run() {
        if (!isCancelled()) {
            try {
                Thread.sleep(1);
                List<String> files = new ArrayList<>();

                mStringBuilder.append(DeviceHelper.getDeviceInfo(mContext.get()))
                        .append("\r\n").append(mDescription).append("\r\n");

                File brokenAppFilter = ReportBugsHelper.buildBrokenAppFilter(mContext.get());
                if (brokenAppFilter != null) files.add(brokenAppFilter.toString());

                File brokenDrawables = ReportBugsHelper.buildBrokenDrawables(mContext.get());
                if (brokenDrawables != null) files.add(brokenDrawables.toString());

                File activityList = ReportBugsHelper.buildActivityList(mContext.get());
                if (activityList != null) files.add(activityList.toString());

                String stackTrace = Preferences.get(mContext.get()).getLatestCrashLog();
                File crashLog = ReportBugsHelper.buildCrashLog(mContext.get(), stackTrace);
                if (crashLog != null) files.add(crashLog.toString());

                mZipPath = FileHelper.createZip(files, new File(mContext.get().getCacheDir(),
                        RequestHelper.getGeneratedZipName(ReportBugsHelper.REPORT_BUGS)));
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
        if (mContext.get() == null) return;
        if (((AppCompatActivity) mContext.get()).isFinishing()) return;

        mDialog.dismiss();
        if (ok) {
            String emailAddress = mContext.get().getString(R.string.regular_request_email);
            // Fallback to dev_email
            if (emailAddress.length() == 0)
                emailAddress = mContext.get().getString(R.string.dev_email);

            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    "Report Bugs " + (mContext.get().getString(
                            R.string.app_name)));
            intent.putExtra(Intent.EXTRA_TEXT, mStringBuilder.toString());

            if (mZipPath != null) {
                File zip = new File(mZipPath);
                if (zip.exists()) {
                    Uri uri = getUriFromFile(mContext.get(), mContext.get().getPackageName(), zip);
                    if (uri == null) uri = Uri.fromFile(zip);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            mContext.get().startActivity(Intent.createChooser(intent,
                    mContext.get().getResources().getString(R.string.app_client)));
        } else {
            Toast.makeText(mContext.get(), R.string.report_bugs_failed,
                    Toast.LENGTH_LONG).show();
        }

        mZipPath = null;
    }
}
