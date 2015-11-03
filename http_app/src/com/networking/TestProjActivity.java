package com.networking;

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

import com.iface.NetworkClass;

public class TestProjActivity extends Activity
{
    private static final String DEBUG_TAG = "HttpExample";
    public final static String EXTRA_MESSAGE = "com.example.TestProjActivity.MESSAGE";

    public NetworkClass serverInterface;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Enable the app icon as the Up button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up NetworkClass
        serverInterface = new NetworkClass();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items dor use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //Handle presses on the action bar items
        switch (item.getItemId())
        {
            case R.id.action_search:
                //openSearch();
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Called when the user clicks the Send button */
    public void sendMessage(View view)
    {
        ConnectivityManager connMgr = (ConnectivityManager) 
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) 
        {
            Log.d(DEBUG_TAG, "Attempting to download webpage...");
            serverInterface.login("testuser", "testpass");
        } 
        else 
        {
            Log.d(DEBUG_TAG, "No network connection available.");
        }
    }
}
