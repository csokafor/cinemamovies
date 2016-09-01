package com.chinedusokafor.silverbirdmoviis;

/**
 * Created by cokafor on 1/20/2015.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.CinemaEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.MovieEntry;
import com.chinedusokafor.silverbirdmoviis.sync.SilverbirdmoviisSyncAdapter;

import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MainFragment.class.getSimpleName();

    private MovieAdapter movieAdapter;
    private static final int MOVIE_LOADER = 0;
    private String cinema;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    private static final String[] MOVIE_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying. On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIEID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_GENRE,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_MPAARATING,
            CinemaEntry.COLUMN_CINEMA_NAME
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIEID = 1;
    public static final int COL_MOVIE_TITLE = 2;
    public static final int COL_MOVIE_GENRE = 3;
    public static final int COL_MOVIE_RUNTIME = 4;
    public static final int COL_MOVIE_MPAARATING = 5;
    public static final int COL_CINEMA_NAME = 6;

    public MainFragment() {
    }

    public interface Callback {
        public void onItemSelected(Integer movie_id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        cinema = prefs.getString(getString(R.string.pref_cinema_key),
                getString(R.string.pref_cinema_ikeja));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //updateMovies();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String prefCinema = prefs.getString(getString(R.string.pref_cinema_key),
                getString(R.string.pref_cinema_ikeja));
        if (cinema != null && !cinema.equals(prefCinema)) {
            Log.d(LOG_TAG, "MainFragment restart movie loader " + prefCinema);
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
           outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.mainfragment, menu);
        //super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        movieAdapter = new MovieAdapter(getActivity(), null, 0);

        mListView = (ListView)rootView.findViewById(R.id.listview_movie);
        mListView.setAdapter(movieAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = movieAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {

                    ((Callback)getActivity())
                            .onItemSelected(cursor.getInt(COL_MOVIE_ID));
                    mPosition = position;
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMovies() {
        /*
        UpdateMovieTask updateMovieTask = new UpdateMovieTask(this.getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String cinema = prefs.getString(getString(R.string.pref_cinema_key),
                getString(R.string.pref_cinema_lagos));
        Log.d(LOG_TAG, "updateMovies() cinema: " + cinema);
        updateMovieTask.execute(cinema);
        */
        SilverbirdmoviisSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Sort order: Ascending, by title.
        String sortOrder = MovieEntry.COLUMN_TITLE + " ASC";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String cinema = prefs.getString(getString(R.string.pref_cinema_key),
                getString(R.string.pref_cinema_ikeja));

        Uri movieForCinemaUri = MovieEntry.buildCinemaMovie(cinema);
        Log.d(LOG_TAG, "onCreateLoader() movieForCinemaUri: " + movieForCinemaUri);
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                movieForCinemaUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        movieAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        movieAdapter.swapCursor(null);
    }


}
