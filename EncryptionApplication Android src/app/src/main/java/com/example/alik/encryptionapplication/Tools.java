package com.example.alik.encryptionapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.interfaces.RSAKey;

/**
 * Tools that are used to aid main activity
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */
public class Tools {
    private static final String DIR_FOLDER = "/encryption_testing/";
    private static final String RSA_KEY_FOLDER ="/RSAKeyFolder/";
    private static final String RSA_FILE_NAME = "RSAKey.bin";

    /**
     * Write encrypted text to file
     * @param data string to write
     * @param encryptionType the type of encryption used
     */
    public static void writeEncryptedText(String data, String encryptionType) {
        String fileName;

        if (MainActivity.isExternalStorageWritable()) {
            File path = null;
            if(encryptionType.equals("RSAKey")){
                fileName = encryptionType + ".txt";
                path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER + RSA_KEY_FOLDER );
            } else {
                fileName = encryptionType + ".txt";

                 path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER + "/Text");
            }
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, fileName);

            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(data);
                myOutWriter.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        } else {
            Log.e("Error", "Cant write to external");
        }
    }

    /**
     * Used to save file
     * @param dataToSave byte array
     * @param fileName name the file to be saved
     * @param encryptionAlgo used encryption algorithm
     * @param decryptMode encryption or decryption mode
     * @return
     */
    public static boolean saveFile(byte[] dataToSave, String fileName, String encryptionAlgo, boolean decryptMode) {
        if (MainActivity.isExternalStorageWritable()) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER + "/Encrypted-Files");
            if (!path.exists()) {
                path.mkdirs();
            }
            try {
                File file = null;
                if(decryptMode){
                    String decryptedFile = fileName.replace(".encry", "");
                    file = new File(path, decryptedFile);
                } else {
                    if(fileName.contains(encryptionAlgo)) {
                        fileName = fileName.replace(encryptionAlgo, "");
                    }
                    file = new File(path, encryptionAlgo + "-" + fileName + ".encry");
                    //file = new File(path, encryptionAlgo + "-" + fileName);
                }
                Log.e("FilePath", file.getAbsolutePath());
                file.createNewFile();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(dataToSave);
                bos.flush();
                bos.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Log.e("Error", "Cant write to external");
            return false;
        }
    }

    /**
     * Save RSA key to Base64 text file
     * @param data RSA key data
     */
    public static void saveRSAKey(byte[] data) {

        if (MainActivity.isExternalStorageWritable()) {
            File path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER + RSA_KEY_FOLDER);
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, RSA_FILE_NAME);

            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fOut);
                bos.write(data);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        } else {
            Log.e("Error", "Cant write to external");
        }
    }

    /**
     * Get the RSA key file to be send later
     * @param context of the application
     * @return the rsa key file
     */
    public static File returnRSAKeyFile(Context context){
        try{
            File file =  new File (Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER + RSA_KEY_FOLDER + RSA_FILE_NAME).getAbsolutePath());
            return file;
        } catch (Exception e){
            Toast.makeText(context, "Please save RSAKey first", Toast.LENGTH_LONG).show();
        }
        return null;
    }
}


