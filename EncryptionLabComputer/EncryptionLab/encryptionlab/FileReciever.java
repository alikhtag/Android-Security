/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptionlab;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

/**
* Used to receive files by using the socket 
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */
public class FileReciever extends Thread {

	private ServerSocket serverSocket;
	private volatile boolean serverRunning = true;
/**
 * Constructor that sets up the server socket
 * @param port selected port to open socket on
 * @throws IOException
 */
	public FileReciever(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		//System.out.println(System.getProperty("user.dir") + "/recieved");
	}

	private void stopRunning() {
		serverRunning = false;
	}

	@Override
	public void interrupt() {
		stopRunning();
		try {
			serverSocket.close();
		} catch (IOException e) { // TODO Auto-generated
			e.printStackTrace();
		}
		super.interrupt();
	}
/**
 * Main run component of the thread, runs until it is stopped
 */
	public void run() {
		setServerState(true); 
		while (serverRunning) {
			try {
				Socket sock = serverSocket.accept();
				System.out.println(System.getProperty("user.dir") + "\recieved");
				recieveFile(sock);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
/**
 * Used to receive and save the file 
 * @param sock socket that is being usedn
 */
	private void recieveFile(Socket sock) throws IOException, BindException {
		int bytesRead;
		int current = 0;

		InputStream in = sock.getInputStream();
		DataInputStream clientData = new DataInputStream(in);
		String fileName = clientData.readUTF();
		File inPath = new File(System.getProperty("user.dir") + File.separator + "recieved"); 
		if(!inPath.exists()) {
			inPath.mkdirs(); 
		}
		File outFile = new File (inPath + File.separator + fileName);
		outFile.createNewFile(); 
		OutputStream output = new FileOutputStream(outFile);
		int fileSize = clientData.readInt();
		byte[] buffer = new byte[2048];
		while (fileSize > 0
				&& (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
			output.write(buffer, 0, bytesRead);
			fileSize -= bytesRead;
		}
		in.close();
		clientData.close();
		output.close();

	}
/**
 * Used to set the state of loop in the run () method
 * @param serverState what state to set
 */
	public void setServerState(boolean serverState) {
		serverRunning = serverState;
	}
/**
 * Check if port is used, returns error if port is used
 * @param portNumber
 * @return result if port is used. 
 */
	private boolean portUsed(int portNumber) {
		boolean result = true;

		try {

			ServerSocket s = new ServerSocket(portNumber);
			s.close();
		} catch (Exception e) {
			result = false;
			String infoMsg = "Port is in Use";
			JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
		}

		return (result);
	}

}
