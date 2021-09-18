package candybar.lib.helpers;

import android.content.Context;
import android.util.Log;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

public class PresetsHelper {

    public static int getPresetsCount(Context context) {
        try {
            String[] komponents = context.getAssets().list("komponents");
            String[] lockscreens = context.getAssets().list("lockscreens");
            String[] wallpapers = context.getAssets().list("wallpapers");
            String[] widgets = context.getAssets().list("widgets");

            return komponents.length + lockscreens.length + wallpapers.length + widgets.length;
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            return 0;
        }
    }

}
