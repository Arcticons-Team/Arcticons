package candybar.lib.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.items.Language;
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

public class LocaleHelper {

    public static void setLocale(@NonNull Context context) {
        Locale locale = Preferences.get(context).getCurrentLocale();
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.setDefault(new LocaleList(locale));
            configuration.setLocales(new LocaleList(locale));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }

        //Todo:
        // Find out a solution to use context.createConfigurationContext(configuration);
        // It breaks onConfigurationChanged()
        // Still can't find a way to fix that
        // No other options, better use deprecated code for now
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }

    public static List<Language> getAvailableLanguages(@NonNull Context context) {
        List<Language> languages = new ArrayList<>();
        String[] names = context.getResources().getStringArray(R.array.languages_name);
        String[] codes = context.getResources().getStringArray(R.array.languages_code);

        for (int i = 0; i < names.length; i++) {
            Language language = new Language(names[i], getLocale(codes[i]));
            languages.add(language);
        }
        return languages;
    }

    public static Language getCurrentLanguage(@NonNull Context context) {
        List<Language> languages = getAvailableLanguages(context);
        Locale locale = Preferences.get(context).getCurrentLocale();

        for (Language language : languages) {
            Locale l = language.getLocale();
            if (locale.toString().equals(l.toString())) {
                return language;
            }
        }
        return new Language("English", new Locale("en", "US"));
    }

    public static Locale getLocale(String language) {
        String[] codes = language.split("_");
        if (codes.length == 2) {
            return new Locale(codes[0], codes[1]);
        }
        return Locale.getDefault();
    }

    @Nullable
    public static String getOtherAppLocaleName(@NonNull Context context, @NonNull Locale locale, @NonNull String componentNameStr) {
        try {
            int slashIndex = componentNameStr.indexOf("/");
            String packageName = componentNameStr.substring(0, slashIndex);
            String activityName = componentNameStr.substring(slashIndex + 1);
            ComponentName componentName = new ComponentName(packageName, activityName);

            PackageManager packageManager = context.getPackageManager();
            ActivityInfo info = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA);

            Resources res = packageManager.getResourcesForActivity(componentName);
            Configuration configuration = new Configuration();

            configuration.locale = locale;
            res.updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            return info.loadLabel(packageManager).toString();
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }
}
