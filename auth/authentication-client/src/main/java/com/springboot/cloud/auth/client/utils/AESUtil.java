package com.springboot.cloud.auth.client.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;


public enum AESUtil {
	;

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String encrypt(String src, String key, String iv) {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, makeKey(key), makeIv(iv));
//			return Base64.encodeBytes(cipher.doFinal(src.getBytes()));
			return bytesToHex(cipher.doFinal(src.getBytes())).toLowerCase();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String decrypt(String src, String key, String iv) {
		String decrypted = "";
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, makeKey(key), makeIv(iv));
//			decrypted = new String(cipher.doFinal(Base64.decode(src)));
			decrypted = new String(cipher.doFinal(hexStringToByteArray(src)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return decrypted;
	}
	
	static AlgorithmParameterSpec makeIv(String iv) {
		try {
			return new IvParameterSpec(iv.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static Key makeKey(String keyTemp) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] key = md.digest(keyTemp.getBytes("UTF-8"));
			return new SecretKeySpec(keyTemp.getBytes(), "AES");
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}


}
