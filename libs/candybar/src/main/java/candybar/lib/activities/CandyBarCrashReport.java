package candybar.lib.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.FileHelper;

import java.io.File;

import candybar.lib.R;
import candybar.lib.helpers.DeviceHelper;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.ReportBugsHelper;

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

public class CandyBarCrashReport extends AppCompatActivity {

    public static final String EXTRA_STACKTRACE = "stacktrace";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                finish();
                return;
            }

            LocaleHelper.setLocale(this);

            String stackTrace = bundle.getString(EXTRA_STACKTRACE);
            String deviceInfo = DeviceHelper.getDeviceInfoForCrashReport(this);

            String message = getResources().getString(R.string.crash_report_message, getResources().getString(R.string.app_name));
            String emailAddress = getResources().getString(R.string.regular_request_email).length() > 0
                    ? getResources().getString(R.string.regular_request_email)
                    // Fallback to dev_email
                    : getResources().getString(R.string.dev_email);

            new MaterialDialog.Builder(this)
                    .title(R.string.crash_report)
                    .content(message)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .positiveText(R.string.crash_report_send)
                    .negativeText(R.string.close)
                    .onPositive((dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "CandyBar: Crash Report");

                        prepareUri(deviceInfo, stackTrace, intent);

                        startActivity(Intent.createChooser(intent,
                                getResources().getString(R.string.app_client)));
                        dialog.dismiss();
                    })
                    .dismissListener(dialogInterface -> finish())
                    .show();
        } catch (Exception e) {
            finish();
        }
    }

    private void prepareUri(String deviceInfo, String stackTrace, Intent intent) {
        File crashLog = ReportBugsHelper.buildCrashLog(this, stackTrace);
        if (crashLog != null) {
            Uri uri = FileHelper.getUriFromFile(this, getPackageName(), crashLog);
            if (uri != null) {
                intent.putExtra(Intent.EXTRA_TEXT, deviceInfo + "\r\n");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                return;
            }
        }

        intent.putExtra(Intent.EXTRA_TEXT, deviceInfo + stackTrace);
    }
}
