package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.content.Intent;
import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ScoresWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = ScoresWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };

    // these indices must match the projection
    private static final int INDEX_MATCH_LEAGUE = 0;
    private static final int INDEX_MATCH_DATE = 1;
    private static final int INDEX_MATCH_TIME = 2;
    private static final int INDEX_MATCH_HOME = 3;
    private static final int INDEX_MATCH_AWAY = 4;
    private static final int COL_LEAGUE = 5;
    private static final int COL_HOME_GOALS = 6;
    private static final int COL_AWAY_GOALS = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                Uri matchesWithDateUri = DatabaseContract.scores_table.buildScoreWithDate();
                String[] selectionArgs = new String[1];
                Date fragmentdate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                selectionArgs[0] = mformat.format(fragmentdate);
//                Log.d(LOG_TAG, "onDataSetChanged query for day " + selectionArgs[0]); //log spam
                data = getContentResolver().query(matchesWithDateUri, null, null, selectionArgs, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),R.layout.widget_scores_list_item);

                String time = data.getString(INDEX_MATCH_TIME);
                String home = data.getString(INDEX_MATCH_HOME);
                String away = data.getString(INDEX_MATCH_AWAY);
                String score = Utility.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));
//                Log.d(LOG_TAG, "Match " + home + " vs " + away + " at " + time + " with score:" + score);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, getCount() + " " + getString(R.string.widget_scores));
                }
                views.setTextViewText(R.id.data_textview, time);
                views.setTextViewText(R.id.home_name, home);
                views.setTextViewText(R.id.away_name, away);
                views.setTextViewText(R.id.score_textview, score);

                // Create an Intent to launch MainActivity
                Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.hashCode();
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}