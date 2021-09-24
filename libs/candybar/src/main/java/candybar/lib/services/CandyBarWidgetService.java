package candybar.lib.services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.AlarmClock;
import android.widget.RemoteViews;

import candybar.lib.R;

public class CandyBarWidgetService extends AppWidgetProvider {

    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(act)) {
            RemoteViews clockView = new RemoteViews(context.getPackageName(), R.layout.analog_clock);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent clockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                clockView.setOnClickPendingIntent(R.id.analog_clock, PendingIntent.getActivity(context, 0, clockIntent, 0));
            }

            AppWidgetManager.getInstance(context).updateAppWidget(intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS), clockView);
        }
    }
}
