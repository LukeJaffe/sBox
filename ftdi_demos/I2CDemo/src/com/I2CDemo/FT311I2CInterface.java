//User must modify the below package with their package name
package com.I2CDemo; 
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
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;


/******************************FT311 GPIO interface class******************************************/
public class FT311I2CInterface extends Activity
{
	
	private static final String ACTION_USB_PERMISSION =    "com.I2CDemo.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor;
	public FileInputStream inputstream;
	public FileOutputStream outputstream;
	public boolean mPermissionRequestPending = false;
	public boolean READ_ENABLE = true;
	public boolean accessory_attached = false;
	public handler_thread handlerThread;
	
	private byte [] usbdata; 
    private byte []	writeusbdata;
    private int readcount;
    private byte status;
    private byte  maxnumbytes = 60;
    public boolean datareceived = false;
	
    
    public Context global_context;
   
    public static String ManufacturerString = "mManufacturer=FTDI";
    public static String ModelString = "mModel=FTDII2CDemo";
    public static String VersionString = "mVersion=1.0";	
		
		/*constructor*/
	 public FT311I2CInterface(Context context){
		 	super();
		 	global_context = context;
			/*shall we start a thread here or what*/
			usbdata = new byte[64]; 
	        writeusbdata = new byte[64];
	        
	        /***********************USB handling******************************************/
			
	        usbmanager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	       // Log.d("LED", "usbmanager" +usbmanager);
	        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
	        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
	       filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
	       context.registerReceiver(mUsbReceiver, filter);
	        
	       inputstream = null;
	       outputstream = null;
		}
	 
	 	/*reset method*/
	 	public void Reset()
	 	{
	 		/*create the packet*/
	 		writeusbdata[0] = 0x34;
			writeusbdata[1] = 0x00;
			writeusbdata[2] = 0x00;
			writeusbdata[3] = 0x00;
			writeusbdata[4] = 0x00;
			
			/*send the packet over the USB*/
			SendPacket(5);
	 	}
	 	
	 	
	 	/*SetFrequency*/
	 	public void SetFrequency(byte freq)
	 	{
	 		/*check for maximum and minimum freq*/
	 		if(freq > 92)
	 			freq = 92;
	 		
	 		if (freq < 23)
	 			freq = 23;
	 		
	 		/*create the packet*/
	 		writeusbdata[0] = 0x31;
	 		switch(freq)
	 		{
	 		case 23:
	 			writeusbdata[1] = 10;
	 			break;
	 		case 44:
	 			writeusbdata[1] = 21;
	 			break;
	 		case 60:
	 			writeusbdata[1] = 30;
	 			break;
	 		default:
	 			writeusbdata[1] = 56;
	 			break;
	 		}
			writeusbdata[2] = 0x00;
			writeusbdata[3] = 0x00;
			writeusbdata[4] = 0x00;
			
			/*send the packet over the USB*/
			SendPacket(5);
	 	}
	 	
	 	
	 	/*write data*/
	 	public byte WriteData(byte i2cDeviceAddress,byte transferOptions,
	 					     byte numBytes, byte[] buffer, 
	 					     byte [] actualNumBytes)
	 	{
	 		
	 		status = 0x01; /*error by default*/
	 		/*
	 		 * if num bytes are more than maximum limit
	 		 */
	 		if(numBytes < 1){
	 			
	 			actualNumBytes[0] = (byte)0x00;
	 			/*return the status with the error in the command*/
	 			return status;
	 		}
	 		
	 		/*check for maximum limit*/
	 		if(numBytes > maxnumbytes){
	 			numBytes = maxnumbytes;
		
	 		}
	 		
	 		
	 		/*prepare the packet to be sent*/
	 		for(int count = 0;count<numBytes;count++)
	 		{
	 			
	 			writeusbdata[4+count] = (byte)buffer[count];
 			
	 		}
	 		
	 		/*prepare the usbpacket*/
	 		writeusbdata[0] = 0x32;
	 		writeusbdata[1] = i2cDeviceAddress;
	 		writeusbdata[2] = transferOptions;
	 		writeusbdata[3] = numBytes;

	 		SendPacket((int)(numBytes+4));
	 		
	 		datareceived = false;
	 		/*wait while the data is received*/
	 		/*FIXME, may be create a thread to wait on , but any
	 		 * way has to wait in while loop
	 		 */
	 		while(true){
	 			
	 			if(datareceived == true){
	 				break;
	 			}
	 		}
	 		
	 		/*by default it error*/
	 		status = 0x01;
	 		/*check for the received data*/
	 		if(usbdata[0] == 0x32)
	 		{
	 			/*check for return error status*/
	 			status = usbdata[1];
	 			
	 			 		
	 			/*updated the written bytes*/
	 			actualNumBytes[0] = usbdata[3];
	 		}
	 		datareceived = false;
	 		return status;
	 	}
	 	
	 	/*read data*/
	 	public byte ReadData(byte i2cDeviceAddress,byte transferOptions,
	 						  byte numBytes, byte[] readBuffer,
	 						  byte [] actualNumBytes)
	 	{
	 			status = 0x01; /*error by default*/
	 			
	 			/*should be at least one byte to read*/
	 			if(numBytes < 1){
	 				return status;
	 			}
	 			
	 			/*check for max limit*/
	 			if(numBytes > maxnumbytes){
	 				numBytes = maxnumbytes;
	 			}
	 			
	 			
	 			/*prepare the packet to send this command*/
	 			writeusbdata[0] = 0x33; /*read data command*/
	 			writeusbdata[1] = i2cDeviceAddress; /*device address*/
	 			writeusbdata[2] = transferOptions; /*transfer options*/
	 			writeusbdata[3] = numBytes; /*number of bytes*/
	 			
	 			
	 			/*send the data on USB bus*/
	 			SendPacket(4);
	 			
	 			datareceived = false;
	 			/*wait for data to arrive*/
	 			while(true)
	 			{
		 			if(datareceived == true){
		 				break;
		 			}
		 		}
	 			
	 			
	 						
	 			
	 			
	 			/*check the received data*/
	 			if(usbdata[0] == 0x33)
	 			{
	 				/*check the return status*/
	 				status = usbdata[1];
	 				
	 				/*check the received data length*/
	 				numBytes = usbdata[3];
	 				if(numBytes > maxnumbytes){
	 					numBytes = maxnumbytes;
	 				}
	 				
	 				for(int count = 0; count<numBytes;count++)
	 				{
	 					readBuffer[count] = usbdata[4+count];
	 				}
	 				/*update the actual number of bytes*/
	 				actualNumBytes[0] = numBytes;
	 				datareceived = false;
	 			}
	 		return status;
	 	}
	 		
		
		/*method to send on USB*/
		private void SendPacket(int numBytes)
		{
			
			
			try {
				if(outputstream != null){
					outputstream.write(writeusbdata, 0,numBytes);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		
		
		
		/*resume accessory*/
		public void ResumeAccessory()
		{
			// Intent intent = getIntent();
			if (inputstream != null && outputstream != null) {
				return;
			}
			
			UsbAccessory[] accessories = usbmanager.getAccessoryList();
			if(accessories != null)
			{
				Toast.makeText(global_context, "Accessory Attached", Toast.LENGTH_SHORT).show();
			}
			else
			{
				accessory_attached = false;
				return;
			}

			UsbAccessory accessory = (accessories == null ? null : accessories[0]);
			if (accessory != null) {
				if( -1 == accessory.toString().indexOf(ManufacturerString))
				{
					Toast.makeText(global_context, "Manufacturer is not matched!", Toast.LENGTH_SHORT).show();
					return;
				}

				if( -1 == accessory.toString().indexOf(ModelString))
				{
					Toast.makeText(global_context, "Model is not matched!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if( -1 == accessory.toString().indexOf(VersionString))
				{
					Toast.makeText(global_context, "Version is not matched!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(global_context, "Manufacturer, Model & Version are matched!", Toast.LENGTH_SHORT).show();
				accessory_attached = true;

				if (usbmanager.hasPermission(accessory)) {
					OpenAccessory(accessory);
				} 
				else
				{
					synchronized (mUsbReceiver) {
						if (!mPermissionRequestPending) {
							Toast.makeText(global_context, "Request USB Permission", Toast.LENGTH_SHORT).show();
							usbmanager.requestPermission(accessory,
									mPermissionIntent);
							mPermissionRequestPending = true;
						}
				}
			}
			} else {}

		}
		
		/*destroy accessory*/
		public void DestroyAccessory(){
			global_context.unregisterReceiver(mUsbReceiver);
			if(accessory_attached == true)
			{
				READ_ENABLE = false;
				byte i2cDeviceAddress = 1;
				byte transferOptions = bOption.START_BIT;
			    byte numBytes = 2;
			    byte [] actualNumBytes = new byte[1];
			    byte [] readBuffer = new byte[60];
			    //byte deviceAddress = 1;
			    readBuffer[0] = 1;

				ReadData(i2cDeviceAddress,transferOptions,
						  numBytes, readBuffer,
						  actualNumBytes);
				try{Thread.sleep(10);}
		 		catch(Exception e){}
			}
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
	    
		
		/*usb input data handler*/
		private class handler_thread  extends Thread {
			FileInputStream instream;
			
			handler_thread(FileInputStream stream ){
				instream = stream;
			}
			
			public void run()
			{
				
				while(READ_ENABLE == true)
				{
					try
					{
						/*dont overwrite the previous buffer*/
						if((instream != null) && (datareceived==false))
						{
							readcount = instream.read(usbdata,0,64);
							if(readcount > 0)
							{
								datareceived = true;
								/*send only when you find something*/	
							}
						}
					}catch (IOException e){}
				}
			}
		}
	    
		
		
	}