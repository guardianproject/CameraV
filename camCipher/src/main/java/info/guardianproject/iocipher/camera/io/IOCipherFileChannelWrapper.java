package info.guardianproject.iocipher.camera.io;


import info.guardianproject.iocipher.IOCipherFileChannel;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jcodec.common.SeekableByteChannel;

import android.util.Log;
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
public class IOCipherFileChannelWrapper implements SeekableByteChannel {

    private IOCipherFileChannel ch;
    private final static String TAG = "IOCipherFileChannelWrapper";
    
    public IOCipherFileChannelWrapper(IOCipherFileChannel ch) throws FileNotFoundException {
        this.ch = ch;
    }

    @Override
    public int read(ByteBuffer arg0) throws IOException {

        return ch.read(arg0);
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

    @Override
    public boolean isOpen() {
        return ch.isOpen();
    }

    @Override
    public synchronized int write(ByteBuffer arg0) throws IOException {
    	int size = arg0.remaining();
    	long newPos = position() + size;
        int result = ch.write(arg0, position());
        position(newPos);
        return result;
    }

    @Override
    public long position() throws IOException {
        return ch.position();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        
        ch.position(newPosition);
        
        return this;
    }

    @Override
    public long size() throws IOException {
        return ch.size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        ch.truncate(size);
        return this;
    }
}