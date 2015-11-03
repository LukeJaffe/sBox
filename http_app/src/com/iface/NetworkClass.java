package com.iface;

import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Intent;
import android.content.Context;
import android.widget.EditText;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Map;
import java.util.LinkedHashMap;

import java.net.URLEncoder;
import java.net.URL;
import java.net.HttpURLConnection;

import com.networking.DisplayMessageActivity;

public class NetworkClass implements NetworkInterface
{
    public final static String EXTRA_MESSAGE = "com.example.TestProjActivity.MESSAGE";
    private static final String DEBUG_TAG = "HttpEx2";
    public Activity parent;

    public static void main(String[] args)
    {
        System.out.println(SERVER_IP);
    }

    public NetworkClass(Activity parent)
    {
        this.parent = parent;
    }

    public void login(String username, String password)
    {
        new LoginTask().execute(username, password);
    }

     // Uses AsyncTask to create a task away from the main UI thread. This task takes a 
     // URL string and uses it to create an HttpUrlConnection. Once the connection
     // has been established, the AsyncTask downloads the contents of the webpage as
     // an InputStream. Finally, the InputStream is converted into a string, which is
     // displayed in the UI by the AsyncTask's onPostExecute method.
     private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String ... args) 
        {
            try 
            {
                return loginPostRequest(args[0], args[1]);
            } 
            catch (IOException e) 
            {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) 
        {
            Log.d(DEBUG_TAG, "Reached onPostExecute!");
            Log.d(DEBUG_TAG, result);
            Intent intent = new Intent(parent, DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, result);
            parent.startActivity(intent);
        }
    }


    private String loginPostRequest(String username, String password) throws IOException 
    {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(LOGIN_URL);
            Map<String,Object> params = new LinkedHashMap();
            params.put("username", username);
            params.put("password", password);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            // Starts the query
            Log.d(DEBUG_TAG, "Attempt connection...");
            conn.connect();
            //int response = conn.getResponseCode();
            //Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) 
        throws IOException, UnsupportedEncodingException    
    {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

}
