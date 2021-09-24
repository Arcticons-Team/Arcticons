package candybar.lib.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;

import static com.danimahardhika.android.helpers.core.DrawableHelper.getTintedDrawable;

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

public class UrlHelper {

    @Nullable
    public static Drawable getSocialIcon(@NonNull Context context, @NonNull Type type) {
        int color = ConfigurationHelper.getSocialIconColor(context, CandyBarApplication.getConfiguration().getSocialIconColor());
        switch (type) {
            case EMAIL:
                return getTintedDrawable(context, R.drawable.ic_toolbar_email, color);
            case BEHANCE:
                return getTintedDrawable(context, R.drawable.ic_toolbar_behance, color);
            case DRIBBBLE:
                return getTintedDrawable(context, R.drawable.ic_toolbar_dribbble, color);
            case FACEBOOK:
                return getTintedDrawable(context, R.drawable.ic_toolbar_facebook, color);
            case GITHUB:
                return getTintedDrawable(context, R.drawable.ic_toolbar_github, color);
            case INSTAGRAM:
                return getTintedDrawable(context, R.drawable.ic_toolbar_instagram, color);
            case PINTEREST:
                return getTintedDrawable(context, R.drawable.ic_toolbar_pinterest, color);
            case TWITTER:
                return getTintedDrawable(context, R.drawable.ic_toolbar_twitter, color);
            case TELEGRAM:
                return getTintedDrawable(context, R.drawable.ic_toolbar_telegram, color);
            default:
                return getTintedDrawable(context, R.drawable.ic_toolbar_website, color);
        }
    }

    public static Type getType(String url) {
        if (url == null) return Type.INVALID;
        if (!URLUtil.isValidUrl(url)) {
            if (Patterns.EMAIL_ADDRESS.matcher(url).matches()) {
                return Type.EMAIL;
            }
            return Type.INVALID;
        }

        if (url.contains("behance.")) {
            return Type.BEHANCE;
        } else if (url.contains("dribbble.")) {
            return Type.DRIBBBLE;
        } else if (url.contains("facebook.")) {
            return Type.FACEBOOK;
        } else if (url.contains("github.")) {
            return Type.GITHUB;
        } else if (url.contains("instagram.")) {
            return Type.INSTAGRAM;
        } else if (url.contains("pinterest.")) {
            return Type.PINTEREST;
        } else if (url.contains("twitter.")) {
            return Type.TWITTER;
        } else if (url.contains("t.me/") || url.contains("telegram.me/")) {
            return Type.TELEGRAM;
        } else {
            return Type.UNKNOWN;
        }
    }

    public enum Type {
        EMAIL,
        BEHANCE,
        DRIBBBLE,
        FACEBOOK,
        GITHUB,
        INSTAGRAM,
        PINTEREST,
        TWITTER,
        TELEGRAM,
        UNKNOWN,
        INVALID
    }
}
