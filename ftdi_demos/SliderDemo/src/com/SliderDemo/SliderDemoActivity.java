package com.SliderDemo;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;




public class SliderDemoActivity extends Activity {
	
	private static final String ACTION_USB_PERMISSION =    "com.SliderDemo.USB_PERMISSION";
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
	public int timeStamp, prevTimeStamp;
	public long currentData,prevData;
	public float mUsbX, mUsbY, mPrevUsbX, mPrevUsbY;
	
	
	public float mBallX, mBallY;
	public float mPrevBallX, mPrevBallY;
	public long mCurrentTime,mPrevTime;
	public byte mDir, mPrevDir;
	public byte mVertical = 0;
	public int mSteps;
	
	
	public int readcount;
    /*thread to listen USB data*/
    public handler_thread handlerThread;
    public draw_helper_thread drawHelperThread;
    
    public testTouch mTestTouch;
	 
	    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mTestTouch = new testTouch(this);
        setContentView(mTestTouch);
        
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
    }
    
    
    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        
        
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
		
		drawHelperThread = new draw_helper_thread(drawHelperHandler);
		drawHelperThread.start();
		
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
	
	
	public void ReadUsbData()
	{
		
		   mVertical = usbdata[1];  
		   mCurrentTime = System.nanoTime();
		   mUsbX = usbdata[3];
		 
		   if(mPrevTime != 0)
			   mTestTouch.ProcessUsbTouch();
		   
		   mPrevTime = mCurrentTime;
		   mPrevUsbX = mUsbX;
	}
	
	final Handler handler =  new Handler()
    {
    	@Override 
    	public void handleMessage(Message msg)
    	{
    		ReadUsbData();
    	}
    	
    };
    
    
    final Handler drawHelperHandler =  new Handler()
    {
    	@Override 
    	public void handleMessage(Message msg)
    	{
    		//ReadUsbData();
    		
    		/*draw those many steps*/
			if(mSteps != 0)
			{
			
				if(mDir == 1)
					mBallX = 5;
				else
					mBallX = -5;
			
				mSteps--;
				
			}
    	}	
    	
    };
    
    
    
    
    private class draw_helper_thread extends Thread 
    {
    	Handler mDrawHandler;
    	
    	draw_helper_thread(Handler handler){
    		mDrawHandler = handler;
    	}
    	
    	
    	public void run()
		{
			
			while(true)
			{
				Message msg = mDrawHandler.obtainMessage();
				
				
					try{	
						Thread.sleep(50);
					} catch(InterruptedException e){}
				
					
					mDrawHandler.sendMessage(msg);
					
				//}	
			}
		}
    	
    }
   
  
	
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
	
    
	public class testTouch extends View
	{
		 // diameter of the balls in meters
	    private static final float sBallDiameter = 0.01f;
	    //private static final float sBallDiameter2 = sBallDiameter * sBallDiameter;
		float mXDpi,mYDpi;
		 private float mMetersToPixelsX;
	     private float mMetersToPixelsY;
	     private Bitmap mBitmap;
	     private float mXOrigin;
	     private float mYOrigin;
	     private float mXLimit;
	     private float mYLimit;
	     private Bitmap banner;

	     public testTouch(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			DisplayMetrics metrics = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(metrics);
	        mXDpi = metrics.xdpi;
	        mYDpi = metrics.ydpi;
	        mMetersToPixelsX = mXDpi / 0.0254f;
	        mMetersToPixelsY = mYDpi / 0.0254f;

	        // rescale the ball so it's about 0.5 cm on screen
	        Bitmap ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
	        final int dstWidth = (int) (sBallDiameter * mMetersToPixelsX + 0.5f);
	        final int dstHeight = (int) (sBallDiameter * mMetersToPixelsY + 0.5f);
	        mBitmap = Bitmap.createScaledBitmap(ball, dstWidth, dstHeight, true);
	        
	        mUsbX = 0;
	        mPrevUsbX = 0;
	        
	        banner = BitmapFactory.decodeResource(getResources(), R.drawable.banner);
	        
		}
		
		public void ProcessUsbTouch()
		{
			/*wow, so this was changing our false direction*/
			//mDir = 4;
			long timeDiff = (mCurrentTime-mPrevTime)/1000000; 
			
			if(timeDiff < 1000){
				/*take care of tapping*/
				if(mUsbX == mPrevUsbX)
					return;
				/*take care of the tap*/
				if(mUsbX > mPrevUsbX){
					/*forward direction*/
					mDir = 1;
				}else{
					mDir = 0;
				}
				
				/*take care of switching the fingers very fast*/
				if(mUsbX == 0x01 ||  mUsbX == 0x08 || mUsbX == 0x10 || mUsbX == 0x80)
					mPrevUsbX = 0;
				
				
				if(mPrevDir != mDir)
						mSteps = 0;
				
				mPrevDir = mDir;
				
				if(timeDiff < 50){
					/*we move very fast*/	
					//mBallX = 100;
					mSteps = 15;
					
				}else				
				if(timeDiff < 100){
					/*we move very fast*/	
					//mBallX = 50;
					mSteps = 10;
					
					
				}else				
				if(timeDiff < 200){
					mSteps = 5;
		
				}
			}
			
		}
		
		
		 @Override
	     protected void onSizeChanged(int w, int h, int oldw, int oldh) 
		 {
	         // compute the origin of the screen relative to the origin of
	         // the bitmap
	         //mXOrigin = (w - mBitmap.getWidth()) * 0.5f;
	        // mYOrigin = (h - mBitmap.getHeight()) * 0.5f;
			 mXLimit = (w - (mBitmap.getWidth() * 2.0f));
			 mYLimit = (h - (mBitmap.getHeight()* 2.0f));
	         mXOrigin = 10;//mBitmap.getWidth()*2.0f;
	         /*put the ball in the middle*/
	         mYOrigin = h* 0.5f;
	         mPrevBallX = mXOrigin;
	         mPrevBallY  = mYOrigin;
	     }
		
		
		@Override
		public 
		void onDraw(Canvas canvas)
		{
			long timeStamp = ((System.nanoTime()-mCurrentTime)/1000000); 
			
			canvas.drawBitmap(banner,0,0,null);
			
			/*check for the vertical map*/
			if(mVertical == 1){
				mBallY = -mBallX;
				mBallX = 0;
			}
			float xc = mBallX+mPrevBallX;
	        float yc = mBallY+mPrevBallY;
	    //    final float xs = mMetersToPixelsX;
	     //   final float ys = mMetersToPixelsY;
	        final Bitmap bitmap = mBitmap;
	    
			/*reset to prevvious state*/	
			if( timeStamp > 3000){
				mCurrentTime = 0;
				mPrevTime = 0;
				mPrevUsbX = 0;
				mUsbX = 0;
				
				
			}
	        
	        
	      
	        mPrevBallY = yc;
	        mBallX = 0;
	        mBallY = 0; 
	        
	        /*take care of the boundry condition*/
	        if(xc >=mXLimit){
	        	
	        		xc = mXLimit;
	        		
	        }
	        if(xc < 10)
	        {
	        	xc  = 10;
	        }
	        
	        if(yc >= mYLimit){
	        	yc = mYLimit;
	        }
	        
	        if(yc < 10){
	        	yc = 10;
	        }
	        
	        
	        mPrevBallX = xc;
	        mPrevBallY = yc;
	        canvas.drawBitmap(bitmap, xc, yc, null);
	        invalidate();
		}
	
	}/*end of testTouch class*/
	
	
}