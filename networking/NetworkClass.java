package box.networking;

import org.json.*;

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

public class NetworkClass extends Activity implements NetworkInterface
{
    public final static String EXTRA_MESSAGE = "box.example.TestProjActivity.MESSAGE";
    private static final String DEBUG_TAG = "box_app";

    public static void main(String[] args)
    {
        System.out.println(SERVER_IP);
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

    public void login(String username, String password)
    {
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            Log.d(DEBUG_TAG, "Username: "+username);
            Log.d(DEBUG_TAG, "Password: "+password);
            new LoginTask().execute(username, password);
        }
        else
        {
            Log.d(DEBUG_TAG, "No network connection available.");
        }
    }

     private class LoginTask extends AsyncTask<String, Void, String> 
     {
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
            try
            {
                JSONObject obj = new JSONObject(result);
                int success = obj.getInt("success");
                String message = obj.getString("message");
                loginCallback(success, message);
            }
            catch (JSONException e)
            {
                Log.d(DEBUG_TAG, "JSON error {onPostExecute}");
            }
        }
    }

    public void loginCallback(int success, String message) {}

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

    public void updateLockStatus(int force)
    {
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            new UpdateLockTask().execute(Integer.toString(force));
        }
        else
        {
            Log.d(DEBUG_TAG, "No network connection available.");
        }
    }

    public boolean readFlag;

     private class UpdateLockTask extends AsyncTask<String, Void, String> 
     {
        @Override
        protected String doInBackground(String ... args) 
        {
            Log.d(DEBUG_TAG, "UpdateLockTask: pre-loop");
            int force = Integer.parseInt(args[0]);
            while (readFlag || force==1)
            {
                Log.d(DEBUG_TAG, "UpdateLockTask: in-loop");
                try 
                {
                    /* Check the server for the status of the lock */
                    String result = updateLockPostRequest();
                    /* Parse the result */
                    try
                    {
                        JSONObject obj = new JSONObject(result);
                        final int success = obj.getInt("success");
                        final String message = obj.getString("message");
                        final int lock_status = obj.getInt("lock_status");
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateLockCallback(success, message, lock_status);
                            }
                        });
                    }
                    catch (JSONException e)
                    {
                        Log.d(DEBUG_TAG, "JSON error {onPostExecute}");
                    }
                    /* Take a nap */
                    Thread.sleep(200);
                } 
                catch (Exception e) 
                {
                    Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
                }
                if (force == 1)
                    break;
            }
            return "DEFAULT";
        }
    }

    public void updateLockCallback(int success, String message, int lock_status) {}

    private String updateLockPostRequest() throws IOException 
    {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(LOCK_STATUS_URL);
            Map<String,Object> params = new LinkedHashMap();
            params.put("boxID", BOX_ID);

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

    public void changeLockStatus(int status, int delay)
    {
        /* Set the read flag to false to (temporarily) exit the read loop */
        readFlag = false;

        /* Do the rest */
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            new ChangeLockTask().execute(Integer.toString(status), Integer.toString(delay));
        }
        else
        {
            Log.d(DEBUG_TAG, "No network connection available.");
        }
    }

     private class ChangeLockTask extends AsyncTask<String, Void, String> 
     {
        @Override
        protected String doInBackground(String ... args) 
        {
            try 
            {
                /* Wait for delay to expire */
                Log.d(DEBUG_TAG, "ENTER: ChangeLockTask() -- pre-delay");
                Thread.sleep(Integer.parseInt(args[1]));
                Log.d(DEBUG_TAG, "ENTER: ChangeLockTask() -- post-delay");
                /* Check the server for the status of the lock */
                String result = changeLockPostRequest(Integer.parseInt(args[0]));
                return result;
            } 
            catch (Exception e) 
            {
                Log.d(DEBUG_TAG, "Unable to retrieve web page. URL may be invalid.");
                return "FAILURE";
            }
        }

        @Override
        protected void onPostExecute(String result) 
        {
            try
            {
                JSONObject obj = new JSONObject(result);
                int success = obj.getInt("success");
                String message = obj.getString("message");
                changeLockCallback(success, message);
            }
            catch (JSONException e)
            {
                Log.d(DEBUG_TAG, "JSON error {onPostExecute}");
            }
        }
    }

    public void changeLockCallback(int success, String message) {}

    private String changeLockPostRequest(int lock_status) throws IOException 
    {
        Log.d(DEBUG_TAG, "ENTER: changeLockPostRequest()");
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(LOCK_CHANGE_URL);
            Map<String,Object> params = new LinkedHashMap();
            params.put("boxID", BOX_ID);
            params.put("lock_status", lock_status);

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

}
