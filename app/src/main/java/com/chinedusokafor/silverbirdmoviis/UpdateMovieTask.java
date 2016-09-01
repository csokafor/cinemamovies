package com.chinedusokafor.silverbirdmoviis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.CastEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.CinemaEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.MovieEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.ReviewEntry;
import com.chinedusokafor.silverbirdmoviis.rottentomatoes.RottentomatoesAPI;
import com.chinedusokafor.silverbirdmoviis.rss.MovieItem;
import com.chinedusokafor.silverbirdmoviis.rss.RssReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * Created by cokafor on 1/20/2015.
 */
public class UpdateMovieTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = UpdateMovieTask.class.getSimpleName();
    private final Context mContext;

    private static final String WARRI_URL = "http://silverbirdcinemas.com/warri?format=feed&type=rss";
    private static final String UYO_URL = "http://silverbirdcinemas.com/uyo?format=feed&type=rss";
    private static final String ABUJA_URL = "http://silverbirdcinemas.com/abuja?format=feed&type=rss";
    private static final String LAGOS_URL = "http://silverbirdcinemas.com/lagos?format=feed&type=rss";
    private static final String IKEJA_URL = "http://silverbirdcinemas.com/ikeja?format=feed&type=rss";
    private static final String SEC_ABUJA_URL = "http://silverbirdcinemas.com/sec-abuja?format=feed&type=rss";

    private static final String[] CINEMA_COLUMNS = {
            CinemaEntry.TABLE_NAME + "." + CinemaEntry._ID,
            CinemaEntry.COLUMN_CINEMA_NAME
    };

    private RottentomatoesAPI rottentomatoesAPI = new RottentomatoesAPI();

    public UpdateMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        String cinema = params[0];
        ArrayList<String> movieTitles = getCinemaRssFeed(cinema);

        try {
            //get cinema id
            Uri cinemaUri = CinemaEntry.buildCinemaName(cinema);
            Cursor cinemaCursor = mContext.getContentResolver().query(
                    cinemaUri, CINEMA_COLUMNS, null, null, "");
            cinemaCursor.moveToFirst();
            Integer cinema_id = cinemaCursor.getInt(0);
            Log.d(LOG_TAG, "cinema Id:" + cinema_id);

            for(String movieTitle : movieTitles) {
                JSONObject searchJson = rottentomatoesAPI.searchForMovie(movieTitle);
                if(searchJson != null) {
                    String movieId = (String)searchJson.get("id");
                    Log.d(LOG_TAG, "movieId: " + movieId);

                    String movieUrl = rottentomatoesAPI.getMovieJsonUrl(searchJson);
                    Log.d(LOG_TAG, "movieJson: " + movieUrl);
                    JSONObject theMovieJson = rottentomatoesAPI.getMovieJson(movieUrl);

                    Integer movie_id = insertMovie(cinema_id, movieTitle, theMovieJson);
                    Log.d(LOG_TAG, "Db movie Id: " + movie_id);
                    insertMovieCast(movie_id, theMovieJson);
                    insertMovieReviews(movie_id, theMovieJson);

                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "doInBackground " + e.getMessage(), e);
        }

        return null;
    }

    private Integer insertMovie(Integer cinema_id, String movieTitle, JSONObject movieJson) {
        Integer movie_id = 0;
        try {
            Integer movieId = (Integer)movieJson.get("id");
            Log.d(LOG_TAG, "insertMovie movieId: " + movieId);

            String synopsis = (String) movieJson.get("synopsis");
            //Log.d(LOG_TAG,"synopsis: " + synopsis);
            String mpaa_rating = (String) movieJson.get("mpaa_rating");
            Log.d(LOG_TAG,"mpaa_rating: " + mpaa_rating);
            Integer runtime = (Integer) movieJson.get("runtime");
            Log.d(LOG_TAG,"runtime: " + runtime);

            String genres = rottentomatoesAPI.getMovieGenres(movieJson);
            Log.d(LOG_TAG,"genres: " + genres);

            JSONObject posterUrls = (JSONObject) movieJson.get("posters");
            String posterUrl = (String) posterUrls.get("detailed");
            Log.d(LOG_TAG,"posterUrl: " + posterUrl);

            JSONObject ratings = (JSONObject) movieJson.get("ratings");
            Integer criticsScore = (Integer) ratings.get("critics_score");
            Integer audienceScore = (Integer) ratings.get("audience_score");
            //Log.d(LOG_TAG,"criticsScore: " + criticsScore);
            //Log.d(LOG_TAG,"audienceScore: " + audienceScore);

            JSONObject releaseDate = (JSONObject) movieJson.get("release_dates");
            String theaterReleaseDate = (String) releaseDate.get("theater");
            //Log.d(LOG_TAG,"theaterReleaseDate: " + theaterReleaseDate);

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
            movieValues.put(MovieEntry.COLUMN_CINEMA_KEY, cinema_id);
            movieValues.put(MovieEntry.COLUMN_MOVIEID, movieId);
            movieValues.put(MovieEntry.COLUMN_TITLE, movieTitle);
            movieValues.put(MovieEntry.COLUMN_GENRE, genres);
            movieValues.put(MovieEntry.COLUMN_MPAARATING, mpaa_rating);
            movieValues.put(MovieEntry.COLUMN_RUNTIME, runtime);
            movieValues.put(MovieEntry.COLUMN_POSTER, posterUrl);
            movieValues.put(MovieEntry.COLUMN_SYNOPSIS, synopsis);
            movieValues.put(MovieEntry.COLUMN_AUDIENCESCORE, audienceScore);
            movieValues.put(MovieEntry.COLUMN_CRITICSCORE, criticsScore);
            movieValues.put(MovieEntry.COLUMN_DIRECTOR, directors);
            movieValues.put(MovieEntry.COLUMN_RELEASEDATE, theaterReleaseDate);
            Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);

            movie_id = MovieEntry.getIdFromUri(movieUri);

        } catch (Exception e) {
            Log.e(LOG_TAG, "insertMovie " + e.getMessage(), e);
        }

        return movie_id;
    }

    private void insertMovieCast(Integer movie_id, JSONObject castJson) {
        Map<String,String> theCast = rottentomatoesAPI.getMovieCast(castJson);
        if(theCast != null && theCast.size() > 0) {
            Log.d(LOG_TAG, "Characters");

            Vector<ContentValues> castVector = new Vector<ContentValues>(theCast.size());
            Iterator keyIt = theCast.keySet().iterator();
            while(keyIt.hasNext()) {
                ContentValues characterValues = new ContentValues();
                String name = (String)keyIt.next();
                String character = theCast.get(name);
                Log.d(LOG_TAG, name + ": " + character);
                characterValues.put(CastEntry.COLUMN_MOVIE_KEY, movie_id);
                characterValues.put(CastEntry.COLUMN_NAME, name);
                characterValues.put(CastEntry.COLUMN_CHARACTER, character);
                castVector.add(characterValues);
            }

            if (castVector.size() > 0) {
                ContentValues[] castArray = new ContentValues[castVector.size()];
                castVector.toArray(castArray);
                int noInserted = mContext.getContentResolver().bulkInsert(CastEntry.CONTENT_URI, castArray);
                Log.d(LOG_TAG, noInserted + " casts added");
            }

        }
    }

    private void insertMovieReviews(Integer movied_id, JSONObject movieJson) {
        try {

            JSONArray reviewJson = rottentomatoesAPI.getMovieReviewsJson(movieJson);
            Vector<ContentValues> reviewVector = new Vector<ContentValues>(reviewJson.length());
            for(int i = 0; i < reviewJson.length(); i++) {
                System.out.println("Review: " + i);
                JSONObject jsonObj = (JSONObject)reviewJson.get(i);
                String critic = (String)jsonObj.get("critic");
                String date = (String)jsonObj.get("date");
                String publication = (String)jsonObj.get("publication");
                String originalScore = (String)jsonObj.opt("original_score");
                String quote = (String)jsonObj.get("quote");
                Log.d(LOG_TAG, "critic: " + critic);

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(ReviewEntry.COLUMN_MOVIE_KEY, movied_id);
                reviewValues.put(ReviewEntry.COLUMN_CRITIC, critic);
                reviewValues.put(ReviewEntry.COLUMN_PUBLICATION, publication);
                reviewValues.put(ReviewEntry.COLUMN_DATE, date);
                reviewValues.put(ReviewEntry.COLUMN_QUOTE, quote);
                reviewValues.put(ReviewEntry.COLUMN_SCORE, originalScore == null? "": originalScore);
                reviewVector.add(reviewValues);
            }

            if (reviewVector.size() > 0) {
                ContentValues[] reviewArray = new ContentValues[reviewVector.size()];
                reviewVector.toArray(reviewArray);
                int noInserted = mContext.getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI, reviewArray);
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
            if(cinema.equalsIgnoreCase(mContext.getString(R.string.pref_cinema_abuja))) {
                url = ABUJA_URL;
            } else if(cinema.equalsIgnoreCase(mContext.getString(R.string.pref_cinema_lagos))) {
                url = LAGOS_URL;
            } else if(cinema.equalsIgnoreCase(mContext.getString(R.string.pref_cinema_ikeja))) {
                url = IKEJA_URL;
            } else if(cinema.equalsIgnoreCase(mContext.getString(R.string.pref_cinema_sec_abuja))) {
                url = SEC_ABUJA_URL;
            } else if(cinema.equalsIgnoreCase(mContext.getString(R.string.pref_cinema_uyo))) {
                url = UYO_URL;
            } else if(cinema.equalsIgnoreCase(mContext.getString(R.string.pref_cinema_warri))) {
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
                Log.d(LOG_TAG, "Formated title: " + title + "-");
                movieTitles.add(title);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return movieTitles;
    }
}
