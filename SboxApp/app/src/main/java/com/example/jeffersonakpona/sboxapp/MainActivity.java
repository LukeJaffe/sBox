package com.example.jeffersonakpona.sboxapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupEnterButton();
        setupScanButton();
    }


    //GLOBAL VARIABLES REFERENCES
    private EditText barcodeEditText;
    private Button enterButton;
    private Button scanButton;


    private void setupScanButton() {
        barcodeEditText = (EditText) findViewById(R.id.barcode_editText);
        scanButton = (Button) findViewById(R.id.scan_button);

        // 1. Set the click listener to run code when button is clicked
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use intent to call zxing barcode app
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                //Use frontCamera
                intent.putExtra("SCAN_CAMERA_ID", 1);
                startActivityForResult(intent, 0);
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                // Handle successful scan
                barcodeEditText.setText(contents);
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }


    private void setupEnterButton() {
        barcodeEditText = (EditText) findViewById(R.id.barcode_editText);
        enterButton = (Button) findViewById(R.id.enter_button);

        // 1. Set the click listener to run code when button is clicked
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeEditText.getText().toString().matches("")) {
                    Toast.makeText(getBaseContext(), "You Did Not Enter A Tracking Number!" , Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
