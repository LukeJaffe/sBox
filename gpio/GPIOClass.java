package box.gpio;

import android.app.Activity;

import box.networking.NetworkClass;
import box.gpio.FT311GPIOInterface;

public class GPIOClass extends NetworkClass implements GPIOInterface
{
    public FT311GPIOInterface gpioInterface;
    public byte outMap;
    public byte inMap;
    public byte outData;

    public void unlockCall() 
    {
        outMap = (byte)0x7f;
        inMap = (byte)0x80;
        outData &= ~outMap;
        gpioInterface.ConfigPort(outMap, inMap);
        unlockCallback(0);
    }

    public void unlockCallback(int result) {}

    public void lockCall() 
    {
        // Add in functional code when we figure out GPIO mapping 
        lockCallback(1);
    }

    public void lockCallback(int result) {} 

    @Override
    protected void onResume() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        gpioInterface = new FT311GPIOInterface(this);
        gpioInterface.ResumeAccessory();
    }

    @Override
    protected void onPause() {
        // Ideally should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        gpioInterface.DestroyAccessory();
        super.onDestroy();
    }
}
