package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.content.Intent;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;

public class NextMatchWidgetIntentService extends IntentService {

    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL
    };

    // these indices must match the projection
    private static final int INDEX_MATCH_LEAGUE = 0;
    private static final int INDEX_MATCH_DATE = 1;
    private static final int INDEX_MATCH_TIME = 2;
    private static final int INDEX_MATCH_HOME = 3;
    private static final int INDEX_MATCH_AWAY = 4;

    public NextMatchWidgetIntentService() {
        super("NextMatchWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                NextMatchWidgetProvider.class));

        // Get tomorrow's match from the ContentProvider
        Uri matchesWithDateUri = DatabaseContract.scores_table.buildScoreWithDate();
        String[] selectionArgs = new String[1];
        Date fragmentdate = new Date(System.currentTimeMillis() + 86400000);
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        selectionArgs[0] = mformat.format(fragmentdate);

        Cursor data = getContentResolver().query(matchesWithDateUri, null, null, selectionArgs, null);

        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the Match data from the Cursor
        String time = data.getString(INDEX_MATCH_TIME);
        String home = data.getString(INDEX_MATCH_HOME);
        String away = data.getString(INDEX_MATCH_AWAY);
//        Log.e("Widget", "cursor time:" + time);
//        Log.e("Widget", "cursor home:" + home);
//        Log.e("Widget", "cursor away :" + away);

        data.close();

        // Perform this loop procedure for each Next Match widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_next_default_width);
            int layoutId;
            if (widgetWidth > defaultWidth) {
                layoutId = R.layout.widget_next;
            } else {
                layoutId = R.layout.widget_next_small;
            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, getString(R.string.tomorrow) + " " + time + " - " + home + " vs " + away);
            }
            views.setTextViewText(R.id.widget_match, getString(R.string.tomorrow) + " " + time);
            views.setImageViewResource(R.id.widget_home_crest, Utility.getTeamCrestByTeamName(home));
            views.setImageViewResource(R.id.widget_away_crest, Utility.getTeamCrestByTeamName(away));
            views.setTextViewText(R.id.widget_home_name, home);
            views.setTextViewText(R.id.widget_away_name, away);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.layout, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_next_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp, displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_next_default_width);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.layout, description);
    }
}
