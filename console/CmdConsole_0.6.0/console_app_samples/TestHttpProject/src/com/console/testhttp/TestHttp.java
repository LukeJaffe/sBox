package com.console.testhttp;

import android.util.Log;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import com.sss.consolehelper.CmdApp;

public class TestHttp
{
   private static CmdApp cmdApp;

   public static void main(HashMap<Integer, Object> args) 
   {
      cmdApp = new CmdApp(args);

      if (cmdApp.argv == null || cmdApp.argv.length == 0) {
         cmdApp.stdOut.println("Usage: TestHttp [url]");
         return;
      }

      HttpCall.httpGet(cmdApp.argv[0], new MyResHandler(), null);
      
   }

   private static class MyResHandler implements HttpResponseHandler 
   {
    	public void handle(int retCode, LinkedHashMap<String, String> headers, long contentLength, InputStream bodyStream) throws IOException
    	{
           cmdApp.stdOut.println("Return code = " + retCode);
           cmdApp.stdOut.println("Headers: ");
         
           Iterator<Map.Entry<String,String> > it = headers.entrySet().iterator();
           while (it.hasNext()) {
        	  Map.Entry<String,String> entry = it.next();
              cmdApp.stdOut.println(entry.getKey() + " : " + entry.getValue());
           }
           
           cmdApp.stdOut.println("");
           cmdApp.stdOut.println("Content length = " + contentLength);
           if (bodyStream != null) {
        	   
        	  byte[] buf = new byte[256];
        	  int totalRead = 0;
        	  while (true) {
        		 int nrRead = 0;
        		 try {
        	        nrRead = bodyStream.read(buf);
        		 }
        		 catch (java.net.SocketTimeoutException e) {
        			// do nothing 
        		 }
        	     if (nrRead <= 0)
         	        break;
        	     cmdApp.stdOut.println(new String(buf, 0 , nrRead, "UTF-8"));
        	     totalRead += nrRead;
        	  }
        	  
        	  if (contentLength > 0) {
        		  if (contentLength == totalRead)
        			  cmdApp.stdOut.println("[totalRead equals content lenght]");
        		  else
        			  cmdApp.stdOut.println("[totalRead != content lenght]");
        	  }
        	  else {
        		  cmdApp.stdOut.println("[totalRead = " + totalRead + "]");
        	  }
           }
           
    	}
    	
    	public void handleError(Exception e) 
    	{
    		cmdApp.stdOut.println(">>> Error ( " + e.getClass().toString() + " ) = " + e.getMessage());
    	}
    }

}
