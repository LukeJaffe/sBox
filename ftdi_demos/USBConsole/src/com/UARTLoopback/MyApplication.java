package com.UARTLoopback;

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
    private static Context context;

	public handler_thread handlerThread;

	/* local variables */
	byte[] writeBuffer;
	byte[] readBuffer2;
	char[] readBufferToChar;
	int[] actualNumBytes;

	private static final String ACTION_USB_PERMISSION =    "com.UARTLoopback.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor = null;
	public FileInputStream inputstream = null;
	public FileOutputStream outputstream = null;
	public boolean mPermissionRequestPending = false;
	public read_thread readThread;

	private byte [] usbdata; 
	private byte []	writeusbdata;
	private byte  [] readBuffer; /*circular buffer*/
	private int readcount;
	private int totalBytes;
	private int writeIndex;
	private int readIndex;
	private byte status;
	final int  maxnumbytes = 65536;

	public boolean datareceived = false;
	public boolean READ_ENABLE = false;
	public boolean accessory_attached = false;

	public Context global_context;
    public CmdApp cmdApp;

	public static String ManufacturerString = "mManufacturer=FTDI";
	public static String ModelString1 = "mModel=FTDIUARTDemo";
	public static String ModelString2 = "mModel=Android Accessory FT311D";
	public static String VersionString = "mVersion=1.0";

	public SharedPreferences intsharePrefSettings;
    public MyApplication(HashMap<Integer, Object> args)
    {
        super();
        cmdApp = new CmdApp(args);
        context = cmdApp.getContext();
		global_context = context;

		/*shall we start a thread here or what*/
		usbdata = new byte[1024]; 
		writeusbdata = new byte[256];
		/*128(make it 256, but looks like bytes should be enough)*/
		readBuffer = new byte [maxnumbytes];

		readIndex = 0;
		writeIndex = 0;
		/***********************USB handling******************************************/

		usbmanager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		// Log.d("LED", "usbmanager" +usbmanager);
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		context.registerReceiver(mUsbReceiver, filter);

		inputstream = null;
		outputstream = null;
        cmdApp.stdOut.println("main1");
        /* Try resume accessory? */
        ResumeAccessory();
        cmdApp.stdOut.println("main2");
        /* Configure the UART channel */
		int baudRate = 9600;
		byte dataBit = 8;
		byte stopBit = 1;
		byte parity = 0;
		byte flowControl = 0;
		SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
        cmdApp.stdOut.println("main3");
        /* Set up the read thread */
		writeBuffer = new byte[64];
		readBuffer2 = new byte[4096];
		readBufferToChar = new char[4096]; 
		actualNumBytes = new int[1];
		handlerThread = new handler_thread(handler);
		handlerThread.start();
        cmdApp.stdOut.println("main4");
        /* Write some bytes! */
        while (true)
        {
            try 
            {
                Thread.sleep(200);
            } 
            catch (Exception e) 
            {
                cmdApp.stdOut.println("wait exception!");
            }
            //cmdApp.stdOut.println("Attempt write:");
            //SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
            write();         
        }
    }

    public void write()
    {
    	String destStr = "testmessage\0";
		int numBytes = destStr.length();
		for (int i = 0; i < numBytes; i++) {
			writeBuffer[i] = (byte)destStr.charAt(i);
		}
		SendData(numBytes, writeBuffer);
    }

    public static void main(HashMap<Integer, Object> args)
    {
        MyApplication app = new MyApplication(args);
    }

	final Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
            cmdApp.stdOut.println("got a message!");
			
			for(int i=0; i<actualNumBytes[0]; i++)
			{
				readBufferToChar[i] = (char)readBuffer2[i];
			}
            if(actualNumBytes[0] >= 1)    		
            {
	            StringBuffer readSB = new StringBuffer();
                readSB.append(String.copyValueOf(readBufferToChar, 0, actualNumBytes[0]));
                cmdApp.stdOut.println(readSB);
            }
		}
	};

	/* usb input data handler */ 
	private class handler_thread extends Thread {
		Handler mHandler;

		/* constructor */ 
		handler_thread(Handler h) {
			mHandler = h;
		}

		public void run() {
			Message msg;

			while (true) 
            {
				try 
                {
					Thread.sleep(200);
				} 
                catch (Exception e) 
                {
                    cmdApp.stdOut.println("wait exception!");
				}

                byte read_status = 0;
                try
                {
				    read_status = ReadData(4096, readBuffer2,actualNumBytes);
                }
                catch (Exception e)
                {
                    cmdApp.stdOut.println("read exception!");
                }

                try
                {
                    if (read_status == 0x00 && actualNumBytes[0] > 0) 
                    {
                        cmdApp.stdOut.println("received bytes!");
                        msg = mHandler.obtainMessage();
                        mHandler.sendMessage(msg);
                    }
                }
                catch (Exception e)
                {
                    cmdApp.stdOut.println("check exception!");
                }
			}
		}
	}

	public void SetConfig(int baud, byte dataBits, byte stopBits,
			byte parity, byte flowControl)
	{

		/*prepare the baud rate buffer*/
		writeusbdata[0] = (byte)baud;
		writeusbdata[1] = (byte)(baud >> 8);
		writeusbdata[2] = (byte)(baud >> 16);
		writeusbdata[3] = (byte)(baud >> 24);

		/*data bits*/
		writeusbdata[4] = dataBits;
		/*stop bits*/
		writeusbdata[5] = stopBits;
		/*parity*/
		writeusbdata[6] = parity;
		/*flow control*/
		writeusbdata[7] = flowControl;

		/*send the UART configuration packet*/
		SendPacket((int)8);
	}


	/*write data*/
	public byte SendData(int numBytes, byte[] buffer) 
	{
		status = 0x00; /*success by default*/
		/*
		 * if num bytes are more than maximum limit
		 */
		if(numBytes < 1){
			/*return the status with the error in the command*/
			return status;
		}

		/*check for maximum limit*/
		if(numBytes > 256){
			numBytes = 256;
		}

		/*prepare the packet to be sent*/
		for(int count = 0;count<numBytes;count++)
		{	
			writeusbdata[count] = buffer[count];
		}

		if(numBytes != 64)
		{
            //cmdApp.stdOut.println("sending packet");
			SendPacket(numBytes);
		}
		else
		{
			byte temp = writeusbdata[63];
			SendPacket(63);
			writeusbdata[0] = temp;
			SendPacket(1);
		}

		return status;
	}

	/*read data*/
	public byte ReadData(int numBytes,byte[] buffer, int [] actualNumBytes)
	{
		status = 0x00; /*success by default*/

		/*should be at least one byte to read*/
		if((numBytes < 1) || (totalBytes == 0)){
			actualNumBytes[0] = 0;
			status = 0x01;
			return status;
		}

		/*check for max limit*/
		if(numBytes > totalBytes)
			numBytes = totalBytes;

		/*update the number of bytes available*/
		totalBytes -= numBytes;

		actualNumBytes[0] = numBytes;	

		/*copy to the user buffer*/	
		for(int count = 0; count<numBytes;count++)
		{
			buffer[count] = readBuffer[readIndex];
			readIndex++;
			/*shouldnt read more than what is there in the buffer,
			 * 	so no need to check the overflow
			 */
			readIndex %= maxnumbytes;
		}
		return status;
	}

	/*method to send on USB*/
	private void SendPacket(int numBytes)
	{	
		try 
        {
			if(outputstream != null)
            {
                //cmdApp.stdOut.println("writing # bytes: "+numBytes);
				outputstream.write(writeusbdata, 0,numBytes);
			}
            else
            {
                cmdApp.stdOut.println("outputstream == null");
            }
		} 
        catch (IOException e) 
        {
            cmdApp.stdOut.println("IOException in SendPacket: "+e);
		}
	}

	/*resume accessory*/
	public int ResumeAccessory()
	{
		// Intent intent = getIntent();
		if (inputstream != null && outputstream != null) {
            cmdApp.stdOut.println("Streams are not null");
			return 1;
		}

		UsbAccessory[] accessories = usbmanager.getAccessoryList();
        for (int i = 0; i < accessories.length; i++)
        {
            cmdApp.stdOut.println(accessories[i]);
        }

		if(accessories != null)
		{
			//Toast.makeText(global_context, "Accessory Attached", Toast.LENGTH_SHORT).show();
            cmdApp.stdOut.println("Accessory Attached");
		}
		else
		{
			// return 2 for accessory detached case
			//Log.e(">>@@","ResumeAccessory RETURN 2 (accessories == null)");
			accessory_attached = false;
            cmdApp.stdOut.println("Accessory NOT Attached");
			return 2;
		}

		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if( -1 == accessory.toString().indexOf(ManufacturerString))
			{
				//Toast.makeText(global_context, "Manufacturer is not matched!", Toast.LENGTH_SHORT).show();
                cmdApp.stdOut.println("Manufacturer is not matched!");
				return 1;
			}
            else
            {
                cmdApp.stdOut.println("Manufacturer matches!");
            }

			if( -1 == accessory.toString().indexOf(ModelString1) && -1 == accessory.toString().indexOf(ModelString2))
			{
				//Toast.makeText(global_context, "Model is not matched!", Toast.LENGTH_SHORT).show();
                cmdApp.stdOut.println("Model is not matched!");
				return 1;
			}
            else
            {
                cmdApp.stdOut.println("Model matches!");
            }

			if( -1 == accessory.toString().indexOf(VersionString))
			{
				//Toast.makeText(global_context, "Version is not matched!", Toast.LENGTH_SHORT).show();
                cmdApp.stdOut.println("Version is not matched!");
				return 1;
			}
            else
            {
                cmdApp.stdOut.println("Version matches!");
            }

			//Toast.makeText(global_context, "Manufacturer, Model & Version are matched!", Toast.LENGTH_SHORT).show();
			accessory_attached = true;

			if (usbmanager.hasPermission(accessory)) {
                cmdApp.stdOut.println("Opening accessory...");
				OpenAccessory(accessory);
			} 
			else
			{
                cmdApp.stdOut.println("Permission NOT granted.");
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						//Toast.makeText(global_context, "Request USB Permission", Toast.LENGTH_SHORT).show();
						usbmanager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {}

		return 0;
	}

	/*destroy accessory*/
	public void DestroyAccessory(boolean bConfiged){

		if(true == bConfiged){
			READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
			writeusbdata[0] = 0;  // send dummy data for instream.read going
			SendPacket(1);
		}
		else
		{
			SetConfig(9600,(byte)1,(byte)8,(byte)0,(byte)0);  // send default setting data for config
			try{Thread.sleep(10);}
			catch(Exception e){}

			READ_ENABLE = false;  // set false condition for handler_thread to exit waiting data loop
			writeusbdata[0] = 0;  // send dummy data for instream.read going
			SendPacket(1);
			if(true == accessory_attached)
			{
				saveDefaultPreference();
			}
		}

		try{Thread.sleep(10);}
		catch(Exception e){}			
		CloseAccessory();
	}

	/*********************helper routines*************************************************/		

	public void OpenAccessory(UsbAccessory accessory)
	{
		filedescriptor = usbmanager.openAccessory(accessory);
		if(filedescriptor != null)
        {
            cmdApp.stdOut.println("File descriptor valid!");
			usbaccessory = accessory;

			FileDescriptor fd = filedescriptor.getFileDescriptor();

			inputstream = new FileInputStream(fd);
			outputstream = new FileOutputStream(fd);
			/*check if any of them are null*/
			if(inputstream == null || outputstream==null)
            {
                cmdApp.stdOut.println("Input or output stream is null.");
				return;
			}
            else
            {
                cmdApp.stdOut.println("Input and output stream valid!");
            }

			if(READ_ENABLE == false)
            {
                cmdApp.stdOut.println("READ_ENABLE == false");
				READ_ENABLE = true;
				readThread = new read_thread(inputstream);
				readThread.start();
			}
            else
            {
                cmdApp.stdOut.println("READ_ENABLE == true");
            }
		}
        else
        {
            cmdApp.stdOut.println("File descriptor is null.");
        }
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

	protected void saveDetachPreference() {
		if(intsharePrefSettings != null)
		{
			intsharePrefSettings.edit()
			.putString("configed", "FALSE")
			.commit();
		}
	}

	protected void saveDefaultPreference() {
		if(intsharePrefSettings != null)
		{
			intsharePrefSettings.edit().putString("configed", "TRUE").commit();
			intsharePrefSettings.edit().putInt("baudRate", 9600).commit();
			intsharePrefSettings.edit().putInt("stopBit", 1).commit();
			intsharePrefSettings.edit().putInt("dataBit", 8).commit();
			intsharePrefSettings.edit().putInt("parity", 0).commit();			
			intsharePrefSettings.edit().putInt("flowControl", 0).commit();
		}
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
                cmdApp.stdOut.println("mUsbReceiver: has permission");
				synchronized (this)
				{
                    cmdApp.stdOut.println("mUsbReceiver: synchronized");
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
					{
                        cmdApp.stdOut.println("mUsbReceiver: permission granted");
						Toast.makeText(global_context, "Allow USB Permission", Toast.LENGTH_SHORT).show();
						OpenAccessory(accessory);
					} 
					else 
					{
                        cmdApp.stdOut.println("mUsbReceiver: permission denied");
						Toast.makeText(global_context, "Deny USB Permission", Toast.LENGTH_SHORT).show();
						Log.d("LED", "permission denied for accessory "+ accessory);

					}
					mPermissionRequestPending = false;
				}
			} 
			else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) 
			{
                cmdApp.stdOut.println("wait exception!");
                cmdApp.stdOut.println("mUsbReceiver: detached");
				saveDetachPreference();
				DestroyAccessory(true);
				//CloseAccessory();
			}
            else
			{
                cmdApp.stdOut.println("mUsbReceiver: LED");
			}
		}	
	};

	/*usb input data handler*/
	private class read_thread  extends Thread 
	{
		FileInputStream instream;

		read_thread(FileInputStream stream ){
			instream = stream;
			this.setPriority(Thread.MAX_PRIORITY);
		}


		public void run()
		{		
            if (instream == null)
                cmdApp.stdOut.println("instream == null");
            else
                cmdApp.stdOut.println("instream != null");

            if (READ_ENABLE == true)
                cmdApp.stdOut.println("READ_ENABLE == true");
            else
                cmdApp.stdOut.println("READ_ENABLE != true");
            

			while(READ_ENABLE == true)
			{
                cmdApp.stdOut.println("read loop...");
				while(totalBytes > (maxnumbytes - 1024))
				{
                    cmdApp.stdOut.println("totalBytes > (maxnumbytes - 1024)");
					try 
					{
						Thread.sleep(50);
					}
					catch (InterruptedException e) {e.printStackTrace();}
				}

				try
				{
                    cmdApp.stdOut.println("try instream:");
					if(instream != null)
					{	
                        cmdApp.stdOut.println("START READ");
						readcount = instream.read(usbdata,0,1024);
                        cmdApp.stdOut.println("FINISH READ");
						if(readcount > 0)
						{
                            cmdApp.stdOut.println("readcount > 0");
							for(int count = 0;count<readcount;count++)
							{					    			
								readBuffer[writeIndex] = usbdata[count];
								writeIndex++;
								writeIndex %= maxnumbytes;
							}

							if(writeIndex >= readIndex)
								totalBytes = writeIndex-readIndex;
							else
								totalBytes = (maxnumbytes-readIndex)+writeIndex;

                            cmdApp.stdOut.println("totalBytes: "+totalBytes);
						}
                        else
                        {
                            cmdApp.stdOut.println("readcount <= 0");
                        }
					}
                    else
                    { 
                        cmdApp.stdOut.println("instream == null");
                    }
				}
				catch (IOException e)
                {
                        cmdApp.stdOut.println("IOException");
                }
			}
		}
	}
}
