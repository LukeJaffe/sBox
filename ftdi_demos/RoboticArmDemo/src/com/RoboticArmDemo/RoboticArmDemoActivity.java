package com.RoboticArmDemo;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.RoboticArmDemo.R.drawable;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;


public class RoboticArmDemoActivity extends Activity {
    /** Called when the activity is first created. */
		private static final String ACTION_USB_PERMISSION =    "com.RoboticArmDemo.USB_PERMISSION";
		public UsbManager usbmanager;
		public UsbAccessory usbaccessory;
		public PendingIntent mPermissionIntent;
		public ParcelFileDescriptor filedescriptor;
		public FileInputStream inputstream;
		public FileOutputStream outputstream;
		public boolean mPermissionRequestPending = true;
		
		//public Handler usbhandler;
		public byte[] usbdata;
		public byte[] writeusbdata;
		
		
		
		
	    /*thread to listen USB data*/
	      public usb_send_thread	usbSendThread;
	      public usb_demo_thread	usbDemoThread;
	      /*dirty way,,,:), still learning android*/
	      public byte mStopDemo;
	      public byte demoStep;
	    
	    public AccSensorClass mAccSensorClass;
	    
	    public SensorManager mSensorManager;
		public WindowManager mWindowManager;
		public Display mDisplay;
	 
	
	 
	 public Button Base, Arm, Wrist, Grip;
	 public Button mHome, mUser, mAuto;
	 public EditText mEditText;
	 public byte mDefaultPosition;
	
	 int mCurrentBase;
	 int mCurrentARM;
	 public byte mCurrentWrist;
	 public byte mCurrentGrip;
	 public byte mCurrentShoulder;
	 public byte mWristData, mGripData, mBaseData, mArmData, mShoulderData;
	 
	 public long mCurrentUsbTime, mPrevUsbTime;
	 
	 public SeekBar seekbar1,seekbar2;
	
	
	 
	 
	// @Override
	// public boolean onTouchEvent(MotionEvent e){
	//	 byte x, y;
	//	 x = (byte)e.getAxisValue(MotionEvent.AXIS_X);
	//	 y = (byte)e.getAxisValue(MotionEvent.AXIS_Y);
		 
		 
	//	 led2.setText("x"+x);
	//	 led3.setText("y"+y);
	 
	//	return true;
		 
		 
	 //}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       // mTestTouch = new testTouch(this);
        //setContentView(mTestTouch);
        setContentView(R.layout.main);
       // LinearLayout layout = new LinearLayout(this);
               
        
       // led1 = new Button(this);
       // led2 = new Button(this);
       // led3 = new Button(this);
       // led4 = new Button(this);
        
       // led1.setText("led1");
       // led2.setText("led2");
       // led3.setText("led3");
       // led4.setText("led4");
       // textView.setTag("my name");
       // textView.bringToFront();
       // led1.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
       // led2.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
       // led3.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
       // led4.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        
       // layout.addView(led1);
       // layout.addView(led2);
       // layout.addView(led3);
       // layout.addView(led4);
       // setContentView(layout);
        
       
        //  led1.setOnClickListener(new Button.OnClickListener(){

        //		public void onClick(View v) {
				// TODO Auto-generated method stub
        //			led1.setText("clicked");
				
        //		}
        //});	
       	
        
        
        
        /*sensor specific*/
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        
        mAccSensorClass = new AccSensorClass(this);
        
        
      

        
        usbdata = new byte[4]; 
        writeusbdata = new byte[4];
        
        usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d("LED", "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        Log.d("LED", "filter" +filter);
        registerReceiver(mUsbReceiver, filter);
        
       inputstream = null;
       outputstream = null;
       
       
       Base = (Button)findViewById(R.id.button1);
       Arm = (Button)findViewById(R.id.button2);
       Wrist = (Button)findViewById(R.id.button3);
       Grip = (Button)findViewById(R.id.button4);
       
      
       
       seekbar1 = (SeekBar)findViewById(R.id.wristseekbar1);
       seekbar2 = (SeekBar)findViewById(R.id.gripseekbar2);
       
       
       seekbar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			
			/*check if the default value is set
			 * 
			 */
			if(mDefaultPosition == 1)
					return;
			
			//if(progress >= 0){
			
				
   				mCurrentGrip = (byte)(progress);
   			//}
			/*else{
   				
   				mCurrentGrip = (byte)((16-progress)/2);
   				mCurrentGrip = (byte) -mCurrentGrip;
 			
   			}
   			*/
   			Grip.setText("Grip:" + mCurrentGrip);
			
		}
	});
       
       
       
       seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
   		
   		@Override
   		public void onStopTrackingTouch(SeekBar seekBar) {
   			// TODO Auto-generated method stub
   			
   		}
   		
   		@Override
   		public void onStartTrackingTouch(SeekBar seekBar) {
   			// TODO Auto-generated method stub
   			
   		}
   		
   		@Override
   		public void onProgressChanged(SeekBar seekBar, int progress,
   				boolean fromUser) {
   			// TODO Auto-generated method stub
   			
   			/*check if the default value is set
			 * 
			 */
			if(mDefaultPosition == 1)
					return;
			
   			
   			if(progress >= 16){
   				mCurrentWrist = (byte)((progress-16));
   			}else{
   				
   				mCurrentWrist = (byte)((16-progress));
   				mCurrentWrist = (byte) -mCurrentWrist;
 			
   			}
   			
   			Wrist.setText("Wrist:" + mCurrentWrist);
   			
   		}
   	});  
       
      
       mDefaultPosition = 0;
       mStopDemo = 1;
       
       /*
        * default position
        */
       mCurrentARM = -2;
	   mCurrentBase = 0;
	   mCurrentGrip = 0;
	   mCurrentWrist = 0;
	   mCurrentShoulder = 0;
       
       mUser = (Button)findViewById(R.id.StopButton);
       mHome = (Button)findViewById(R.id.StartButton);
       mAuto = (Button)findViewById(R.id.DemoButton);
       mEditText = (EditText)findViewById(R.id.edittext);
       
       /*goto the default positions*/
       mHome.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
				mDefaultPosition = 1;
				mCurrentARM = -2;
				mCurrentBase = 0;
				mCurrentGrip = 0;
				mCurrentWrist = 0;
				mCurrentShoulder = 0;
				mStopDemo = 1;
				
				mEditText.setText("Reset State, press <User> to use Android Device for user Inputs");
				//mStart.setClickable(false);
				//mStart.setBackgroundResource(drawable.button2);
				//mStart.setTextColor(0xffffffff);
				//mStop.setText("Start");
				//mStart.setBackgroundColor(0xff0000);
				mHome.setBackgroundResource(drawable.home1);
		}
	});
       
       
       mHome.setOnTouchListener(new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				mHome.setBackgroundResource(drawable.home);
			}
			return false;
		}
	});

       /*stop the default position and start taking the input from
        * 	the tablet
        */
       mUser.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mUser.setBackgroundResource(drawable.user);
			// TODO Auto-generated method stub
			mDefaultPosition = 0;
			mStopDemo = 1;
			//usbDemoThread.stop();
			//if(mStopDemo == 0){
			mHome.setClickable(true);
			mAuto.setClickable(true);
			mEditText.setText("Android Device can be used for user input mode");
			mHome.setBackgroundResource(drawable.home1);
			mAuto.setBackgroundResource(drawable.start);
			//mStart.setBackgroundResource(drawable.button1);
			
			//mDemo.setBackgroundResource(drawable.button1);
			//mDemo.setClickable(true);
			
			
			
			
			
		}
	});
       
      
     mUser.setOnTouchListener(new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				mUser.setBackgroundResource(drawable.user1);
			}
			
			return false;
		}
	});
       
       /*
        * start the demo thread
        */
       
     
       mAuto.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			/*
			 * start a thread, that dies after the demo; 
			 */
		
			if(mStopDemo == 1){
				mDefaultPosition = 1;
				mStopDemo = 0;
				demoStep = 1;
				mEditText.setText("Demo is running, press <User> to stop the demo");
				//mStop.setText("Stop");
				//mDemo.setBackgroundResource(drawable.button2);
				mAuto.setClickable(false);
				mHome.setClickable(false);
				//mStart.setBackgroundResource(drawable.button2);
				mHome.setBackgroundResource(drawable.home);
				mAuto.setBackgroundResource(drawable.start);
				
			}
		
		}
	});
       
      
       mAuto.setOnTouchListener(new View.OnTouchListener() {
   		
   		@Override
   		public boolean onTouch(View v, MotionEvent event) {
   			// TODO Auto-generated method stub
   			if(event.getAction() == MotionEvent.ACTION_DOWN){
   				mAuto.setBackgroundResource(drawable.start1);
   			}
   			
   			return false;
   		}
   	});
      
       /*start the demo thread*/
       usbDemoThread = new usb_demo_thread(usbDemoHandler);
		usbDemoThread.start();
       
       
  }
    
    
    
    
    
    
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mAccSensorClass.StartSensor();
        
        
       // Intent intent = getIntent();
		if (inputstream != null && outputstream != null) {
			return;
		}
		
		UsbAccessory[] accessories = usbmanager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (usbmanager.hasPermission(accessory)) {
				OpenAccessory(accessory);
			} 
			else
			{
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						usbmanager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
			}
		}
		} else {}
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
       
  
    }
    
    @Override 
    protected void onDestroy(){
    	
    	unregisterReceiver(mUsbReceiver);
    	mAccSensorClass.StopSensor();
    	super.onDestroy();
    }
    
    /*open the accessory*/
	private void OpenAccessory(UsbAccessory accessory)
	{
		filedescriptor = usbmanager.openAccessory(accessory);
		if(filedescriptor != null){
			usbaccessory = accessory;
			FileDescriptor fd = filedescriptor.getFileDescriptor();
			inputstream = new FileInputStream(fd);
			outputstream = new FileOutputStream(fd);
			/*check if any of them are null*/
			if(inputstream == null || outputstream==null){
				return;
			}
		}
		
		
		
		usbSendThread = new usb_send_thread(usbSendHandler);
		usbSendThread.start();
	} /*end OpenAccessory*/
    
	
	
	
	private void CloseAccessory()
	{
		try{
			filedescriptor.close();
		}catch (IOException e){}
		
		try {
			inputstream.close();
		} catch(IOException e){}
		
		try {
			outputstream.close();
			
		}catch(IOException e){}
		/*FIXME, add the notfication also to close the application*/
		//unregisterReceiver(mUsbReceiver);
		//CloseAccessory();
		//super.onDestroy();
		filedescriptor = null;
		inputstream = null;
		outputstream = null;
	
		System.exit(0);
	
	}
	
    
   
    
    
    final Handler usbSendHandler =  new Handler()
    {
    	byte mPwm = 0;
    	byte mData = 0;
    	@Override 
    	public void handleMessage(Message msg)
    	{
    		/*check for the data*/
    		if(mWristData != 0){
    			mPwm = 2;
    			mData = mCurrentWrist;
    			mWristData = 0;
    			mBaseData = 1;
    		}else if(mBaseData != 0){
    			mPwm = 0;
    			mData = (byte) mCurrentBase;
    			mBaseData = 0;
    			mGripData = 1;
    		}else if(mGripData != 0){
    			mPwm = 3;
    			mData = mCurrentGrip;
    			mGripData = 0;
    			mArmData = 1;
    		}else if(mArmData != 0){
    			mPwm = 1;
    			mData = (byte) mCurrentARM;
    			mArmData = 0;
    			mShoulderData = 1;
    		}else if(mShoulderData != 0){
    			mPwm = 6;
    			mData = mCurrentShoulder;
    			mShoulderData = 0;
    			mWristData = 1;
    		}
    		
    		//if(mData != 0){
    			writeusbdata[0] = 1;
    			writeusbdata[1] = mPwm;
    			writeusbdata[3] = mData;
    			mData = 0;
    			
    			/*send the data across*/
    			try {
    				if(outputstream != null){
    					outputstream.write(writeusbdata, 0, 4);
    				}
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				//e.printStackTrace();
    			}
    		//}
    	}	
    };
    
    
    private class usb_send_thread extends Thread 
    {
    	Handler mUsbHandler;
    	
    	usb_send_thread(Handler usb_handler){
    		mUsbHandler = usb_handler;
    	}
    	
    	
    	public void run()
		{
			
			while(true)
			{
				Message msg = mUsbHandler.obtainMessage();
				
				
					try{	
						/*make it slow*/
						Thread.sleep(50);
					} catch(InterruptedException e){}
				
					mUsbHandler.sendMessage(msg);
					
				//}	
			}
		}
    	
    }
    
    
    final Handler usbDemoHandler =  new Handler()
    {
    	
    	@Override 
    	public void handleMessage(Message msg)
    	{
    		demoStep++;
    		if(mStopDemo == 1){
    			mEditText.setText("Demo is done, press <User> for user input mode");
    			mAuto.setClickable(true);
    			mHome.setClickable(true);
    			mUser.setClickable(true);
    			mHome.setBackgroundResource(drawable.home1);
    			mAuto.setBackgroundResource(drawable.start);
    			
    		}
    		
    	}
    };
    
    /*
     * FIXME, i dont know any other way out, may be 
     * there is a simpler method to do this
     */
    private class usb_demo_thread extends Thread 
    {
    	Handler mDemoHandler;
    	
    	usb_demo_thread(Handler demo_handler){
    		mDemoHandler = demo_handler;
    	}
    	
    	
    	public void run()
		{
		int waitTime = 100;	
			
    		while(true)
    		{	
				Message msg = mDemoHandler.obtainMessage();
				
				
					try{	
						/*make it slow*/
						Thread.sleep(waitTime);
					} catch(InterruptedException e){}
					
					
					if(mStopDemo == 0)
					{
					if(demoStep == 1)
					{
						mCurrentARM = -2;
						mCurrentBase = 0;
						mCurrentGrip = 0;
						mCurrentWrist = 0;
						waitTime = 2000;
					}else if(demoStep ==2)
					{
						mCurrentBase = 10;
						waitTime = 2000;
					}else if(demoStep == 3){
						mCurrentARM += 9;
				
					}else if(demoStep == 4) {
						mCurrentWrist -= 6;
	
					}else if(demoStep == 5){
						mCurrentGrip += 12;
	
					}else if(demoStep == 6){
						mCurrentWrist += 6;
					}else if(demoStep == 7){
						mCurrentARM -= 9;
					}else if(demoStep == 8){
						mCurrentBase = -15;
						waitTime = 3000;
					}else if(demoStep == 9){
						mCurrentARM += 9;
						waitTime = 2000;
					}
					else if(demoStep == 10){
						mCurrentWrist -= 6;
					}else if(demoStep == 11){
						mCurrentGrip -= 12;
					}
					else if(demoStep == 12){
						mCurrentWrist += 6;
					}else if(demoStep == 13){
						mCurrentARM -= 9;
						
					}else if(demoStep == 14){
						mCurrentBase = 0;
						mStopDemo = 1;
					
					}
					/*
					else if(demoStep == 15){
						mCurrentWrist -= 5;
					}else if(demoStep == 16){
						mCurrentARM -= 6;
					}else if(demoStep == 17){
						mCurrentWrist -= 4;
					}else if(demoStep == 18){
						mCurrentARM -= 8;
					}else if(demoStep == 19){
						mCurrentWrist -= 6;
					}else if(demoStep == 20){
						mCurrentBase = -25;
						waitTime = 2000;
					}else if(demoStep == 21){
						mCurrentGrip = 0;
						waitTime = 500;
					}
					*/
					mDemoHandler.sendMessage(msg);
				}
			
    		}		
					
		
		}
    	
    }
    
    
    
  private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) 
			{
				synchronized (this)
				{
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
					{
						OpenAccessory(accessory);
					} 
					else 
					{
						Log.d("LED", "permission denied for accessory "+ accessory);
						
					}
					mPermissionRequestPending = false;
				}
			} 
			else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) 
			{
				UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null )//&& accessory.equals(usbaccessory))
				{
					CloseAccessory();
				}
			}else
			{
				Log.d("LED", "....");
			}
		}	
	};
	
    
	
	
	public class AccSensorClass implements SensorEventListener 
	{
    	private Sensor mAccelerometer;
    	    	    	
    	public void onSensorChanged(SensorEvent event){
    		/*get the sensor values*/
    		if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER){
    			return;
    		}
    		
    		/*if default position flag is set 
    		 * then dont update the base and arm values
    		 */
    		if(mDefaultPosition == 1)
    				return;
    		
    		
    		switch(mDisplay.getRotation())
    		{
	
    		case Surface.ROTATION_0:
    			mCurrentBase = (byte)event.values[0];
    			mCurrentARM = (byte)event.values[1];
    			break;
    			
    		case Surface.ROTATION_90:
    			mCurrentARM = (byte)event.values[0];
    			mCurrentBase = (byte)(-event.values[1]);
    			
    			break;
    		case Surface.ROTATION_180:
    	
    			mCurrentARM = (byte)(-event.values[1]);
    			mCurrentBase = (byte)(-event.values[0]);
    			
    			break;
    			
    		case Surface.ROTATION_270:
    			mCurrentARM = (byte)(-event.values[0]);
    			mCurrentBase = (byte)(event.values[1]);
     			break;
     		}
    		
    		
    		/*mCurrent Base are too low for this*/
    		mCurrentBase = (mCurrentBase * 4) + (byte)(((event.values[0]-mCurrentBase) * 100)/25);
    		mCurrentARM = (mCurrentARM * 4) + (byte)(((event.values[1]-mCurrentARM) * 100)/25);
    		/*my default location is -2*/
    		mCurrentARM += 2;
    		Base.setText("Base:" + mCurrentBase);
			Arm.setText("Arm:" + mCurrentARM);
			/*actually its reveresd*/
			mCurrentARM = -mCurrentARM;
    		
    		
    		
    		
    	}
    	
    	/*place holder only*/
    	public void onAccuracyChanged(Sensor sensor, int accuracy){
    		
    	}
    	
    	
    	
    	public AccSensorClass(Context context){
    		super();
    		/*get the accelerometer and set one listener*/
    		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    		mCurrentARM = 0;
    		mCurrentBase = 0;
    		
    		
    		/*send the first locations */
    		mBaseData = 1;
    		mArmData = 1;
    		mWristData = 1;
    		mGripData = 1;
    		mShoulderData = 1;
      	}
    	
    	public void StartSensor()
    	{
    		/*set the lsietenr*/
    		mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_UI);
    		
    	}
    	
    	public void StopSensor()
    	{
    		/*set the lsietenr*/
    		mSensorManager.unregisterListener(this);
    		
    	}
    	
    } /*end of AccSensorClass*/
	
	
}