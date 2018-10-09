/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptionlab;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
* GUI of the program
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */
public class GUI extends JFrame implements Serializable {

	private JButton decryptButton, openFileButton, socketButton, fileDecryptionButton, fileRecieverButton,
			dirPathButton, rsaButton;
	protected JLabel decryptedLabel, selectedFileLabel, encryptedLabel, passwordLabel, portLabel;
	private JTextField encryptedTextField, portTextField;
	private JPasswordField passwordTextField;
	private JPanel mainPanel, buttonPanel, filePanel, networkPanel;
	private JFileChooser fileChooser;
	private JComboBox<String> decryptionBox;
	private static final long serialVersionUID = 123213213;
	private final String[] decryptionOptions = { "AES", "Blowfish", "ARC4", "RSA" };
	protected String chosenFile;
	private Decryption decryptionTools;
	protected boolean networkThreadBool;
	protected FileReciever fileReciever;
/**
 * Constructor to set uo all the GUI elements
 */
	public GUI() {
		decryptionTools = new Decryption();
		setupFrame();
		pack();
		repaint();
	}
/**
 * Uses to build the whole frame
 */
	public void setupFrame() {
		setTitle("EncryptionLab");
		setSize(600, 600);
		setResizable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setLayout(new BorderLayout());
		mainPanel = new JPanel();
		// setContentPane(jpanel);
		add(mainPanel, BorderLayout.CENTER);

		BorderLayout bl = new BorderLayout();
		mainPanel.setLayout(bl);
		decryptedLabel = new JLabel("Decrypted: ");
		mainPanel.add(decryptedLabel, BorderLayout.PAGE_START);

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		mainPanel.add(buttonPanel, BorderLayout.CENTER);

		encryptedLabel = new JLabel("Encrypted Text:");

		encryptedTextField = new JTextField();
		JPanel textFieldPanel = new JPanel();
		textFieldPanel.setLayout(new FlowLayout());
		textFieldPanel.add(encryptedTextField);
		encryptedTextField.setPreferredSize(new Dimension(200, 20));
		decryptButton = new JButton("Decrypt Text");
		decryptionButtonListener();

		decryptionBox = new JComboBox<String>(decryptionOptions);

		filePanel = new JPanel(new FlowLayout());
		passwordTextField = new JPasswordField();
		JPanel passwordFieldPanel = new JPanel();
		passwordFieldPanel.setLayout(new FlowLayout());
		passwordFieldPanel.add(passwordTextField);
		passwordTextField.setPreferredSize(new Dimension(200, 20));
		passwordLabel = new JLabel("Password:");

		selectedFileLabel = new JLabel("Chosen File:");
		openFileButton = new JButton("Open File");
		fileButtonListener();
		fileDecryptionButton = new JButton("Decrypt File");
		fileDecryptionButtonListener();
		dirPathButton = new JButton("Open Folder");
		dirPathButtonListneter();
		rsaButton = new JButton("Load RSA key");

		filePanel.add(passwordLabel);
		filePanel.add(passwordTextField);
		filePanel.add(openFileButton);
		filePanel.add(selectedFileLabel);
		filePanel.add(fileDecryptionButton);
		filePanel.add(dirPathButton);
		filePanel.add(rsaButton);

		networkPanel = new JPanel(new FlowLayout());
		fileRecieverButton = new JButton("Recieve Files");
		fileRecieverButtonListener();
		portLabel = new JLabel("Enter port");
		JPanel portFieldPanel = new JPanel();
		portTextField = new JTextField();
		portTextField.setPreferredSize(new Dimension(100, 20));
		portFieldPanel.setLayout(new FlowLayout());
		portFieldPanel.add(portTextField);

		networkPanel.add(portLabel);
		networkPanel.add(portTextField);
		networkPanel.add(fileRecieverButton);

		mainPanel.add(filePanel, BorderLayout.PAGE_END, 1);
		mainPanel.add(networkPanel, BorderLayout.EAST, 1);

		buttonPanel.add(encryptedLabel);
		buttonPanel.add(textFieldPanel);
		buttonPanel.add(decryptButton);
		buttonPanel.add(decryptionBox);
	}
/**
 * Uses JFileChooser to pick a file
 * @return the chosen file
 */
	public File fileChooser() {
		JPanel fileChooserPanel = new JPanel();
		fileChooser = new JFileChooser();
		// fileChooserPanel.add(fileChooser);
		int returnVal = fileChooser.showOpenDialog(null);
		File pickedFile = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			pickedFile = fileChooser.getSelectedFile();
		}
		return pickedFile;
	}
/**
 * Button listener for open file button
 */
	public void fileButtonListener() {
		openFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = fileChooser();
				chosenFile = file.getAbsolutePath();
				selectedFileLabel.setText("Chosen File: " + file.getName());
				pack();
			}
		});
	}
/**
 * Button listener for decryption
 */
	public void decryptionButtonListener() {
		decryptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (encryptedTextField.getText().isEmpty()
						&& decryptionBox.getSelectedItem().toString().equals("RSA")) {
					String infoMsg = "Please write encrypted Base64 String";
					JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);

				} else if (encryptedTextField.getText().isEmpty() || passwordTextField.getPassword().length < 1 && !decryptionBox.getSelectedItem().toString().equals("RSA")) {
					String infoMsg = "Please write encrypted Base64 String, Password and use AES, ARC4, or Blowfish Decryption";
					JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
				} else if (!encryptedTextField.getText().isEmpty()
						&& decryptionBox.getSelectedItem().toString().equals("RSA")) {
					LoadRSAKey loadRSAKey = new LoadRSAKey();
					byte[] rsaKey = loadRSAKey.getRSAKey();
					String encryptedText = encryptedTextField.getText();
					byte[] encryptedByte = Base64.getDecoder().decode(encryptedText);
					try {
						byte[] decryptedByte = decryptionTools.rsaDecrypt(encryptedByte, rsaKey);
						String decryptedStr = new String(decryptedByte, "UTF-8");
						decryptedLabel.setText("Decrypted: " + decryptedStr);
					} catch (InvalidKeyException | UnrecoverableEntryException | NoSuchAlgorithmException
							| KeyStoreException | NoSuchPaddingException | IllegalBlockSizeException
							| BadPaddingException | InvalidKeySpecException | UnsupportedEncodingException e1) {

						e1.printStackTrace();
					}
				} else {
					String currentDecryptAlgo = decryptionBox.getSelectedItem().toString();
					String password = new String(passwordTextField.getPassword());
					String encryptedText = encryptedTextField.getText();
					byte[] encryptedByte = Base64.getDecoder().decode(encryptedText);
					System.out.println(currentDecryptAlgo + password + encryptedText);
					decryptTextData(currentDecryptAlgo, password, encryptedByte);

				}
			}
		});
	}
/**
 * Button listener for file decryption
 */
	public void fileDecryptionButtonListener() {
		fileDecryptionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chosenFile == null && decryptionBox.getSelectedItem().toString().equals("RSA")) {
					String infoMsg = "Please chose a file";
					JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
				} else if (chosenFile == null || passwordTextField.getPassword().length < 1 && !decryptionBox.getSelectedItem().toString().equals("RSA") ) {
					String infoMsg = "Please chose a file and type the password";
					JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
				} else if (decryptionBox.getSelectedItem().toString().equals("RSA")) {
					LoadRSAKey loadRSAKey = new LoadRSAKey();
					byte[] rsaKey = loadRSAKey.getRSAKey();
					File file = new File(chosenFile);
					String fileName = file.getName();
					byte[] encryptedByte = decryptionTools.fileToByte(file);
					try {
						byte[] decryptedByte = decryptionTools.rsaDecrypt(encryptedByte, rsaKey);
						decryptionTools.saveFile(decryptedByte, fileName, "RSA");
						decryptedLabel.setText("Decrypted: " + fileName);
					} catch (InvalidKeyException | UnrecoverableEntryException | NoSuchAlgorithmException
							| KeyStoreException | NoSuchPaddingException | IllegalBlockSizeException
							| BadPaddingException | InvalidKeySpecException e1) {

					}
				} else {
					String currentDecryptAlgo = decryptionBox.getSelectedItem().toString();
					String password = new String(passwordTextField.getPassword());
					File file = new File(chosenFile);
					String fileName = file.getName();
					decryptedLabel.setText("Decrypted: " + file.getAbsolutePath());
					decryptFileData(currentDecryptAlgo, password, file, fileName);
				}
			}
		});
	}
/**
 * Decrypt text data from inputted string
 * @param algorithm type of algortihm used
 * @param password password used
 * @param data data to decrypt
 */
	public void decryptTextData(String algorithm, String password, byte[] data) {

		byte[] decrypted = decryptionTools.decryptionHandler(algorithm, password, data);
		try {
			if (decrypted != null) {
				String decryptedStr = new String(decrypted, "UTF-8");

				decryptedLabel.setText("Decrypted: " + decryptedStr);
			} else {
				String infoMsg = "Wrong Password";
				JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
/**
 * Used to start a socket that listens for incoming file on chosen port
 */
	public void fileRecieverButtonListener() {
		fileRecieverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!networkThreadBool) {
					if (!portTextField.getText().isEmpty()) {
						String portStr = portTextField.getText();
						try {
							int port = Integer.parseInt(portStr);
							fileRecieverButton.setText("Stop Recieving");
							pack();
							networkThreadBool = true;
							try {
								fileReciever = new FileReciever(port);

								fileReciever.start();

								String infoMsg = "Started Listening for files";
								JOptionPane.showMessageDialog(null, infoMsg, "Information",
										JOptionPane.INFORMATION_MESSAGE);
							} catch (IOException ex) {
								String infoMsg = "Port is in Use";
								JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
							}
						} catch (NumberFormatException ex) {
							String infoMsg = "Please enter only integers for port";
							JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
						}

					} else {
						String infoMsg = "Please enter port";
						JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
					}

				} else if (networkThreadBool) {
					fileReciever.interrupt();
					networkThreadBool = false;
					fileRecieverButton.setText("Recieve Files");
					String infoMsg = "Stopped Listening for files";
					JOptionPane.showMessageDialog(null, infoMsg, "Information", JOptionPane.INFORMATION_MESSAGE);

				}
			}
		});
	}
/**
 * Used to open containing directory of decrypted files
 */
	public void dirPathButtonListneter() {
		dirPathButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String filePath = System.getProperty("user.dir") + "/Decrypted-Files";
				try {
					Desktop.getDesktop().open(new File(filePath));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}
/**
 * Used to decrypt the encrypted files
 * @param algorithm type of algorithm used
 * @param password password used
 * @param file file to decrypt
 * @param fileName name of the file
 */
	public void decryptFileData(String algorithm, String password, File file, String fileName) {
		byte[] data = decryptionTools.fileToByte(file);
		byte[] decryptedFile = decryptionTools.decryptionHandler(algorithm, password, data);
		if (decryptedFile != null) {
			decryptionTools.saveFile(decryptedFile, fileName, algorithm);
		} else {
			String infoMsg = "Wrong Password";
			JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
