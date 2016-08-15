package com.example.m_alrajab.popularmovies.ux;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.m_alrajab.popularmovies.BuildConfig;
import com.example.m_alrajab.popularmovies.R;
import com.example.m_alrajab.popularmovies.model_data.ReviewAdapter;
import com.example.m_alrajab.popularmovies.model_data.data.PopMovieContract.MovieItemEntry;
import com.example.m_alrajab.popularmovies.model_data.data.PopMovieContract.MovieItemReviewEntry;
import com.example.m_alrajab.popularmovies.model_data.data.PopMovieContract.MovieItemTrailerEntry;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.Picasso;

import static android.provider.BaseColumns._ID;
import static com.example.m_alrajab.popularmovies.R.id.container;
import static com.example.m_alrajab.popularmovies.controller.Utility.isNetworkAvailable;
import static com.example.m_alrajab.popularmovies.model_data.data.PopMovieContract.MovieItemReviewEntry.COLUMN_REVIEW_OF_MOVIE_KEY;
import static com.example.m_alrajab.popularmovies.ux.DetailsActivityFragment.ARG_TYPE;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragmentLand extends Fragment {
    private  String urlPosterApi;
    public static final String ARG_COUNT = "ARG_COUNT";
    public static final String ARG_KEY = "ARG_KEY";
    public static final String ARG_URL = "ARG_URL";
    private ReviewAdapter mReviewAdapter;
    private static final int LOADER_ID = 42;
    private final String SELECTED_ITEM_KEY="movie_key";
    private  String blackposterKey, movieTitle;
    private ToggleButton toggleButton;
    private TextView mTV_Details;
    private TextView mTV_Title;
    private TextView mTV_Date;
    private RatingBar ratingBar;
    private ViewPager viewPager;
    private LinearLayout mTlrCntnr;
    private LinearLayout mRvwCntnr;
    private SharedPreferences sharedPref;
    private int _id, favSize;
    private int layout_id=-1;
    private Button tmpBtn;
    private ImageView blockbuster ;
    private SharedPreferences.Editor editor ;


    public DetailsFragmentLand() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        urlPosterApi=getActivity().getString(R.string.poster_base_url)+"/w500";
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        editor = sharedPref.edit();
        Bundle arguments = getArguments();
        if (arguments!=null&&arguments.containsKey(ARG_KEY))
            _id = arguments.getInt(ARG_KEY);

        if (arguments!=null&&arguments.containsKey(ARG_COUNT))
            favSize=arguments.getInt(ARG_COUNT); else favSize=-1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_details_land, container, false);

        // creating handles to this fragment views.
        toggleButton = (ToggleButton)   view.findViewById(R.id.details_icon_favorite);
        mTV_Details  = (TextView)       view.findViewById(R.id.details_overview);
        mTV_Date     = (TextView)       view.findViewById(R.id.details_date);
        mTV_Title    = (TextView)       view.findViewById(R.id.details_movie_title);
        ratingBar    = (RatingBar)      view.findViewById(R.id.movie_ratingbar);
        mTlrCntnr    = (LinearLayout)   view.findViewById(R.id.trailer_container);
        mRvwCntnr    = (LinearLayout)   view.findViewById(R.id.review_container);
        blockbuster  = (ImageView)      view.findViewById(R.id.backdrop_container);

        //Obtain from ContentProvider the details of this movie in detailsCursor
        Cursor detailsCursor= this.getContext().getContentResolver().query(
                MovieItemEntry.CONTENT_URI.buildUpon().appendPath(
                        sharedPref.getString(this.getContext().getString(R.string.pref_sorting_key),"top_rated")).build()
                , RefVal.projectionsMovieDetails,MovieItemEntry.COLUMN_MOVIE_ID + " = ? ",
                new String[]{String.valueOf(_id)}, null);
       // Log.v("Cursor ..>", ""+ (detailsCursor.moveToFirst()));
        if(!detailsCursor.moveToFirst()) {
            detailsCursor= this.getContext().getContentResolver().query(
                    MovieItemEntry.CONTENT_URI.buildUpon().appendPath(
                            sharedPref.getString(this.getContext().getString(R.string.pref_sorting_key),"top_rated")).build()
                    , RefVal.projectionsMovieDetails,null,null, null);
        }

        //Obtain from ContentProvider the trailers info of this movie in trailersCursor
        final Cursor trailersCursor=getActivity().getContentResolver().query(
                MovieItemTrailerEntry.CONTENT_URI.buildUpon().appendEncodedPath(String.valueOf(_id)
                        +"/videos").build(), null,MovieItemTrailerEntry.COLUMN_TRAILER_OF_MOVIE_KEY
                        + " = ? ",new String[]{String.valueOf(_id)}, null);

        //Obtain from ContentProvider the review info of this movie in reviewCursor
        final Cursor reviewCursor=getActivity().getContentResolver().query(
                MovieItemReviewEntry.CONTENT_URI.buildUpon().appendEncodedPath(String.valueOf(_id)
                        +"/reviews").build(), null,MovieItemReviewEntry.COLUMN_REVIEW_OF_MOVIE_KEY
                        + " = ? ",new String[]{String.valueOf(_id)}, null);

            if  (detailsCursor  != null)      pupolateMovieDetails      (detailsCursor  ,   view);
            if  (trailersCursor != null)      pupolateMovieTrailers     (trailersCursor ,   view);
            if  (reviewCursor   != null)      pupolateMovieReviewGlimpse(reviewCursor   ,   view);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void pupolateMovieDetails(Cursor detailsCursor, View view){
        if(detailsCursor.moveToFirst()) {
            mTV_Details.setText(detailsCursor.getString(RefVal.MI_COL_OVERVIEW));
            mTV_Date.setText(detailsCursor.getString(RefVal.MI_COL_RELEASE));
            ratingBar.setRating(detailsCursor.getFloat(RefVal.MI_COL_RATING) / 2.0f);
            mTV_Title.setText(detailsCursor.getString(RefVal.MI_COL_TITL));
            Log.v("Cursor ..>", ""+_id +" ... "+ARG_COUNT);
            _id=detailsCursor.getInt(RefVal.MI_COL_ID);
            Log.v("Cursor ..>", ""+_id +" ... "+ARG_COUNT);
            Picasso.with(getContext()).load(urlPosterApi+ detailsCursor.getString(RefVal.MI_COL_BACKDROPPATH))
                    .into(blockbuster);
            if (sharedPref.getBoolean(String.valueOf("FAV_" + _id), false)) {
                toggleButton.setChecked(true);
                toggleButton.setBackgroundColor(Color.GREEN);
            }
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (((ToggleButton) v).isChecked()) {
                            v.setBackgroundColor(Color.GREEN);
                            Toast.makeText(v.getContext(), "Added to your favorite", Toast.LENGTH_SHORT).show();
                            editor.putBoolean(String.valueOf("FAV_" + _id), true);
                        } else {
                            v.setBackgroundColor(Color.LTGRAY);
                            Toast.makeText(v.getContext(), "Removed from your favorite", Toast.LENGTH_SHORT).show();
                            editor.putBoolean(String.valueOf("FAV_" + _id), false);
                            if (favSize == 1)
                                editor.putBoolean(String.valueOf(getActivity().getString(
                                        R.string.pref_checked_favorite_key)), false);
                        }
                        editor.commit();
                    } catch (Exception e) {
                        Log.e("Error details fragment", e.getMessage(), e);
                    }
                }
            });
        }
    }
    private void pupolateMovieReviewGlimpse(Cursor cursor, View view){
        int colr, i=0;

        if( cursor.moveToFirst()){
            do{ colr=(i%2)*25+220;
                Button reviewAuthor=new Button(getContext());
                reviewAuthor.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                reviewAuthor.setBackgroundColor(Color.argb(255,colr,colr,colr));
                reviewAuthor.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                reviewAuthor.setCompoundDrawablesWithIntrinsicBounds(view.getResources()
                        .getDrawable(R.drawable.ic_face_black_48dp), null,null,null);
                reviewAuthor.setText(cursor.getString(RefVal.COL_MOV_RE_AUTHOR));
                //i++;colr=(i%2)*25+220;
                TextView reviewContent=new TextView(getContext());
                reviewContent.setBackgroundColor(Color.argb(255,colr,colr,colr));
                reviewContent.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                reviewContent.setText(cursor.getString(RefVal.COL_MOV_RE_CONTENT));
                reviewContent.setPadding(16,16,16,16);
                mRvwCntnr.addView(reviewAuthor);
                mRvwCntnr.addView(reviewContent);i++;
            }while(cursor.moveToNext());
        }
    }
    private void pupolateMovieTrailers(Cursor cursor, View view){
        if( cursor.moveToFirst()){
            do{
                Button trailerItem=new Button(getContext());
                trailerItem.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                trailerItem.setBackgroundColor(Color.argb(0,255,255,255));
                trailerItem.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                trailerItem.setCompoundDrawablesWithIntrinsicBounds(view.getResources()
                        .getDrawable(R.drawable.ic_play_arrow_black_24dp), null,null,null);
                trailerItem.setText(cursor.getString(RefVal.COL_MOV_TLR_NAME));
                final String movieKey=cursor.getString(RefVal.COL_MOV_TLR_KEY);
                trailerItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isNetworkAvailable(getActivity()))
                            try {
                                Intent newTrailer = YouTubeStandalonePlayer.createVideoIntent(getActivity()
                                        , BuildConfig.POP_MOVIES_YOUTUBE_APIKEY, movieKey,0,true,true);
                                startActivity(newTrailer);
                            }catch (ActivityNotFoundException e){
                                Intent i = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.youtube.com/watch?v=" + movieKey));
                                getActivity().startActivity(i);
                            }catch (Exception e){
                                Log.e("Youtube > ", e.getMessage(),e);
                            }
                        else
                            Toast.makeText(getActivity(), "You need internet to play this video",
                                    Toast.LENGTH_LONG).show();
                    }
                });
                mTlrCntnr.addView(trailerItem);
            }while(cursor.moveToNext());
        }
    }

    /**
     * Using the Cursor loader for the review
     * the reviews of a movie will appear in sperate page view using
     * pager. Only if the reviews are available the glimspe is clickable
     * ToDo (details review) disable the click on the glimpse if no review
     * ToDo (details review) make the glimpse color gray to indicate unclickable.
     * @param i Even though the loader is unique to this activity. I used the id
     * @param bundle
     * @return
     *
     */


    public static class RefVal{


        // Movies' details projection and their indices
        public final static int MI_COL_ID            =0;
        public final static int MI_COL_TITL          =1;
        public final static int MI_COL_OVERVIEW      =2;
        public final static int MI_COL_POPULARITY    =3;
        public final static int MI_COL_RATING        =4;
        public final static int MI_COL_RELEASE       =5;
        public final static int MI_COL_POSTERPATH    =6;
        public final static int MI_COL_BACKDROPPATH  =7;
        public static String[] projectionsMovieDetails  ={
                MovieItemEntry.COLUMN_MOVIE_ID,
                MovieItemEntry.COLUMN_MOVIE_TITLE,
                MovieItemEntry.COLUMN_MOVIE_OVERVIEW,
                MovieItemEntry.COLUMN_MOVIE_POPULARITY,
                MovieItemEntry.COLUMN_MOVIE_RATING,
                MovieItemEntry.COLUMN_MOVIE_RELEASE,
                MovieItemEntry.COLUMN_MOVIE_POSTERPATH,
                MovieItemEntry.COLUMN_MOVIE_BACKDROPPATH
        };

        // Reviews projection and their indices
        public final static int COL_RV_ID               = 0;
        public final static int COL_RVW_MVIE_KEY        = 1;
        public final static int COL_MOV_RE_ID           = 2;
        public final static int COL_MOV_RE_AUTHOR       = 3;
        public final static int COL_MOV_RE_CONTENT      = 4;
        public static String[] projectionsMovieReview  ={
                MovieItemReviewEntry._ID,
                MovieItemReviewEntry.COLUMN_REVIEW_OF_MOVIE_KEY,
                MovieItemReviewEntry.COLUMN_MOVIE_REVIEW_ID,
                MovieItemReviewEntry.COLUMN_MOVIE_REVIEW_AUTHOR,
                MovieItemReviewEntry.COLUMN_MOVIE_REVIEW_CONTENT,};

        // Trailers projection and their indices
        public final static int COL_TLR_ID              = 0;
        public final static int COL_TLR_MOVIE_KEY       = 1;
        public final static int COL_MOV_TLR_ID          = 2;
        public final static int COL_MOV_TLR_KEY         = 3;
        public final static int COL_MOV_TLR_NAME        = 4;
        public final static int COL_MOV_TLR_SITE        = 5;
        public static String[] projectionsMovieTrailer  ={
                MovieItemTrailerEntry._ID,
                MovieItemTrailerEntry.COLUMN_TRAILER_OF_MOVIE_KEY,
                MovieItemTrailerEntry.COLUMN_MOVIE_TRAILER_ID,
                MovieItemTrailerEntry.COLUMN_MOVIE_TRAILER_KEY,
                MovieItemTrailerEntry.COLUMN_MOVIE_TRAILER_NAME,
                MovieItemTrailerEntry.COLUMN_MOVIE_TRAILER_SITE,    };

    }

}
