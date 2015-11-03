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
    private static final String DEBUG_TAG = "HttpEx2";
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
        serverInterface = new NetworkClass(this);
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
        /*
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.username_field);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
        */
        // Get username
        EditText usernameText = (EditText) findViewById(R.id.username_field);
        String username = usernameText.getText().toString();
        // Get password
        EditText passwordText = (EditText) findViewById(R.id.password_field);
        String password = passwordText.getText().toString();
        // Attempt login
        ConnectivityManager connMgr = (ConnectivityManager) 
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) 
        {
            Log.d(DEBUG_TAG, "Username: "+username);
            Log.d(DEBUG_TAG, "Password: "+password);
            serverInterface.login(username, password);
        } 
        else 
        {
            Log.d(DEBUG_TAG, "No network connection available.");
        }
    }
}
