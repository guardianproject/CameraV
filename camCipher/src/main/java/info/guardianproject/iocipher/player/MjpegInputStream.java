package info.guardianproject.iocipher.player;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MjpegInputStream extends DataInputStream {
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final String CONTENT_LENGTH = "Content-Length";
    //private final static int HEADER_MAX_LENGTH = 100;
    //private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;

    private final static int HEADER_MAX_LENGTH = 4096*4;
    private final static int FRAME_MAX_LENGTH = 1000000 + HEADER_MAX_LENGTH;

    private int mContentLength = -1;
	private SeekableStream stream;
	private int fullLength = 0; 
    
    public static MjpegInputStream read(String url, File cacheDir) {
        HttpResponse res;
        DefaultHttpClient httpclient = new DefaultHttpClient();     
        try {
            res = httpclient.execute(new HttpGet(URI.create(url)));
            return new MjpegInputStream(res.getEntity().getContent(), cacheDir);              
        } catch (ClientProtocolException e) {
        } catch (IOException e) {}
        return null;
    }


    public MjpegInputStream(InputStream in, File cacheDir) { 
    	super(new BufferedInputStream(in, FRAME_MAX_LENGTH)); 
    	Log.v("BLAH", cacheDir.getAbsolutePath());
    	//this.stream = SeekableStream.wrapInputStream(in, true);
		this.stream = null;
        try {
        	this.stream = new FileCacheSeekableStream(this, cacheDir);
		} catch (IOException e) {
			System.out.println("Couldn't create FileCacheSeekableStream");
			e.printStackTrace();
		}
		
    }

	private int getEndOfSeqeunce(InputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c;
        for(int i=0; i < FRAME_MAX_LENGTH; i++) {
            //c = (byte) stream.readUnsignedByte();
        	c = (byte) stream.read();
            if(c == sequence[seqIndex]) {
                seqIndex++;
                if(seqIndex == sequence.length) return i + 1;
            } else seqIndex = 0;
        }
        return -1;
    }

    private int getStartOfSequence(InputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        try {
        	props.load(headerIn);
        } catch (IllegalArgumentException e) {
        	System.err.println("IllegalArgumentException...");
        	e.printStackTrace();
        }
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }   

    public Bitmap readMjpegFrame() throws IOException {
        stream.mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        stream.reset();
        if (headerLen < 1)
            return null;

        byte[] header = new byte[headerLen];
        //Log.v("MjpegInputStream", "headerLen: " + headerLen);
        stream.readFully(header);
        //buffer.write(header);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) { 
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER); 
        }
        stream.reset();
        fullLength += mContentLength;
        //Log.v("MjpegInputStream", "mContentLength: " + mContentLength);
        byte[] frameData = new byte[mContentLength];
        stream.skipBytes(headerLen);
        stream.readFully(frameData);
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
    
  

	public void seek(int pos) throws IOException {
		this.stream.seek(pos);
	}
	
	public long getFilePointer() throws IOException {
		return this.stream.getFilePointer();
	}

	public double getPosition() throws IOException {
		long point = getFilePointer();
		System.out.println("FilePointer = " + point);
		System.out.println("FullLength = " + fullLength);
		return (fullLength==0) ? 0 : (double)point / fullLength;
	}
	
	public int getSize() throws IOException {
		return fullLength;
	}

	public void seekTo(double ratio) throws IOException {
		int pos = (int) ((double)fullLength * ratio);
		System.out.println("seek to position = " + pos);
		seek(pos);
	}

	public void seekToLive() throws IOException {
		int nearEnd = fullLength-FRAME_MAX_LENGTH;
		seek((nearEnd > 0) ? fullLength-FRAME_MAX_LENGTH : fullLength);		
	}
}
