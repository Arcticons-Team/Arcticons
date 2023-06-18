package com.donnnno.arcticons.services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.AlarmClock;
import android.widget.RemoteViews;

import com.donnnno.arcticons.R;

import java.util.Date;
import java.text.DateFormat;

/**
 * Implementation of App Widget functionality.
 */
public class DigitalClockWidget extends AppWidgetProvider {

    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        int flags = 0;

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(act)) {
            RemoteViews clockView = new RemoteViews(context.getPackageName(), R.layout.digital_clock_widget);

            Intent clockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            clockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            }
            clockView.setOnClickPendingIntent(R.id.digital_clock_view, PendingIntent.getActivity(context, 0, clockIntent, flags));

            AppWidgetManager.getInstance(context).updateAppWidget(intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS), clockView);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.digital_clock_widget);
        DateFormat currentTimeFormat;
        currentTimeFormat = DateFormat.getTimeInstance();
        String currentTimeString = currentTimeFormat.format(new Date());

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}