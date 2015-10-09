package com.PWMDemo;

import com.PWMDemo.R.drawable;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class PWMDemoActivity extends Activity {
	
	/*declare a FT311 PWM interface variable*/
    public FT311PWMInterface pwmInterface;
    /*graphical objects*/
    EditText periodValue;
    EditText dutyCycle0Value;
    EditText dutyCycle1Value;
    EditText dutyCycle2Value;
    EditText dutyCycle3Value;
    
    SeekBar seekbar0;
    SeekBar seekbar1;
    SeekBar seekbar2;
    SeekBar	seekbar3;
    
    Button periodButton;
    Button resetButton;
    
    
    int period;
    byte dutyCycle;
	
    private Handler mHandler = new Handler();
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        
        /*create the objects*/
        periodValue = (EditText)findViewById(R.id.PeriodValue);
        dutyCycle0Value = (EditText)findViewById(R.id.DutyCycle0Value);
        dutyCycle1Value = (EditText)findViewById(R.id.DutyCycle1Value);
        dutyCycle2Value = (EditText)findViewById(R.id.DutyCycle2Value);
        dutyCycle3Value = (EditText)findViewById(R.id.DutyCycle3Value);
        seekbar0 = (SeekBar)findViewById(R.id.Channel0Seekbar);
        seekbar1 = (SeekBar)findViewById(R.id.Channel1Seekbar);
        seekbar2 = (SeekBar)findViewById(R.id.Channel2Seekbar);
        seekbar3 = (SeekBar)findViewById(R.id.Channel3Seekbar);
        
        periodButton = (Button)findViewById(R.id.PeriodButton);
        resetButton = (Button)findViewById(R.id.resetButton);
        
        pwmInterface = new FT311PWMInterface(this);
        resetFT311();
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//resetButton.setBackgroundResource(drawable.start);
				resetFT311();
			}
		});
        
        periodButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//periodButton.setBackgroundResource(drawable.start);
				/*read the period value*/
				if(periodValue.length() == 0x00){
				 periodValue.setText("100");	
				}
				period = Integer.parseInt(periodValue.getText().toString());
				/*take care of zero*/
				if(period < 100){
					period = 100;
					periodValue.setText("100");
				}
				
				/*take care of MAX value*/
				if(period > 250){
					period = 250;
					periodValue.setText("250");
				}
				periodValue.setSelection(periodValue.getText().length());
				pwmInterface.SetPeriod(period);
				
				
			}
		});
        
        /*take the valus of channel 0*/
        seekbar0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
				dutyCycle = (byte)progress;
				dutyCycle0Value.setText(Integer.toString(progress));
				pwmInterface.SetDutyCycle((byte)0,dutyCycle);
				
				
			}
		});
        
        /*take the values of channel 1*/
        seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
				dutyCycle = (byte)progress;
				dutyCycle1Value.setText(Integer.toString(progress));;
				pwmInterface.SetDutyCycle((byte)1,dutyCycle);
				
			}
		});
        
        
        /*take the valus of channel 2*/
        seekbar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
				dutyCycle = (byte)progress;
				dutyCycle2Value.setText(Integer.toString(progress));
				pwmInterface.SetDutyCycle((byte)2,dutyCycle);
				
				
			}
		});

        
        /*take the values of channel 3*/
        seekbar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
				dutyCycle = (byte)progress;
				dutyCycle3Value.setText(Integer.toString(progress));
				pwmInterface.SetDutyCycle((byte)3,dutyCycle);
			}
		});
    }
    
    protected void resetFT311()
    {	
		seekbar0.setProgress(0);
		seekbar1.setProgress(0);
		seekbar2.setProgress(0);
		seekbar3.setProgress(0);
		periodValue.setText(Integer.toString(100));
		periodValue.setSelection(periodValue.getText().length());
		pwmInterface.Reset();
		//pwmInterface.SetDutyCycle((byte)0,dutyCycle);
    }
    
    @Override
    protected void onResume() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        pwmInterface.ResumeAccessory();
    }

    @Override
    protected void onPause() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        
       
  
    }
    
    @Override 
    protected void onDestroy(){
    	pwmInterface.DestroyAccessory();
    	super.onDestroy();
    }
    
}