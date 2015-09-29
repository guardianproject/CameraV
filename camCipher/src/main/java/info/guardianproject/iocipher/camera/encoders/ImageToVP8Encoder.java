package info.guardianproject.iocipher.camera.encoders;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jcodec.codecs.vpx.VP8Encoder;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mkv.muxer.MKVMuxer;
import org.jcodec.containers.mkv.muxer.MKVMuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.BitmapUtil;
import org.jcodec.scale.RgbToYuv420p;

import android.graphics.Bitmap;

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
public class ImageToVP8Encoder {
    private SeekableByteChannel ch;
    private MKVMuxerTrack videoTrack;
    private int frameNo = 0;
    private MKVMuxer muxer;
    private Size size;
    
    private VP8Encoder encoder;
    private RgbToYuv420p transform;
    
    public ImageToVP8Encoder(SeekableByteChannel ch) throws IOException {
        this.ch = ch;
        // Muxer that will store the encoded frames
        muxer = new MKVMuxer();

        encoder = new VP8Encoder(10);
        transform = new RgbToYuv420p(0, 0);
    }

    
    public void addBitmap(int width, int height, Bitmap src, int frameIdx) throws IOException {
        if (size == null) {
            size = new Size(width,height);
        }
        
        if (videoTrack == null) {
            videoTrack = muxer.createVideoTrack(new Size(width, height), "V_MPEG4/ISO/AVC");
        }
        
        // Add packet to video track
        Picture yuv = Picture.create(width, height, ColorSpace.YUV420);
        transform.transform(BitmapUtil.fromBitmap(src), yuv);        
        ByteBuffer buf = ByteBuffer.allocate(width * height * 3);
        ByteBuffer ff = encoder.encodeFrame(yuv, buf);
        videoTrack.addSampleEntry(ff, frameIdx);

        frameNo++;
    }

    public void finish() throws IOException {
     
    	muxer.mux(ch);
        NIOUtils.closeQuietly(ch);
    }
}
