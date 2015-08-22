package org.witness.informacam.crypto;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.utils.Constants.App.Crypto;
import org.witness.informacam.utils.Constants.Codes;

import android.util.Base64;
import android.util.Log;

public class AesUtility {
	public final static String LOG = Crypto.LOG;
	
	public static byte[] DecryptWithKey(SecretKey secret_key, byte[] iv, byte[] message) {
		return DecryptWithKey(secret_key, iv, message, true);
	}
	
	public static byte[] DecryptWithKey(SecretKey secret_key, byte[] iv, byte[] message, boolean isBase64) {
		byte[] new_message = null;
		
		if(isBase64) {
			iv = Base64.decode(iv, Base64.DEFAULT);
			message = Base64.decode(message, Base64.DEFAULT);
		}
		
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secret_key, new IvParameterSpec(iv));
			
			new_message = cipher.doFinal(message);
			
		} catch (IllegalBlockSizeException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		return new_message;
	}
	
	public static String EncryptToKey(SecretKey secret_key, String message) {		
		try {
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, secret_key);
			
			AlgorithmParameters params = cipher.getParameters();
			String iv = Base64.encodeToString(params.getParameterSpec(IvParameterSpec.class).getIV(), Base64.DEFAULT);
			String new_message = Base64.encodeToString(cipher.doFinal(message.getBytes("UTF-8")), Base64.DEFAULT);
			
			JSONObject pack = new JSONObject();
			pack.put(Codes.Keys.IV, iv);
			pack.put(Codes.Keys.VALUE, new_message);
			
			return pack.toString();
			
		} catch (IllegalBlockSizeException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (InvalidParameterSpecException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		
		return null;
	}
}
