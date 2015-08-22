package org.witness.informacam.informa.embed;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;

import org.apache.commons.io.IOUtils;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IAsset;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.transport.ITransportStub;
import org.witness.informacam.utils.MediaHasher;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Ffmpeg;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.MetadataEmbededListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class VideoConstructor {
	
	java.io.File fileBinDir;
	
	FfmpegController ffmpegCtrl;
	String ffmpegBinPath;
	
	IMedia media;
	IAsset destinationAsset, sourceAsset, metadata;
	ITransportStub connection;

	private final static String LOG = Ffmpeg.LOG;
	//private boolean intendedForIOCipher = false;
	
	public VideoConstructor(Context context) throws FileNotFoundException, IOException {
		fileBinDir = context.getDir("bin",Context.MODE_PRIVATE);		
		ffmpegCtrl = new FfmpegController(context, context.getCacheDir());
		ffmpegBinPath = ffmpegCtrl.getBinaryPath();
	}
	
	public VideoConstructor(InformaCam informaCam, IMedia media, IAsset destinationAsset, ITransportStub connection) throws IOException {
		this(informaCam);
		
		this.media = media;
		this.destinationAsset = destinationAsset;
		this.connection = connection;
		
		sourceAsset = this.media.dcimEntry.fileAsset;
		metadata = media.getAsset(this.media.dcimEntry.name + ".j3m");
		
		String metadataPath = metadata.path;
		String sourcePath = sourceAsset.path;
		
		int streamCount = 2;
		
		if (destinationAsset.source == Type.FILE_SYSTEM)
		{

			java.io.File fileDest = new java.io.File(destinationAsset.path);
			if (fileDest.exists())
				fileDest.delete(); //delete a file if it is there
			else
			{
				fileDest.getParentFile().mkdirs();
			}
		
			
			if(sourceAsset.source == Type.IOCIPHER) {
				
				String basePath = fileDest.getParentFile().getAbsolutePath();
				
				// If the assets were in IOCIPHER, we have to save them to local storage.
				// unfortunately, Ffmpeg CLI works that way.
				metadataPath = metadata.copy(Type.IOCIPHER, Type.FILE_SYSTEM, basePath);
				sourcePath = sourceAsset.copy(Type.IOCIPHER, Type.FILE_SYSTEM, basePath);			
			
			}

			constructVideo(streamCount,sourcePath,metadataPath,destinationAsset.path);
		}
		else if (destinationAsset.source == Type.IOCIPHER)
		{
			//FOR NOW, just set destination to source, so we don't need to make huge copies
			//should use JCodec here to make MKV file in java from source MP4 and metadata track
			//info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(new info.guardianproject.iocipher.File(sourceAsset.path));
			//info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(new info.guardianproject.iocipher.File(destinationAsset.path));
	//		IOUtils.copyLarge(fis, fos);
		
			destinationAsset.path = sourceAsset.path;
			
			if(connection != null) {
				
				connection.setAsset(destinationAsset, "video/mp4", destinationAsset.source);
				((MetadataEmbededListener) media).onMediaReadyForTransport(connection);
			}
			
			((MetadataEmbededListener) media).onMetadataEmbeded(destinationAsset);
		}
		
	}

	private void constructVideo(int streamCount, String sourcePath, String metadataPath, String outPath) throws IOException {
		
		String[] ffmpegCommand = new String[] {
				ffmpegBinPath, "-y", "-i", sourcePath,
				"-attach", metadataPath,
				"-metadata:s:" + streamCount, "mimetype=\"text/plain\"",
				"-vcodec", "copy",				
				"-acodec", "copy",
				outPath
		};

		StringBuffer sb = new StringBuffer();
		for(String f: ffmpegCommand) {
			sb.append(f + " ");
		}
		Log.d(LOG, "command to ffmpeg: " + sb.toString());

		try {
			execProcess(ffmpegCommand, new ShellUtils.ShellCallback () {

				@Override
				public void shellOut(String shellLine) {
					Log.d(LOG, shellLine);
				}

				@Override
				public void processComplete(int exitValue) {
					Log.d(LOG, "ffmpeg process completed");
					
					// if user wanted this encrypted, copy the destination asset
					/*
					if(intendedForIOCipher) {
						try {
							destinationAsset.copy(Type.FILE_SYSTEM, Type.IOCIPHER, media.rootFolder);
							storageType = Storage.Type.IOCIPHER;
						} catch (IOException e) {
							Logger.e(LOG, e);
						}
						
						java.io.File publicRoot = new java.io.File(IOUtility.buildPublicPath(new String[] { media.rootFolder }));
						informaCam.ioService.clear(publicRoot.getAbsolutePath(), Type.FILE_SYSTEM);
					}*/
					
					if(connection != null) {
						
						connection.setAsset(destinationAsset, "video/mp4", destinationAsset.source);
						((MetadataEmbededListener) media).onMediaReadyForTransport(connection);
					}
					
					((MetadataEmbededListener) media).onMetadataEmbeded(destinationAsset);
				}
			},null);


		} catch (Exception e) {
			Logger.e(LOG, e);
		}
	}

	String newHash = null;
	
	
	public void testFFmpeg() {
		execProcess(new String[] {ffmpegBinPath, "-version"}, new ShellUtils.ShellCallback () {

			@Override
			public void shellOut(String shellLine) {
				Logger.d(LOG, shellLine);
				
			}

			@Override
			public void processComplete(int exitValue) {
				Logger.d(LOG, "DONE WITH: " + exitValue);
			}
		},null);
	}
	
	public String hashVideo(String pathToMedia, int fileType, String extension) {
		/**
		 * Hashes the video frames 
		 * using FFMpeg's RGB hashing function and
		 * hashes audio stream
		 */
		
		try
		{
			java.io.File tmpMedia = null;
			
			if (fileType == Type.FILE_SYSTEM)
			{
				tmpMedia = new java.io.File(pathToMedia);
			
				String[] cmdHash = new String[] {
						ffmpegBinPath, "-i", tmpMedia.getCanonicalPath(),
						"-vcodec", "copy", "-an", "-f", "md5", "-"
				};
				
				//Logger.d(LOG, "ALSO, Storage.EXTERNAL_DIR: " + Storage.EXTERNAL_DIR);
				//Logger.d(LOG, "HASING VIDEO: " + tmpMedia.getAbsolutePath() + " (cannonical: " + tmpMedia.getCanonicalPath() + ")");
				
				execProcess(cmdHash, new ShellUtils.ShellCallback () {
	
					@Override
					public void shellOut(String shellLine) {
						
						if(shellLine.contains("MD5=")) {
							String hashLine = shellLine.split("=")[1];
							newHash = hashLine.split(" ")[0].trim();
						}
						
					}
	
					@Override
					public void processComplete(int exitValue) {
						
							if (newHash == null)
								newHash = "unknown";
					}
				},null);
				
				//wait for a hash to be found, or set to unknown
				while (newHash == null)
				{
					try { Thread.sleep(500); } 
					catch (Exception e){}
				}
				
				return newHash;
			}
			else if (fileType == Type.IOCIPHER)
			{
			
				//just use the hash of the first thumbnail for now
				String thumbPath = pathToMedia + ".thumb.jpg";
				InputStream is = new info.guardianproject.iocipher.FileInputStream(new info.guardianproject.iocipher.File(thumbPath));
			    newHash = MediaHasher.getJpegHash(is); //this is sha-1
			    is.close();
			    return newHash;
				/*
				try {
					
				    String[] cmdHash = { ffmpegBinPath, "-i", "pipe:",
							"-vcodec", "copy", "-an", "-f", "md5", "-" };
				    
				    InputStream is = new info.guardianproject.iocipher.FileInputStream(new info.guardianproject.iocipher.File(pathToMedia));
				    
				    execProcess(cmdHash, new ShellUtils.ShellCallback () {
				    	
						@Override
						public void shellOut(String shellLine) {
							
							if(shellLine.contains("MD5=")) {
								String hashLine = shellLine.split("=")[1];
								newHash = hashLine.split(" ")[0].trim();
							}
							
						}
		
						@Override
						public void processComplete(int exitValue) {
							
								if (newHash == null)
									newHash = "unknown";
						}https://news.google.com/
					},is);
					
					while (newHash == null)https://news.google.com/
					{
						try { Thread.sleep(500); } 
						catch (Exception e){}
					}
					
					if (fileType == Type.IOCIPHER)
						tmpMedia.delete();
					
					return newHash;
				    
				} catch (Exception ex) {
					Log.d("VideoCon","error",ex);
				    return null;
				}
				*/
				
			}
			else
			{
				return null;
			}
			
		}
		catch (Exception e)
		{
			Log.d("VideoCon","error",e);
		}
		
		return null;
	}
	
	private static void execProcess(String[] cmds, ShellUtils.ShellCallback sc, InputStream is) {
		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.redirectErrorStream(true);
		Process process;
		try {
			process = pb.start();
			BufferedReader reader = null;
			
			if (is != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				Pipe.pipe(process, is, baos);
			
				reader = new BufferedReader(new InputStreamReader(new StringBufferInputStream(baos.toString())));
			}
			else
			{
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			}
			
			String line;

			while ((line = reader.readLine()) != null)
			{
				if (sc != null) {
					Log.d(LOG, line);
					sc.shellOut(line);
				}
			}

			int result = process.waitFor();
			
			sc.processComplete(result);
			
			process.destroy();   
			
		} catch (Exception e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}      

	}
	
	public Bitmap getAFrame(java.io.File source, int[] dims) throws IOException {
		return getAFrame(source, dims, 1);
	}

	public Bitmap getAFrame(java.io.File source, int[] dims, int frame) throws IOException {		
		final java.io.File tmp = new java.io.File(Storage.EXTERNAL_DIR, "bmp_" + System.currentTimeMillis());

		String[] ffmpegCommand = new String[] {
				ffmpegBinPath, "-t", String.valueOf(frame), 
				"-i", source.getAbsolutePath(),
				"-ss", "0.5",
				"-s", (dims[0] + "x" + dims[1]),
				"-f", "image2",
				tmp.getAbsolutePath()
		};

		StringBuffer sb = new StringBuffer();
		for(String f: ffmpegCommand) {
			sb.append(f + " ");
		}
		Log.d(LOG, "command to ffmpeg: " + sb.toString());

		try {
			execProcess(ffmpegCommand, new ShellUtils.ShellCallback () {

				@Override
				public void shellOut(String shellLine) {
					Log.d(LOG, shellLine);
				}

				@Override
				public void processComplete(int exitValue) {
					
				}
			},null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return BitmapFactory.decodeFile(tmp.getAbsolutePath());
	}
}
