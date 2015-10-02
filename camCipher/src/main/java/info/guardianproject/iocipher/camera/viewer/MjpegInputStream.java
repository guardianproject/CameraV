/**
 * 
 * This file contains code from the IOCipher Camera Library "CipherCam".
 *
 * For more information about IOCipher, see https://guardianproject.info/code/iocipher
 * and this sample library: https://github.com/n8fr8/IOCipherCameraExample
 *
 * IOCipher Camera Sample is distributed under this license (aka the 3-clause BSD license)
 *
 * Some of this class was originally part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author n8fr8, The JCodec project
 * 
 */
package info.guardianproject.iocipher.camera.viewer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MjpegInputStream extends DataInputStream {
    private static final String TAG = "MjpegInputStream";

    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 4096*4;
    private final static int FRAME_MAX_LENGTH = 1000000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;

    public MjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }

    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
    	
        int seqIndex = 0;
        byte c;
        int i = 0;
        while(true) {
        	try
        	{
	            c = (byte) in.readUnsignedByte();
	            if(c == sequence[seqIndex]) {
	                seqIndex++;
	                if(seqIndex == sequence.length) {
	                    return i + 1;
	                }
	            } else {
	                seqIndex = 0;
	            }
	            i++;
        	}
        	catch (EOFException ef)
        	{
        		Log.e(TAG,"EOF Exception at idx:" + i);
        		break;
        	}
        }
        return -1;
    }

    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }   

    public Bitmap readMjpegFrame() throws IOException {
    	
    	//if (in.available() < FRAME_MAX_LENGTH)
    		//return null;
    	
        mark(FRAME_MAX_LENGTH);
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        
        if (headerLen < 0)
        	return null;
        
        reset();
        byte[] header = new byte[headerLen];
   //     Log.d(TAG,"frame header: " + new String(header));
        readFully(header);
        mContentLength = getEndOfSeqeunce(this, EOF_MARKER); 
        
        if (mContentLength < 0)
        	return null;
        
        /*
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {             
        	Log.d(TAG, "catch NumberFormatException hit", nfe);
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER); 
        }*/
        reset();
        byte[] frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData);
        
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
}