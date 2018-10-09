package encryptionlab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.swing.JOptionPane;
/**
* Decryption class that is used to load the RSA key  
 * @author  Alikhan Tagybergen
 * @version 1.0
 * @since   2018-04-20
 */
public class LoadRSAKey {
	
	private final String FILE_NAME = "RSAKey.bin"; 
	/**
	 * Loads RSA key from binary file
	 * @return the byte array of RSA key
	 */
	public byte[] getRSAKey() {
		File file = new File (System.getProperty("user.dir") + File.separator + "recieved"  + File.separator + FILE_NAME); 
		byte[] RSAKey = null; 
		try {
			InputStream is = new FileInputStream(file);
			RSAKey = new byte[(int) file.length()]; 
			is.read(RSAKey); 
			is.close();
			return RSAKey; 
		} catch (FileNotFoundException e) {
			String infoMsg = "Please download RSAkey file first";
			JOptionPane.showMessageDialog(null, infoMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return RSAKey; 
	}
}
