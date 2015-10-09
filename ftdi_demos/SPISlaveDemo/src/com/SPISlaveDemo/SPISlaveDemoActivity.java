package com.SPISlaveDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.SPISlaveDemo.R.drawable;


public class SPISlaveDemoActivity extends Activity {
	
	// menu item
	Menu myMenu;
    final int MENU_FORMAT = Menu.FIRST;
    final int MENU_CLEAN = Menu.FIRST+1;
    final String[] formatSettingItems = {"ASCII","Hexadecimal", "Decimal"};
    
	final int FORMAT_ASCII = 0;
	final int FORMAT_HEX = 1;
	final int FORMAT_DEC = 2;
	
	int inputFormat = FORMAT_ASCII;
	StringBuffer readSB = new StringBuffer();
	
	
	public handler_thread handlerThread;
	/*declare a FT311 SPISlave interface variable*/
    public FT311SPISlaveInterface spisInterface;
    /*graphical objects*/
    EditText readText;
    EditText writeText;
    EditText addressText,writeAddressText;
    EditText numBytesText;
    EditText statusText,writeStatusText;
    
    Button terminateButton, writeButton;
    Button configButton, resetButton;
    
    Spinner clockPhaseSpinner;
    Spinner dataOrderSpinner;
    
    /*local variables*/
    char[] readBuffer;
    byte [] numReadWritten;
        
    byte numBytes;
    byte count;
    byte status;
    byte clockPhaseMode;
    byte dataOrderSelected;
    int clockFreq;
    
    /*parse the input values seperated by comma*/
    byte byteCount = 0;
	byte tempCount = 0;
	byte tempByte;
	byte [] tempBytes;
	String tempString;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*create editable text objects*/         
        readText = (EditText)findViewById(R.id.ReadValues);
        writeText = (EditText)findViewById(R.id.WriteValues);
        
        writeStatusText = (EditText)findViewById(R.id.WriteStatusValues);
        writeStatusText.setInputType(0);       
         
        terminateButton = (Button)findViewById(R.id.ReadButton);
        writeButton = (Button)findViewById(R.id.WriteButton);
        configButton = (Button)findViewById(R.id.ConfigButton);
        resetButton = (Button)findViewById(R.id.resetButton);
        
        /*allocate buffer*/
        readBuffer = new char[64];
        numReadWritten = new byte[1];
        
        tempBytes = new byte[3]; /*a byte can not be more than 3 digits, max 255*/
        /*default mode is set to mode 1*/
        clockPhaseMode = 1;
               
        clockPhaseSpinner = (Spinner)findViewById(R.id.ClockPhaseValue);
        ArrayAdapter<CharSequence> clockPhaseAdapter = ArrayAdapter.createFromResource(this,R.array.clock_phase, 
        																	android.R.layout.simple_spinner_item);
        
        clockPhaseSpinner.setAdapter(clockPhaseAdapter);
        clockPhaseSpinner.setSelection(1);
        
        /*default data order is set to MSB*/
        dataOrderSelected = 0;
        
        dataOrderSpinner = (Spinner)findViewById(R.id.DataOrderValue);
        ArrayAdapter<CharSequence> dataOrderAdapter = ArrayAdapter.createFromResource(this,R.array.data_order, 
        																	android.R.layout.simple_spinner_item);
        
        dataOrderSpinner.setAdapter(dataOrderAdapter);
        dataOrderSpinner.setSelection(dataOrderSelected);
        
        spisInterface = new FT311SPISlaveInterface(this);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        resetButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				/*clear the receive and send edit boxes*/
	        	readSB.delete(0, readSB.length());
	        	readText.setText(readSB);
				writeText.setText("");
				writeStatusText.setText("");
				clockPhaseSpinner.setSelection(1);
				dataOrderSpinner.setSelection(0);
				spisInterface.Reset();
				
			}
		});
        
        terminateButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				spisInterface.EndHandleThread();				
			}
		});
        
        /*handle write click*/
        writeButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) 
			{				
				int intRadix = 10;
				if(FORMAT_HEX == inputFormat)
				{
					intRadix = 16;
				}
				
				numReadWritten[0] = 0x00;
				
				/*parse the string*/
				byteCount = 0;
				tempCount = 0;
				
				/*read the bytes from the write box*/
				if(writeText.length() != 0)
				{
					writeData();
				}	
					writeStatusText.setText(Integer.toString(numReadWritten[0], intRadix));
					
			}
		});    
        
       /*config section*/ 
        configButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				clockPhaseMode = (byte)clockPhaseSpinner.getSelectedItemPosition();
				dataOrderSelected = (byte)dataOrderSpinner.getSelectedItemPosition();
				spisInterface.SetConfig(clockPhaseMode,dataOrderSelected);
			}
		});
        
        handlerThread = new handler_thread(handler);
        handlerThread.start();
 }
     
    @Override
    protected void onResume() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        spisInterface.ResumeAccessory();
    }

    @Override
    protected void onPause() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
    }
    
    @Override 
    protected void onDestroy(){    	
    	spisInterface.DestroyAccessory();
    	super.onDestroy();
    }
    
    
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (numReadWritten[0] != 0x00) {
				appendData(readBuffer, numReadWritten[0]);
			}
		}
	};
		
	/*usb input data handler*/
	private class handler_thread  extends Thread {
		Handler mHandler;
		/*constructor*/
		handler_thread(Handler h ){
			mHandler = h;
		}
		
		public void run()
		{
			
			while(true)
			{
				Message msg = mHandler.obtainMessage();
				try{	
					Thread.sleep(100);
				} catch(InterruptedException e){}
				
				numReadWritten[0] = 0x00;
				status = spisInterface.ReadData((byte)64, readBuffer, numReadWritten);
				
				if (status == 0x00) {
				    mHandler.sendMessage(msg);
				}
			}
		}
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		myMenu = menu;
		myMenu.add(0, MENU_FORMAT, 0, "Format - ASCII");
		myMenu.add(0, MENU_CLEAN, 0, "Clean Read Bytes Field");		
		return super.onCreateOptionsMenu(myMenu);
	}	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
        case MENU_FORMAT:
        	new AlertDialog.Builder(this).setTitle("Data Format")
			.setItems(formatSettingItems, new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{	
					MenuItem item = myMenu.findItem(MENU_FORMAT);
					if(0 == which)
					{						
						inputFormat = FORMAT_ASCII;
					    item.setTitle("Format - "+ formatSettingItems[0]);
					}
					else if(1 == which)
					{
						inputFormat = FORMAT_HEX;
						item.setTitle("Format - "+ formatSettingItems[1]);
					}
					else
					{
						inputFormat = FORMAT_DEC;
						item.setTitle("Format - "+ formatSettingItems[2]);						
					}
				    char[] ch = new char[1];
				    appendData(ch, 0);
				}
			}).show();           	
        	
        	break;
        	
        case MENU_CLEAN:
        default:        	
        	readSB.delete(0, readSB.length());
        	readText.setText(readSB);
        	break;
        }
 
        return super.onOptionsItemSelected(item);
    }    
    
    public void writeData()
    {    	    	
    	String srcStr = writeText.getText().toString();
    	String destStr = "";
    	switch(inputFormat)
    	{
    	case FORMAT_HEX:
    		{
				String[] tmpStr = srcStr.split(" ");
				
				for(int i = 0; i < tmpStr.length; i++)
				{
					if(tmpStr[i].length() == 0)
					{
						msgToast("Incorrect input for HEX format."
								+"\nThere should be only 1 space between 2 HEX words.",Toast.LENGTH_SHORT);
						return;
					}					
					else if(tmpStr[i].length() != 2)
					{
						msgToast("Incorrect input for HEX format."
								+"\nIt should be 2 bytes for each HEX word.",Toast.LENGTH_SHORT);
						return;
					}						
				}
				
				try	
				{
					destStr = hexToAscii(srcStr.replaceAll(" ", ""));
				}
				catch(IllegalArgumentException e)
				{
					msgToast("Incorrect input for HEX format."
						    +"\nAllowed charater: 0~9, a~f and A~F",Toast.LENGTH_SHORT);
					return;
				}
    		}
    		break;
    		
    	case FORMAT_DEC:
    		{
    			String[] tmpStr = srcStr.split(" ");
				
				for(int i = 0; i < tmpStr.length; i++)
				{
					if(tmpStr[i].length() == 0)
					{
						msgToast("Incorrect input for DEC format."
								+"\nThere should be only 1 space between 2 DEC words.",Toast.LENGTH_SHORT);
						return;
					}					
					else if(tmpStr[i].length() != 3)
					{
						msgToast("Incorrect input for DEC format."
								+"\nIt should be 3 bytes for each DEC word.",Toast.LENGTH_SHORT);
						return;
					}						
				}
				
				try
				{
					destStr = decToAscii(srcStr.replaceAll(" ", ""));
				}
				catch(IllegalArgumentException e)
				{	
					if(e.getMessage().equals("ex_a"))
					{
						msgToast("Incorrect input for DEC format."
								+"\nAllowed charater: 0~9",Toast.LENGTH_SHORT);					
					}
					else
					{
						msgToast("Incorrect input for DEC format."
							    +"\nAllowed range: 0~255",Toast.LENGTH_SHORT);
					}					
					return;
				}
    		}
    		break;
    		
    	case FORMAT_ASCII:    		
    	default:
    		destStr = srcStr;
    		break;
    	}
    	
		numBytes = (byte)destStr.length();
		for (int i = 0; i < numBytes; i++) {
			readBuffer[i] = destStr.charAt(i);
		}					
		spisInterface.SendData(numBytes, readBuffer, numReadWritten);
    }
    
	String hexToAscii(String s) throws IllegalArgumentException
	{
		  int n = s.length();
		  StringBuilder sb = new StringBuilder(n / 2);
		  for (int i = 0; i < n; i += 2)
		  {
		    char a = s.charAt(i);
		    char b = s.charAt(i + 1);
		    sb.append((char) ((hexToInt(a) << 4) | hexToInt(b)));
		  }
		  return sb.toString();
	}
	
	static int hexToInt(char ch)
	{
		  if ('a' <= ch && ch <= 'f') { return ch - 'a' + 10; }
		  if ('A' <= ch && ch <= 'F') { return ch - 'A' + 10; }
		  if ('0' <= ch && ch <= '9') { return ch - '0'; }
		  throw new IllegalArgumentException(String.valueOf(ch));
	}

	String decToAscii(String s) throws IllegalArgumentException
	{
		
		int n = s.length();
		boolean pause = false;
		StringBuilder sb = new StringBuilder(n / 2);
		for (int i = 0; i < n; i += 3)
		{
			char a = s.charAt(i);
			char b = s.charAt(i + 1);
			char c = s.charAt(i + 2);
			int val = decToInt(a)*100 + decToInt(b)*10 + decToInt(c);
			if(0 <= val && val <= 255)
			{
				sb.append((char) val);
			}
			else
			{
				pause = true;
				break;
			}
		}
		
		if(false == pause)
			return sb.toString();
		throw new IllegalArgumentException("ex_b");
	}
	
	static int decToInt(char ch)
	{
		  if ('0' <= ch && ch <= '9') { return ch - '0'; }
		  throw new IllegalArgumentException("ex_a");
	}
	
    void msgToast(String str, int showTime)
    {
    	Toast.makeText(this, str, showTime).show();
    }
    
    public void appendData(String s)
    {
    	switch(inputFormat)
    	{
    	case FORMAT_HEX:
    		{
    			readText.append("Hex");    		
    		}
    		break;
    		
    	case FORMAT_DEC:
    		{
    			readText.append("Dec");
    		}
    		break;
    		
    	case FORMAT_ASCII:    		
    	default:
    		readText.append(s);
    		break;
    	}
    }
    
    public void appendData(char[] data, int len)
    {
    	if(len >= 1)    		
    		readSB.append(String.copyValueOf(data, 0, len));
    	
    	switch(inputFormat)
    	{
    	case FORMAT_HEX:
    		{
    			char[] ch = readSB.toString().toCharArray();
    			String temp;
    			StringBuilder tmpSB = new StringBuilder();
    			for(int i = 0; i < ch.length; i++)
    			{
    				temp = String.format("%02x", (int) ch[i]);

    				if(temp.length() == 4)
    				{
    					tmpSB.append(temp.substring(2, 4));
    				}
    				else
    				{
    					tmpSB.append(temp);
    				}

   					if(i+1 < ch.length)
   					{
   						tmpSB.append(" ");	
   					}
    			}
    			readText.setText(tmpSB);
    			tmpSB.delete(0, tmpSB.length());
    		}
    		break;
    		
    	case FORMAT_DEC:
    		{
    			char[] ch = readSB.toString().toCharArray();
    			String temp;
    			StringBuilder tmpSB = new StringBuilder();
    			for(int i = 0; i < ch.length; i++)
    			{   				
    				temp = Integer.toString((int)(ch[i] & 0xff));
    				for(int j = 0; j < (3 - temp.length()); j++)
    				{
    					tmpSB.append("0");
    				}
   					tmpSB.append(temp);
   					
   					if(i+1 < ch.length)
   					{
   						tmpSB.append(" ");	
   					}
    			}    			
    			readText.setText(tmpSB);
    			tmpSB.delete(0, tmpSB.length());    			
    		}
    		break;
    		
    	case FORMAT_ASCII:    		
    	default:
    		readText.setText(readSB);
    		break;
    	}
    }
}