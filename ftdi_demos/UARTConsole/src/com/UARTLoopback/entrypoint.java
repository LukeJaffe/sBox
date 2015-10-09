package com.UARTLoopback;

import android.util.Log;
import android.app.Application;
import java.util.HashMap;
import com.sss.consolehelper.CmdApp;

public class entrypoint
{
    public static void main(HashMap<Integer, Object> args)
    {
        CmdApp cmdApp = new CmdApp(args);
        cmdApp.stdOut.println("Uart console app*~!");

        UARTLoopbackActivity test = new UARTLoopbackActivity(cmdApp);

             //try {
             //   Thread.sleep(1000);
             //}
             //catch (Exception e) {
             //   Log.d("MyHello", "Error : " + e.getMessage() + "; exp = " + e.getClass().getName());
             //}
    }
}
