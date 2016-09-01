package com.chinedusokafor.silverbirdmoviis.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.CastEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.CinemaEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.MovieEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.ReviewEntry;

/**
 * Created by cokafor on 1/17/2015.
 */
public class MoviisDatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "moviis.db";

    public MoviisDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create cinema table
        final String SQL_CREATE_CINEMA_TABLE = "CREATE TABLE " + CinemaEntry.TABLE_NAME + " (" +
                CinemaEntry._ID + " INTEGER PRIMARY KEY," +
                CinemaEntry.COLUMN_CINEMA_NAME + " TEXT UNIQUE NOT NULL, " +
                "UNIQUE (" + CinemaEntry.COLUMN_CINEMA_NAME +") ON CONFLICT IGNORE"+
                " );";

        final String SQL_CREATE_CAST_TABLE = "CREATE TABLE " + CastEntry.TABLE_NAME + " (" +
                CastEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CastEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                CastEntry.COLUMN_CHARACTER + " TEXT NOT NULL, " +
                CastEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +

                // movie_id column as a foreign key to movie table.
                "FOREIGN KEY (" + CastEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +

                "UNIQUE (" + CastEntry.COLUMN_MOVIE_KEY + "," + CastEntry.COLUMN_NAME +") ON CONFLICT REPLACE"+
                " );";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReviewEntry.COLUMN_CRITIC + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_PUBLICATION + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_QUOTE + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_SCORE + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +

                // movie_id column as a foreign key to movie table.
                "FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +

                "UNIQUE (" + ReviewEntry.COLUMN_MOVIE_KEY + "," + ReviewEntry.COLUMN_CRITIC +") ON CONFLICT REPLACE"+
                " );";

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieEntry.COLUMN_MOVIEID + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_GENRE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_MPAARATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASEDATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DIRECTOR + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RUNTIME + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_AUDIENCESCORE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_CRITICSCORE + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_CINEMA_KEY + " INTEGER NOT NULL, " +

                // movie_id column as a foreign key to movie table.
                "FOREIGN KEY (" + MovieEntry.COLUMN_CINEMA_KEY + ") REFERENCES " +
                CinemaEntry.TABLE_NAME + " (" + CinemaEntry._ID + "), " +

                "UNIQUE (" + MovieEntry.COLUMN_CINEMA_KEY + "," + MovieEntry.COLUMN_TITLE +") ON CONFLICT REPLACE"+
                " );";

        final String SQL_INSERT_LAGOS_CINEMA = "INSERT INTO cinema (" + CinemaEntry._ID + "," + CinemaEntry.COLUMN_CINEMA_NAME + ") " +
                "VALUES (1, 'lagos');";
        final String SQL_INSERT_IKEJA_CINEMA = "INSERT INTO cinema (" + CinemaEntry._ID + "," + CinemaEntry.COLUMN_CINEMA_NAME + ") " +
                "VALUES (2, 'ikeja');";
        final String SQL_INSERT_ABUJA_CINEMA = "INSERT INTO cinema (" + CinemaEntry._ID + "," + CinemaEntry.COLUMN_CINEMA_NAME + ") " +
                "VALUES (3, 'abuja');";
        final String SQL_INSERT_SECABUJA_CINEMA = "INSERT INTO cinema (" + CinemaEntry._ID + "," + CinemaEntry.COLUMN_CINEMA_NAME + ") " +
                "VALUES (4, 'sec-abuja');";
        final String SQL_INSERT_UYO_CINEMA = "INSERT INTO cinema (" + CinemaEntry._ID + "," + CinemaEntry.COLUMN_CINEMA_NAME + ") " +
                "VALUES (5, 'uyo');";
        final String SQL_INSERT_WARRI_CINEMA = "INSERT INTO cinema (" + CinemaEntry._ID + "," + CinemaEntry.COLUMN_CINEMA_NAME + ") " +
                "VALUES (6, 'warri');";

        db.execSQL(SQL_CREATE_CINEMA_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_CAST_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);

        db.execSQL(SQL_INSERT_LAGOS_CINEMA);
        db.execSQL(SQL_INSERT_IKEJA_CINEMA);
        db.execSQL(SQL_INSERT_ABUJA_CINEMA);
        db.execSQL(SQL_INSERT_SECABUJA_CINEMA);
        db.execSQL(SQL_INSERT_UYO_CINEMA);
        db.execSQL(SQL_INSERT_WARRI_CINEMA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CastEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CinemaEntry.TABLE_NAME);
        onCreate(db);
    }
}
