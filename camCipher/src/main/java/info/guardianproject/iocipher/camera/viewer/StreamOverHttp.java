package info.guardianproject.iocipher.camera.viewer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import android.net.Uri;
import android.util.Log;

/**
 * This is simple HTTP local server for streaming InputStream to apps which are capable to read data from url.
 * Random access input stream is optionally supported, depending if file can be opened in this mode. 
 *
 * from: http://stackoverflow.com/a/9096241
 */
public class StreamOverHttp{
	
   private static boolean debug = false;
 
   private final File file;
   private final String fileMimeType;
   private long fileSize;
   private InputStream fileInputStream;
   private final ServerSocket serverSocket;
   private Thread mainThread;
 
   /**
    * Some HTTP response status codes
    */
   private static final String 
      HTTP_BADREQUEST = "400 Bad Request",
      HTTP_416 = "416 Range not satisfiable",
      HTTP_INTERNALERROR = "500 Internal Server Error";
 
   public StreamOverHttp(File f, String forceMimeType, long forceFileSize, InputStream fis, int serverPort) throws IOException{
      file = f;
      
      fileMimeType = forceMimeType;
      serverSocket = new ServerSocket(serverPort);
      fileSize = forceFileSize;
      fileInputStream = fis;
      
      if (debug)
    	  log ("streaming " + f.getAbsolutePath() + " type=" + fileMimeType + " len=" + fileSize);
      
      mainThread = new Thread(new Runnable(){
         @Override
         public void run(){
            try{
               while(true) {
                  Socket accept = serverSocket.accept();
                  new HttpSession(accept);
               }
            }catch(IOException e){
               log("socket closed: "+e);
            }
         }
 
      });
      mainThread.setName("Stream over HTTP");
      mainThread.setDaemon(true);
      mainThread.start();
   }
 
   private class HttpSession implements Runnable{
      private boolean canSeek;
      private InputStream is;
      private final Socket socket;
 
      HttpSession(Socket s){
         socket = s;
         log("Stream over localhost: serving request on "+s.getInetAddress());
         Thread t = new Thread(this, "Http response");
         t.setDaemon(true);
         t.start();
      }
 
      @Override
      public void run(){
         try{
            openInputStream();
            handleResponse(socket);
         }catch(IOException e){
            e.printStackTrace();
         }finally {
            if(is!=null) {
               try{
                  is.close();
               }catch(IOException e){
                  e.printStackTrace();
               }
            }
         }
      }
 
      private void openInputStream() throws IOException{
         // openRandomAccessInputStream must return RandomAccessInputStream if file is ssekable, null otherwise
         is = openRandomAccessInputStream(file);
         if(is!=null)
            canSeek = true;
         else
            is = openInputStream(file, 0);
      }
 
      public InputStream openInputStream (File file, int value) throws IOException
      {
    	  return fileInputStream;
      }
      
      
      public RandomAccessInputStream openRandomAccessInputStream (File file) throws IOException
      {
    	  if (fileInputStream instanceof info.guardianproject.iocipher.FileInputStream)
    		  fileInputStream = new info.guardianproject.iocipher.FileInputStream(file.getAbsolutePath());
    	  else
    		  fileInputStream = new java.io.FileInputStream(file);
    	  
    	  RandomAccessInputStream rais = new RandomAccessInputStream ()
    	  {

    		  
			@Override
			public int available() throws IOException {
				
				return fileInputStream.available();
			}

			@Override
			public void close() throws IOException {
				fileInputStream.close();
			}

			@Override
			public void mark(int readlimit) {
				fileInputStream.mark(readlimit);
			}

			@Override
			public boolean markSupported() {
				return fileInputStream.markSupported();
			}

			@Override
			public int read(byte[] buffer, int byteOffset, int byteCount)
					throws IOException {
				return fileInputStream.read(buffer, byteOffset, byteCount);
			}

			@Override
			public int read(byte[] buffer) throws IOException {
				return fileInputStream.read(buffer);
			}

			@Override
			public synchronized void reset() throws IOException {
				fileInputStream.reset();
			}

			@Override
			long length() {
				
				return fileSize;
			}

			@Override
			void seek(long offset) throws IOException {
				
				fileInputStream.skip(offset);
				
			}
    		  
    	  };
    	  
    	 return rais;
      }
      
      private void handleResponse(Socket socket){
         try{
            InputStream inS = socket.getInputStream();
            if(inS == null)
               return;
            byte[] buf = new byte[8192];
            int rlen = inS.read(buf, 0, buf.length);
            if(rlen <= 0)
               return;
 
            // Create a BufferedReader for parsing the header.
            ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
            BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
            Properties pre = new Properties();
 
            // Decode the header into params and header java properties
            if(!decodeHeader(socket, hin, pre))
               return;
            
            String range = pre.getProperty("range");
 
            Properties headers = new Properties();
            if(fileSize!=-1)
               headers.put("Content-Length", String.valueOf(fileSize));
            headers.put("Accept-Ranges", canSeek ? "bytes" : "none");
 
            int sendCount;
 
            String status;
            if(range==null || !canSeek) {
               status = "200 OK";
               sendCount = (int)fileSize;
            }else {
               if(!range.startsWith("bytes=")){
                  sendError(socket, HTTP_416, null);
                  return;
               }
               if(debug)
                  log(range);
               range = range.substring(6);
               long startFrom = 0, endAt = -1;
               int minus = range.indexOf('-');
               if(minus > 0){
                  try{
                     String startR = range.substring(0, minus);
                     startFrom = Long.parseLong(startR);
                     String endR = range.substring(minus + 1);
                     endAt = Long.parseLong(endR);
                  }catch(NumberFormatException nfe){
                  }
               }
 
               if(startFrom >= fileSize){
                  sendError(socket, HTTP_416, null);
                  inS.close();
                  return;
               }
               if(endAt < 0)
                  endAt = fileSize - 1;
               sendCount = (int)(endAt - startFrom + 1);
               if(sendCount < 0)
                  sendCount = 0;
               status = "206 Partial Content";
               ((RandomAccessInputStream)is).seek(startFrom);
 
               headers.put("Content-Length", "" + sendCount);
               String rangeSpec = "bytes " + startFrom + "-" + endAt + "/" + fileSize;
               headers.put("Content-Range", rangeSpec);
            }
            sendResponse(socket, status, fileMimeType, headers, is, sendCount, buf, null);
            inS.close();
            if(debug)
               log("Http stream finished");
         }catch(IOException ioe){
            if(debug)
               ioe.printStackTrace();
            try{
               sendError(socket, HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }catch(Throwable t){
            }
         }catch(InterruptedException ie){
            // thrown by sendError, ignore and exit the thread
            if(debug)
               ie.printStackTrace();
         }
      }
 
      private boolean decodeHeader(Socket socket, BufferedReader in, Properties pre) throws InterruptedException{
         try{
            // Read the request line
            String inLine = in.readLine();
            if(inLine == null)
               return false;
            StringTokenizer st = new StringTokenizer(inLine);
            if(!st.hasMoreTokens())
               sendError(socket, HTTP_BADREQUEST, "Syntax error");
 
            String method = st.nextToken();
            if(!method.equals("GET"))
               return false;
 
            if(!st.hasMoreTokens())
               sendError(socket, HTTP_BADREQUEST, "Missing URI");
 
            while(true) {
               String line = in.readLine();
               if(line==null)
                  break;
   //            if(debug && line.length()>0) log(line);
               int p = line.indexOf(':');
               if(p<0)
                  continue;
               final String atr = line.substring(0, p).trim().toLowerCase();
               final String val = line.substring(p + 1).trim();
               pre.put(atr, val);
               
               if (debug)
            	   log ("header=" + atr + ": " + val);
            }
         }catch(IOException ioe){
            sendError(socket, HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
         }
         return true;
      }
   }
 
 
   /**
    * @param fileName is display name appended to Uri, not really used (may be null), but client may display it as file name.
    * @return Uri where this stream listens and servers.
    */
   public Uri getUri(String fileName){
      int port = serverSocket.getLocalPort();
      String url = "http://localhost:"+port;
      if(fileName!=null)
         url += '/'+URLEncoder.encode(fileName);
      return Uri.parse(url);
   }
 
   public void close(){
      log("Closing stream over http");
      try{
         serverSocket.close();
         mainThread.join();
      }catch(Exception e){
         e.printStackTrace();
      }
   }
 
   /**
    * Returns an error message as a HTTP response and
    * throws InterruptedException to stop further request processing.
    */
   private static void sendError(Socket socket, String status, String msg) throws InterruptedException{
      sendResponse(socket, status, "text/plain", null, null, 0, null, msg);
      throw new InterruptedException();
   }
 
  private static void copyStream(InputStream in, OutputStream out, byte[] tmpBuf, long maxSize) throws IOException{
 
     while(maxSize>0){
        int count = (int)Math.min(maxSize, tmpBuf.length);
        count = in.read(tmpBuf, 0, count);
        if(count<0)
           break;
        out.write(tmpBuf, 0, count);
        maxSize -= count;
     }
  }
   /**
    * Sends given response to the socket, and closes the socket.
    */
   private static void sendResponse(Socket socket, String status, String mimeType, Properties header, InputStream isInput, int sendCount, byte[] buf, String errMsg){
      try{
         OutputStream out = socket.getOutputStream();
         PrintWriter pw = new PrintWriter(out);
 
         {
            String retLine = "HTTP/1.0 " + status + " \r\n";
            pw.print(retLine);
         }
         if(mimeType!=null) {
            String mT = "Content-Type: " + mimeType + "\r\n";
            pw.print(mT);
         }
         if(header != null){
            Enumeration<?> e = header.keys();
            while(e.hasMoreElements()){
               String key = (String)e.nextElement();
               String value = header.getProperty(key);
               String l = key + ": " + value + "\r\n";
               if(debug) log(l);
               pw.print(l);
            }
         }
         pw.print("\r\n");
         pw.flush();
         if(isInput!=null)
            copyStream(isInput, out, buf, sendCount);
         else if(errMsg!=null) {
            pw.print(errMsg);
            pw.flush();
         }
         out.flush();
         out.close();
      }catch(IOException e){
         if(debug)
            log(e.getMessage());
      }finally {
         try{
            socket.close();
         }catch(Throwable t){
         }
      }
   }
   
   private final static void log (String msg)
   {
	   if (debug)
		   Log.d("StreamProxy",msg);
   }
}
 
/**
 * Seekable InputStream.
 * Abstract, you must add implementation for your purpose.
 */
abstract class RandomAccessInputStream extends InputStream{
 
   /**
    * @return total length of stream (file)
    */
   abstract long length();
 
   /**
    * Seek within stream for next read-ing.
    */
   abstract void seek(long offset) throws IOException;
 
   @Override
   public int read() throws IOException{
      byte[] b = new byte[1];
      read(b);
      return b[0]&0xff;
   }
}