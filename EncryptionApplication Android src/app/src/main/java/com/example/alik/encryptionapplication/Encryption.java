package com.example.alik.encryptionapplication;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption class that is able to encryp and decrypt inputted data
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */
public class Encryption {
    final String STRING_FORMAT = "UTF-8";

    /**
     * Generate password key using a string password
     * @param password passwor dstring
     * @param type algorithm encryption type
     * @return SecretKey to use in encryption
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws UnsupportedEncodingException
     */
    public SecretKey passwordKey(String password, String type) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException {
        int iterationCount = 1024;
        int keyLength = 128;
        int saltLen  = 8;
        String saltyStr = "salt";
        byte[] salt = new byte[saltLen];
        salt = saltyStr.getBytes(STRING_FORMAT);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt
                , iterationCount, keyLength);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] keyByte = keyFactory.generateSecret(spec).getEncoded();
        SecretKey key = new SecretKeySpec(keyByte, type);

        Log.e("Key", Base64.encodeToString(keyByte ,  Base64.DEFAULT));
        Log.e("Key", type);
        Log.e("Key", Base64.encodeToString(key.getEncoded() ,  Base64.DEFAULT));
        return key;
    }

    /**
     * Used to encrypt with AES mode
     * @param key that is used to encrypt
     * @param plainByte byte array to encrypt
     * @return byte array of encrypted data
     */
    public byte[] aesEncrypt(SecretKey key, byte[] plainByte) throws NoSuchPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ciphertext = cipher.doFinal(plainByte);
        return ciphertext;
    }

    /**
     * Used to decrypt with AES mode
     * @param encryptedByte encrypted byte[]
     * @param key key to use
     * @return
     */
    public byte[] aesDecrypt(byte[] encryptedByte, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            NoSuchPaddingException, IllegalBlockSizeException {
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bytePlainText = aesCipher.doFinal(encryptedByte);
        Log.e("Decrypting", "aesDecrypt");
        return bytePlainText;
    }
    /**
     * Used to encrypt with Blowfish mode
     * @param key that is used to encrypt
     * @param plainByte byte array to encrypt
     * @return byte array of encrypted data
     */
    public byte[] blowfishEncrypt(byte[] plainByte, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            NoSuchPaddingException, IllegalBlockSizeException {
            Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainByte);
    }
    /**
     * Used to decrypt with Blowfish mode
     * @param encryptedByte encrypted byte[]
     * @param key key to use
     * @return
     */
    public byte[] blowfishDecrypt(byte[] encryptedByte, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            NoSuchPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bytePlainText = cipher.doFinal(encryptedByte);
        return bytePlainText;
    }
    /**
     * Used to encrypt with arc4 mode
     * @param key that is used to encrypt
     * @param plainByte byte array to encrypt
     * @return byte array of encrypted data
     */
    public byte[] arc4Encrypt(byte[] plainByte, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            NoSuchPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RC4/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plainByte);
    }
    /**
     * Used to decrypt with arc4 mode
     * @param encryptedByte encrypted byte[]
     * @param key key to use
     * @return
     */
    public  byte[] arc4Decrypt(byte[] encryptedByte, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            NoSuchPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RC4/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] bytePlainText = cipher.doFinal(encryptedByte);
        return bytePlainText;
    }

    /**
     * Uses public key to encrypt in RSA mode
     * @param sp shared preferences where key pair is stored
     * @param plainByte plain byte that is encrypted
     * @return encrypted byte array
     */
    public byte[] rsaEncrypt(SharedPreferences sp, byte[] plainByte) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException  {
        byte[] keyByte = Base64.decode(sp.getString("PublicKey", ""), Base64.DEFAULT);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyByte);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plainByte);
        return encrypted;
    }

    /**
     * Uses private key to decrypt RSA mode
     * @param sp shared preferences where key pair is stored
     * @param encryptedByte encrypted data
     * @return decrypted byte array
     */
    public  byte[] rsaDecrypt(SharedPreferences sp, byte[] encryptedByte) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        //KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("RSA", null);
//        RSAPrivateKey privateKey =  (RSAPrivateKey) privateKeyEntry.getPrivateKey();
        byte[] keyByte = Base64.decode(sp.getString("PrivateKey", ""), Base64.DEFAULT);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyByte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(encryptedByte);
        return  decrypted;
    }
}