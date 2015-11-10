package box.example;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import android.util.Log;


public class DisplayMessageActivity extends Activity 
{
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        /*
        try 
        {
            URL url = new URL("http://10.0.0.8/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            Log.e("filter123", "msg1");
            urlConnection.setDoOutput(true);
            Log.e("filter123", "msg2");
            urlConnection.setChunkedStreamingMode(0);
            Log.e("filter123", "msg3");
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            Log.e("filter123", "msg4");
            urlConnection.disconnect();
            Log.e("filter123", "msg5");
        }
        catch (MalformedURLException e) 
        {
            Log.e("filter123", "MalformedURLException");
        }
        catch (IOException e)
        {
            Log.e("filter123", "IOException");
        }
        */

        // Get the message from the intent
        Intent intent = getIntent();
        String message = intent.getStringExtra(TestProjActivity.EXTRA_MESSAGE);

        // Create the text view
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        setContentView(textView);
    }
}
