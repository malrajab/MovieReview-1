package com.example.m_alrajab.popularmovies.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.m_alrajab.popularmovies.BuildConfig;
import com.example.m_alrajab.popularmovies.R;
import com.example.m_alrajab.popularmovies.controller.connection.URLBuilderPref;
import com.example.m_alrajab.popularmovies.model_data.data.PopMovieDbHelper;
import com.example.m_alrajab.popularmovies.ux.SettingsActivity;
import com.facebook.stetho.Stetho;

/**
 *
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private DisplayMetrics metrics;
    private URLBuilderPref urlBuilderPref;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rv;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        metrics = Resources.getSystem().getDisplayMetrics();


            if (BuildConfig.DEBUG) {
                Stetho.initialize(
                        Stetho.newInitializerBuilder(this.getContext())
                                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this.getContext()))
                                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this.getContext()))
                                .build()
                );
            }


        if (savedInstanceState==null || !savedInstanceState.containsKey("MoviesInfoSet"))
           ; //updateInfo();
        else {
           // movies = savedInstanceState.getParcelableArrayList("MoviesInfoSet");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        urlBuilderPref=new URLBuilderPref(rootView.getContext());
        rv=(RecyclerView) rootView.findViewById(R.id.rv);
        swipeRefreshLayout=(SwipeRefreshLayout)rootView.findViewById(R.id.container);
        swipeRefreshLayout.setOnRefreshListener(this);
        updateUI();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putParcelableArrayList("MoviesInfoSet",(ArrayList<MovieItem>)movies);
        super.onSaveInstanceState(outState);
    }

    private int layoutColNum(){
        return metrics.widthPixels/urlBuilderPref.getPosterWidth();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.popular_movies_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateUI();
            return true;
        }else if (id == R.id.action_settings){
            startActivity(new Intent(getActivity(),SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        Toast.makeText(this.getContext(),"Refresh movie list", Toast.LENGTH_LONG).show();
        if (   swipeRefreshLayout.isRefreshing()) {
            updateUI();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      //  if (key.equals(R.string.pref_poster_res_key))
         updateUI();
    }

    private void updateUI()  {
        try {

            PopMovieDbHelper f=new PopMovieDbHelper(this.getContext());
            f.onUpgrade(f.getWritableDatabase(),0,0);
            GridLayoutManager staggeredGridLayoutManager = new GridLayoutManager(this.getContext(), layoutColNum(),
                    GridLayoutManager.VERTICAL, false);
            rv.setLayoutManager(staggeredGridLayoutManager);
            new PopulateAPIData_to_RView(this.getContext(), urlBuilderPref.getAPIURL()
                    , urlBuilderPref.getPosterApiBaseURL(),
                    rv, rv.getContext().getResources().getStringArray(R.array.parsingJsonParams)).execute();
        }catch (IllegalStateException e){
            e.printStackTrace();
            Log.e("Error in MA Fragment",e.getMessage(),e);
        }
    }
}


