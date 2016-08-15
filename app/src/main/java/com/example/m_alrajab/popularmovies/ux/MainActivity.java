package com.example.m_alrajab.popularmovies.ux;

import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.m_alrajab.popularmovies.R;
import com.example.m_alrajab.popularmovies.controller.MainActivityFragment;
import com.example.m_alrajab.popularmovies.controller.Utility;
import com.example.m_alrajab.popularmovies.controller.sync.MoviesSyncAdapter;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_SHOW_FRAGMENT = ":android:show_fragment";
    public static final String EXTRA_NO_HEADERS = ":android:no_headers";
    static  final String TAG="Main activity";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MoviesSyncAdapter.initializeSyncAdapter(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
        if (findViewById(R.id.main_fragment) != null) {
            MainActivityFragment firstFragment = new MainActivityFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null).add(R.id.main_fragment, firstFragment).commit();
        }}


        try {
            //updateMovieList();
            File httpCacheDir = new File(this.getCacheDir(), "http");
            if(!httpCacheDir.toString().contains("youtube")){
            long httpCacheSize = 50 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);}
        } catch (IOException e) {
            Log.i(TAG, "HTTP response cache installation failed:" + e);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(Utility.getLayoutCol(this)==3){
            if (findViewById(R.id.fragment3) != null) {
                //if(savedInstanceState!=null) return;
                DetailsFragmentLand secondFragment = new DetailsFragmentLand();
                secondFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment3, secondFragment)
                        .addToBackStack(null).commit();
            }}
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //if(!menu.getItem(1).getTitle().equals(getString(R.string.action_settings)))
        if(menu.size()==0)
             getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings){
            startActivity(new Intent(this,SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
