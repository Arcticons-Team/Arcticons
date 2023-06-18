package com.donnnno.arcticons.services;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.donnnno.arcticons.R;

public class GoogleWeatherWidget extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            setupShortcut();
            finish();
            return;
        }
    }void setupShortcut() {
        Intent shortcutIntent = new Intent(this, GoogleWeatherWidget.class);


        Intent.ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.google_weather);
        shortcutIntent.setData(Uri.parse("https:///..."));
        shortcutIntent.setComponent(ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.apps.search.weather.WeatherExportedActivity"));
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.google_weather))
                .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        setResult(RESULT_OK, intent);
    }

}
