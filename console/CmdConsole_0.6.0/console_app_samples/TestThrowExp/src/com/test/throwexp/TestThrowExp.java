package com.test.throwexp;


import android.app.Application;
import java.io.IOException;
import java.util.HashMap;
import com.sss.consolehelper.CmdApp;

public class TestThrowExp
{
   
   public static void main(HashMap<Integer, Object> args) 
   {
      CmdApp cmdApp = new CmdApp(args);

      cmdApp.stdOut.println("Test throw exception ... ");
      cmdApp.stdOut.println("argv[10] = " + cmdApp.argv[10]);  // throw exception

   }
}
