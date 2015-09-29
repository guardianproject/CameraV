package info.guardianproject.iocipher.camera.encoders;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jcodec.codecs.vpx.IVFMuxer;
import org.jcodec.codecs.vpx.VP8Encoder;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mkv.muxer.MKVMuxerTrack;

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
public class YUVtoWebmMuxer {
    private SeekableByteChannel ch;
    private VP8Encoder encoder;    
    private MKVMuxerTrack videoTrack;
    private IVFMuxer muxer;
    
    public YUVtoWebmMuxer(SeekableByteChannel ch, int width, int height) throws IOException {
    
        this.ch = ch;
        
        muxer = new IVFMuxer(ch,width,height,25);

        // Create an instance of encoder
        encoder = new VP8Encoder(10);


    }

    public void encodeNativeFrame(ByteBuffer data, int width, int height, int frameIdx) throws IOException {
    	
        Picture yuv = Picture.create(width, height, ColorSpace.YUV420);    
        
        ByteBuffer ff = encoder.encodeFrame(yuv, data);
        Packet packet = new Packet(ff, frameIdx, 1, 1, frameIdx, true, null);
        muxer.addFrame(packet);


    }

    public void finish() throws IOException {

        NIOUtils.closeQuietly(ch);
    }
}
