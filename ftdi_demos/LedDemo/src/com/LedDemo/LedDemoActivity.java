package com.LedDemo;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.LedDemo.R.drawable;

public class LedDemoActivity extends Activity
{
	
	private static final String ACTION_USB_PERMISSION =    "com.LedDemo.USB_PERMISSION";
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
	public byte  ledPrevMap = 0x00;
	//public byte[] usbdataIN;
	
	public SeekBar volumecontrol;
    public ProgressBar slider;
    
    public ImageButton button1; //Button led1;
    public ImageButton button2; //Button led2;
    public ImageButton button3; //Button led3;
    public ImageButton button4; //Button led4;
    
    public ImageView led1;
    public ImageView led2;
    public ImageView led3;
    public ImageView led4;
    public ImageView ledvolume;
    
    public int readcount;
    /*thread to listen USB data*/
    public handler_thread handlerThread;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        usbdata = new byte[4]; 
        writeusbdata = new byte[4];
        
        usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d("LED", "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        Log.d("LED", "filter" +filter);
        registerReceiver(mUsbReceiver, filter);
        
        led1 = (ImageView) findViewById(R.id.LED1);
		led2 = (ImageView) findViewById(R.id.LED2);
		led3 = (ImageView) findViewById(R.id.LED3);
		led4 = (ImageView) findViewById(R.id.LED4);
               
        button1 = (ImageButton) findViewById(R.id.Button1);
        button1.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		byte ibutton = 0x01;
        		Log.d("LED", "Button 1 pressed");
        		
        		ledPrevMap ^= 0x01;
        		
        		if((ledPrevMap & 0x01) == 0x01){
        				led1.setImageResource(drawable.image100);
        			}
        			else{
        				led1.setImageResource(drawable.image0);		
        		}
        			
        		
        		
        		//v.bringToFront();
        		WriteUsbData(ibutton);
        	}
		});
        
        button2 = (ImageButton) findViewById(R.id.Button2);
        button2.setOnClickListener(new View.OnClickListener()
        {		
			public void onClick(View v)
			{
        		byte ibutton = 0x02;
        		//v.bringToFront();
        		
        		ledPrevMap ^= 0x02;
        		
        		if((ledPrevMap & 0x02) == 0x02){
        				led2.setImageResource(drawable.image100);
        			}
        			else{
        				led2.setImageResource(drawable.image0);		
        		}
        		
        		WriteUsbData(ibutton);	
			}
		});
        
        button3 = (ImageButton) findViewById(R.id.Button3);
        button3.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
        		byte ibutton = 0x04;
        		//v.bringToFront();
        		
        		ledPrevMap ^= 0x04;
        		
        		if((ledPrevMap & 0x04) == 0x04){
        				led3.setImageResource(drawable.image100);
        			}
        			else{
        				led3.setImageResource(drawable.image0);		
        		}
        		WriteUsbData(ibutton);	
			}
		});
        
        button4 = (ImageButton) findViewById(R.id.Button4);
        button4.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
        		byte ibutton = 0x08;
        		//v.bringToFront();
        		ledPrevMap ^= 0x08;
        		
        		if((ledPrevMap & 0x08) == 0x08){
        				led4.setImageResource(drawable.image100);
        			}
        			else{
        				led4.setImageResource(drawable.image0);		
        		}
        		WriteUsbData(ibutton);	
			}
		});   
        
        volumecontrol = (SeekBar)findViewById(R.id.seekBar1);
        
        //set the max value to 50
        volumecontrol.setMax(50);
        
        volumecontrol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
        {		
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				writeusbdata[0] = 1;
				writeusbdata[1] = 1;
				writeusbdata[2] = 2;
				writeusbdata[3] = (byte) progress;
				try
				{
					if(outputstream != null)
					{
						outputstream.write(writeusbdata,0,4);
					}
				}
				catch (IOException e)
				{					
				}
				
				ledvolume = (ImageView) findViewById(R.id.LEDvolume);
				if(progress == 0)
				{
					ledvolume.setImageResource(drawable.image0);
				}
				else if(progress > 0 && (int)progress < 11)
				{
					ledvolume.setImageResource(drawable.image10);
				}
				else if (progress > 10 && progress < 21)
				{
					ledvolume.setImageResource(drawable.image20);
				}
				else if (progress > 20 && progress < 36)
				{
					ledvolume.setImageResource(drawable.image35);
				}
				else if (progress > 35 && progress < 51)
				{
					ledvolume.setImageResource(drawable.image50);
				}
				else if (progress > 50 && progress < 66)
				{
					ledvolume.setImageResource(drawable.image65);
				}
				else if (progress > 65 && progress < 76)
				{
					ledvolume.setImageResource(drawable.image75);
				}
				else if (progress > 75 && progress < 91)
				{
					ledvolume.setImageResource(drawable.image90);
				}
				else
				{
					ledvolume.setImageResource(drawable.image100);
				}
			}
        });
    }
        
        
    @Override
    public void onResume()
    {
    		super.onResume();
    		Intent intent = getIntent();
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
	public void onDestroy()
	{
		unregisterReceiver(mUsbReceiver);
		//CloseAccessory();
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
		
		handlerThread = new handler_thread(handler, inputstream);
		handlerThread.start();
		
	} /*end OpenAccessory*/
    
	public void ReadUsbData()
	{
		if(usbdata[0] == 0)
		{
			/*
			led1 = (ImageView) findViewById(R.id.LED1);
			led2 = (ImageView) findViewById(R.id.LED2);
			led3 = (ImageView) findViewById(R.id.LED3);
			led4 = (ImageView) findViewById(R.id.LED4);
			*/
			ledPrevMap ^= usbdata[3];
			usbdata[3] = ledPrevMap;
			
			if((usbdata[3]& 0x01) == 0x01)
			{
				led1.setImageResource(drawable.image100);
			}
			else{
				led1.setImageResource(drawable.image0);		
			}
			
			if((usbdata[3]& 0x02) == 0x02){
				led2.setImageResource(drawable.image100);
			}else{
				led2.setImageResource(drawable.image0);
			}
			
			if((usbdata[3]& 0x04) == 0x04){
				led3.setImageResource(drawable.image100);
			}else{
				led3.setImageResource(drawable.image0);
			}
			
			if((usbdata[3]& 0x08) == 0x08){
				led4.setImageResource(drawable.image100);
			}else{
				led4.setImageResource(drawable.image0);
			}
		}
		else if (usbdata[0] == 1)
		{
			ledvolume = (ImageView) findViewById(R.id.LEDvolume);
			if((int)usbdata[3] == 0)
			{
				ledvolume.setImageResource(drawable.image0);
			}
			else if((int)usbdata[3] > 0 && (int)usbdata[3] < 11)
			{
				ledvolume.setImageResource(drawable.image10);
			}
			else if ((int)usbdata[3] > 10 && (int)usbdata[3] < 21)
			{
				ledvolume.setImageResource(drawable.image20);
			}
			else if ((int)usbdata[3] > 20 && (int)usbdata[3] < 36)
			{
				ledvolume.setImageResource(drawable.image35);
			}
			else if ((int)usbdata[3] > 35 && (int)usbdata[3] < 51)
			{
				ledvolume.setImageResource(drawable.image50);
			}
			else if ((int)usbdata[3] > 50 && (int)usbdata[3] < 66)
			{
				ledvolume.setImageResource(drawable.image65);
			}
			else if ((int)usbdata[3] > 65 && (int)usbdata[3] < 76)
			{
				ledvolume.setImageResource(drawable.image75);
			}
			else if ((int)usbdata[3] > 75 && (int)usbdata[3] < 91)
			{
				ledvolume.setImageResource(drawable.image90);
			}
			else
			{
				ledvolume.setImageResource(drawable.image100);
			}
		}
	}
	
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
		
	
	final Handler handler =  new Handler()
    {
    	@Override 
    	public void handleMessage(Message msg)
    	{
    		ReadUsbData();
    	}
    	
    };
  
	
	private class handler_thread  extends Thread {
		Handler mHandler;
		FileInputStream instream;
		
		handler_thread(Handler h,FileInputStream stream ){
			mHandler = h;
			instream = stream;
		}
		
		public void run()
		{
			
			while(true)
			{
				Message msg = mHandler.obtainMessage();
				try{
					if(instream != null)
					{	
					readcount = instream.read(usbdata,0,4);
					if(readcount > 0)
					{
						msg.arg1 = usbdata[0];
						msg.arg2 = usbdata[3];
					}
					mHandler.sendMessage(msg);
					}
					}catch (IOException e){}
			}
		}
	}
	
	public void WriteUsbData(byte iButton){	
		writeusbdata[0] = 0;
		writeusbdata[1] = 1;
		writeusbdata[2] = 2;
		writeusbdata[3] = iButton;
		
		Log.d("LED", "pressed " +iButton);
		
		try{
			if(outputstream != null){
				outputstream.write(writeusbdata,0,4);
			}
		}
		catch (IOException e) {}		
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
};



