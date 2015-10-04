package de.visi0nary.motogmultitaskingfixer;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ApplyPermissionsService extends Service {

    private Process suProcess;
    private DataOutputStream os;

    public ApplyPermissionsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        try {
            // start an SU process
            this.suProcess = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(suProcess.getOutputStream());
            // check for root
            if (deviceIsRooted()) {
                alterPermissions();

                // get settings storage reference
                final SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
                // get current setting
                boolean applyMinfrees = settings.getBoolean(getResources().getString(R.string.text_minfree_values), false);
                if (applyMinfrees) {
                    // if switch is checked, apply reasonable minfree values
                    setReasonableMinfrees();
                }

                if (checkIfPermissionsAreSetCorrect()) {
                    Toast.makeText(getApplicationContext(), "Everything went fine. Enjoy multitasking! :)", Toast.LENGTH_SHORT).show();
                } else {
                    // should never happen, but just in case... :)
                    Toast.makeText(getApplicationContext(), "Mhm... Something went wrong. The permissions can't be altered even though we are rooted.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please allow root access!", Toast.LENGTH_LONG).show();
            }
            // if an exception is thrown, the process is destroyed
            this.suProcess.destroy();
        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Your device is not rooted. :(", Toast.LENGTH_LONG).show();
        }
        stopSelf();
    }
    // set reasonable minfrees according to RAM size
    public void setReasonableMinfrees() {
        // 2048,3072,4096,28342,31041,33740
        // get devices' total RAM
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long totalRAM = memoryInfo.totalMem;
        if(totalRAM<1200000000) {
            // we have a device with less than 1GB RAM (Moto G, Razr HD, ...)
            try {
                this.os.writeBytes("echo '2048,3072,4096,28342,31041,33740' > /sys/module/lowmemorykiller/parameters/minfree\n");
                this.os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            // we have a device with less than 2GB RAM (Moto X)
            try {
                this.os.writeBytes("echo '2048,3072,4096,69100,77738,86375' > /sys/module/lowmemorykiller/parameters/minfree\n");
                this.os.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // returns true if device is rooted and false if not
    private boolean deviceIsRooted() {
        int returnValue = -1;
        // create temporary SU process
        Process testSU = null;
        try {
            // try to create a root process
            testSU = Runtime.getRuntime().exec("su");
            DataOutputStream testingStream = new DataOutputStream(testSU.getOutputStream());
            // exit the process again
            testingStream.writeBytes("exit\n");
            testingStream.flush();
            try {
                // wait for the SU process to terminate (if it exists)
                testSU.waitFor();
                // check for the exit value (0 if it was a root process, 255 if not)
                returnValue = testSU.exitValue();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            // clean up
            if(testSU!=null) {
                testSU.destroy();
            }
        }
        return(returnValue==0);
    }

    // this method is called after applying the values. It returns true if the values were applied correctly, false otherwise
    public boolean checkIfPermissionsAreSetCorrect() {
        boolean adjPermissionSetRight = false;
        boolean minfreePermissionSetRight = false;
        try {
            DataInputStream is = new DataInputStream(suProcess.getInputStream());

            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            // check if both values are applied correctly by simply comparing the output of 'ls -al' for each file
            os.writeBytes("ls -al /sys/module/lowmemorykiller/parameters/adj\n");
            os.flush();
            if(bufferedReader.readLine().contains("rw-rw-")) {
                adjPermissionSetRight = true;
            }
            os.writeBytes("ls -al /sys/module/lowmemorykiller/parameters/minfree\n");
            os.flush();
            if(bufferedReader.readLine().contains("rw-rw-")) {
                minfreePermissionSetRight = true;
            }
        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            // if an exception is thrown, clean up
            suProcess.destroy();
        }
        return(adjPermissionSetRight && minfreePermissionSetRight);
    }

    // do the magic. set the "right" permissions for the two files (adj/minfree)
    public void alterPermissions() {
                try {
                    os.writeBytes("chmod 660 /sys/module/lowmemorykiller/parameters/adj\n");
                    os.writeBytes("chmod 660 /sys/module/lowmemorykiller/parameters/minfree\n");
                    os.flush();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    // if an exception is thrown, clean up
                    suProcess.destroy();
                }

    }
}
