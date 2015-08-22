package org.witness.informacam.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.witness.informacam.InformaCam;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Informa;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Logger;
import org.xml.sax.SAXException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Base64;
import android.util.Log;

public class IOUtility {
	private final static String LOG = App.Storage.LOG;
	
	public static String buildPublicPath(String[] segments) {
		
		java.io.File fileExt = new java.io.File(Storage.EXTERNAL_DIR);
		if (!fileExt.canWrite());
		{
			fileExt = new File("/sdcard/InformaCam");
		}
		
		String builtPath = buildPath(segments); 
		
		return StringUtils.join(new String[] {fileExt.getAbsolutePath(), builtPath }, builtPath.startsWith("/") ? "" : "/");
	}
	
	public static String buildPath(String[] segments) {
		//Log.d(LOG, StringUtils.join(segments, "/"));
		return StringUtils.join(segments, "/");
	}

	public static Uri getUriFromFile(Context context, Uri authority, java.io.File file) {
		Uri uri = null;

		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(authority, new String[] {BaseColumns._ID}, MediaColumns.DATA + "=?", new String[] {file.getAbsolutePath()}, null);
		if(c != null && c.moveToFirst()) {
			uri = Uri.withAppendedPath(authority, String.valueOf(c.getLong(c.getColumnIndex(BaseColumns._ID))));
			c.close();
		}
		return uri;
	}
	
	public final static JSONObject xmlToJson(InputStream is) {
		
		try {
			JSONObject j = new JSONObject();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();

			NodeList answers = doc.getDocumentElement().getChildNodes();
			Log.d(LOG, "there are " + answers.getLength() + " child nodes");
			for(int n=0; n<answers.getLength(); n++) {
				Node node = answers.item(n);

				Log.d(LOG, "node: " + node.getNodeName());
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					try {
						Element el = (Element) node;
						j.put(node.getNodeName(), el.getFirstChild().getNodeValue());
					} catch(NullPointerException e) {
						continue;
					}
				}
			}
			
			return j;
		} catch (ParserConfigurationException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (DOMException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public final static byte[] getBytesFromBitmap(Bitmap bitmap) {
		return getBytesFromBitmap(bitmap, false);
	}
	
	public final static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
		return getBytesFromBitmap(bitmap, quality, false);
	}

	public final static byte[] getBytesFromBitmap(Bitmap bitmap, boolean asBase64) {
		return getBytesFromBitmap(bitmap, 100, asBase64);
	}

	public final static byte[] getBytesFromBitmap(Bitmap bitmap, int quality, boolean asBase64) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		if(asBase64) {
			return Base64.encode(baos.toByteArray(), Base64.DEFAULT);
		} else {
			return baos.toByteArray();
		}
	}

	public static byte[] gzipBytes(byte[] bytes) {		
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gos = new GZIPOutputStream(baos);
			gos.write(bytes);
			gos.flush();
			gos.close();
			
			return baos.toByteArray();
		} catch (IOException e) {
			Logger.e(LOG, e);
		}
		
		return null;
	}
	
	public static byte[] zipBytes(byte[] bytes, String fileName, int source) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			bytes = null;
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();			
			ZipOutputStream zos = new ZipOutputStream(baos);
			ZipEntry ze = new ZipEntry(fileName);
			ze.setSize(bais.available());
			
			zos.putNextEntry(ze);
			byte[] buf = new byte[1024];
			int b;
			while((b = bais.read(buf)) > 0) {
				zos.write(buf, 0, b);
			}
			
			zos.closeEntry();
			zos.close();
			
			return baos.toByteArray();
		} catch (FileNotFoundException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}

		return null;

	}
	
	public static boolean zipFiles(Map<String, InputStream> elements, String fileName, int destination) {
		ZipOutputStream zos = null;
		Logger.d(LOG, "ZIPPING TO: " + fileName);
		
		try {
			switch(destination) {
			case Type.IOCIPHER:
				zos = new ZipOutputStream(new info.guardianproject.iocipher.FileOutputStream(fileName));
				break;
			case Type.INTERNAL_STORAGE:
				zos = new ZipOutputStream(new java.io.FileOutputStream(fileName));
				break;
			case Type.FILE_SYSTEM:
				zos = new ZipOutputStream(new java.io.FileOutputStream(fileName));
				break;
			}

			Iterator<Entry<String, InputStream>> i = elements.entrySet().iterator();
			while(i.hasNext()) {
				Entry<String, InputStream> file = i.next();
				
				Logger.d(LOG, "zipping up: " + file.getKey() + " (bytes: " + file.getValue().available() + ")");
				
				ZipEntry ze = new ZipEntry(file.getKey());
				zos.putNextEntry(ze);

				IOUtils.copyLarge(file.getValue(), zos);
				
				zos.flush();
			}

			zos.close();
			return true;
		} catch(IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}

		return false;
	}
	
	public final static Bitmap getBitmapFromFile(String pathToFile, int source) throws IOException {
		return getBitmapFromFile(pathToFile, source, -1);
	}
	
	public final static Bitmap getBitmapFromFile(String pathToFile, int source,int size) throws IOException {
		InputStream is = InformaCam.getInstance().ioService.getStream(pathToFile, source);

		BitmapFactory.Options opts = new BitmapFactory.Options();
		
		if (size != -1)
		{
	        opts.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(is, null, opts);
			
	     // scale the image
	        float maxSideLength = size;
	        float scaleFactor = Math.min(maxSideLength / opts.outWidth, maxSideLength / opts.outHeight);
	        // do not upscale!
	        if (scaleFactor < 1) {
	            opts.inDensity = 10000;
	            opts.inTargetDensity = (int) ((float) opts.inDensity * scaleFactor);
	        }
	        opts.inJustDecodeBounds = false;
	
	        try {
	            is.close();
	        } catch (IOException e) {
	            // ignore
	        }
	        try {
	        	is = InformaCam.getInstance().ioService.getStream(pathToFile, source);
	        } catch (FileNotFoundException e) {
	            Log.e(Informa.LOG, "Image not found.", e);
	            return null;
	        }
		}
		
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
        try {
            is.close();
        } catch (IOException e) {
            // ignore
        }

		return bitmap;
	}

	public static List<String> unzipFile (byte[] rawContent, String root, int destination) {
		IOService ioService = InformaCam.getInstance().ioService;
		List<String> paths = new ArrayList<String>();

		String rootFolderPath = "";

		switch(destination) {
		case Type.IOCIPHER:
			info.guardianproject.iocipher.File zf;
			if(root != null) {
				info.guardianproject.iocipher.File rootFolder = new info.guardianproject.iocipher.File(root);
				if(!rootFolder.exists()) {
					rootFolder.mkdir();
				}

				zf = new info.guardianproject.iocipher.File(rootFolder, System.currentTimeMillis() + ".zip");
				rootFolderPath = rootFolder.getAbsolutePath();
			} else {
				zf = new info.guardianproject.iocipher.File(System.currentTimeMillis() + ".zip");
			}

			ioService.saveBlob(rawContent, zf);
			break;
		}

		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(rawContent));

		ZipEntry entry = null;
		try {
			while((entry = zis.getNextEntry()) != null) {
				boolean isOmitable = false;
				for(String omit : Storage.ICTD.ZIP_OMITABLES) {
					if(entry.getName().contains(omit) || String.valueOf(entry.getName().charAt(0)).compareTo(".") == 0) {
						isOmitable = true;
					}

					if(isOmitable)
						break;
				}

				if(isOmitable)
					continue;

				if(entry.isDirectory()) {
					switch(destination) {
					case Type.IOCIPHER:
						info.guardianproject.iocipher.File rootFolder = new info.guardianproject.iocipher.File(entry.getName());
						if(!rootFolder.exists()) {
							rootFolder.mkdir();
						}

						rootFolderPath = rootFolder.getAbsolutePath();
						break;
					}

					continue;
				}

				BufferedOutputStream bos = null;
				try {
					switch(destination) {
					case Type.IOCIPHER:
						info.guardianproject.iocipher.File entryFile = new info.guardianproject.iocipher.File(rootFolderPath, entry.getName());
						bos = new BufferedOutputStream(new info.guardianproject.iocipher.FileOutputStream(entryFile));
						paths.add(entryFile.getAbsolutePath());
						break;
					}

					byte[] buf = new byte[1024];
					int ch;
					while((ch = zis.read(buf)) > 0) {
						bos.write(buf, 0, ch);
					}

					bos.close();

				} catch (FileNotFoundException e) {
					Log.e(LOG, e.toString());
					e.printStackTrace();
					return null;
				}
			}


			zis.close();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			return null;
		}

		return paths;
	}
	
	public static boolean unGZipAndSave(byte[] bytes, String pathToFile, int source) {
		ByteArrayOutputStream baos = unGZipBytes(bytes);
		
		if(baos != null) {
			switch(source) {
			case Type.IOCIPHER:
				info.guardianproject.iocipher.File ifile = new info.guardianproject.iocipher.File(pathToFile);
				return InformaCam.getInstance().ioService.saveBlob(baos.toByteArray(), ifile);
			case Type.FILE_SYSTEM:
				java.io.File jfile = new java.io.File(pathToFile);
				try {
					return InformaCam.getInstance().ioService.saveBlob(baos.toByteArray(), jfile, true);
				} catch (IOException e) {
					Log.e(LOG, e.toString());
					e.printStackTrace();
				}
			}
			
		}
		return false;
	}

	public static ByteArrayOutputStream unGZipBytes(byte[] bytes) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			GZIPInputStream gis = new GZIPInputStream(bais);
			BufferedReader br = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"));
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int c;
			while((c = br.read()) != -1) {
				baos.write(c);
			}
			
			br.close();
			gis.close();
			bais.close();
			
			baos.close();
			return baos;
			
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		return null;
		
	}
}