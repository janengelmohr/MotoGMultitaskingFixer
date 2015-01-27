package de.visi0nary.motogmultitaskingfixer;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ApplyPermissionsService extends Service {
    public ApplyPermissionsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        alterPermissions();
        stopSelf();
    }

    // returns true if device is rooted and false if not
    private boolean deviceIsRooted() {
        int returnValue = -1;
        try {
            // try to create a root process
            Process testSU = Runtime.getRuntime().exec("su");
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
            }
        }
        catch (Exception e) {
        }
        return(returnValue==0);
    }

    // this method is called after applying the values. It returns true if the values were applied correctly, false otherwise
    public boolean checkIfPermissionsAreSetCorrect() {
        boolean adjPermissionSetRight = false;
        boolean minfreePermissionSetRight = false;
        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
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
            // clean up
            suProcess.destroy();
        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return(adjPermissionSetRight && minfreePermissionSetRight);
    }

    // do the magic. set the "right" permissions for the two files (adj/minfree)
    public void alterPermissions() {
            // check if device is rooted
            if (deviceIsRooted()) {
                try {
                    Process suProcess = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                    DataInputStream is = new DataInputStream(suProcess.getInputStream());

                    os.writeBytes("chmod 660 /sys/module/lowmemorykiller/parameters/adj\n");
                    os.writeBytes("chmod 660 /sys/module/lowmemorykiller/parameters/minfree\n");
                    os.flush();
                    // clean up
                    // suProcess.destroy();
                    if (checkIfPermissionsAreSetCorrect()) {
                        Toast.makeText(getApplicationContext(), "Everything went fine. Enjoy multitasking! :)", Toast.LENGTH_SHORT).show();
                    } else {
                        // should never happen, but just in case... :)
                        Toast.makeText(getApplicationContext(), "Mhm... Something went wrong. The permissions can't be altered even though we are rooted.", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(getApplicationContext(), "Failed to acquire root access :(", Toast.LENGTH_SHORT).show();

    }
}
