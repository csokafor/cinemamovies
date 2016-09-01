package com.chinedusokafor.silverbirdmoviis.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by cokafor on 1/17/2015.
 */
public class MoviisProvider extends ContentProvider {
    private static final String LOG_TAG = MoviisProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int CINEMA = 100;
    private static final int CINEMA_WITH_NAME = 101;
    private static final int MOVIE = 200;
    private static final int MOVIE_WITH_CINEMA = 201;
    private static final int MOVIE_ID = 202;
    private static final int MOVIE_WITH_CINEMA_AND_DATE = 203;
    private static final int MOVIE_WITH_CINEMA_AND_MOVIE_ID = 204;
    private static final int CAST = 300;
    private static final int CAST_WITH_MOVIE = 301;
    private static final int REVIEW = 400;
    private static final int REVIEW_WITH_MOVIE = 401;

    private MoviisDatabaseHelper moviisDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviisContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviisContract.PATH_CINEMA, CINEMA);
        matcher.addURI(authority, MoviisContract.PATH_CINEMA + "/*", CINEMA_WITH_NAME);

        matcher.addURI(authority, MoviisContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MoviisContract.PATH_MOVIE + "/#", MOVIE_ID);
        matcher.addURI(authority, MoviisContract.PATH_MOVIE + "/*", MOVIE_WITH_CINEMA);
        matcher.addURI(authority, MoviisContract.PATH_MOVIE + "/*/*", MOVIE_WITH_CINEMA_AND_DATE);
        matcher.addURI(authority, MoviisContract.PATH_MOVIE + "/*/#", MOVIE_WITH_CINEMA_AND_MOVIE_ID);

        matcher.addURI(authority, MoviisContract.PATH_CAST, CAST);
        matcher.addURI(authority, MoviisContract.PATH_CAST + "/#", CAST_WITH_MOVIE);

        matcher.addURI(authority, MoviisContract.PATH_REVIEW, REVIEW);
        matcher.addURI(authority, MoviisContract.PATH_REVIEW + "/#", REVIEW_WITH_MOVIE);

        return matcher;
    }

    private static final SQLiteQueryBuilder movieByCinemaBuilder;
    private static final SQLiteQueryBuilder castByMovieBuilder;
    private static final SQLiteQueryBuilder reviewByMovieBuilder;
    static{
        movieByCinemaBuilder = new SQLiteQueryBuilder();
        movieByCinemaBuilder.setTables(MoviisContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                MoviisContract.CinemaEntry.TABLE_NAME + " ON " +
                MoviisContract.MovieEntry.TABLE_NAME + "." + MoviisContract.MovieEntry.COLUMN_CINEMA_KEY +
                " = " + MoviisContract.CinemaEntry.TABLE_NAME + "." + MoviisContract.CinemaEntry._ID);

        castByMovieBuilder = new SQLiteQueryBuilder();
        castByMovieBuilder.setTables(MoviisContract.CastEntry.TABLE_NAME + " INNER JOIN " +
                MoviisContract.MovieEntry.TABLE_NAME + " ON " +
                MoviisContract.CastEntry.TABLE_NAME + "." + MoviisContract.CastEntry.COLUMN_MOVIE_KEY +
                " = " + MoviisContract.MovieEntry.TABLE_NAME + "." + MoviisContract.MovieEntry._ID);

        reviewByMovieBuilder = new SQLiteQueryBuilder();
        reviewByMovieBuilder.setTables(MoviisContract.ReviewEntry.TABLE_NAME + " INNER JOIN " +
                MoviisContract.MovieEntry.TABLE_NAME + " ON " +
                MoviisContract.ReviewEntry.TABLE_NAME + "." + MoviisContract.ReviewEntry.COLUMN_MOVIE_KEY +
                " = " + MoviisContract.MovieEntry.TABLE_NAME + "." + MoviisContract.MovieEntry._ID);
    }

    private static final String withCinemaSelection =
            MoviisContract.CinemaEntry.TABLE_NAME+
                    "." + MoviisContract.CinemaEntry.COLUMN_CINEMA_NAME + " = ? ";

    private static final String withCinemaAndDateSelection =
            MoviisContract.CinemaEntry.TABLE_NAME+
                    "." + MoviisContract.CinemaEntry.COLUMN_CINEMA_NAME + " = ? AND " +
            MoviisContract.MovieEntry.COLUMN_RELEASEDATE + " <= ?";

    private static final String withCinemaAndMovieIdSelection =
            MoviisContract.CinemaEntry.TABLE_NAME +
                    "." + MoviisContract.CinemaEntry.COLUMN_CINEMA_NAME + " = ? AND " +
                    MoviisContract.MovieEntry.COLUMN_MOVIEID + " = ?";

    private Cursor getMovieByCinema(Uri uri, String[] projection, String sortOrder) {
        String cinema = MoviisContract.MovieEntry.getCinemaFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = withCinemaSelection;
        selectionArgs = new String[]{cinema};
        Cursor movieCursor = movieByCinemaBuilder.query(moviisDbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);

        return movieCursor;
    }

    private Cursor getMovieByCinemaAndDate(Uri uri, String[] projection, String sortOrder) {
        String cinema = MoviisContract.MovieEntry.getCinemaFromUri(uri);
        String releaseDate = MoviisContract.MovieEntry.getReleaseDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = withCinemaAndDateSelection;
        selectionArgs = new String[]{cinema, releaseDate};

        return movieByCinemaBuilder.query(moviisDbHelper.getReadableDatabase(),
                projection, selection, selectionArgs,
                null, null, sortOrder
        );
    }

    private Cursor getMovieByCinemaAndMovieId(Uri uri, String[] projection, String sortOrder) {
        String cinema = MoviisContract.MovieEntry.getCinemaFromUri(uri);
        String movie_id = MoviisContract.MovieEntry.getMovieIdFromUri(uri);
        Log.d(LOG_TAG, "getMovieByCinemaAndMovieId cinema:" + cinema + " moviedId:" + movie_id);
        String[] selectionArgs;
        String selection;

        selection = withCinemaAndMovieIdSelection;
        selectionArgs = new String[]{cinema, movie_id};

        return movieByCinemaBuilder.query(moviisDbHelper.getReadableDatabase(),
                projection, selection, selectionArgs,
                null, null, sortOrder
        );
    }


    private Cursor getCinemaByName(Uri uri, String[] projection, String sortOrder) {
        String cinemaName = MoviisContract.CinemaEntry.getCinemaNameFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = withCinemaSelection;
        selectionArgs = new String[]{cinemaName};

        return moviisDbHelper.getReadableDatabase().query(
                MoviisContract.CinemaEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

    }

    @Override
    public boolean onCreate() {
        moviisDbHelper = new MoviisDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor = null;
        try {
            switch (sUriMatcher.match(uri)) {
                case MOVIE_WITH_CINEMA: {
                    retCursor = getMovieByCinema(uri, projection, sortOrder);
                    break;
                }
                case MOVIE_WITH_CINEMA_AND_DATE: {
                    retCursor = getMovieByCinemaAndDate(uri, projection, sortOrder);
                    break;
                }
                case MOVIE_WITH_CINEMA_AND_MOVIE_ID: {
                    retCursor = getMovieByCinemaAndMovieId(uri, projection, sortOrder);
                    break;
                }
                case CAST_WITH_MOVIE: {
                    retCursor = moviisDbHelper.getReadableDatabase().query(
                            MoviisContract.CastEntry.TABLE_NAME, projection,
                            MoviisContract.CastEntry.COLUMN_MOVIE_KEY + " = '" +
                                    MoviisContract.CastEntry.getMovieFromUri(uri)+ "'",
                            null, null, null, sortOrder
                    );
                    break;
                }
                case REVIEW_WITH_MOVIE: {
                    retCursor = moviisDbHelper.getReadableDatabase().query(
                            MoviisContract.ReviewEntry.TABLE_NAME, projection,
                            MoviisContract.ReviewEntry.COLUMN_MOVIE_KEY + " = '" +
                                    MoviisContract.ReviewEntry.getMovieFromUri(uri) + "'",
                            null, null, null, sortOrder
                    );
                    break;
                }
                case CINEMA_WITH_NAME: {
                    retCursor = getCinemaByName(uri, projection, sortOrder);
                    break;
                }
                case MOVIE_ID: {
                    retCursor = moviisDbHelper.getReadableDatabase().query(
                            MoviisContract.MovieEntry.TABLE_NAME,
                            projection,
                            MoviisContract.MovieEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                            null, null, null, sortOrder
                    );
                    break;
                }
                case CINEMA: {
                    retCursor = moviisDbHelper.getReadableDatabase().query(
                            MoviisContract.CinemaEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder
                    );
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (Exception e) {
            Log.e(LOG_TAG, "MoviisProvider query error" + e.getMessage(), e );
        }
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_WITH_CINEMA:
                return MoviisContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE:
                return MoviisContract.MovieEntry.CONTENT_TYPE;
            case CINEMA:
                return MoviisContract.CinemaEntry.CONTENT_TYPE;
            case CAST:
                return MoviisContract.CastEntry.CONTENT_TYPE;
            case CAST_WITH_MOVIE:
                return MoviisContract.CastEntry.CONTENT_ITEM_TYPE;
            case REVIEW:
                return MoviisContract.CastEntry.CONTENT_TYPE;
            case REVIEW_WITH_MOVIE:
                return MoviisContract.CastEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = moviisDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CINEMA: {
                long _id = db.insert(MoviisContract.CinemaEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviisContract.CinemaEntry.buildCinemaUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIE: {
                long _id = db.insert(MoviisContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviisContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CAST: {
                long _id = db.insert(MoviisContract.CastEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviisContract.CastEntry.buildCastUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW: {
                long _id = db.insert(MoviisContract.ReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviisContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = moviisDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case CINEMA:
                rowsDeleted = db.delete(
                        MoviisContract.CinemaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE:
                rowsDeleted = db.delete(
                        MoviisContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CAST:
                rowsDeleted = db.delete(
                        MoviisContract.CastEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(
                        MoviisContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = moviisDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case CINEMA:
                rowsUpdated = db.update(MoviisContract.CinemaEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MOVIE:
                rowsUpdated = db.update(MoviisContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CAST:
                rowsUpdated = db.update(MoviisContract.CastEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MoviisContract.ReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = moviisDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviisContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case CAST:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviisContract.CastEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case REVIEW:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviisContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
