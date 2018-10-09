package com.example.alik.permissiontest;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Class that zips the collected files so that it can be send
 * @author Alikhan Tagybergen
 * @version 1.0
 */

public class SocketSender {
    /**
     * Used to send file over the socket
     * @param ip target IP adress
     * @param port target port
     * @param zipFile the file that will be sent
     */
    public static void sendFile(String ip, int port, File zipFile) {
        try {
            Socket sock = new Socket(ip, port);

            byte[] fileByte = new byte[(int) zipFile.length()];
            Log.e("socket", "sending file");

            FileInputStream fis = new FileInputStream(zipFile);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(fileByte, 0, fileByte.length);

            OutputStream os = sock.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(zipFile.getName());
            dos.writeInt(fileByte.length);
            dos.write(fileByte, 0, fileByte.length);
            dos.flush();

            os.close();
            dos.close();
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
