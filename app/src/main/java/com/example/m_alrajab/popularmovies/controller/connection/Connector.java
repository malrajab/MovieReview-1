package com.example.m_alrajab.popularmovies.controller.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by m_alrajab on 7/28/16.
 */
public class Connector {
    /**
     * connect method establishes the httpURLconnection get method
     * it will timeout if bad connection
     * @param urlPath a string value
     * @return httpURLconnection
     */
    public static HttpURLConnection connect(String urlPath) throws IOException {

            URL url=new URL(urlPath);
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(HttpURLConnection.HTTP_REQ_TOO_LONG);
            httpURLConnection.setReadTimeout(HttpURLConnection.HTTP_CLIENT_TIMEOUT);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            return httpURLConnection;

    }
}
