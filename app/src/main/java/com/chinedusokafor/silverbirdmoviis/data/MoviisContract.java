package com.chinedusokafor.silverbirdmoviis.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cokafor on 1/17/2015.
 */
public class MoviisContract {

    public static final String CONTENT_AUTHORITY = "com.chinedusokafor.silverbirdmoviis";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_CINEMA = "cinema";
    public static final String PATH_CAST = "cast";
    public static final String PATH_REVIEW = "review";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDbDateString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    public static final class CinemaEntry implements BaseColumns {
        public static final String TABLE_NAME = "cinema";
        public static final String COLUMN_CINEMA_NAME = "cinema_name";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CINEMA).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CINEMA;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CINEMA;

        public static Uri buildCinemaUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCinemaName(String name) {
            return CONTENT_URI.buildUpon().appendPath(name).build();
        }

        public static String getCinemaNameFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class CastEntry implements BaseColumns {
        public static final String TABLE_NAME = "moviecast";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CHARACTER = "character";
        // Column with the foreign key into the movie table.
        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAST).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CAST;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CAST;

        public static Uri buildCastUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieCast(String movie) {
            return CONTENT_URI.buildUpon().appendPath(movie).build();
        }

        public static String getMovieFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Integer getIdFromUri(Uri uri) {
            Integer id = new Integer(uri.getPathSegments().get(1));
            return id;
        }
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";
        public static final String COLUMN_CRITIC = "critic";
        public static final String COLUMN_PUBLICATION = "publication";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_QUOTE = "quote";
        public static final String COLUMN_DATE = "date";
        // Column with the foreign key into the movie table.
        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieReview(String movie) {
            return CONTENT_URI.buildUpon().appendPath(movie).build();
        }

        public static String getMovieFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Integer getIdFromUri(Uri uri) {
            Integer id = new Integer(uri.getPathSegments().get(1));
            return id;
        }
    }

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_MOVIEID = "movieid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_MPAARATING = "mpaarating";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_RELEASEDATE = "releasedate";
        public static final String COLUMN_DIRECTOR = "director";
        public static final String COLUMN_CRITICSCORE = "criticscore";
        public static final String COLUMN_AUDIENCESCORE = "audiencescore";

        // Column with the foreign key into the cinema table.
        public static final String COLUMN_CINEMA_KEY = "cinema_id";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCinemaMovie(String cinema) {
            return CONTENT_URI.buildUpon().appendPath(cinema).build();
        }

        public static Uri buildCinemaAndDateMovie(String cinema, String releaseDate) {
            return CONTENT_URI.buildUpon().appendPath(cinema).appendPath(releaseDate).build();
        }

        public static Uri buildCinemaAndMovieId(String cinema, String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(cinema).appendPath(movie_id).build();
        }

        public static Integer getIdFromUri(Uri uri) {
            Integer id = new Integer(uri.getPathSegments().get(1));
            return id;
        }

        public static String getCinemaFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getReleaseDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}
