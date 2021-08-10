package com.donnnno.arcticons.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentUtils {
    public static void openActivity(Context context, Class<?> name) {
        Intent intent = new Intent(context, name);
        context.startActivity(intent);
    }

    public static void openUrl(Context context, int resId) {
        Uri uri = Uri.parse(context.getResources().getString(resId));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}