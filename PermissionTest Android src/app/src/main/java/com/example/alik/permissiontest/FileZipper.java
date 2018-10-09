package com.example.alik.permissiontest;

import android.os.Environment;
import android.util.Log;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class that zips the collected files so that it can be send
 * @author Alikhan Tagybergen
 * @version 1.0
 */

public class FileZipper {

    private final String ZIP_DIR_FOLDER = "/permission-test-zip/";
    private final String SOURCE_DIR_FOLDER = "/permission-test/";
    private final String ZIP_FILE_NAME = "/allpermissions.zip";
    private final String ZIP_FILE_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS + ZIP_DIR_FOLDER+ZIP_FILE_NAME).getAbsolutePath();
    protected static String ipAddress;
    protected static int port;

    /**
     * Used to write to zip file
     * @param sourceFolder folder to zip
     * @param fileList list of files
     * @param targetFolder target directory
     */
    public  void writeZipFile(File sourceFolder, ArrayList<File> fileList, File targetFolder) {

        try {
            File zipFile = new File(targetFolder+ZIP_FILE_NAME);
            Log.e("Ziped to", zipFile.getAbsolutePath());
            zipFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);


            for (File fileInFolder : fileList) {
                FileInputStream fis = new FileInputStream(fileInFolder);
                String zippedPath = fileInFolder.getCanonicalPath().replace(sourceFolder.getCanonicalPath(), "");
                Log.e("Zipped to", fileInFolder.getName() + "   :" + zippedPath);
                ZipEntry zipEntry = new ZipEntry(zippedPath);
                zos.putNextEntry(zipEntry);

                byte[] bufByte = new byte[1048];
                int count;
                while ((count = fis.read(bufByte)) >= 0) {
                    zos.write(bufByte, 0, count);
                }

                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *Used to get files and zip them. makes directory if target does not exist
     */
    public void zipFolder() {
        File sourcePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS + SOURCE_DIR_FOLDER);
        File targetPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS + ZIP_DIR_FOLDER);
        if(!targetPath.exists()){
            targetPath.mkdirs();
        }
        ArrayList<File> fileList = new ArrayList();
        getAndZipFiles(sourcePath, fileList);
        writeZipFile(sourcePath, fileList, targetPath);
        Log.e("Zipping", "Zipping folder ");
    }

    /**
     * Used to populate the array with files from the selected directory
     * @param sourceFolder source directory
     * @param fileList  of files that will be populated to
     */
    public void getAndZipFiles(File sourceFolder, ArrayList<File> fileList) {
        File[] fileArr = sourceFolder.listFiles();
        for (File file : fileArr) {
            if(file.isDirectory()){
                getAndZipFiles(file, fileList);
            } else {
                fileList.add(file);
            }

        }

    }

    /**
     * Used to define a runnable so that zip files can be send over the socket
     */
    public final Runnable serverRun = new Runnable() {
        @Override
        public void run() {
            File zipFile = new File(ZIP_FILE_PATH);
            SocketSender.sendFile(ipAddress, port, zipFile );
        }
    };

    /**
     * Sets  global varaibles ipAddress and port
     * @param ip adress to use
     * @param socketPort that will be used
     */
    public static void setIPandPort(String ip, int socketPort){
        ipAddress = ip;
        port = socketPort;
    }


}
