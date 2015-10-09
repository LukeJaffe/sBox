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


/*FT311 GPIO interface exposes the following methods:
 *   ConfigPort, WritePort and ReadPort are for user to use for port operations.
 *   - ConfigPort(outMap, inMap): to configure the port as input or output, with outMap and inMap 
 *   							  are arguments for out bitmap and input bitmap.
 *   - WritePort(outData): to write the port data, with outData as argument.
 *   - ReadPort: to read port, it returns the current level on the input IOs.
 *   
 *   DestroyAccessory and ResumeAccessory methods should be called from
 *   overridden  from onResume() and onDestroy routines of main activity class.
 *   
 *   - DestoryAccessory: to be called from onDestory routine of main activity class.
 *   - ResumeAccessory: to be called from onResume routine of main activity class.
 *   
 *   
 */

public class GPIODemoActivity extends Activity {
	
   /*declare a FT311 GPIO interface variable*/
    public FT311GPIOInterface gpiointerface;
	
	/* object variables to hold layout objects for output maps*/
	public CheckBox checkbox0;
	public CheckBox checkbox1;
	public CheckBox checkbox2;
	public CheckBox checkbox3;
	public CheckBox checkbox4;
	public CheckBox checkbox5;
	public CheckBox checkbox6;
	//public CheckBox checkbox7;
	
	/*object variables to hold layout objects for input maps*/
	public CheckBox checkboxin0;
	public CheckBox checkboxin1;
	public CheckBox checkboxin2;
	public CheckBox checkboxin3;
	public CheckBox checkboxin4;
	public CheckBox checkboxin5;
	public CheckBox checkboxin6;
	//public CheckBox checkboxin7;
	
	/*variables to hold the out value*/
	public ToggleButton togglebutton0;
	public ToggleButton togglebutton1;
	public ToggleButton togglebutton2;
	public ToggleButton togglebutton3;
	public ToggleButton togglebutton4;
	public ToggleButton togglebutton5;
	public ToggleButton togglebutton6;
	//public ToggleButton togglebutton7;
	/*input data variables*/
	public ImageView inputbutton0;
	public ImageView inputbutton1;
	public ImageView inputbutton2;
	public ImageView inputbutton3;
	public ImageView inputbutton4;
	public ImageView inputbutton5;
	public ImageView inputbutton6;
	//public ImageView inputbutton7;
	
	/*button object*/
	public Button configbutton;
	public Button writebutton;
	public Button readbutton;
	public Button resetbutton;
		
	/*text boxes for data display*/
	public EditText configoutdata;
	public EditText configindata;
	public EditText writedata;
	public EditText readdata;
	
	
	/*variables*/
	public byte outMap; /*outmap*/
	public byte inMap; /*inmap*/
	public byte outData; /*output data*/
	public byte inData; /*input Data*/
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*create objects of out checkboxes*/
        checkbox0 = (CheckBox)findViewById(R.id.checkbox1);
        checkbox1 = (CheckBox)findViewById(R.id.checkbox2);
        checkbox2 = (CheckBox)findViewById(R.id.checkbox3);
        checkbox3 = (CheckBox)findViewById(R.id.checkbox4);
        checkbox4 = (CheckBox)findViewById(R.id.checkbox5);
        checkbox5 = (CheckBox)findViewById(R.id.checkbox6);
        checkbox6 = (CheckBox)findViewById(R.id.checkbox7);
        //checkbox7 = (CheckBox)findViewById(R.id.checkbox8);
        
        /*create objects of IN checkboxes*/
        checkboxin0 = (CheckBox)findViewById(R.id.checkboxin1);
        checkboxin1 = (CheckBox)findViewById(R.id.checkboxin2);
        checkboxin2 = (CheckBox)findViewById(R.id.checkboxin3);
        checkboxin3 = (CheckBox)findViewById(R.id.checkboxin4);
        checkboxin4 = (CheckBox)findViewById(R.id.checkboxin5);
        checkboxin5 = (CheckBox)findViewById(R.id.checkboxin6);
        checkboxin6 = (CheckBox)findViewById(R.id.checkboxin7);
        //checkboxin7 = (CheckBox)findViewById(R.id.checkboxin8);

        /*create output data objects*/
        togglebutton0 = (ToggleButton)findViewById(R.id.togglebutton1);
        togglebutton1 = (ToggleButton)findViewById(R.id.togglebutton2);
        togglebutton2 = (ToggleButton)findViewById(R.id.togglebutton3);
        togglebutton3 = (ToggleButton)findViewById(R.id.togglebutton4);
        togglebutton4 = (ToggleButton)findViewById(R.id.togglebutton5);
        togglebutton5 = (ToggleButton)findViewById(R.id.togglebutton6);
        togglebutton6 = (ToggleButton)findViewById(R.id.togglebutton7);
        //togglebutton7 = (ToggleButton)findViewById(R.id.togglebutton8);
        
        /*image buttons to be changed on receiving the input data*/
        inputbutton0 = (ImageView)findViewById(R.id.intogglebutton1);
        inputbutton1 = (ImageView)findViewById(R.id.intogglebutton2);
        inputbutton2 = (ImageView)findViewById(R.id.intogglebutton3);
        inputbutton3 = (ImageView)findViewById(R.id.intogglebutton4);
        inputbutton4 = (ImageView)findViewById(R.id.intogglebutton5);
        inputbutton5 = (ImageView)findViewById(R.id.intogglebutton6);
        inputbutton6 = (ImageView)findViewById(R.id.intogglebutton7);
        //inputbutton7 = (ImageView)findViewById(R.id.intogglebutton8);
        
        
        /*text boxes for data display*/
        configoutdata = (EditText)findViewById(R.id.outmapdata);
        configindata = (EditText)findViewById(R.id.inmapdata);
        writedata = (EditText)findViewById(R.id.writedata);
        readdata = (EditText)findViewById(R.id.readdata);
        
        /**** comamnd buttons*****/
        configbutton = (Button)findViewById(R.id.configbutton);
        readbutton = (Button)findViewById(R.id.readbutton);
        writebutton = (Button)findViewById(R.id.writebutton);
        resetbutton = (Button)findViewById(R.id.resetbutton);
        
        
/************************process the OUTPUT bitmap******************************/
        checkbox0.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				outMap &= ~(1<<0);
				/*when setting the direction, its driven to low*/
				togglebutton0.setChecked(false);
				if(((CheckBox)v).isChecked()==true)
				{
					outMap |= (1<<0);
					inMap &= ~(1<<0);
					checkboxin0.setChecked(false);
					
					
				}
				outData &= ~outMap;
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});
        
		checkbox1.setOnClickListener(new View.OnClickListener() {
					
					//@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						/*by default clear the out map*/
						outMap &= ~(1<<1);
						/*when setting the direction, its driven to low*/
						togglebutton1.setChecked(false);
						if(((CheckBox)v).isChecked()==true)
						{
							outMap |= (1<<1);
							inMap &= ~(1<<1);
							checkboxin1.setChecked(false);
							
						}
						configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
						configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
						
					}
				});
		
		checkbox2.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				outMap &= ~(1<<2);
				if(((CheckBox)v).isChecked()==true)
				{
					outMap |= (1<<2);
					inMap &= ~(1<<2);
					checkboxin2.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});
		
		checkbox3.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				outMap &= ~(1<<3);
				if(((CheckBox)v).isChecked()==true)
				{
					outMap |= (1<<3);
					inMap &= ~(1<<3);
					checkboxin3.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});

		
		checkbox4.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				outMap &= ~(1<<4);
				if(((CheckBox)v).isChecked()==true)
				{
					outMap |= (1<<4);
					inMap &= ~(1<<4);
					checkboxin4.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});
        
		checkbox5.setOnClickListener(new View.OnClickListener() {
					
					//@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						/*by default clear the out map*/
						outMap &= ~(1<<5);
						if(((CheckBox)v).isChecked()==true)
						{
							outMap |= (1<<5);
							inMap &= ~(1<<5);
							checkboxin5.setChecked(false);
						}
						configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
						configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
						
					}
				});
		
		checkbox6.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				outMap &= ~(1<<6);
				if(((CheckBox)v).isChecked()==true)
				{
					outMap |= (1<<6);
					inMap &= ~(1<<6);
					checkboxin6.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});
		
/************************process the INPUT bitmap******************************/
        checkboxin0.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				inMap &= ~(1<<0);
				if(((CheckBox)v).isChecked()==true)
				{
					inMap |= (1<<0);
					outMap &= ~(1<<0);
					checkbox0.setChecked(false);
				}
				
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});
        
		checkboxin1.setOnClickListener(new View.OnClickListener() {
					
					//@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						/*by default clear the out map*/
						inMap &= ~(1<<1);
						if(((CheckBox)v).isChecked()==true)
						{
							inMap |= (1<<1);
							outMap &= ~(1<<1);
							checkbox1.setChecked(false);
						}
						configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
						configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
						
					}
				});
		
		checkboxin2.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				inMap &= ~(1<<2);
				if(((CheckBox)v).isChecked()==true)
				{
					inMap |= (1<<2);
					outMap &= ~(1<<2);
					checkbox2.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});
		
		checkboxin3.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				inMap &= ~(1<<3);
				if(((CheckBox)v).isChecked()==true)
				{
					inMap |= (1<<3);
					outMap &= ~(1<<3);
					checkbox3.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});

		
		checkboxin4.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				inMap &= ~(1<<4);
				if(((CheckBox)v).isChecked()==true)
				{
					inMap |= (1<<4);
					outMap &= ~(1<<4);
					checkbox4.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});
        
		checkboxin5.setOnClickListener(new View.OnClickListener() {
					
					//@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						/*by default clear the out map*/
						inMap &= ~(1<<5);
						if(((CheckBox)v).isChecked()==true)
						{
							inMap |= (1<<5);
							outMap &= ~(1<<5);
							checkbox5.setChecked(false);
						}
						configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
						configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
						
					}
				});
		
		checkboxin6.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*by default clear the out map*/
				inMap &= ~(1<<6);
				if(((CheckBox)v).isChecked()==true)
				{
					inMap |= (1<<6);
					outMap &= ~(1<<6);
					checkbox6.setChecked(false);
				}
				configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
				configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));
				
			}
		});

/******************************process output data******************************************/
		togglebutton0.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				outData &= ~(1<<0);
				if(((ToggleButton)v).isChecked()== true){
					outData |= (1<<0);
				}
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				
				
			}
		});
		
		togglebutton1.setOnClickListener(new View.OnClickListener() {
					
					//@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						outData &= ~(1<<1);
						if(((ToggleButton)v).isChecked()== true){
							outData |= (1<<1);
						}
						
						writedata.setText("0x" + Integer.toHexString(outData & 0xff));
						
						
					}
				});
		
		togglebutton2.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				outData &= ~(1<<2);
				if(((ToggleButton)v).isChecked()== true){
					outData |= (1<<2);
				}
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				
				
			}
		});
		
		togglebutton3.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				outData &= ~(1<<3);
				if(((ToggleButton)v).isChecked()== true){
					outData |= (1<<3);
				}
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				
				
			}
		});
		
		togglebutton4.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				outData &= ~(1<<4);
				if(((ToggleButton)v).isChecked()== true){
					outData |= (1<<4);
				}
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				
				
			}
		});
		
		togglebutton5.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				outData &= ~(1<<5);
				if(((ToggleButton)v).isChecked()== true){
					outData |= (1<<5);
				}
				
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				
			}
		});
		
		togglebutton6.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				outData &= ~(1<<6);
				if(((ToggleButton)v).isChecked()== true){
					outData |= (1<<6);
				}
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				
			}
		});
		
/******************************process button presses*********************************/
		/*user code to configure port data*/
		configbutton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				togglebutton0.setChecked(false);
				togglebutton1.setChecked(false);
				togglebutton2.setChecked(false);
				togglebutton3.setChecked(false);
				togglebutton4.setChecked(false);
				togglebutton5.setChecked(false);
				togglebutton6.setChecked(false);
				//togglebutton7.setChecked(false);
				outData &= ~outMap;
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				gpiointerface.ConfigPort(outMap, inMap);
			}
		});
		
		/*user code to write port*/
		writebutton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				/*send only the output mapped data*/
				outData &= outMap;
				writedata.setText("0x" + Integer.toHexString(outData & 0xff));
				gpiointerface.WritePort(outData);
				
				
				
			}
				
		});
		
		/*user code to read the accessory data*/
		readbutton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//readbutton.setBackgroundResource(drawable.start);
				inData = gpiointerface.ReadPort();
				////readdata.setText("0y" + Integer.toHexString(inData & 0xff));
				ProcessReadData(inData);
			}
		});
		
		/*user code to reset the accessory data*/
		resetbutton.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				resetFT311();;
			}
		});
		
		/*create an object of GPIO interface class*/
		 gpiointerface = new FT311GPIOInterface(this); 
		 resetFT311();

    }
    
    protected void resetFT311()
    {
		gpiointerface.ResetPort();
		
		checkbox0.setChecked(false);
		checkbox1.setChecked(false);
		checkbox2.setChecked(false);
		checkbox3.setChecked(false);
		checkbox4.setChecked(false);
		checkbox5.setChecked(false);
		checkbox6.setChecked(false);
		
		checkboxin0.setChecked(true);
		checkboxin1.setChecked(true);
		checkboxin2.setChecked(true);
		checkboxin3.setChecked(true);
		checkboxin4.setChecked(true);
		checkboxin5.setChecked(true);
		checkboxin6.setChecked(true);
		
		togglebutton0.setChecked(false);
		togglebutton1.setChecked(false);
		togglebutton2.setChecked(false);
		togglebutton3.setChecked(false);
		togglebutton4.setChecked(false);
		togglebutton5.setChecked(false);
		togglebutton6.setChecked(false);
		
		ProcessReadData((byte)0);
		
		outMap = (byte)0x80;
		inMap = (byte)0x7f;
		configoutdata.setText("0x" + Integer.toHexString(outMap & 0x7f));
		configindata.setText("0x" + Integer.toHexString(inMap & 0x7f));  
		
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
		readdata.setText("0x" + Integer.toHexString(cmddata & 0xff));
		/*read data is to update the LEDs*/
		if((cmddata & 0x01) == 0x01)
			inputbutton0.setImageResource(drawable.image100);
		else
			inputbutton0.setImageResource(drawable.image1);
		
		if((cmddata & 0x02) == 0x02)
			inputbutton1.setImageResource(drawable.image100);
		else
			inputbutton1.setImageResource(drawable.image1);
		
		if((cmddata & 0x04) == 0x04)
			inputbutton2.setImageResource(drawable.image100);
		else
			inputbutton2.setImageResource(drawable.image1);
		
		if((cmddata & 0x08) == 0x08)
			inputbutton3.setImageResource(drawable.image100);
		else
			inputbutton3.setImageResource(drawable.image1);
		
		if((cmddata & 0x10) == 0x10)
			inputbutton4.setImageResource(drawable.image100);
		else
			inputbutton4.setImageResource(drawable.image1);
		
		if((cmddata & 0x20) == 0x20)
			inputbutton5.setImageResource(drawable.image100);
		else
			inputbutton5.setImageResource(drawable.image1);
		
		
		if((cmddata & 0x40) == 0x40)
			inputbutton6.setImageResource(drawable.image100);
		else
			inputbutton6.setImageResource(drawable.image1);
	}
}

