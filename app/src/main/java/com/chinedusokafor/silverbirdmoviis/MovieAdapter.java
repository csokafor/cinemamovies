package com.chinedusokafor.silverbirdmoviis;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinedusokafor.silverbirdmoviis.data.MoviisContract;

/**
 * Created by cokafor on 1/22/2015.
 */
public class MovieAdapter extends CursorAdapter {

    public static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public static class ViewHolder {
        public final TextView titleView;
        public final TextView genreView;
        public final TextView mpaaratingView;
        public final TextView durationView;
        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
            genreView = (TextView) view.findViewById(R.id.list_item_genre_textview);
            mpaaratingView = (TextView) view.findViewById(R.id.list_item_mpaarating_textview);
            durationView = (TextView) view.findViewById(R.id.list_item_duration_textview);
        }
    }

    public MovieAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Log.d(LOG_TAG, "Cursor position: " + cursor.getPosition());
        int layoutId = R.layout.list_item_movie;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Integer runtime = cursor.getInt(MainFragment.COL_MOVIE_RUNTIME);
        String title = cursor.getString(MainFragment.COL_MOVIE_TITLE);
        String mpaarating = cursor.getString(MainFragment.COL_MOVIE_MPAARATING);
        String genre = cursor.getString(MainFragment.COL_MOVIE_GENRE);
        //Log.d(LOG_TAG, "title: " + title);

        viewHolder.titleView.setText(title);
        viewHolder.durationView.setText(runtime.toString() + " " + context.getString(R.string.mins));
        viewHolder.mpaaratingView.setText(mpaarating);
        viewHolder.genreView.setText(genre);
    }
}
