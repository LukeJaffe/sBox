package com.console.testhttp;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

public interface HttpResponseHandler 
{
   /**
    * Handle response from server
    *   
    * @param [in] retCode -- return HTTP code from server 
    * @param [in] headers -- list of headers
    * @param [in] contentLength -- content length if any (check it whether > 0 )
    * @param [in] bodyStream -- stream to get the response body if any (null if none)
    * @throws IOException
    */
   public void handle(int retCode, LinkedHashMap<String, String> headers, 
		              long contentLength, InputStream bodyStream) throws IOException;
   
   /**
    * To handle error during HTTP call *AND* exception thrown in "handle" above
    */
   public void handleError(Exception e);
}
