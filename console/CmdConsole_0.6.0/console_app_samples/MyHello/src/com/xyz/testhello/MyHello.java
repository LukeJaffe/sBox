package com.xyz.testhello;

import android.util.Log;
import android.app.Application;
import java.util.HashMap;
import com.sss.consolehelper.CmdApp;

public class MyHello
{

   public static void main(HashMap<Integer, Object> args) 
   {
      CmdApp cmdApp = new CmdApp(args);

      for (int j = 0; j < 7; ++j) {
         cmdApp.stdOut.println("@@@@@@ go @@@@@@@ hello world (777) **************");
         for (int i = 0; cmdApp.argv != null && i < cmdApp.argv.length; ++i)
            cmdApp.stdOut.println(">--argv = " + cmdApp.argv[i]);
         
         try {
            Thread.sleep(1000);
         }
         catch (Exception e) {
            Log.d("MyHello", "Error : " + e.getMessage() + "; exp = " + e.getClass().getName());
         }
      }

      cmdApp.stdOut.print("Please input a string: ");
      String s = cmdApp.readln();
      cmdApp.stdOut.println("Your input string: " + s);

      cmdApp.stdOut.print("Input one more: ");
      s = cmdApp.readln();
      cmdApp.stdOut.println("Your second string: " + s);
      
   }

}
