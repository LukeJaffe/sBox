package box.example;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import box.gpio.GPIOClass;

public class TestProjActivity extends GPIOClass
{
    private static final String DEBUG_TAG = "box_app";
    public final static String EXTRA_MESSAGE = "box.example.TestProjActivity.MESSAGE";

    public int curr_status = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        /* Basic stuff */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Enable the app icon as the Up button
        getActionBar().setDisplayHomeAsUpEnabled(true);

        /* Display stuff */
        Intent intent = getIntent();
        String message = intent.getStringExtra(TestProjActivity.EXTRA_MESSAGE);
        // Create the text view
        
        TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setTextSize(40);
        textView.setText("DEFAULT");

        readFlag = true;
        updateLockStatus(0);
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
    public void unlockButton(View view)
    {
        /* Unlock the sBox */
        changeLockStatus(1, 0);
        /* Do a forced update of the lock status */
        updateLockStatus(1);
        /* Set a callback for locking the sBox */
        changeLockStatus(0, 3000);
    }

    public void unlockCallback(int result)
    {
        /* Update text view for demo */
        TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setText("STATUS: UNLOCKED");
    }

    public void lockCallback(int result)
    {
        /* Update text view for demo */
        TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setText("STATUS: LOCKED");
    }

    public void updateLockCallback(int success, String message, int lock_status)
    {
        /* Change actual lock status */
        if (lock_status != curr_status)
        {
            curr_status = lock_status;
            if (curr_status == 0)
               lockCall();
            if (curr_status == 1)
               unlockCall(); 
        }
        else
        {
            /* Update text view for demo */
            TextView textView = (TextView) findViewById(R.id.text_view);
            if (curr_status == 0)
                textView.setText("STATUS: LOCKED");
            if (curr_status == 1)
                textView.setText("STATUS: UNLOCKED");
        }
    }

    public void changeLockCallback(int success, String message)
    {
        Log.d(DEBUG_TAG, "FINALLY: changeLockCallback()");
        readFlag = true;
        updateLockStatus(0);
    }
}
