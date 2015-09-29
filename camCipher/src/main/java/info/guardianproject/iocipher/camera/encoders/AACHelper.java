package info.guardianproject.iocipher.camera.encoders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;
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
//from here: http://stackoverflow.com/questions/21804390/pcm-aac-encoder-pcmdecoder-in-real-time-with-correct-optimization

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AACHelper
{	

    private AudioRecord recorder;
    private AudioTrack player;

    private MediaCodec encoder;
    private MediaCodec decoder;

    private short audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private short channelConfig = AudioFormat.CHANNEL_IN_MONO;

    private int bufferSize;
    private boolean isRecording;
    private boolean isPlaying;

    public void startRecording (OutputStream outStream) throws IOException
    {
    		int read;
    		
            byte[] buffer1 = new byte[bufferSize];

            ByteBuffer[] inputBuffers;
            ByteBuffer[] outputBuffers;

            ByteBuffer inputBuffer;
            ByteBuffer outputBuffer;

            MediaCodec.BufferInfo bufferInfo;
            int inputBufferIndex;
            int outputBufferIndex;

            byte[] outData;

            encoder.start();
            recorder.startRecording();
            isRecording = true;
            
            while (isRecording)
            {
                read = recorder.read(buffer1, 0, bufferSize);
               // Log.d("AudioRecoder", read + " bytes read");
                //------------------------

                inputBuffers = encoder.getInputBuffers();
                outputBuffers = encoder.getOutputBuffers();
                inputBufferIndex = encoder.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0)
                {
                    inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();

                    inputBuffer.put(buffer1);

                    encoder.queueInputBuffer(inputBufferIndex, 0, buffer1.length, 0, 0);
                }

                bufferInfo = new MediaCodec.BufferInfo();
                outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 0);

                while (outputBufferIndex >= 0)
                {
                    outputBuffer = outputBuffers[outputBufferIndex];

                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);

                    //TODO write out Data to stream?
                    outStream.write(outData);
                    
                    encoder.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 0);

                }
                // ----------------------;

            }
            encoder.stop();
            recorder.stop();
         
        }
    
    public void stopRecording ()
    {
    	isRecording = false;
    }
       

		public void startPlaying(InputStream is)
        {
            int len = 1024;
            byte[] buffer2 = new byte[len];

            byte[] data = new byte[len];

            ByteBuffer[] inputBuffers;
            ByteBuffer[] outputBuffers;

            ByteBuffer inputBuffer;
            ByteBuffer outputBuffer;

            MediaCodec.BufferInfo bufferInfo;
            int inputBufferIndex;
            int outputBufferIndex;
            byte[] outData;
            try
            {
                player.play();
                decoder.start();
                isPlaying = true;
                while (isPlaying)
                {
                    
                    	int read = is.read(data);
                    	
                        //===========
                        inputBuffers = decoder.getInputBuffers();
                        outputBuffers = decoder.getOutputBuffers();
                        inputBufferIndex = decoder.dequeueInputBuffer(-1);
                        if (inputBufferIndex >= 0)
                        {
                            inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();

                            inputBuffer.put(data);

                            decoder.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
                        }

                        bufferInfo = new MediaCodec.BufferInfo();
                        outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);

                        while (outputBufferIndex >= 0)
                        {
                            outputBuffer = outputBuffers[outputBufferIndex];

                            outputBuffer.position(bufferInfo.offset);
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                            outData = new byte[bufferInfo.size];
                            outputBuffer.get(outData);

                          //  Log.d("AudioDecoder", outData.length + " bytes decoded");

                            player.write(outData, 0, outData.length);

                            decoder.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);

                        }

                        //===========

                    
                }

                decoder.stop();
                player.stop();

            }
            catch (Exception e)
            {
            }
        }


    protected void onDestroy()
    {

        recorder.release();
        player.release();
        encoder.release();
        decoder.release();

    } 
    
    private int initAudioRecord(int rate)
    {
        try
        {
            Log.v("===========Attempting rate ", rate + "Hz, bits: " + audioFormat + ", channel: " + channelConfig);
            bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

            if (bufferSize != AudioRecord.ERROR_BAD_VALUE)
            {
                // check if we can instantiate and have a success
                recorder = new AudioRecord(AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);

                if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                {
                    Log.v("===========final rate ", rate + "Hz, bits: " + audioFormat + ", channel: " + channelConfig);

                    return rate;
                }
            }
        }
        catch (Exception e)
        {
            Log.v("error", "" + rate);
        }

        return -1;
    }

    public boolean setEncoder(int sampleRate, int channels, int bitRate) throws Exception
    {
        encoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channels);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate * 1024);//AAC-HE 64kbps
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        
        initAudioRecord(sampleRate);
        
        return true;
    }

    public boolean setDecoder(int sampleRate, int channels, int bitRate) throws Exception
    {
        decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channels);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate * 1024);//AAC-HE 64kbps
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);

        decoder.configure(format, null, null, 0);
        
        setPlayer(sampleRate);

        return true;
    }

    public boolean setPlayer(int rate)
    {
        int bufferSizePlayer = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, audioFormat);
        Log.d("====buffer Size player ", String.valueOf(bufferSizePlayer));

        player= new AudioTrack(AudioManager.STREAM_MUSIC, rate, AudioFormat.CHANNEL_OUT_MONO, audioFormat, bufferSizePlayer, AudioTrack.MODE_STREAM);


        if (player.getState() == AudioTrack.STATE_INITIALIZED)
        {

            return true;
        }
        else
        {
            return false;
        }

    }


}