package com.console.testhttp;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.params.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import java.net.URI;
import java.net.URLEncoder;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.lang.StringBuilder;


public class HttpCall 
{
   public static final int CONNTIMEOUT = 30 * 1000;
   public static final int DATAWAITTIMEOUT = 30 * 1000;

   /**
    * Escape the name-value pairs and append to url provided
    * @return a URL with those name-value pairs appended
    */
   private static String getFullRequestPath(String url, LinkedHashMap<String, String> nameValPairs)
   {
       if (nameValPairs != null && nameValPairs.size() > 0) { 
			  
		  StringBuilder sb = new StringBuilder(256);
		  sb.append(url).append("?");
			  
		  try {
			 Iterator<Map.Entry<String,String> > it = nameValPairs.entrySet().iterator();
		     while(it.hasNext()) {
		        Map.Entry<String,String> entry = it.next();
		        String k = URLEncoder.encode(entry.getKey(), "UTF-8");
		        String v = URLEncoder.encode(entry.getValue(), "UTF-8");
		        sb.append(k).append("=").append(v).append("&");
		     }
		  }
		  catch (java.io.UnsupportedEncodingException e) {
			 // actually won't happen
		  }
		  sb.deleteCharAt(sb.length() - 1); // delete the last '&'
		  url = sb.toString();
	  }
       
      return url;
   }
   
   /**
    *  
    * @param [in] url -- URL to access (e.g. "http://www.xyz.com/req/path")
    * @param [in] h -- handler to handle response from server
    * @param [in] nameValPairs -- extra name value pairs to be appended to the URL
    * 
    * @return true if no exception thrown during the process
    * @return false otherwise 
    */
   public static boolean httpGet(String url, HttpResponseHandler h, LinkedHashMap<String, String> nameValPairs)
   {
	  url = getFullRequestPath(url, nameValPairs);
	  return httpGet(url, h, CONNTIMEOUT, DATAWAITTIMEOUT);
   }
   
   /**
    * Specify custom timeout values
    */
   public static boolean httpGet(String url, HttpResponseHandler h, LinkedHashMap<String, String> nameValPairs,
		                         int connTimeout, int dataWaitTimeout)
   {
	  url = getFullRequestPath(url, nameValPairs);
	  return httpGet(url, h, connTimeout, dataWaitTimeout);
   }
   
   public static boolean httpGet(String url, HttpResponseHandler h)
   {
      return httpGet(url, h, CONNTIMEOUT, DATAWAITTIMEOUT);
   }
   
   /**
    * Specify custom timeout values
    */
   public static boolean httpGet(String url, HttpResponseHandler h, 
		                         int connTimeout, int dataWaitTimeout)
   {
	  boolean isOK = true;
	  InputStream is = null;
	  
	  connTimeout = (connTimeout > 0) ? connTimeout : CONNTIMEOUT;
	  dataWaitTimeout = (dataWaitTimeout > 0) ? dataWaitTimeout : DATAWAITTIMEOUT;
	  
      try {

    	 // prepare params
         HttpParams httpParams = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(httpParams, connTimeout);
         HttpConnectionParams.setSoTimeout(httpParams, dataWaitTimeout);
         HttpClient httpClient = new DefaultHttpClient(httpParams);
         URI uri = new URI(url);
         HttpGet method = new HttpGet(uri);
         
         // make request
         HttpResponse httpRes = httpClient.execute(method);
    	 
    	 // pass to response handler
         StatusLine sl = httpRes.getStatusLine();
         Header[] headers = httpRes.getAllHeaders();
         LinkedHashMap<String, String> headerList = new LinkedHashMap<String, String>(headers.length);
         for (int i = 0 ; i < headers.length; ++i) {
        	headerList.put(headers[i].getName(), headers[i].getValue());
         }
         
         long contentLength = 0;
         HttpEntity entity = httpRes.getEntity();
         if (entity != null) {
            is = entity.getContent();
            contentLength = entity.getContentLength();
         }
         
         h.handle(sl.getStatusCode(), headerList, contentLength, is);
      }
      catch (Exception e) {
    	 try {
    	    h.handleError(e);
    	 }
    	 catch (Exception e2) {
    		 // to prevent further exception
    	 }
    	 isOK = false;
      }
      finally {
    	  try {
    	     if (is != null)
                is.close();
    	  }
    	  catch (IOException e) {
    		  
    	  }
      }
      
      return isOK;
   }
 
   
}
