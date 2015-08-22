package org.witness.informacam.utils;

import org.spongycastle.util.encoders.Hex;

import de.matthiasmann.jpegdecoder.JPEGDecoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;

public class MediaHasher 
{
	public static String hash (File file, String hashFunction)  throws IOException, NoSuchAlgorithmException
	{
		return hash (new FileInputStream(file), hashFunction);
	}
	
	public static String hash (byte[] bytes, String hashFunction) throws NoSuchAlgorithmException, IOException
	{
		return hash (new ByteArrayInputStream(bytes), hashFunction);
	}
	
	public static String hash (InputStream is, String hashFunction) throws IOException, NoSuchAlgorithmException
	{
		MessageDigest digester;
		
		digester = MessageDigest.getInstance(hashFunction); //MD5 or SHA-1
		int BYTE_READ_SIZE = 1024*64; // 64k chunks
		
		  byte[] bytes = new byte[BYTE_READ_SIZE];
		  int byteCount;
		  while ((byteCount = is.read(bytes)) > 0) {
		    digester.update(bytes, 0, byteCount);
		  }
		  
		  byte[] messageDigest = digester.digest();
		  
	      return new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
	
	}
	
	public static String getJpegHash(InputStream is) throws NoSuchAlgorithmException, IOException {
		
		
		JPEGDecoder decoder = new JPEGDecoder(is);
	    decoder.decodeHeader();
	    int width = decoder.getImageWidth();
	    //int height = decoder.getImageHeight();
	    decoder.startDecode();
	    
	    int stride = width*4; //4 bytes per pixel RGBA
	
	    MessageDigest digester = MessageDigest.getInstance("SHA-1");
		
	//    System.out.println("Stride: " + stride);
	    
		for(int h=0; h<decoder.getNumMCURows(); h++) {
			
		    ByteBuffer bb = ByteBuffer.allocate(stride * decoder.getMCURowHeight());

		//	System.out.println("handling row: " + h);
			
		    decoder.decodeRGB(bb, stride, 1);
			
			digester.update(bb.array());
			
		}
		
		byte[] messageDigest = digester.digest();
		return new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
	}
	
	public static String getJpegHash(byte[] jpegBytes) throws NoSuchAlgorithmException, IOException {
		
		JPEGDecoder decoder = new JPEGDecoder(new ByteArrayInputStream(jpegBytes));
	    decoder.decodeHeader();
	    int width = decoder.getImageWidth();
	    //int height = decoder.getImageHeight();
	    decoder.startDecode();
	    
	    int stride = width*4; //4 bytes per pixel RGBA
	
	    MessageDigest digester = MessageDigest.getInstance("SHA-1");
		
	//    System.out.println("Stride: " + stride);
	    
		for(int h=0; h<decoder.getNumMCURows(); h++) {
			
		    ByteBuffer bb = ByteBuffer.allocate(stride * decoder.getMCURowHeight());

		//	System.out.println("handling row: " + h);
			
		    decoder.decodeRGB(bb, stride, 1);
			
			digester.update(bb.array());
			
		}
		
		byte[] messageDigest = digester.digest();
		return new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
	}

	public static String getBitmapHash(Bitmap bitmap) throws NoSuchAlgorithmException, IOException {
		MessageDigest digester = MessageDigest.getInstance("SHA-1");
		
		for(int h=0; h<bitmap.getHeight(); h++) {
			int[] row = new int[bitmap.getWidth()];
			bitmap.getPixels(row, 0, row.length, 0, h, row.length, 1);
			

			//System.out.println("row " + h + "=" + row[0]);
			
			byte[] rowBytes = new byte[row.length];
			for(int b=0; b<row.length; b++) {
				
				int p = row[b];
				rowBytes[b] = (byte) p;
				

				int R = (p >> 16) & 0xff;
				int G = (p >> 8) & 0xff;
				int B = p & 0xff;
				
				if (b == 0)
				System.out.println("row " + h + ": " + R +"," + G + "," + B);
			}
			
			digester.update(rowBytes);

			//byte[] messageDigest = digester.digest();
			//String lineHash = new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
			//System.out.println("line " + h + "=" + lineHash);
			
			rowBytes = null;
			row = null;
			
		}
		
		byte[] messageDigest = digester.digest();
		return new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
	}
	
	/*
	public static String getBitmapHash(java.io.File file) throws NoSuchAlgorithmException, IOException {
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		String hash = "";
		ByteBuffer buf;
		
		buf = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
		
		bitmap.copyPixelsToBuffer(buf);
		hash = MediaHasher.hash(buf.array(), "MD5");
		buf.clear();
		buf = null;
		return hash;
	}
	
	public static String getBitmapHash(info.guardianproject.iocipher.FileInputStream fis) throws NoSuchAlgorithmException, IOException {
		Bitmap bitmap = BitmapFactory.decodeStream(fis);
		String hash = "";
		ByteBuffer buf;
		
		buf = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
		
		bitmap.copyPixelsToBuffer(buf);
		hash = MediaHasher.hash(buf.array(), "MD5");
		buf.clear();
		buf = null;
		return hash;
	}
	
	public static String getBitmapHash(java.io.FileInputStream fis) throws NoSuchAlgorithmException, IOException {
		Bitmap bitmap = BitmapFactory.decodeStream(fis);
		String hash = "";
		ByteBuffer buf;
		
		buf = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
		
		bitmap.copyPixelsToBuffer(buf);
		hash = MediaHasher.hash(buf.array(), "MD5");
		buf.clear();
		buf = null;
		return hash;
	}
	*/
	
}
