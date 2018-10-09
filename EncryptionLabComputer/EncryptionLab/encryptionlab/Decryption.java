/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptionlab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
* Decryption class that is used to handle decryption 
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */
public class Decryption {

	final String STRING_FORMAT = "UTF-8";
/**
 * Used to handle decryption to use correct mode
 * @param algorithm
 * @param password
 * @param data
 * @return the decrypted data byte array
 */
	public byte[] decryptionHandler(String algorithm, String password, byte[] data) {
		byte[] decryptedData;
		SecretKey key;
		try {
			switch (algorithm) {
			case "AES":
				key = passwordKey(password, algorithm);
				decryptedData = aesDecrypt(data, key);
				return decryptedData;
			case "ARC4":
				key = passwordKey(password, algorithm);
				decryptedData = arc4Decrypt(data, key);
				return decryptedData;
			case "Blowfish":
				key = passwordKey(password, algorithm);
				decryptedData = blowfishDecrypt(data, key);
				return decryptedData;

			}

		} catch (Exception e) {

		}
		return null;
	}
    /**
     * Generate password key using a string password
     * @param password password string
     * @param type algorithm encryption type
     * @return SecretKey to use in encryption
     */
	public SecretKey passwordKey(String password, String type)
			throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
		int iterationCount = 1024;
		int keyLength = 128;
		int saltLen = 8;
		String saltyStr = "salt";
		byte[] salt = new byte[saltLen];
		salt = saltyStr.getBytes("UTF-8");

		// SecureRandom random = new SecureRandom();
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] keyByte = keyFactory.generateSecret(spec).getEncoded();
		SecretKey key = new SecretKeySpec(keyByte, type);
		return key;
	}
    /**
     * Used to decrypt with AES mode
     * @param encryptedByte encrypted byte[]
     * @param key key to use
     * @return
     */
	public byte[] aesDecrypt(byte[] encryptedByte, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException,
			BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, IOException {

		Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		aesCipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bytePlainText = aesCipher.doFinal(encryptedByte);
		return bytePlainText;
	}
    /**
     * Used to decrypt with Blowfish mode
     * @param encryptedByte encrypted byte[]
     * @param key key to use
     * @return
     */
	public byte[] blowfishDecrypt(byte[] encryptedByte, SecretKey key) throws NoSuchAlgorithmException,
			InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bytePlainText = cipher.doFinal(encryptedByte);
		return bytePlainText;
	}
    /**
     * Used to decrypt with ARC4 mode
     * @param encryptedByte encrypted byte[]
     * @param key key to use
     * @return
     */
	public byte[] arc4Decrypt(byte[] encryptedByte, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException,
			BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("RC4");
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bytePlainText = cipher.doFinal(encryptedByte);
		return bytePlainText;
	}
    /**
     * Used to decrypt with RSA mode
     * @param encryptedByte encrypted byte[]
     * @param keybyte key to use
     * @return
     */
	public byte[] rsaDecrypt(byte[] encryptedByte, byte[] keyByte)
			throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyByte);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decrypted = cipher.doFinal(encryptedByte);
		return decrypted;
	}
/**
 * Transform file into byte array
 * @param file to transform
 * @return byte array of file
 */
	public byte[] fileToByte(File file) {
		byte[] data;
		int size = (int) file.length();
		data = new byte[size];
		try {
			BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
			// FileInputStream fileInputStream = new FileInputStream(file);

			try {
				// fileInputStream.read(data);
				buf.read(data);
				buf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return data;
	}
/**
 * Used to save the file
 * @param dataToSave byte array of file to save
 * @param fileName name of the file
 * @param encryptionAlgo encryption algorithm mode
 * @return true if operation was successful
 */
	public boolean saveFile(byte[] dataToSave, String fileName, String encryptionAlgo) {
		String filePath = System.getProperty("user.dir") + "/Decrypted-Files";
		File path = new File(filePath);
		if (!path.exists()) {
			path.mkdirs();
		}
		try {
			File file = null;
			if (fileName.contains(encryptionAlgo)) {
				fileName = fileName.replace(encryptionAlgo + "-", "");
			}
			String decryptedFile = fileName.replace(".encry", "");
			file = new File(path, decryptedFile);

			file.createNewFile();

			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(dataToSave);
			bos.flush();
			bos.close();
			/*
			 * fos.write(dataToSave); fos.flush(); fos.close();
			 */
			System.out.println(file.getAbsolutePath());
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	public byte[] aesEncrypt(SecretKey key, byte[] data)
			throws NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException,
			NoSuchAlgorithmException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] ciphertext = cipher.doFinal(data);
		return ciphertext;
	}
/**
 * Used to test 
 * @param args
 */
	public static void main(String args[]) {
		File file = new File("C:/Users/Alik/Desktop/random/kote.gif");
		Decryption dc = new Decryption();
		byte[] fileByte = dc.fileToByte(file);

		byte[] encrypted;
		try {
			SecretKey key = dc.passwordKey("password", "AES");
			encrypted = dc.aesEncrypt(key, fileByte);
			dc.saveFile(encrypted, "Encrypted" + file.getName(), "AES");
			// File encryptedFile = new
			// File("D:/EclipseWorkSpace/EncryptionLab/Decrypted-Files/" + "Encrypted" +
			// file.getName()) ;
			File encryptedFile = new File("C:/Users/Alik/Desktop/AES-20160723_194433~01.jpg.encry");
			byte[] encryptedFileByte = dc.fileToByte(encryptedFile);
			byte[] decrypted = dc.aesDecrypt(encryptedFileByte, key);
			boolean ass = dc.saveFile(decrypted, encryptedFile.getName(), "AES");
		} catch (Exception ex) {
			Logger.getLogger(Decryption.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
