package com.example.alik.permissiontest;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String DIR_FOLDER = "/permission-test/";
    private String ipAdress;
    private static  int port;
    private LocationManager locManager;
    private Tools tools = new Tools();
    //listens to location updates and opens settings if location is disabled
    private final LocationListener locListen = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            tools.writeLocation(location.getLongitude(), location.getLatitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    };
    private PermissionService perService;
    private Intent serviceIntent;
    private boolean serviceConnected = false;
    private ArrayList<Contact> contactList;
    //used to connect to a service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PermissionService.ServiceBinder binder = (PermissionService.ServiceBinder) service;
            perService = binder.getBinder();
            //perService.runCamera();
            Log.e("Service", "service running");
            serviceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("Service", "service disc");
            serviceConnected = false;
            Toast.makeText(getApplicationContext(), "Stopping Service", Toast.LENGTH_SHORT).show();
        }
    };

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Run when applicaiton is created
     * @param savedInstanceState saved instance of application
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        permissionCheck();
        perService = new PermissionService();
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        super.onCreate(savedInstanceState);
    }

    /**
     * Used to check write file permission
     */
    public void permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    /**
     * Used to get location. Asks permission first if it was not granted
     */
    public void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListen);

            Location location = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location != null) {
                tools.writeLocation(location.getLongitude(), location.getLatitude());
                String loca = "Longtitute = " + location.getLongitude() + " Latitute = " + location.getLatitude();
                Toast.makeText(getApplicationContext(), "Location has been read", Toast.LENGTH_SHORT).show();
                Log.e("loca", loca);
            }
        }

    }

    /**
     * Used to trigger getLocation method when button is pressed
     * @param view of the button
     */
    public void locButton(View view) {

        getLocation();
    }

    /**
     * Used when contact button is pressed
     * Asks for contact permissions if they were not granted earlier
     * @param view of the button
     */
    public void contactButton(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
            Toast.makeText(getApplicationContext(), "Permission Asked Press Again", Toast.LENGTH_SHORT).show();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

            contactList = getContacts();
            if (contactList != null) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        tools.writeContacts(contactList);
                    }
                });
                Toast.makeText(getApplicationContext(), "Contact data has been read", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "No Contacts Were Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Used to get all of the contact list using content provided data into an ArrayList
     * @return populated ArrayList
     */
    public ArrayList getContacts() {

        ArrayList<Contact> contactList = new ArrayList<>();

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor.moveToFirst()) {
            final int NAME_INDEX = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            final int ID_INDEX = cursor.getColumnIndex(ContactsContract.Contacts._ID);

            while (cursor.moveToNext()) {
                String ID = cursor.getString(ID_INDEX);
                Contact person = new Contact();
                person.setName(cursor.getString(NAME_INDEX));
                Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + ID, null, null);
                while (phoneCursor.moveToNext()) {
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    if (type == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                        person.setHomeNumber(number);
                    } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                        person.setMobileNumber(number);
                    } else if (type == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                        person.setWorkNumber(number);
                    } else {
                        person.setNumber(number);
                    }
                }

                phoneCursor.close();
                Cursor emailCursor = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + ID, null, null);

                while (emailCursor.moveToNext()) {
                    person.setEmail(emailCursor.getString((emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))));
                }

                contactList.add(person);

                emailCursor.close();
            }

        }
        cursor.close();
        return contactList;
    }

    /**
     * Invoked when sms button is pressed
     * Checks permission if it was not granted earlier
     * @param view of the button
     */
    public void readSMS(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
            Toast.makeText(getApplicationContext(), "Permission Asked Press Again", Toast.LENGTH_SHORT).show();

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    getSMSData();
                }
            });
            Toast.makeText(getApplicationContext(), "SMS data has been read", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Used to get the sms data from the content provider
     */
    public void getSMSData() {
        ContentResolver resolver = getContentResolver();
        String[] projection = {"_id", "thread_id", "address", "person", "date", "body"};
        Cursor cursor = resolver.query(Uri.parse("content://sms/"), projection, null, null, null);
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                String message = "";
                String address = "";
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (i == 2) {
                        address = cursor.getString(i);
                    }
                    message += " " + cursor.getColumnName(i) + ":" + cursor.getString(i);
                }
                message += "\n";
                tools.writeSMS(message, address);
            }
        }
        cursor.close();

    }

    /**
     * Sets IP and Port when the button was pressed
     * @param view of the button
     */
    public void getIP(View view){
        EditText ipTextField = findViewById(R.id.ipTextInput);
        EditText portTextField = findViewById(R.id.portNumText);

        if(!ipTextField.getText().toString().toLowerCase().equals("enter ip address")){
            try {
                port = Integer.parseInt(portTextField.getText().toString());
                ipAdress = ipTextField.getText().toString();
                FileZipper.setIPandPort(ipAdress,port);

            } catch(NumberFormatException e){
                Toast.makeText(getApplicationContext(), "Please enter port correctly (integers)", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * invoked when calendar button is pressed
     * It checks for permissions
     * @param view
     */
    public void readCalendar(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1);
            Toast.makeText(getApplicationContext(), "Permission Asked Press Again", Toast.LENGTH_SHORT).show();

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    getCalendarData();
                }
            });
            Toast.makeText(getApplicationContext(), "Calendar Data has been read", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Used to get calendar data using content provider
     */
    public void getCalendarData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            String[] projection = new String[]{
                    CalendarContract.Calendars._ID,                           // 0
                    CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                    CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
            };
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null);
            //Uri.parse("content://com.android.calendar/calendars")
            if (cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    String calendar = "";
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        calendar = " " + cursor.getColumnName(i) + ":" + cursor.getString(i);
                    }
                    calendar += "\n";
                    tools.writeCalendar(calendar);
                }
            }

            cursor.close();
        }


    }

    /**
     * Starts the service when the button is pressed
     * @param view
     */
    public void serviceStart(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //if ( ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            if (serviceConnected != true) {

                serviceIntent = new Intent(this, PermissionService.class);
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                startService(serviceIntent);
                Toast.makeText(getApplicationContext(), "Starting Service", Toast.LENGTH_SHORT).show();
                Log.e("Service", "starting service");
            } else if (serviceConnected == true) {
                //perService.runCamera();
                Log.e("Service", "run camera");
            }
            //}

        }
    }

    /**
     * Used to stop the service
     * @param view of the button
     */
    public void stopService(View view) {
        stopService(serviceIntent);
    }

    /**
     * Called when application is destoryed
     */
    @Override
    protected void onDestroy() {
        serviceConnected = false;
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
        perService = null;
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
