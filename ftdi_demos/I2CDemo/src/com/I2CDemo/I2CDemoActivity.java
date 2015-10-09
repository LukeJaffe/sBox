package com.I2CDemo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.I2CDemo.R.drawable;

interface bOption
{
    public static final byte START_BIT = 0x01;
    public static final byte STOP_BIT = 0x02;
    public static final byte NACK_LAST_BYTE = 0x04; 
    public static final byte NO_DEVICE_ADDRESS = 0x08;       
}


public class I2CDemoActivity extends Activity {

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
	
	/*declare a FT311 I2C interface variable*/
    public FT311I2CInterface i2cInterface;
    /*graphical objects*/
    EditText readText;
    EditText writeText;
    EditText deviceAddressText,writeDeviceAddressText;
    EditText addressText,writeAddressText;
    EditText numBytesText, writeNumBytesText;
    EditText freqText;
    TextView freqText1;
    TextView freqText2;
    TextView freqText3;
    TextView freqText4;
    //AutoCompleteTextView freqText;
    EditText statusText,writeStatusText;
        
    Button readButton, writeButton;
    Button freqButton;
    Button resetButton;
    
    /*local variables*/
    /*write buffer*/
    byte [] writeBuffer;
    /*read buffer*/
    byte[] readBuffer;    
    byte [] actualNumBytes;
    
    /*could have done better to parse the
     * strings
     * */
    byte [] tempBytes;
    int numtempBytes = 0;
    String tempString;
    byte tempCount=0;
    byte byteCount = 0;
    
    byte numBytes;
    byte deviceAddress;
    byte address;
    byte count;
    byte status;
    
    
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*create editable text objects*/
        readText = (EditText)findViewById(R.id.ReadValues);
        //readText.setInputType(0);
        writeText = (EditText)findViewById(R.id.WriteValues);
        freqText = (EditText)findViewById(R.id.FrequencyValue);
        freqText.setInputType(0);
        /*dirty way of displaying options*/
        freqText1 = (TextView)findViewById(R.id.FrequencyValue1);
        freqText2 = (TextView)findViewById(R.id.FrequencyValue2);
        freqText3 = (TextView)findViewById(R.id.FrequencyValue3);
        freqText4 = (TextView)findViewById(R.id.FrequencyValue4);
        //freqText = (AutoCompleteTextView)findViewById(R.id.FrequencyValue);
        
        deviceAddressText = (EditText)findViewById(R.id.DeviceAddressValue);
        writeDeviceAddressText = (EditText)findViewById(R.id.WriteDeviceAddressValue);
        
        addressText = (EditText)findViewById(R.id.MemoryAddressValue);
        writeAddressText = (EditText)findViewById(R.id.WriteMemoryAddressValue);
        
        numBytesText = (EditText)findViewById(R.id.NumberOfBytesValue);
        writeNumBytesText = (EditText)findViewById(R.id.WriteNumberOfBytesValue);
        writeNumBytesText.setInputType(0);
        
        statusText=(EditText)findViewById(R.id.StatusValues);
        statusText.setInputType(0);
        writeStatusText = (EditText)findViewById(R.id.WriteStatusValues);
        writeStatusText.setInputType(0);
        
        readButton = (Button)findViewById(R.id.ReadButton);
        writeButton = (Button)findViewById(R.id.WriteButton);
        freqButton = (Button)findViewById(R.id.FrequencyButton);
        resetButton = (Button)findViewById(R.id.resetButton);
        
        
        /*allocate buffer*/
        writeBuffer = new byte[60];
        readBuffer = new byte[60];
        actualNumBytes = new byte[1];
        tempBytes = new byte [60];
             
        i2cInterface = new FT311I2CInterface(this);
        resetFT311();
       
        /*handle the reset button*/	
        resetButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				resetFT311();

			}
		});
        
        /*read button*/
        readButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				/*for every read, clear the previous read*/
				readText.setText("");
				
				status = 0x00;
				/*do the sanity checks*/
				/*device address???*/
				if(deviceAddressText.length() == 0){
					status = 0x4;
				}
				
				/*address to read from*/			
				if(addressText.length() == 0){
					status |= 0x04;
				}

				/*if all good, then only proceed to read*/
				if(status == 0x00)
				{
					int intRadix = 10;
					if(FORMAT_HEX == inputFormat)
					{
						intRadix = 16;
					}
					
					try{
						deviceAddress = (byte) Integer.parseInt(deviceAddressText.getText().toString(), intRadix);						
					}catch(NumberFormatException ex){
						msgToast("Invalid input for Read Device Address",Toast.LENGTH_SHORT);
						return;						
					}

					try{
						address = (byte)Integer.parseInt(addressText.getText().toString(), intRadix);						
					}catch(NumberFormatException ex){
						msgToast("Invalid input for Read Address",Toast.LENGTH_SHORT);
						return;						
					}
					
					try{
						numBytes = (byte)Integer.parseInt(numBytesText.getText().toString(), intRadix);						
					}catch(NumberFormatException ex){
						msgToast("Invalid input for Read Number Bytes",Toast.LENGTH_SHORT);
						return;						
					}					
					
					/*blocking call*/
					byte transferOptions;
					transferOptions = bOption.START_BIT;
					byteCount = 0;
					writeBuffer[byteCount++] = address;		
					/* write the address first without stop condition */
					status = i2cInterface.WriteData(deviceAddress, transferOptions, byteCount, writeBuffer, actualNumBytes);

					/*read the bytes from the text box*/					
					transferOptions = (bOption.START_BIT | bOption.STOP_BIT | bOption.NACK_LAST_BYTE);
					status = i2cInterface.ReadData(deviceAddress, transferOptions, numBytes, readBuffer, actualNumBytes);
					
					char [] displayReadbuffer;
					displayReadbuffer = new char[60];
					
					for(count = 0;count<(actualNumBytes[0]);count++){
						displayReadbuffer[count] = (char)readBuffer[count];
					}
					
//		        	readSB.delete(0, readSB.length());
//		        	readText.setText(readSB);
					appendData(displayReadbuffer, actualNumBytes[0]);
				}	
				
				statusText.setText("0x"+Integer.toHexString(status));
			}
		});
        
        /*handle write click*/
        writeButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				
				/*by default, status is good*/
				status = 0x00;
				/*do the sanity checks for required values*/
				/*device address needed??*/
				if(writeDeviceAddressText.length() == 0){
					status = 0x4;
				}
				/*the write values have to be specified*/
				if(writeText.length() == 0){
					status |= 1; /*general error*/
					
				}
				/*also need the address to write to*/
				if(writeAddressText.length() == 0){
					status |= 0x04;
				}

				/*if all good??/*/
				if(status ==  0x00)
				{
					/*read the bytes from the write box*/
					int intRadix = 10;
					if(FORMAT_HEX == inputFormat)
					{
						intRadix = 16;
					}
					
					try{
						deviceAddress = (byte) Integer.parseInt(writeDeviceAddressText.getText().toString(), intRadix);						
					}
					catch(NumberFormatException ex){
						msgToast("Invalid input for Write Device Address",Toast.LENGTH_SHORT);
						return;						
					}

					try{
						address = (byte)Integer.parseInt(writeAddressText.getText().toString(), intRadix);						
					}
					catch(NumberFormatException ex){
						msgToast("Invalid input for Write Address",Toast.LENGTH_SHORT);
						return;						
					}	
					
					/*parse the string*/
					byteCount = 0;
					tempCount = 0;
					
					
					//Set Data: address
					writeBuffer[byteCount++] = address;
					
					if(true == convertData()){
						numBytes = (byte)numtempBytes;
						byteCount += (byte)numtempBytes;
						
						Log.e("t","numBytes:"+ numBytes);
						Log.e("t","byteCount:"+ byteCount);
						for(int i=0; i<numBytes; i++)
						{
							writeBuffer[i+1] = tempBytes[i];
						}						
					}
					else{
						msgToast("Invalid input for Write Bytes",Toast.LENGTH_SHORT);
						return;
					}
					
					/* subtract 1 for the address byte */
					writeNumBytesText.setText(Integer.toString(byteCount-1, intRadix));
					
					/*blocking call*/
					byte transferOptions;
					transferOptions = (bOption.START_BIT | bOption.STOP_BIT);
					
					status = i2cInterface.WriteData(deviceAddress, transferOptions, byteCount, writeBuffer, actualNumBytes);
				}
				writeStatusText.setText("0x"+Integer.toHexString(status)); 
			}
		});
        
        /*handle write click*/
        freqButton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				
				// TODO Auto-generated method stub
				freqText1.setVisibility(View.INVISIBLE);
				freqText2.setVisibility(View.INVISIBLE);
				freqText3.setVisibility(View.INVISIBLE);
				freqText4.setVisibility(View.INVISIBLE);
				//freqButton.setBackgroundResource(drawable.start);
				
				
				/*read the bytes from the write box*/
				if(freqText.length() != 0)
					deviceAddress = (byte) Integer.parseInt(freqText.getText().toString());
				else
					deviceAddress = 92; /*default*/
				
				freqText.setText(Integer.toString(deviceAddress));
				i2cInterface.SetFrequency(deviceAddress);
			}
		});

        
        /*select the frequency*/
        freqText.setOnTouchListener(new View.OnTouchListener() {
			
			//@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				freqText1.setVisibility(View.VISIBLE);
				freqText2.setVisibility(View.VISIBLE);
				freqText3.setVisibility(View.VISIBLE);
				freqText4.setVisibility(View.VISIBLE);
				
				return false;
			}
		});
        
        
        /*set the selected value*/
        freqText1.setOnTouchListener(new View.OnTouchListener() {
			
			//@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				freqText.setText(freqText1.getText().toString());
				freqText1.setVisibility(View.INVISIBLE);
				freqText2.setVisibility(View.INVISIBLE);
				freqText3.setVisibility(View.INVISIBLE);
				freqText4.setVisibility(View.INVISIBLE);
				return false;
			}
		});

        /*set the selected value*/
        freqText2.setOnTouchListener(new View.OnTouchListener() {
			
			//@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				freqText.setText(freqText2.getText().toString());
				freqText1.setVisibility(View.INVISIBLE);
				freqText2.setVisibility(View.INVISIBLE);
				freqText3.setVisibility(View.INVISIBLE);
				freqText4.setVisibility(View.INVISIBLE);
				return false;
			}
		});
        
        /*set the selected value*/
        freqText3.setOnTouchListener(new View.OnTouchListener() {
			
			//@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				freqText.setText(freqText3.getText().toString());
				freqText1.setVisibility(View.INVISIBLE);
				freqText2.setVisibility(View.INVISIBLE);
				freqText3.setVisibility(View.INVISIBLE);
				freqText4.setVisibility(View.INVISIBLE);
				return false;
			}
		});
        
        /*set the selected value*/
        freqText4.setOnTouchListener(new View.OnTouchListener() {
			
			//@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				freqText.setText(freqText4.getText().toString());
				freqText1.setVisibility(View.INVISIBLE);
				freqText2.setVisibility(View.INVISIBLE);
				freqText3.setVisibility(View.INVISIBLE);
				freqText4.setVisibility(View.INVISIBLE);
				return false;
			}
		});
    }
    
    protected void resetFT311()
    {
		freqText.setText("92");		
    	readSB.delete(0, readSB.length());
    	readText.setText(readSB);
//		readText.setText("");		
	    deviceAddressText.setText("");
	    addressText.setText("");
	    numBytesText.setText("");
	    statusText.setText("");	    
	    writeText.setText("");
	    writeDeviceAddressText.setText("");
	    writeAddressText.setText("");	    
	    writeNumBytesText.setText("");
	    writeStatusText.setText("");	    
		i2cInterface.Reset();    
    }
    
    
    @Override
    protected void onResume() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        i2cInterface.ResumeAccessory();
    }

    @Override
    protected void onPause() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
    }
    
    @Override 
    protected void onDestroy(){
    	
    	i2cInterface.DestroyAccessory();
    	super.onDestroy();
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
    
    public boolean convertData()
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
						return false;
					}					
					else if(tmpStr[i].length() != 2)
					{
						msgToast("Incorrect input for HEX format."
								+"\nIt should be 2 bytes for each HEX word.",Toast.LENGTH_SHORT);
						return false;
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
					return false;
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
						return false;
					}					
					else if(tmpStr[i].length() != 3)
					{
						msgToast("Incorrect input for DEC format."
								+"\nIt should be 3 bytes for each DEC word.",Toast.LENGTH_SHORT);
						return false;
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
					return false;
				}
    		}
    		break;
    		
    	case FORMAT_ASCII:
    	default:
    		destStr = srcStr;   			
    		break;
    	}

		numtempBytes = destStr.length();
		for (int i=0; i < numtempBytes; i++) {
			tempBytes[i] = (byte)destStr.charAt(i);
		}

    	return true;
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
    
    void msgToast(String str, int showTime)
    {
    	Toast.makeText(this, str, showTime).show();
    }
}