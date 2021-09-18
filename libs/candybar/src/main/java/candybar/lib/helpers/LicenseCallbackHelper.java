package candybar.lib.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.license.LicenseCallback;
import com.danimahardhika.android.helpers.license.LicenseHelper;

import candybar.lib.R;
import candybar.lib.preferences.Preferences;

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

public class LicenseCallbackHelper implements LicenseCallback {

    private final Context mContext;
    private final Runnable mCallback;
    private final MaterialDialog mDialog;

    public LicenseCallbackHelper(@NonNull Context context, Runnable callback) {
        mContext = context;
        mCallback = callback;

        mDialog = new MaterialDialog.Builder(mContext)
                .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                .content(R.string.license_checking)
                .progress(true, 0)
                .build();
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onLicenseCheckStart() {
        mDialog.show();
    }

    @Override
    public void onLicenseCheckFinished(LicenseHelper.Status status) {
        // Sometimes `onLicenseCheckFinished` gets called just after `onLicenseCheckStart`
        // and it messes up the layout, so delay is the workaround

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mDialog.dismiss();

            if (status == LicenseHelper.Status.RETRY) {
                showRetryDialog();
                return;
            }

            showLicenseDialog(status);
        }, 1000);
    }

    private void showLicenseDialog(LicenseHelper.Status status) {
        int message = status == LicenseHelper.Status.SUCCESS ?
                R.string.license_check_success : R.string.license_check_failed;
        new MaterialDialog.Builder(mContext)
                .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                .title(R.string.license_check)
                .content(message)
                .positiveText(R.string.close)
                .onPositive((dialog, which) -> {
                    onLicenseChecked(status);
                    dialog.dismiss();
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
    }

    private void showRetryDialog() {
        new MaterialDialog.Builder(mContext)
                .typeface(TypefaceHelper.getMedium(mContext), TypefaceHelper.getRegular(mContext))
                .title(R.string.license_check)
                .content(R.string.license_check_retry)
                .positiveText(R.string.close)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .onPositive((dialog, which) -> ((AppCompatActivity) mContext).finish())
                .show();
    }

    private void onLicenseChecked(LicenseHelper.Status status) {
        if (status == LicenseHelper.Status.SUCCESS) {
            Preferences.get(mContext).setLicensed(true);
            mCallback.run();
        } else if (status == LicenseHelper.Status.FAILED) {
            Preferences.get(mContext).setLicensed(false);
            ((AppCompatActivity) mContext).finish();
        }
    }
}
