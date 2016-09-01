package com.chinedusokafor.silverbirdmoviis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.chinedusokafor.silverbirdmoviis.data.MoviisContract;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.MovieEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.ReviewEntry;
import com.chinedusokafor.silverbirdmoviis.data.MoviisContract.CastEntry;

import java.io.InputStream;

/**
 * Created by cokafor on 1/24/2015.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    public static final String MOVIE_ID = "movie_id";
    private static final String MOVIE_SHARE_HASHTAG = " #SilverbirdmoviisApp";

    private ShareActionProvider mShareActionProvider;
    private int movie_id;
    private String movieShare;

    private static final int MOVIE_LOADER = 0;
    private static final int CAST_LOADER = 1;
    private static final int REVIEW_LOADER = 2;

    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_MOVIEID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_SYNOPSIS,
            MovieEntry.COLUMN_GENRE,
            MovieEntry.COLUMN_MPAARATING,
            MovieEntry.COLUMN_POSTER,
            MovieEntry.COLUMN_RELEASEDATE,
            MovieEntry.COLUMN_DIRECTOR,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_AUDIENCESCORE,
            MovieEntry.COLUMN_CRITICSCORE
    };

    private static final String[] CAST_COLUMNS = {
            CastEntry.TABLE_NAME + "." + CastEntry._ID,
            CastEntry.COLUMN_NAME,
            CastEntry.COLUMN_CHARACTER,
            CastEntry.COLUMN_MOVIE_KEY
    };

    private static final String[] REVIEW_COLUMNS = {
            ReviewEntry.TABLE_NAME + "." + ReviewEntry._ID,
            ReviewEntry.COLUMN_CRITIC,
            ReviewEntry.COLUMN_PUBLICATION,
            ReviewEntry.COLUMN_QUOTE,
            ReviewEntry.COLUMN_SCORE,
            ReviewEntry.COLUMN_DATE
    };

    private SimpleCursorAdapter castCursorAdapter;
    private SimpleCursorAdapter reviewCursorAdapter;

    private ImageView posterView;
    private TextView movieTitleView;
    private TextView movieGenreView;
    private TextView movieRatingView;
    private TextView movieLengthView;
    private TextView movieSynopsisView;
    private TextView movieCriticScoreView;
    private TextView movieAudienceScoreView;
    private TextView movieDirectorView;
    private ListView castListView;
    private ListView reviewListView;

    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            movie_id = arguments.getInt(MovieDetailActivity.MOVIE_ID);
        }

        castCursorAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_item_cast, null,
                // the column names to use to fill the textviews
                new String[]{CastEntry.COLUMN_NAME,
                        CastEntry.COLUMN_CHARACTER
                },
                // the textviews to fill with the data pulled from the columns above
                new int[]{R.id.list_item_cast_name_textview,
                        R.id.list_item_cast_character_textview
                },0
        );

        reviewCursorAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_item_review, null,
                // the column names to use to fill the textviews
                new String[]{ReviewEntry.COLUMN_CRITIC,
                        ReviewEntry.COLUMN_PUBLICATION,
                        ReviewEntry.COLUMN_QUOTE,
                        ReviewEntry.COLUMN_SCORE,
                        ReviewEntry.COLUMN_DATE
                },
                // the textviews to fill with the data pulled from the columns above
                new int[]{R.id.list_item_critic_textview,
                        R.id.list_item_publication_textview,
                        R.id.list_item_quote_textview,
                        R.id.list_item_score_textview,
                        R.id.list_item_date_textview
                },0
        );

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        posterView = (ImageView) rootView.findViewById(R.id.movie_poster_imageview);
        movieTitleView = (TextView) rootView.findViewById(R.id.movie_title_textview);
        movieGenreView = (TextView) rootView.findViewById(R.id.movie_genre_textview);
        movieRatingView = (TextView) rootView.findViewById(R.id.movie_rating_textview);
        movieLengthView = (TextView) rootView.findViewById(R.id.movie_length_textview);
        movieSynopsisView = (TextView) rootView.findViewById(R.id.movie_synopsis_textview);
        movieCriticScoreView = (TextView) rootView.findViewById(R.id.movie_criticscore_textview);
        movieAudienceScoreView = (TextView) rootView.findViewById(R.id.movie_audiencescore_textview);
        movieDirectorView = (TextView) rootView.findViewById(R.id.movie_director_textview);
        castListView = (ListView) rootView.findViewById(R.id.listview_cast);
        castListView.setAdapter(castCursorAdapter);
        reviewListView = (ListView) rootView.findViewById(R.id.listview_review);
        reviewListView.setAdapter(reviewCursorAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getActivity().getIntent();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(MovieDetailActivity.MOVIE_ID)) {
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
            getLoaderManager().restartLoader(CAST_LOADER, null, this);
            getLoaderManager().restartLoader(REVIEW_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.detailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (movieShare != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, movieShare + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(MovieDetailActivity.MOVIE_ID)) {
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
            getLoaderManager().initLoader(CAST_LOADER, null, this);
            getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> cursorLoader = null;
        switch (id) {
            case MOVIE_LOADER: {
                Uri movieUri = MovieEntry.buildMovieUri(movie_id);
                cursorLoader = new CursorLoader(getActivity(), movieUri, MOVIE_COLUMNS,
                        null, null, "");
                break;
            }
            case CAST_LOADER: {
                Uri castUri = CastEntry.buildMovieCast(new Integer(movie_id).toString());
                cursorLoader = new CursorLoader(getActivity(), castUri, CAST_COLUMNS,
                       null, null, "");
                break;
            }
            case REVIEW_LOADER: {
                Uri reviewUri = ReviewEntry.buildMovieReview(new Integer(movie_id).toString());
                cursorLoader = new CursorLoader(getActivity(), reviewUri, REVIEW_COLUMNS,
                        null, null, "");
                break;
            }
            default:
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MOVIE_LOADER: {
                //Log.d(LOG_TAG, "onLoadFinished: cursor count" + data.getCount());
                if (data != null && data.moveToFirst()) {
                    String posterUrl = data.getString(data.getColumnIndex(MovieEntry.COLUMN_POSTER));
                    new ImageDownloader(posterView).execute(posterUrl);

                    String movieTitle = data.getString(data.getColumnIndex(MovieEntry.COLUMN_TITLE));
                    movieTitleView.setText(movieTitle);
                    String genre = data.getString(data.getColumnIndex(MovieEntry.COLUMN_GENRE));
                    movieGenreView.setText(genre);
                    String rating = data.getString(data.getColumnIndex(MovieEntry.COLUMN_MPAARATING));
                    movieRatingView.setText(rating);
                    int runtime = data.getInt(data.getColumnIndex(MovieEntry.COLUMN_RUNTIME));
                    movieLengthView.setText(runtime + " " + getActivity().getString(R.string.mins));
                    String synopsis = data.getString(data.getColumnIndex(MovieEntry.COLUMN_SYNOPSIS));
                    movieSynopsisView.setText(synopsis);
                    int criticScore = data.getInt(data.getColumnIndex(MovieEntry.COLUMN_CRITICSCORE));
                    movieCriticScoreView.setText(getActivity().getString(R.string.critics) + " " + criticScore);
                    int audienceScore = data.getInt(data.getColumnIndex(MovieEntry.COLUMN_AUDIENCESCORE));
                    movieAudienceScoreView.setText(getActivity().getString(R.string.audience) + " " + audienceScore);

                    String director = data.getString(data.getColumnIndex(MovieEntry.COLUMN_DIRECTOR));
                    movieDirectorView.setText(director);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String cinema = prefs.getString(getString(R.string.pref_cinema_key),
                            getString(R.string.pref_cinema_lagos));

                    movieShare = String.format(getActivity().getString(R.string.movie_share),
                            movieTitle, rating, cinema);

                    //Log.v(LOG_TAG, "Movie share String: " + movieShare);
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareForecastIntent());
                    }
                }
                break;
            }
            case CAST_LOADER: {
                //Log.d(LOG_TAG, "onLoadFinished: cast cursor count" + data.getCount());
                castCursorAdapter.swapCursor(data);
                setListViewHeightBasedOnChildren(castListView);
                break;
            }
            case REVIEW_LOADER: {
                //Log.d(LOG_TAG, "onLoadFinished: review cursor count" + data.getCount());
                reviewCursorAdapter.swapCursor(data);
                setListViewHeightBasedOnChildren(reviewListView);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        castCursorAdapter.swapCursor(null);
        reviewCursorAdapter.swapCursor(null);
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownloader(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
