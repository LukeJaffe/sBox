package com.networking;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Intent;
import android.widget.EditText;

import com.iface.NetworkClass;

public class TestProjActivity extends NetworkClass
{
    private static final String DEBUG_TAG = "HttpEx2";
    public final static String EXTRA_MESSAGE = "com.example.TestProjActivity.MESSAGE";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Enable the app icon as the Up button
        getActionBar().setDisplayHomeAsUpEnabled(true);
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

    /* Called when the user clicks the Login button */
    public void loginCall(View view)
    {
        // Get username
        EditText usernameText = (EditText) findViewById(R.id.username_field);
        String username = usernameText.getText().toString();
        // Get password
        EditText passwordText = (EditText) findViewById(R.id.password_field);
        String password = passwordText.getText().toString();
        // Attempt login
        login(username, password);
    }

    public void loginCallback(int success, String message)
    {
        Intent intent = new Intent(context(), DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
