package com.example.alik.encryptionapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Socket sender that sends file to target ip address and port
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */

public class SocketSender {

    private int port;
    private String ip;

    /**
     * Sets global parameter for ip and port
     * @param ip target ip address
     * @param port selected port to connect socket
     */
    public void setIPandPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Send the file using thread, since network
     * cannot be run on ui thread
     * @param file Selected file to send
     */

    public void sendFile(final File file) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                sendFile(ip, port, file);
            }
        };
        Thread thread = new Thread(run);
        thread.start();
    }

    /**
     * Send the file using DataOutputStream via OutputStream of the socket
     * @param ip target ip address
     * @param port target port
     * @param file selected file
     */
    public void sendFile(String ip, int port, File file) {
        try {
            Socket sock = new Socket(ip, port);

            byte[] fileByte = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(fileByte, 0, fileByte.length);
            OutputStream os = sock.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(file.getName());
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