package com.GPIODemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.GPIODemo.R.drawable;


public class GPIODemoActivity extends Activity 
{
	
   /*declare a FT311 GPIO interface variable*/
    public FT311GPIOInterface gpiointerface;

    /* Should be enough to trigger lock */
	public Button configbutton;
	
	/*variables*/
	public byte outMap; /*outmap*/
	public byte inMap; /*inmap*/
	public byte outData; /*output data*/
	public byte inData; /*input Data*/
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /**** command buttons****/
        configbutton = (Button)findViewById(R.id.configbutton);

		/*user code to configure port data*/
		configbutton.setOnClickListener(new View.OnClickListener() 
        {
			//@Override
			public void onClick(View v) 
            {
				outData &= ~outMap;
				gpiointerface.ConfigPort(outMap, inMap);
			}
		});

		/*create an object of GPIO interface class*/
        gpiointerface = new FT311GPIOInterface(this); 
		resetFT311();
    }
    
    protected void resetFT311()
    {
		gpiointerface.ResetPort();
		
		ProcessReadData((byte)0);
		
		outMap = (byte)0x80;
		inMap = (byte)0x7f;
    }

    @Override
    protected void onResume() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        gpiointerface.ResumeAccessory();
    }

    @Override
    protected void onPause() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
    }
    
    @Override 
    protected void onDestroy(){
    	
    	gpiointerface.DestroyAccessory();
    	super.onDestroy();
    }
    	
	public void ProcessReadData(byte portData)
	{
		byte cmddata;

		cmddata = portData;
		/*check if the command is write*/
		
		/*just process input map*/
		cmddata &= inMap;
		/*read data is to update the LEDs*/
	}
}
