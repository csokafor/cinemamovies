package com.chinedusokafor.silverbirdmoviis.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.widget.Toast;

import com.chinedusokafor.silverbirdmoviis.MainActivity;
import com.chinedusokafor.silverbirdmoviis.R;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.CastEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.MovieEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.ReviewEntry;
import com.chinedusokafor.silverbirdmoviis.rottentomatoes.RottentomatoesAPI;
import com.chinedusokafor.silverbirdmoviis.rss.MovieItem;
import com.chinedusokafor.silverbirdmoviis.rss.RssReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Created by cokafor on 1/25/2015.
 */
public class SilverbirdmoviisSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String LOG_TAG = SilverbirdmoviisSyncAdapter.class.getSimpleName();

    private static final String WARRI_URL = "http://silverbirdcinemas.com/warri?format=feed&type=rss";
    private static final String UYO_URL = "http://silverbirdcinemas.com/uyo?format=feed&type=rss";
    private static final String ABUJA_URL = "http://silverbirdcinemas.com/abuja?format=feed&type=rss";
    private static final String LAGOS_URL = "http://silverbirdcinemas.com/lagos?format=feed&type=rss";
    private static final String IKEJA_URL = "http://silverbirdcinemas.com/ikeja?format=feed&type=rss";
    private static final String SEC_ABUJA_URL = "http://silverbirdcinemas.com/sec-abuja?format=feed&type=rss";

    private static final String[] CINEMA_COLUMNS = {
            MoviisContract.CinemaEntry.TABLE_NAME + "." + MoviisContract.CinemaEntry._ID,
            MoviisContract.CinemaEntry.COLUMN_CINEMA_NAME
    };

    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIEID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_MPAARATING
    };

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOVIIS_NOTIFICATION_ID = 5004;

    private RottentomatoesAPI rottentomatoesAPI = new RottentomatoesAPI();

    public SilverbirdmoviisSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String cinema = prefs.getString(getContext().getString(R.string.pref_cinema_key),
                getContext().getString(R.string.pref_cinema_ikeja));
        ArrayList<String> movieTitles = getCinemaRssFeed(cinema);

        try {
            //get cinema id
            Uri cinemaUri = MoviisContract.CinemaEntry.buildCinemaName(cinema);
            Cursor cinemaCursor = getContext().getContentResolver().query(
                    cinemaUri, CINEMA_COLUMNS, null, null, "");
            cinemaCursor.moveToFirst();
            Integer cinema_id = cinemaCursor.getInt(0);
            Log.d(LOG_TAG, "cinema Id:" + cinema_id);

            //delete old movies
            deleteOldMovies(cinema, cinema_id);

            for(String movieTitle : movieTitles) {
                JSONObject searchJson = rottentomatoesAPI.searchForMovie(movieTitle);
                if(searchJson != null) {
                    String movieId = (String)searchJson.get("id");
                    //Log.d(LOG_TAG, "movieId: " + movieId);

                    String movieUrl = rottentomatoesAPI.getMovieJsonUrl(searchJson);
                    //Log.d(LOG_TAG, "movieJson: " + movieUrl);
                    JSONObject theMovieJson = rottentomatoesAPI.getMovieJson(movieUrl);

                    Integer movie_id = insertMovie(cinema_id, movieTitle, theMovieJson);
                    Log.d(LOG_TAG, "Db movie Id: " + movie_id);

                    notifyMovie(cinema, movieTitle, (String)theMovieJson.get("mpaa_rating"));

                    insertMovieCast(movie_id, theMovieJson);
                    insertMovieReviews(movie_id, theMovieJson);

                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "onPerformSync " + e.getMessage(), e);
        }
    }

    private void deleteOldMovies(String cinema, Integer cinema_id) {
        Calendar cal = Calendar.getInstance();
        //delete movies released more than 5 months ago
        cal.add(Calendar.MONTH, -5);
        String fiveMonthsAgo = MoviisContract.getDbDateString(cal.getTime());

        Uri movieUri = MovieEntry.buildCinemaAndDateMovie(cinema, fiveMonthsAgo);
        Cursor  movieCursor = getContext().getContentResolver().query(
                movieUri, MOVIE_COLUMNS, null, null, "");

        while (movieCursor.moveToNext()) {
            Integer movie_id = movieCursor.getInt(0);
            //delete casts for movie
            int noOfCast = getContext().getContentResolver().delete(CastEntry.CONTENT_URI,
                    CastEntry.COLUMN_MOVIE_KEY + " = ? ",
                    new String[] {movie_id.toString()});
            Log.d(LOG_TAG, "cast deleted: " + noOfCast);

            //delete reviews for movie
            int noOfReview = getContext().getContentResolver().delete(ReviewEntry.CONTENT_URI,
                    ReviewEntry.COLUMN_MOVIE_KEY + " = ? ",
                    new String[] {movie_id.toString()});
            Log.d(LOG_TAG, "reviews deleted: " + noOfReview);
        }

        //delete movies
        int noOfMovies = getContext().getContentResolver().delete(MovieEntry.CONTENT_URI,
                MovieEntry.COLUMN_CINEMA_KEY + " = ? AND " + MovieEntry.COLUMN_RELEASEDATE + " <= ?",
                new String[] {cinema_id.toString(), fiveMonthsAgo});
        Log.d(LOG_TAG, "movies deleted: " + noOfMovies);

    }

    private Integer insertMovie(Integer cinema_id, String movieTitle, JSONObject movieJson) {
        Integer movie_id = 0;
        try {
            Integer movieId = (Integer)movieJson.get("id");
            Log.d(LOG_TAG, "insertMovie movieId: " + movieId);

            String synopsis = (String) movieJson.get("synopsis");
            //Log.d(LOG_TAG,"synopsis: " + synopsis);
            String mpaa_rating = (String) movieJson.get("mpaa_rating");

            Integer runtime = (Integer) movieJson.get("runtime");

            String genres = rottentomatoesAPI.getMovieGenres(movieJson);

            JSONObject posterUrls = (JSONObject) movieJson.get("posters");
            String posterUrl = (String) posterUrls.get("detailed");

            JSONObject ratings = (JSONObject) movieJson.get("ratings");
            Integer criticsScore = (Integer) ratings.get("critics_score");
            Integer audienceScore = (Integer) ratings.get("audience_score");

            JSONObject releaseDate = (JSONObject) movieJson.get("release_dates");
            String theaterReleaseDate = (String) releaseDate.get("theater");

            String[] theDirectors = rottentomatoesAPI.getMovieDirectors(movieJson);
            String directors = "";
            if(theDirectors != null && theDirectors.length > 0) {
                for(int i = 0; i < theDirectors.length; i++) {
                    if(i == (theDirectors.length - 1)) {
                        directors += theDirectors[i];
                    } else {
                        directors += theDirectors[i] + ", ";
                    }
                    Log.d(LOG_TAG, directors);
                }
            }

            ContentValues movieValues = new ContentValues();
            //persist movie values
            movieValues.put(MoviisContract.MovieEntry.COLUMN_CINEMA_KEY, cinema_id);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_MOVIEID, movieId);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_TITLE, movieTitle);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_GENRE, genres);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_MPAARATING, mpaa_rating);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_RUNTIME, runtime);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_POSTER, posterUrl);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_SYNOPSIS, synopsis);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_AUDIENCESCORE, audienceScore);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_CRITICSCORE, criticsScore);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_DIRECTOR, directors);
            movieValues.put(MoviisContract.MovieEntry.COLUMN_RELEASEDATE, theaterReleaseDate);
            Uri movieUri = getContext().getContentResolver().insert(MoviisContract.MovieEntry.CONTENT_URI, movieValues);

            movie_id = MoviisContract.MovieEntry.getIdFromUri(movieUri);

        } catch (Exception e) {
            Log.e(LOG_TAG, "insertMovie " + e.getMessage(), e);
        }

        return movie_id;
    }

    private void insertMovieCast(Integer movie_id, JSONObject castJson) {
        Map<String,String> theCast = rottentomatoesAPI.getMovieCast(castJson);
        if(theCast != null && theCast.size() > 0) {

            Vector<ContentValues> castVector = new Vector<ContentValues>(theCast.size());
            Iterator keyIt = theCast.keySet().iterator();
            while(keyIt.hasNext()) {
                ContentValues characterValues = new ContentValues();
                String name = (String)keyIt.next();
                String character = theCast.get(name);

                characterValues.put(MoviisContract.CastEntry.COLUMN_MOVIE_KEY, movie_id);
                characterValues.put(MoviisContract.CastEntry.COLUMN_NAME, name);
                characterValues.put(MoviisContract.CastEntry.COLUMN_CHARACTER, character);
                castVector.add(characterValues);
            }

            if (castVector.size() > 0) {
                ContentValues[] castArray = new ContentValues[castVector.size()];
                castVector.toArray(castArray);
                int noInserted = getContext().getContentResolver().bulkInsert(MoviisContract.CastEntry.CONTENT_URI, castArray);
                Log.d(LOG_TAG, noInserted + " casts added");
            }

        }
    }

    private void insertMovieReviews(Integer movied_id, JSONObject movieJson) {
        try {

            JSONArray reviewJson = rottentomatoesAPI.getMovieReviewsJson(movieJson);
            Vector<ContentValues> reviewVector = new Vector<ContentValues>(reviewJson.length());
            for(int i = 0; i < reviewJson.length(); i++) {

                JSONObject jsonObj = (JSONObject)reviewJson.get(i);
                String critic = (String)jsonObj.get("critic");
                String date = (String)jsonObj.get("date");
                String publication = (String)jsonObj.get("publication");
                String originalScore = (String)jsonObj.opt("original_score");
                String quote = (String)jsonObj.get("quote");

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MoviisContract.ReviewEntry.COLUMN_MOVIE_KEY, movied_id);
                reviewValues.put(MoviisContract.ReviewEntry.COLUMN_CRITIC, critic);
                reviewValues.put(MoviisContract.ReviewEntry.COLUMN_PUBLICATION, publication);
                reviewValues.put(MoviisContract.ReviewEntry.COLUMN_DATE, date);
                reviewValues.put(MoviisContract.ReviewEntry.COLUMN_QUOTE, quote);
                reviewValues.put(MoviisContract.ReviewEntry.COLUMN_SCORE, originalScore == null? "": originalScore);
                reviewVector.add(reviewValues);
            }

            if (reviewVector.size() > 0) {
                ContentValues[] reviewArray = new ContentValues[reviewVector.size()];
                reviewVector.toArray(reviewArray);
                int noInserted = getContext().getContentResolver().bulkInsert(MoviisContract.ReviewEntry.CONTENT_URI, reviewArray);
                Log.d(LOG_TAG, noInserted + " reviews added");
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "getMovieReviews " + e.getMessage(), e);
        }
    }

    ArrayList<String> getCinemaRssFeed(String cinema) {
        ArrayList<String> movieTitles = new ArrayList<String>();
        try {
            String url = "";
            if(cinema.equalsIgnoreCase(getContext().getString(R.string.pref_cinema_abuja))) {
                url = ABUJA_URL;
            } else if(cinema.equalsIgnoreCase(getContext().getString(R.string.pref_cinema_lagos))) {
                url = LAGOS_URL;
            } else if(cinema.equalsIgnoreCase(getContext().getString(R.string.pref_cinema_ikeja))) {
                url = IKEJA_URL;
            } else if(cinema.equalsIgnoreCase(getContext().getString(R.string.pref_cinema_sec_abuja))) {
                url = SEC_ABUJA_URL;
            } else if(cinema.equalsIgnoreCase(getContext().getString(R.string.pref_cinema_uyo))) {
                url = UYO_URL;
            } else if(cinema.equalsIgnoreCase(getContext().getString(R.string.pref_cinema_warri))) {
                url = WARRI_URL;
            }

            Log.d(LOG_TAG, "url: " + url);
            java.net.URL movieUrl = new URL(url);

            ArrayList<MovieItem> movieList = RssReader.read(movieUrl);
            for(MovieItem movieItem : movieList) {
                String title = movieItem.getTitle();
                Log.i("RSS Reader", title);

                if(title.contains("(")) {
                    title = title.substring(0,title.indexOf("("));
                    title = title.trim();
                }
                if(title.contains("3D")) {
                    title.replace("3D", "");
                    title = title.trim();
                }
                if(title.contains("2D")) {
                    title.replace("2D", "");
                    title = title.trim();
                }
                if(title.lastIndexOf(":") == title.length()-1) {
                    title = title.substring(0,title.indexOf(":"));
                    title = title.trim();
                }
                //Log.d(LOG_TAG, "Formated title: " + title + "-");
                movieTitles.add(title);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            //if movies cannot be loaded from rss, get currently in theatre movies from Rottentomatoes
            movieTitles = rottentomatoesAPI.getCurrentlyInTheatre();
        }

        return movieTitles;
    }

    private void notifyMovie(String cinema, String movieTitle, String mpaaRating) {
        Context context = getContext();

        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

                int iconId = R.drawable.ic_launcher;
                String title = context.getString(R.string.app_name);

                String contentText = String.format(context.getString(R.string.format_notification),
                        movieTitle, mpaaRating, cinema);

                //Log.d(LOG_TAG, "notification: " + contentText);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getContext())
                                .setSmallIcon(iconId)
                                .setContentTitle(title)
                                .setContentText(contentText);

                Intent resultIntent = new Intent(context, MainActivity.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                                0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(MOVIIS_NOTIFICATION_ID, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.apply();
            }

        }

    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
        */
        ContentResolver.addPeriodicSync(account,authority, new Bundle(), syncInterval);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        SilverbirdmoviisSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
