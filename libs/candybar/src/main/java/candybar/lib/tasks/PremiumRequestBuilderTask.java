package candybar.lib.tasks;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.fragments.dialog.IntentChooserFragment;
import candybar.lib.helpers.DeviceHelper;
import candybar.lib.items.Request;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.Extras;
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

public class PremiumRequestBuilderTask extends AsyncTaskBase {

    private final WeakReference<Context> mContext;
    private final WeakReference<PremiumRequestBuilderCallback> mCallback;
    private String mEmailBody;
    private Extras.Error mError;

    public PremiumRequestBuilderTask(Context context, PremiumRequestBuilderCallback callback) {
        mContext = new WeakReference<>(context);
        mCallback = new WeakReference<>(callback);
    }

    @Override
    protected boolean run() {
        if (!isCancelled()) {
            try {
                Thread.sleep(1);
                if (CandyBarApplication.sRequestProperty == null) {
                    mError = Extras.Error.ICON_REQUEST_PROPERTY_NULL;
                    return false;
                }

                if (CandyBarApplication.sRequestProperty.getComponentName() == null) {
                    mError = Extras.Error.ICON_REQUEST_PROPERTY_COMPONENT_NULL;
                    return false;
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(DeviceHelper.getDeviceInfo(mContext.get()));

                List<Request> requests = Database.get(mContext.get()).getPremiumRequest(null);

                for (int i = 0; i < requests.size(); i++) {
                    stringBuilder.append("\r\n\r\n")
                            .append(requests.get(i).getName())
                            .append("\r\n")
                            .append(requests.get(i).getActivity())
                            .append("\r\n")
                            .append("https://play.google.com/store/apps/details?id=")
                            .append(requests.get(i).getPackageName())
                            .append("\r\n")
                            .append("Order Id: ")
                            .append(requests.get(i).getOrderId())
                            .append("\r\n")
                            .append("Product Id: ")
                            .append(requests.get(i).getProductId());
                }

                mEmailBody = stringBuilder.toString();
                return true;
            } catch (Exception e) {
                CandyBarApplication.sRequestProperty = null;
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

        if (ok) {
            try {
                if (mCallback.get() != null) {
                    mCallback.get().onFinished();
                }

                RequestListener listener = (RequestListener) mContext.get();
                listener.onRequestBuilt(getIntent(CandyBarApplication.sRequestProperty.getComponentName(), mEmailBody),
                        IntentChooserFragment.REBUILD_ICON_REQUEST);
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
            }
        } else {
            if (mError != null) {
                LogUtil.e(mError.getMessage());
                mError.showToast(mContext.get());
            }
        }
    }

    @Nullable
    private Intent getIntent(ComponentName name, String emailBody) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            addIntentExtra(intent, emailBody);
            intent.setComponent(name);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        } catch (IllegalArgumentException e) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                addIntentExtra(intent, emailBody);
                return intent;
            } catch (ActivityNotFoundException e1) {
                LogUtil.e(Log.getStackTraceString(e1));
            }
        }
        return null;
    }

    private void addIntentExtra(@NonNull Intent intent, String emailBody) {
        intent.setType("application/zip");

        if (CandyBarApplication.sZipPath != null) {
            File zip = new File(CandyBarApplication.sZipPath);
            if (zip.exists()) {
                Uri uri = FileHelper.getUriFromFile(mContext.get(), mContext.get().getPackageName(), zip);
                if (uri == null) uri = Uri.fromFile(zip);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        String subject = mContext.get().getResources().getString(R.string.premium_request_email_subject);
        if (subject.length() == 0)
            subject = mContext.get().getResources().getString(R.string.app_name) + " Premium Icon Request";
        subject = "Rebuilt: " + subject;

        String emailAddress = mContext.get().getResources().getString(R.string.premium_request_email);
        // Fallback to regular request email
        if (emailAddress.length() == 0)
            emailAddress = mContext.get().getResources().getString(R.string.regular_request_email);
        // Fallback to dev_email
        if (emailAddress.length() == 0)
            emailAddress = mContext.get().getResources().getString(R.string.dev_email);

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }

    public interface PremiumRequestBuilderCallback {
        void onFinished();
    }
}
