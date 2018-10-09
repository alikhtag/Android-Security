package com.example.alik.permissiontest;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Class cointaining tools that aid whole application
 * @author Alikhan Tagybergen
 * @version 1.0
 */

public class Tools {
    private final String DIR_FOLDER = "/permission-test/";

    /**
     * Used to write collected contacts to a  file
     * @param contactList list of contacts in ArrayList
     */
    public void writeContacts(ArrayList <Contact> contactList){
        String fileName = "Contacts.txt";
        if(MainActivity.isExternalStorageWritable() == true) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER);
            if (!path.exists()) {
                path.mkdirs();
            }
            Log.e("location", path.toString());
            File file = new File(path, fileName);

            try {
                if(file.exists()){
                    file.delete();
                }
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                for(int i = 0; i < contactList.size(); i++){
                    String contact = "Name: " + contactList.get(i).getName() + "\n";
                    if ( contactList.get(i).getHomeNumber() != null){
                        contact += "Home Number: " + contactList.get(i).getHomeNumber() + "\n";
                    }
                    if ( contactList.get(i).getMobileNumber() != null){
                        contact += "Mobile Number: " + contactList.get(i).getMobileNumber() + "\n";
                    }
                    if ( contactList.get(i).getWorkNumber() != null){
                        contact += "Work Number: " + contactList.get(i).getWorkNumber() + "\n";
                    }
                    if ( contactList.get(i).getEmail() != null){
                        contact += "Email: " + contactList.get(i).getEmail() +  "\n";
                    }
                    contact += "Contact: " + i + "\n";
                    myOutWriter.write(contact);
                }
                myOutWriter.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        } else {
            Log.e("Error" ,"Cant write to external");
        }
    }

    /**
     * Used to write location to a output file
     * @param longtitute
     * @param latitute
     */
    public void writeLocation(double longtitute, double latitute){
        String fileName = "Location.txt";
        String location = " Latitute = " + latitute + "Longtitute = " + longtitute ;
        if(MainActivity.isExternalStorageWritable() == true) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER);
            if (!path.exists()) {
                path.mkdirs();
            }
            Log.e("location", path.toString());
            File file = new File(path, fileName);

            try {
                file.createNewFile();
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(location);
                myOutWriter.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        } else {
            Log.e("Error" ,"Cant write to external");
        }
    }

    /**
     * Used to write collected SMS data to file that has a file name of sms recipient contact
     * @param message message to save
     * @param address address or contact number to use as a file
     */
    public void writeSMS(String message, String address){

        String fileName = address + ".txt";
        if(MainActivity.isExternalStorageWritable() == true) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER + "/sms/");
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, fileName);
            try {
                if(!file.exists()){
                    file.createNewFile();
                }
                FileOutputStream fOut = new FileOutputStream(file, true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(message);
                myOutWriter.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        } else {
            Log.e("Error" ,"Cant write to external");
        }
    }

    /**
     * Used to write calendar data to file.
     * @param calendar string to write in calendar file.
     */
    public void writeCalendar(String calendar){

        String fileName = "calendar.txt";
        if(MainActivity.isExternalStorageWritable() == true) {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER);
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, fileName);
            try {
                if(!file.exists()){
                    file.createNewFile();
                }
                FileOutputStream fOut = new FileOutputStream(file, true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(calendar);
                myOutWriter.close();
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        } else {
            Log.e("Error" ,"Cant write to external");
        }
    }


}
