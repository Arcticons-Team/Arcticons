package candybar.lib.helpers;

import android.content.Context;
import android.content.res.Configuration;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Theme;
import candybar.lib.preferences.Preferences;

public class ThemeHelper {
    public static Theme getDefaultTheme(Context context) {
        try {
            return Theme.valueOf(context.getResources().getString(R.string.default_theme).toUpperCase());
        } catch (Exception e) {
            return Theme.AUTO;
        }
    }

    public static boolean isDarkTheme(Context context) {
        boolean isThemingEnabled = CandyBarApplication.getConfiguration().isDashboardThemingEnabled();
        if (!isThemingEnabled) return getDefaultTheme(context) == Theme.DARK;

        Theme currentTheme = Preferences.get(context).getTheme();
        if (currentTheme == Theme.AUTO) {
            switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    return true;
                case Configuration.UI_MODE_NIGHT_NO:
                    return false;
            }
        }

        return currentTheme == Theme.DARK;
    }
}