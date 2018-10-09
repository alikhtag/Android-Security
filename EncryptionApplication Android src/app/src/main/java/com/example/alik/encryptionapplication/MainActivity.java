package com.example.alik.encryptionapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.BadPaddingException;
import 	javax.security.cert.X509Certificate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.SocketHandler;

import javax.crypto.SecretKey;

import in.gauriinfotech.commons.Commons;

/**
 * Main activity class of the application
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */
public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;
    private final String STRING_FORMAT = "UTF-8";
    private final String RSA_KEY_STORE = "RSA";
    private EditText plaintext, encrypted, ipAddress, port;
    private EditText passwordButton;
    private TextView fileTextView;
    private Switch decryptSwitch, fileSwitch;
    private boolean decryptMode = false, useFile = false;
    private Encryption encryptionTools = new Encryption();
    private Uri filePathUri;
    private String lastEncryption;
    private File loadedFile;
    private Button rsaButton;
    protected SocketSender socketSender = new SocketSender();

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Run when application is created
     * @param savedInstanceState of android application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plaintext = (EditText) findViewById(R.id.messageText);
        passwordButton = (EditText) findViewById(R.id.passwordTxt);
        encrypted = (EditText) findViewById(R.id.encryptedText);
        decryptSwitch = (Switch) findViewById(R.id.decryption);
        fileSwitch = (Switch) findViewById(R.id.fileSwitch);
        ipAddress = (EditText) findViewById(R.id.ipText);
        port = (EditText) findViewById(R.id.portText);
        fileTextView = (TextView) findViewById(R.id.fileTextView);
        rsaButton = (Button) findViewById(R.id.rsaButton);
        setSwitchListener();
        keyStoreInit(RSA_KEY_STORE);

    }

    /**
     * Used to get file via intent
     * @param requestCode that corresponds to selecting a file
     * @param resultCode that corresponds to selecting a file
     * @param data chosen fia via intent
     */
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            filePathUri = data.getData();
            Toast.makeText(getApplicationContext(), filePathUri.toString(), Toast.LENGTH_SHORT).show();
            Log.e("path", filePathUri.getPath());
            String filename = loadFile(filePathUri);
            fileTextView.setText(filename);
        }
    }

    /**
     * Sets listeners for two switches
     */
    public void setSwitchListener() {
        if (decryptSwitch != null) {
            decryptSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean switchValue) {
                    if (switchValue) {
                        decryptMode = true;
                        plaintext.setText("");
                    } else if (!switchValue) {
                        decryptMode = false;
                    }
                }
            });
        }
        if (fileSwitch != null) {
            fileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean switchValue) {
                    if (switchValue) {
                        useFile = true;
                        rsaButton.setVisibility(View.GONE);
                    } else if (!switchValue) {
                        useFile = false;
                        rsaButton.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    /**
     * AES button operation that corresponds to the algorithm
     * @param view of the button
     */
    public void aesButton(View view) {
        try {
            Editable pwEditable = passwordButton.getText();
            String passwordString = pwEditable.toString();
            SecretKey key = encryptionTools.passwordKey(passwordString, "AES");

            if (!decryptMode) {
                if (useFile) {
                    encryptAndSaveFile(loadedFile, key, "AES");
                } else {
                    String plainString = plaintext.getText().toString();
                    byte[] plainByte = plainString.getBytes(STRING_FORMAT);
                    byte[] encryptedByte = encryptionTools.aesEncrypt(key, plainByte);
                    String base64Encrypted = Base64.encodeToString(encryptedByte, Base64.DEFAULT);
                    lastEncryption = "AES";
                    encrypted.setText(base64Encrypted);
                }
            } else if (decryptMode) {
                if (useFile) {
                    decryptAndSaveFile(loadedFile, key, "AES");
                } else {
                    String base64Encrypted = encrypted.getText().toString();
                    byte[] encryptedByte = Base64.decode(base64Encrypted, Base64.DEFAULT);
                    String decrypted = new String(encryptionTools.aesDecrypt(encryptedByte, key), STRING_FORMAT);
                    plaintext.setText(decrypted);
                }
            }
        }catch(BadPaddingException ex){
            Toast.makeText(getApplicationContext(),"Wrong Password", Toast.LENGTH_SHORT).show();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Blowfish button operation that corresponds to the algorithm
     * @param view of the button
     */
    public void blowfishButton(View view) {
        try {
            Editable pwEditable = passwordButton.getText();
            String passwordString = pwEditable.toString();
            SecretKey key = encryptionTools.passwordKey(passwordString, "Blowfish");

            if (!decryptMode) {
                if (useFile) {
                    encryptAndSaveFile(loadedFile, key, "Blowfish");
                } else {
                    String plainString = plaintext.getText().toString();
                    byte[] plainByte = plainString.getBytes(STRING_FORMAT);
                    byte[] encryptedByte = encryptionTools.blowfishEncrypt(plainByte, key);
                    String base64Encrypted = Base64.encodeToString(encryptedByte, Base64.DEFAULT);
                    lastEncryption = "Blowfish";
                    encrypted.setText(base64Encrypted);
                }
            } else if (decryptMode) {
                if (useFile) {
                    decryptAndSaveFile(loadedFile, key, "Blowfish");
                } else {
                    String base64Encrypted = encrypted.getText().toString();
                    byte[] encryptedByte = Base64.decode(base64Encrypted, Base64.DEFAULT);
                    String decrypted = new String(encryptionTools.blowfishDecrypt(encryptedByte, key), STRING_FORMAT);
                    plaintext.setText(decrypted);
                }
            }
        }catch(BadPaddingException ex){
            Toast.makeText(getApplicationContext(),"Wrong Password", Toast.LENGTH_SHORT).show();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Arc4 (rc4) button operation that corresponds to the algorithm
     * @param view of the button
     */
    public void arc4Button(View view) {
        try {
            Editable pwEditable = passwordButton.getText();
            String passwordString = pwEditable.toString();
            SecretKey key = encryptionTools.passwordKey(passwordString, "RC4");

            if (!decryptMode) {
                if (useFile) {
                    encryptAndSaveFile(loadedFile, key, "ARC4");
                } else {
                    String plainString = plaintext.getText().toString();
                    byte[] plainByte = plainString.getBytes(STRING_FORMAT);
                    byte[] encryptedByte = encryptionTools.arc4Encrypt(plainByte, key);
                    String base64Encrypted = Base64.encodeToString(encryptedByte, Base64.DEFAULT);
                    lastEncryption = "ARC4";
                    encrypted.setText(base64Encrypted);
                }
            } else if (decryptMode) {
                if (useFile) {
                    decryptAndSaveFile(loadedFile, key, "ARC4");
                } else {
                    String base64Encrypted = encrypted.getText().toString();
                    byte[] encryptedByte = Base64.decode(base64Encrypted, Base64.DEFAULT);
                    String decrypted = new String(encryptionTools.arc4Decrypt(encryptedByte, key), STRING_FORMAT);
                    plaintext.setText(decrypted);
                }
            }
        } catch(BadPaddingException ex){
            Toast.makeText(getApplicationContext(),"Wrong Password", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * RSA button operation that corresponds to the algorithm
     * @param view of the button
     */
    public void rsaButton(View view) {
        try {

            if (!decryptMode) {
                if (useFile) {
                    Toast.makeText(getApplicationContext(),"RSA is not Designed for Files", Toast.LENGTH_SHORT).show();
                } else {
                    String plainString = plaintext.getText().toString();
                    byte[] plainByte = plainString.getBytes(STRING_FORMAT);
                    byte[] encryptedByte = encryptionTools.rsaEncrypt(sharedPreferences, plainByte);
                    String base64Encrypted = Base64.encodeToString(encryptedByte, Base64.DEFAULT);
                    lastEncryption = "RSA";
                    encrypted.setText(base64Encrypted);
                }

            } else if (decryptMode) {
                if (useFile) {
                    Toast.makeText(getApplicationContext(),"RSA is not Designed for Files", Toast.LENGTH_SHORT).show();
                } else {
                    String base64Encrypted = encrypted.getText().toString();
                    byte[] encryptedByte = Base64.decode(base64Encrypted, Base64.DEFAULT);
                    String decrypted = new String(encryptionTools.rsaDecrypt(sharedPreferences, encryptedByte), STRING_FORMAT);
                    plaintext.setText(decrypted);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize and generate RSA key pair.
     * Store it in Shared Prefernces
     * @param type of algorithm
     */
    public void keyStoreInit(String type) {
        try {
            /*
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (!keyStore.containsAlias(type) && type.equals(RSA_KEY_STORE)) { */
            sharedPreferences = getSharedPreferences("RSAKeyPair", MODE_PRIVATE);
            if(!sharedPreferences.contains("PublicKey") && !sharedPreferences.contains("PrivateKey")) {
                Log.e("gen RSA", "gen rsa key");
                KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
                /*KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(type,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS8)
                        .setKeySize(512)
                        .build(); */
                kpg.initialize(512, new SecureRandom());
                KeyPair keyPair = kpg.generateKeyPair();
                PrivateKey privateKey = keyPair.getPrivate();
                PublicKey publicKey = keyPair.getPublic();

                String base64PrivateKey = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
                String base64PublicKey = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);

                spEditor = sharedPreferences.edit();
                spEditor.putString("PublicKey", base64PublicKey );
                spEditor.putString("PrivateKey", base64PrivateKey);
                spEditor.commit();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Button to send intent to get the file
     * @param view of the button
     */
    public void loadFileButton(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent = Intent.createChooser(intent, "Pick a File");
                startActivityForResult(intent, 1001);
            }
        }
    }

    /**
     * Button that saves encrypted string to text file
     * @param view
     */
    public void saveTextFile(View view) {
        if (!decryptMode) {
            String data = encrypted.getText().toString();
            Tools.writeEncryptedText(data, lastEncryption);
        } else {
            Toast.makeText(getApplicationContext(), "Not currently in encryption mode", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads file into global file variable and returns string to be used
     * @param path Uri path to the file
     * @return String of filename
     */
    private String loadFile(Uri path) {
        String filePath = "";
        if (path.getPath().contains("storage")) {
            filePath = path.getPath();
        } else {
            filePath = Commons.getPath(path, getApplicationContext());
        }
        loadedFile = new File(filePath);
        String filename = loadedFile.getName();
        return filename;
        //File destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CustomFolder/" + filename);
    }

    /**
     * Ecrypt and save file with appropriate algorithm
     * @param file inputted file
     * @param key secret key
     * @param encryptionAlgo encryption algorithm mode
     * @return true if was able to encrypt and save
     */
    private boolean encryptAndSaveFile(File file, SecretKey key, String encryptionAlgo) {
        Log.e("filename", file.getPath());
        byte[] fileData = fileToByte(file);
        boolean result = false;
        byte[] encryptedFile = null;
        if (useFile && !decryptMode && file != null) {
            try {
                if (encryptionAlgo.equals("AES")) {
                    encryptedFile = encryptionTools.aesEncrypt(key, fileData);
                } else if (encryptionAlgo.equals("Blowfish")) {
                    encryptedFile = encryptionTools.blowfishEncrypt(fileData, key);
                } else if (encryptionAlgo.equals("ARC4")) {
                    encryptedFile = encryptionTools.arc4Encrypt(fileData, key);
                }
                result = Tools.saveFile(encryptedFile, file.getName(), encryptionAlgo, decryptMode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Used to decrypt and save file
     * @param file inputted file
     * @param key secret key
     * @param encryptionAlgo chosen algorithm mode
     * @return true if was able to decrypt and save
     */
    private boolean decryptAndSaveFile(File file, SecretKey key, String encryptionAlgo) {
        Log.e("filename", file.getPath());
        byte[] fileData = fileToByte(file);
        boolean result = false;

        byte[] decryptedFileByte = null;
        if (useFile && decryptMode && file != null) {
            try {
                if (encryptionAlgo.equals("AES")) {
                    decryptedFileByte = encryptionTools.aesDecrypt(fileData, key);
                } else if (encryptionAlgo.equals("Blowfish")) {
                    decryptedFileByte = encryptionTools.blowfishDecrypt(fileData, key);
                } else if (encryptionAlgo.equals("ARC4")) {
                    decryptedFileByte = encryptionTools.arc4Decrypt(fileData, key);
                } else if (encryptionAlgo.equals("RSA")) {
                    decryptedFileByte = encryptionTools.rsaDecrypt(sharedPreferences, fileData);
                }
                result = Tools.saveFile(decryptedFileByte, file.getName(), encryptionAlgo, decryptMode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Button to set ip address and port
     * @param view of the button
     */
    public void ipAndPortButton(View view){
        String ipAdressStr = ipAddress.getText().toString();
        try {
            int portInt = Integer.parseInt(port.getText().toString());
            socketSender.setIPandPort(ipAdressStr, portInt);
        } catch (NumberFormatException ex) {
            Toast.makeText(getApplicationContext(), "Please enter integers for port", Toast.LENGTH_SHORT ).show();
        }
    }

    /**
     * Button to send selected file via socket
     * @param view of the button
     */
    public void sendFileButton(View view){
        if(loadedFile != null && loadedFile.getName().contains(".encry")){
            try {
                socketSender.sendFile(loadedFile);
            } catch (NullPointerException e){
                Toast.makeText(getApplicationContext(), "Please enter ip and port first", Toast.LENGTH_SHORT ).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please pick .encry file in order to send it", Toast.LENGTH_SHORT ).show();
        }
    }

    /**
     * Export RSA private key using socket to reciever
     * @param  view of the button
     */
    public void exportRSA(View view){
        try {
            String rsaKeyBase64 = sharedPreferences.getString("PrivateKey", "");
            byte[] rsaKeyByte = Base64.decode(rsaKeyBase64, Base64.DEFAULT);
            Log.e("RSAKEY", rsaKeyBase64);
            Tools.saveRSAKey(rsaKeyByte);
            File rsaKeyFile = Tools.returnRSAKeyFile(getApplicationContext());
            if(rsaKeyFile!= null) {
                socketSender.sendFile(rsaKeyFile);
            } else {
                Toast.makeText(getApplicationContext(), "File was not found", Toast.LENGTH_SHORT ).show();
            }
        } catch (NullPointerException e){
            Toast.makeText(getApplicationContext(), "Please enter ip and port first", Toast.LENGTH_SHORT ).show();
            }


    }

    /**
     * Transform file to byte array
     * @param file file to transform
     * @ return byte array of the file
     */
    private byte[] fileToByte(File file) {
        byte[] data;
        int size = (int) file.length();
        data = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(
                    new FileInputStream(file));
            try {
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

}
