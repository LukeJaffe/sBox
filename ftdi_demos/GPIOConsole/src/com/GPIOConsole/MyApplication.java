package com.GPIOConsole;

import android.os.Handler;
import android.os.Message;
import android.os.Looper;

import android.app.Application;
import java.util.HashMap;
import com.sss.consolehelper.CmdApp;

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
import android.content.SharedPreferences;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;


public class MyApplication extends Application
{
    public CmdApp cmdApp;

	private static final String ACTION_USB_PERMISSION =    "com.GPIODemo.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor;
	public FileInputStream inputstream;
	public FileOutputStream outputstream;
	public boolean mPermissionRequestPending = false;
	public boolean READ_ENABLE = true;
	public handler_thread handlerThread;
	
	private byte [] usbdata; 
    private byte []	writeusbdata;
    private int readcount;
	
    public Context global_context;
   
    public static String ManufacturerString = "mManufacturer=FTDI";
    public static String ModelString = "mModel=FTDIGPIODemo";
    public static String VersionString = "mVersion=1.0";	

    /* low level variables */
    public byte outMap; /*outmap*/
    public byte inMap; /*inmap*/
    public byte outData; /*output data*/
    public byte inData; /*input Data*/

		
    public static void main(HashMap<Integer, Object> args)
    {
        MyApplication app = new MyApplication(args);
    }

    public MyApplication(HashMap<Integer, Object> args)
    {
        super();
        cmdApp = new CmdApp(args);
        global_context = cmdApp.getContext();
        cmdApp.stdOut.println("main1");

        /*create an object of GPIO interface class*/
        usbdata = new byte[4]; 
        writeusbdata = new byte[4];
        
        /***********************USB handling**********************/
        
        usbmanager = (UsbManager) global_context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(global_context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);

        global_context.registerReceiver(mUsbReceiver, filter);
        
        inputstream = null;
        outputstream = null;
        cmdApp.stdOut.println("main2");

        /* Get USB permission */
        ResumeAccessory();
        cmdApp.stdOut.println("main3");

        /* Configure the port */
        outMap = 0xF;
        inMap = 0xF;
        ConfigPort(outMap, inMap);
        cmdApp.stdOut.println("main4");
        
        /* Write data to the port */
        outData = 0xF;
        WritePort(outData);
        cmdApp.stdOut.println("main5");
        cmdApp.stdOut.println("outMap: "+outMap);
        cmdApp.stdOut.println("outData: "+outData);
    }

	/*reset port*/
	 public void ResetPort()
	 {
		writeusbdata[0] = 0x14;
		writeusbdata[1] = 0x00;
		writeusbdata[2] = 0x00;
		writeusbdata[3] = 0x00;
			
		try {
			if(outputstream != null){
				outputstream.write(writeusbdata, 0,4);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	 
		/*configure data*/
		public void ConfigPort(byte configOutMap, byte configINMap){
			configOutMap |= 0x80; 	// GPIO pin 7 is OUT
			configINMap &= 0x7F;	// GPIO pin 7 is OUT	
			writeusbdata[0] = 0x11;
			writeusbdata[1] = 0x00;
			writeusbdata[2] = configOutMap;
			writeusbdata[3] = configINMap;
			
			
				try {
					if(outputstream != null){
						outputstream.write(writeusbdata, 0,4);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		/*write port*/
		public void WritePort(byte portData){
			portData |= 0x80; // GPIO pin 7 is high then LED is OFF
			writeusbdata[0] = 0x13;
			writeusbdata[1] = portData;
			writeusbdata[2] = 0x00;
			writeusbdata[3] = 0x00;
			
			
				try {
					if(outputstream != null){
						outputstream.write(writeusbdata, 0,4);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		/*read port*/
		public byte ReadPort(){
			return usbdata[1];
		}
		
		/*resume accessory*/
		public void ResumeAccessory()
		{
            cmdApp.stdOut.println("resume1");
			// Intent intent = getIntent();
			if (inputstream != null && outputstream != null) {
                cmdApp.stdOut.println("inputstream or outputstream is null");
				return;
			}
			
			UsbAccessory[] accessories = usbmanager.getAccessoryList();
			if(accessories != null)
			{
                cmdApp.stdOut.println("Accessory Attached");
			}
            else
            {
                cmdApp.stdOut.println("Accessory NOT Attached!");
            }

			UsbAccessory accessory = (accessories == null ? null : accessories[0]);
			if (accessory != null) {
				if( -1 == accessory.toString().indexOf(ManufacturerString))
				{
                    cmdApp.stdOut.println("Manufacturer is not matched!");
					return;
				}

				if( -1 == accessory.toString().indexOf(ModelString))
				{
					//Toast.makeText(global_context, "Model is not matched!", Toast.LENGTH_SHORT).show();
                    cmdApp.stdOut.println("Model is not matched!");
					return;
				}
				
				if( -1 == accessory.toString().indexOf(VersionString))
				{
					//Toast.makeText(global_context, "Version is not matched!", Toast.LENGTH_SHORT).show();
                    cmdApp.stdOut.println("Version is not matched!");
					return;
				}
				
				//Toast.makeText(global_context, "Manufacturer, Model & Version are matched!", Toast.LENGTH_SHORT).show();
                cmdApp.stdOut.println("Manufacturer, Model & Version are matched!");

				if (usbmanager.hasPermission(accessory)) {
                    cmdApp.stdOut.println("Opening accessory...");
					OpenAccessory(accessory);
				} 
				else
				{
                    cmdApp.stdOut.println("Permission not yet granted...");
					synchronized (mUsbReceiver) {
						if (!mPermissionRequestPending) {
                            cmdApp.stdOut.println("Request USB Permission");
                            
							usbmanager.requestPermission(accessory,
									mPermissionIntent);
							mPermissionRequestPending = true;
						}
					}
				}
			} 
            else 
            {
                cmdApp.stdOut.println("Accessory is null...");
            }

		}
		
		/*destroy accessory*/
		public void DestroyAccessory(){
			
			READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
			ResetPort(); // send dummy data for instream.read going
			try{Thread.sleep(10);}
	 		catch(Exception e){}
			CloseAccessory();
		}
		
/*********************helper routines*************************************************/		
		
		public void OpenAccessory(UsbAccessory accessory)
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
			
			handlerThread = new handler_thread(inputstream);
			handlerThread.start();
		}
		
		private void CloseAccessory()
		{
			try{
				if(filedescriptor != null)
					filedescriptor.close();
				
			}catch (IOException e){}
			
			try {
				if(inputstream != null)
						inputstream.close();
			} catch(IOException e){}
			
			try {
				if(outputstream != null)
						outputstream.close();
				
			}catch(IOException e){}
			/*FIXME, add the notfication also to close the application*/
			
			filedescriptor = null;
			inputstream = null;
			outputstream = null;
		
			System.exit(0);
		}
		
				
		/***********USB broadcast receiver*******************************************/
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
							Toast.makeText(global_context, "Allow USB Permission", Toast.LENGTH_SHORT).show();
							OpenAccessory(accessory);
						} 
						else 
						{
							Toast.makeText(global_context, "Deny USB Permission", Toast.LENGTH_SHORT).show();
							Log.d("LED", "permission denied for accessory "+ accessory);
							
						}
						mPermissionRequestPending = false;
					}
				} 
				else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) 
				{
						CloseAccessory();
				}else
				{
					Log.d("LED", "....");
				}
			}	
		};
	    
		//usb input data handler
		private class handler_thread  extends Thread {
			FileInputStream instream;
			
			handler_thread(FileInputStream stream ){
				instream = stream;
			}
			
			public void run()
			{	
				while(READ_ENABLE == true)
				{
					try{
						if(instream != null)
						{	
						readcount = instream.read(usbdata,0,4);
						}
					}catch (IOException e){}
				}
			}
		}
}

