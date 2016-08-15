package com.example.m_alrajab.popularmovies.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.m_alrajab.popularmovies.BuildConfig;
import com.example.m_alrajab.popularmovies.R;
import com.facebook.stetho.Stetho;

import java.util.Map;

/**
 * Created by m_alrajab on 8/12/16.
 * provides methods for generic use
 * make sure to pass the context of the activity to ensure correct output
 */

public class Utility {
    private static DisplayMetrics metrics;

    // check if no favorites
    public static void validateChangeOfFavListingIfExist
            (Context context, SharedPreferences sPref, String key){
        try {
            if (key.equals(context.getString(R.string.pref_checked_favorite_key))
                    &&   getNumOfFavMovies(sPref)==0) {
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean(context.getString(R.string.pref_checked_favorite_key), false);
                editor.apply();
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.no_fav_dialog_title))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(context.getString(R.string.no_fav_dialog_msg))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}}).show();
            }
        }catch (IllegalStateException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static int getNumOfFavMovies(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getNumOfFavMovies(sharedPref);
    }

    public static int getNumOfFavMovies(SharedPreferences sharedPref){
        int count=0,tmpNone=-1;
        Map<String,?> items=sharedPref.getAll();
        for(String key:items.keySet())
            if(key.startsWith("FAV_") && ((Boolean) items.get(key)))
                count++; else tmpNone++;
        return (tmpNone==-1 && count==0)?  -1: count;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // the number of columns inside the gridlayout of the RecycleView
    public static int layoutColNum(Context context) {
        try {
            metrics = Resources.getSystem().getDisplayMetrics();
            return Math.max(1,Math.round((metrics.widthPixels + 5.0f)
                    / (getPrefPosterWidth(context)*getLayoutCol(context))));
        }catch(NullPointerException e){
            e.printStackTrace();
            return 1;
        }
    }

    // the height of the image inside the gridlayout
    public static int getImageHeight(Context context) {
        metrics = Resources.getSystem().getDisplayMetrics();
        double rt = Math.max((0.01 + metrics.heightPixels) / metrics.widthPixels,
                (0.01 + metrics.widthPixels) / metrics.heightPixels);
        int hgt = (int) Math.round(getImageWidth(context) * rt);
        return hgt;
    }

    // the width of the image inside the gridlayout
    public static int getImageWidth(Context context) {
        metrics = Resources.getSystem().getDisplayMetrics();
        return Math.max(1, metrics.widthPixels / (layoutColNum(context)*getLayoutCol(context)));
    }

    private static int getPrefPosterWidth(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String syncConnPref = sharedPref.getString(context.getString(R.string.pref_poster_res_key), "/w500");
        int width = Integer.parseInt(syncConnPref.split("w")[1]);
        return width;
    }

    // Number of columns of the screen display, ex. 1 or 3 in landscape mode
    public static int getLayoutCol(Context context) {
        try {
            return Integer.valueOf(context.getString(R.string.layout_col));
        } catch(NullPointerException e){
            Log.v("Utilities - > Lyt-Cntxt",e.getMessage(),e);
            return 1;
        }catch (Exception e){
            Log.v("Utilities - > layoutNum",e.getMessage(),e);
            return 1;
        }
    }

    public static void setStethoWatch(Context context) {
        if(BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(context)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
                            .build()
            );
        }
    }
}
