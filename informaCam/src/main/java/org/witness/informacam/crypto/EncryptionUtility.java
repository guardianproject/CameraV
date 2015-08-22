package org.witness.informacam.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPCompressedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPLiteralDataGenerator;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPUtil;
import org.witness.informacam.models.credentials.ISecretKey;
import org.witness.informacam.utils.Constants.App.Crypto;

import android.util.Base64;
import android.util.Log;

public class EncryptionUtility {
	@SuppressWarnings("unused")
	private final static String LOG = Crypto.LOG;
	
	@SuppressWarnings("deprecation")
	public final static byte[] encrypt(byte[] data, byte[] publicKey) {
		try {
			BouncyCastleProvider bc = new BouncyCastleProvider();
			int bufferSize = 1 << 16;
			
			Security.addProvider(bc);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStream aos = new ArmoredOutputStream(baos);
			
			PGPEncryptedDataGenerator edg = new PGPEncryptedDataGenerator(PGPEncryptedData.AES_256, true, new SecureRandom(), bc);
			edg.addMethod(KeyUtility.extractPublicKeyFromBytes(publicKey));
			OutputStream encOs = edg.open(aos, new byte[bufferSize]);
			
			PGPCompressedDataGenerator cdg = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
			OutputStream compOs = cdg.open(encOs);
			
			PGPLiteralDataGenerator ldg = new PGPLiteralDataGenerator();
			OutputStream litOs = ldg.open(compOs, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, new Date(System.currentTimeMillis()), new byte[bufferSize]);
			
			InputStream is = new ByteArrayInputStream(data);
			byte[] buf = new byte[bufferSize];
			
			int len;
			while((len = is.read(buf)) > 0)
				litOs.write(buf, 0, len);
			
			litOs.flush();
			litOs.close();
			ldg.close();
			
			compOs.flush();
			compOs.close();
			cdg.close();
			
			encOs.flush();
			encOs.close();
			edg.close();
			
			baos.flush();
			aos.close();
			baos.close();
			
			is.close();
			
			return baos.toByteArray();
			
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			return null;
		} catch (PGPException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public final static void encrypt(InputStream is, OutputStream os, byte[] publicKey) throws NoSuchProviderException, PGPException, IOException {
		
		BouncyCastleProvider bc = new BouncyCastleProvider();
		int bufferSize = 1 << 16;
		
		Security.addProvider(bc);
		
		OutputStream aos = new ArmoredOutputStream(os);
		
		PGPEncryptedDataGenerator edg = new PGPEncryptedDataGenerator(PGPEncryptedData.AES_256, true, new SecureRandom(), bc);
		edg.addMethod(KeyUtility.extractPublicKeyFromBytes(publicKey));
		OutputStream encOs = edg.open(aos, new byte[bufferSize]);
		
		PGPCompressedDataGenerator cdg = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
		OutputStream compOs = cdg.open(encOs);
		
		PGPLiteralDataGenerator ldg = new PGPLiteralDataGenerator();
		OutputStream litOs = ldg.open(compOs, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, new Date(System.currentTimeMillis()), new byte[bufferSize]);
		
		byte[] buf = new byte[bufferSize];
		
		int len;
		while((len = is.read(buf)) > 0)
			litOs.write(buf, 0, len);
		
		litOs.flush();
		litOs.close();
		ldg.close();
		
		compOs.flush();
		compOs.close();
		cdg.close();
		
		encOs.flush();
		encOs.close();
		edg.close();
		
		aos.close();
		
		is.close();
			
	}
	
	public static info.guardianproject.iocipher.File decrypt(info.guardianproject.iocipher.File file, info.guardianproject.iocipher.File newFile, ISecretKey secretKey) {
		try {
			info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(file);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			fis.close();

			info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(newFile);
			fos.write(decrypt(bytes, secretKey));
			fos.flush();
			fos.close();

			return newFile;
		} catch (FileNotFoundException e) {
			Log.e(Crypto.LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(Crypto.LOG, e.toString());
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e(Crypto.LOG, e.toString());
			e.printStackTrace();
		}

		return null;

	}
	
	public static byte[] decrypt(byte[] bytes, ISecretKey secretKey) {
		return decrypt(bytes, false, secretKey);
	}
	
	@SuppressWarnings({ "deprecation", "rawtypes" })
	public static byte[] decrypt(byte[] bytes, boolean isBase64Encoded, ISecretKey secretKey) {
		if(isBase64Encoded) {
			bytes = Base64.decode(bytes, Base64.DEFAULT);
		}
		
		byte[] decryptedBytes = null;
		PGPSecretKey sk = null;
		PGPPrivateKey pk = null;
		
		try {
			
			BouncyCastleProvider bc = new BouncyCastleProvider();

			sk = KeyUtility.extractSecretKey(secretKey.secretKey.getBytes());
			pk = sk.extractPrivateKey(secretKey.secretAuthToken.toCharArray(), bc);
			
			if(sk == null || pk == null) {
				Log.e(Crypto.LOG, "secret key or private key is null");
				return null;
			}

			InputStream is = PGPUtil.getDecoderStream(new ByteArrayInputStream(bytes));

			PGPObjectFactory pgpF = new PGPObjectFactory(is);
			PGPEncryptedDataList edl;

			Object o = pgpF.nextObject();

			if(o instanceof PGPEncryptedDataList)
				edl = (PGPEncryptedDataList) o;
			else
				edl = (PGPEncryptedDataList) pgpF.nextObject();

			Iterator it = edl.getEncryptedDataObjects();
			PGPPublicKeyEncryptedData ed = null;
			ed = (PGPPublicKeyEncryptedData) it.next();
			if(ed == null) {
				Log.e(Crypto.LOG, "No PGPPublicKeyEncryptedData found.");
				return null;
			}

			InputStream clearStream = ed.getDataStream(pk, bc);
			pgpF = new PGPObjectFactory(clearStream);

			PGPLiteralData ld = null;
			o = pgpF.nextObject();
			try {
				PGPCompressedData cd = (PGPCompressedData) o;
				InputStream compressedStream = new BufferedInputStream(cd.getDataStream());
				pgpF = new PGPObjectFactory(compressedStream);

				Object message = pgpF.nextObject();

				if(message instanceof PGPLiteralData) {
					ld = (PGPLiteralData) message;
				}
			} catch(ClassCastException e) {
				ld = (PGPLiteralData) o;
			}

			if(ld == null)
				return null;

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(baos);

			InputStream ldis = ld.getInputStream();
			int ch;

			while((ch = ldis.read()) >= 0)
				bos.write(ch);

			bos.flush();
			bos.close();

			decryptedBytes = baos.toByteArray();
			baos.close();

		} catch (IOException e) {
			Log.e(Crypto.LOG, e.toString());
			e.printStackTrace();
		} catch (PGPException e) {
			Log.e(Crypto.LOG, e.toString());
			e.printStackTrace();
		}


		return decryptedBytes;
	}
}
