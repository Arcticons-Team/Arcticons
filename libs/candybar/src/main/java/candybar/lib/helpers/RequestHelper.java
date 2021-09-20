package candybar.lib.helpers;

import static candybar.lib.helpers.DrawableHelper.getReqIcon;
import static candybar.lib.helpers.DrawableHelper.getRightIcon;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.TimeHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.items.Request;
import candybar.lib.preferences.Preferences;
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

public class RequestHelper {

    public static final String APPFILTER = "appfilter.xml";
    public static final String APPMAP = "appmap.xml";
    public static final String THEME_RESOURCES = "theme_resources.xml";
    public static final String ZIP = "icon_request.zip";
    public static final String REBUILD_ZIP = "rebuild_icon_request.zip";

    public static String getGeneratedZipName(@NonNull String baseName) {
        return baseName.substring(0, baseName.lastIndexOf(".")) + "_" + TimeHelper.getDateTime(
                new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.ENGLISH)) + ".zip";
    }

    public static String fixNameForRequest(String name) {
        String normalized = Normalizer.normalize(name.toLowerCase(), Normalizer.Form.NFD);

        try {
            // This code causes crash on some android devices
            normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+", "");
        } catch (Exception ignored) {
        }

        normalized = normalized
                .replaceAll("[.\"']", "")
                .replaceAll("[ \\[\\]{}()=!/\\\\,?°|<>;:#~+*-]", "_")
                .replaceAll("&", "_and_");

        if (Character.isDigit(normalized.charAt(0))) normalized = "_" + normalized;
        normalized = normalized.replaceAll("_+", "_");

        return normalized;
    }

    @Nullable
    public static File buildXml(@NonNull Context context, @NonNull List<Request> requests, @NonNull XmlType xmlType) {
        try {
            if (xmlType == XmlType.APPFILTER && !CandyBarApplication.getConfiguration().isGenerateAppFilter()) {
                return null;
            } else if (xmlType == XmlType.APPMAP && !CandyBarApplication.getConfiguration().isGenerateAppMap()) {
                return null;
            } else if (xmlType == XmlType.THEME_RESOURCES & !CandyBarApplication.getConfiguration().isGenerateThemeResources()) {
                return null;
            }

            File file = new File(context.getCacheDir().toString(), xmlType.getFileName());
            String UTF8 = "UTF8";
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), UTF8));
            writer.append(xmlType.getHeader()).append("\n\n");

            for (Request request : requests) {
                writer.append(xmlType.getContent(context, request));
            }
            writer.append(xmlType.getFooter());
            writer.flush();
            writer.close();
            return file;
        } catch (IOException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    public static String buildJsonForArctic(@NonNull List<Request> requests) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        sb.append("{ \"components\": [\n");
        for (Request request : requests) {
            if (!isFirst) sb.append(",\n");
            sb.append(String.format("{ \"name\": \"%s\", \"pkg\": \"%s\", \"componentInfo\": \"%s\", \"drawable\": \"%s\" }",
                    request.getName(),
                    request.getPackageName(),
                    request.getActivity(),
                    fixNameForRequest(request.getName())));
            isFirst = false;
        }
        sb.append("]}");

        return sb.toString();
    }

    public static String buildJsonForMyAP(@NonNull Context context, @NonNull List<Request> requests) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        sb.append("{ \"projectUID\": \"ENTER UID\",");
        sb.append("\"icons\" : [");
        for (Request request : requests) {
            Bitmap appBitmap = getRightIcon(getReqIcon(context, request.getActivity()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert appBitmap != null;
            appBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String base64Icon = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            if (!isFirst) sb.append(",\n");
            sb
                    .append("\"name\": \"").append(request.getName()).append("\",")
                    .append("\"packageName\": \"").append(request.getPackageName()).append("\",")
                    .append("\"imageStr\": \"").append(base64Icon).append("\",")
                    .append("\"activities\": [\"").append(request.getActivity()).append("\"]");
            isFirst = false;
        }
        sb.append("]}");

        return sb.toString();
    }

    public static String getRegularArcticApiKey(Context context) {
        String arcticApiKey = context.getResources().getString(R.string.regular_request_arctic_api_key);
        // Fallback to arctic_manager_api_key
        if (arcticApiKey.length() == 0)
            arcticApiKey = context.getResources().getString(R.string.arctic_manager_api_key);

        return arcticApiKey;
    }

    public static boolean isRegularArcticEnabled(Context context) {
        return context.getResources().getString(R.string.regular_request_method).length() > 0
                ? context.getResources().getString(R.string.regular_request_method).contentEquals("arctic")
                // Use fallback method to check if arctic is enabled
                : getRegularArcticApiKey(context).length() > 0;
    }

    public static String getPremiumArcticApiKey(Context context) {
        String arcticApiKey = context.getResources().getString(R.string.premium_request_arctic_api_key);
        // Fallback to regular request's api key
        if (arcticApiKey.length() == 0) arcticApiKey = getRegularArcticApiKey(context);

        return arcticApiKey;
    }

    public static boolean isPremiumArcticEnabled(Context context) {
        return context.getResources().getString(R.string.premium_request_method).length() > 0
                ? context.getResources().getString(R.string.premium_request_method).contentEquals("arctic")
                // Fallback to regular request's method
                : context.getResources().getString(R.string.regular_request_method).length() > 0
                ? context.getResources().getString(R.string.regular_request_method).contentEquals("arctic")
                // Use fallback method to check if arctic is enabled
                : getRegularArcticApiKey(context).length() > 0;
    }

    public static String sendArcticRequest(List<Request> requests, List<String> iconFiles, File directory, String apiKey) {
        okhttp3.RequestBody okRequestBody = new okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("apps", buildJsonForArctic(requests))
                .addFormDataPart("archive", "icons.zip", okhttp3.RequestBody.create(
                        Objects.requireNonNull(getZipFile(iconFiles, directory.toString(), "icons.zip")),
                        okhttp3.MediaType.parse("application/zip")))
                .build();

        okhttp3.Request okRequest = new okhttp3.Request.Builder()
                .url("https://arcticmanager.com/v1/request")
                .addHeader("TokenID", apiKey)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "afollestad/icon-request")
                .post(okRequestBody)
                .build();

        okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient();

        try {
            okhttp3.Response response = okHttpClient.newCall(okRequest).execute();
            boolean success = response.code() > 199 && response.code() < 300;
            if (!success) {
                JSONObject responseJson = new JSONObject(Objects.requireNonNull(response.body()).string());
                return responseJson.getString("error");
            }
        } catch (IOException | JSONException ignoredException) {
            LogUtil.d("ARCTIC_MANAGER: Error");
            return "";
        }
        return null;
    }

    public static File getZipFile(List<String> files, String filepath, String filename) {
        // Modified from https://github.com/danimahardhika/android-helpers/blob/master/core/src/main/java/com/danimahardhika/android/helpers/core/FileHelper.java
        try {
            final int BUFFER = 2048;
            final File file = new File(filepath, filename);
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(file);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));

            byte[] data = new byte[BUFFER];
            for (int i = 0; i < files.size(); i++) {
                FileInputStream fi = new FileInputStream(files.get(i));
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(files.get(i).substring(
                        files.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
            return file;
        } catch (Exception ignored) {
        }
        return null;
    }

    @NonNull
    public static HashMap<String, String> getAppFilter(@NonNull Context context, @NonNull Key key) {
        try {
            HashMap<String, String> activities = new HashMap<>();
            XmlPullParser xpp = context.getResources().getXml(R.xml.appfilter);

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xpp.getEventType() == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("item")) {
                        String sKey = xpp.getAttributeValue(null, key.getKey());
                        String sValue = xpp.getAttributeValue(null, key.getValue());

                        if (sKey != null && sValue != null) {
                            activities.put(
                                    sKey.replace("ComponentInfo{", "").replace("}", ""),
                                    sValue.replace("ComponentInfo{", "").replace("}", ""));
                        } else {
                            LogUtil.e("Appfilter Error\nKey: " + sKey + "\nValue: " + sValue);
                        }
                    }
                }
                xpp.next();
            }
            return activities;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return new HashMap<>();
    }

    @NonNull
    public static List<Request> getMissingApps(@NonNull Context context) {
        List<Request> requests = new ArrayList<>();
        HashMap<String, String> appFilter = getAppFilter(context, Key.ACTIVITY);
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> installedApps = packageManager.queryIntentActivities(
                intent, PackageManager.GET_RESOLVED_FILTER);
        CandyBarMainActivity.sInstalledAppsCount = installedApps.size();

        try {
            Collections.sort(installedApps,
                    new ResolveInfo.DisplayNameComparator(packageManager));
        } catch (Exception ignored) {
        }

        for (ResolveInfo app : installedApps) {
            String packageName = app.activityInfo.packageName;
            String activity = packageName + "/" + app.activityInfo.name;

            String value = appFilter.get(activity);

            if (value == null) {
                String name = LocaleHelper.getOtherAppLocaleName(context, new Locale("en"), activity);
                if (name == null) {
                    name = app.activityInfo.loadLabel(packageManager).toString();
                }

                boolean requested = Database.get(context).isRequested(activity);
                Request request = Request.Builder()
                        .name(name)
                        .packageName(app.activityInfo.packageName)
                        .activity(activity)
                        .requested(requested)
                        .build();

                requests.add(request);
            }
        }
        return requests;
    }

    public static void showAlreadyRequestedDialog(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(R.string.request_requested)
                .positiveText(R.string.close)
                .show();
    }

    public static void showIconRequestLimitDialog(@NonNull Context context) {
        boolean reset = context.getResources().getBoolean(R.bool.reset_icon_request_limit);
        int limit = context.getResources().getInteger(R.integer.icon_request_limit);
        String message = context.getResources().getString(R.string.request_limit, limit);
        message += " " + context.getResources().getString(R.string.request_used,
                Preferences.get(context).getRegularRequestUsed());

        if (Preferences.get(context).isPremiumRequestEnabled())
            message += " " + context.getResources().getString(R.string.request_limit_buy);

        if (reset)
            message += "\n\n" + context.getResources().getString(R.string.request_limit_reset);
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestRequired(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(R.string.request_title)
                .content(R.string.premium_request_required)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestLimitDialog(@NonNull Context context, int selected) {
        String message = context.getResources().getString(R.string.premium_request_limit,
                Preferences.get(context).getPremiumRequestCount());
        message += " " + context.getResources().getString(R.string.premium_request_limit1, selected);
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestStillAvailable(@NonNull Context context) {
        String message = context.getResources().getString(
                R.string.premium_request_already_purchased,
                Preferences.get(context).getPremiumRequestCount());
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(message)
                .positiveText(R.string.close)
                .show();
    }

    public static boolean isReadyToSendPremiumRequest(@NonNull Context context) {
        boolean isReady = Preferences.get(context).isConnectedToNetwork();
        if (!isReady) {
            new MaterialDialog.Builder(context)
                    .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                    .title(R.string.premium_request)
                    .content(R.string.premium_request_no_internet)
                    .positiveText(R.string.close)
                    .show();
        }
        return isReady;
    }

    public static void showPremiumRequestConsumeFailed(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(R.string.premium_request_consume_failed)
                .positiveText(R.string.close)
                .show();
    }

    public static void showPremiumRequestExist(@NonNull Context context) {
        new MaterialDialog.Builder(context)
                .typeface(TypefaceHelper.getMedium(context), TypefaceHelper.getRegular(context))
                .title(R.string.premium_request)
                .content(R.string.premium_request_exist)
                .positiveText(R.string.close)
                .show();
    }

    public static void checkPiracyApp(@NonNull Context context) {
        boolean premiumRequest = context.getResources().getBoolean(R.bool.enable_premium_request);
        //Dashboard don't need to check piracy app if premium request is disabled
        if (!premiumRequest) {
            Preferences.get(context).setPremiumRequestEnabled(false);
            RequestListener listener = (RequestListener) context;
            return;
        }

        // Lucky Patcher and Freedom package name
        String[] strings = new String[]{
                "com.chelpus.lackypatch",
                "com.dimonvideo.luckypatcher",
                "com.forpda.lp",
                //"com.android.protips", This is not lucky patcher or freedom
                "com.android.vending.billing.InAppBillingService.LUCK",
                "com.android.vending.billing.InAppBillingService.LOCK",
                "cc.madkite.freedom",
                "com.android.vending.billing.InAppBillingService.LACK",
                "com.android.vending.billing.InAppBillingService.CLON",
                "com.android.vending.billing.InAppBillingService.CRAC",
                "com.android.vending.billing.InAppBillingService.COIN"
        };

        boolean isPiracyAppInstalled = false;
        for (String string : strings) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        string, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    isPiracyAppInstalled = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }

        Preferences.get(context).setPremiumRequestEnabled(!isPiracyAppInstalled);

        RequestListener listener = (RequestListener) context;
    }

    public enum XmlType {
        APPFILTER(RequestHelper.APPFILTER, "<resources>", "</resources>"),
        APPMAP(RequestHelper.APPMAP, "<appmap>", "</appmap>"),
        THEME_RESOURCES(RequestHelper.THEME_RESOURCES, "<Theme version=\"1\">", "</Theme>");

        private final String fileName;
        private final String header;
        private final String footer;

        XmlType(String fileName, String header, String footer) {
            this.fileName = fileName;
            this.header = header;
            this.footer = footer;
        }

        private String getFileName() {
            return fileName;
        }

        private String getHeader() {
            return header;
        }

        private String getFooter() {
            return footer;
        }

        private String getContent(@NonNull Context context, @NonNull Request request) {
            switch (this) {
                case APPFILTER:
                    return "\t<!-- " + request.getName() + " -->" +
                            "\n" +
                            "\t" + context.getString(R.string.appfilter_item)
                            .replaceAll("\\{\\{component\\}\\}", request.getActivity())
                            .replaceAll("\\{\\{drawable\\}\\}", fixNameForRequest(request.getName())) +
                            "\n\n";
                case APPMAP:
                    String packageName = "" + request.getPackageName() + "/";
                    String className = request.getActivity().replaceFirst(packageName, "");
                    return "\t<!-- " + request.getName() + " -->" +
                            "\n" +
                            "\t<item class=\"" + className + "\" name=\"" +
                            fixNameForRequest(request.getName()) +
                            "\"/>" +
                            "\n\n";
                case THEME_RESOURCES:
                    return "\t<!-- " + request.getName() + " -->" +
                            "\n" +
                            "\t<AppIcon name=\"" + request.getActivity() + "\" image=\"" +
                            fixNameForRequest(request.getName()) +
                            "\"/>" +
                            "\n\n";
                default:
                    return "";
            }
        }
    }

    public enum Key {
        ACTIVITY("component", "drawable"),
        DRAWABLE("drawable", "component");

        private final String key;
        private final String value;

        Key(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }
    }
}
