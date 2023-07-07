package jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import jp.lopezlab.aoyagi.movesensedatacollection.activity_02_settings.InputActivity;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_04_file_list.FileListActivity;
import jp.lopezlab.aoyagi.movesensedatacollection.esenselib.*;


public class ConnectionListenerManager implements ESenseConnectionListener {

    private final String TAG = "ConectionLisenerManager";
    private final Context context;
    private int samplingRate = 50;

    
    TextView eSenseConnection;
    TextView deviceNameTextView;
    SharedPreferences.Editor sharedPrefEditor;
    SensorListenerManager sensorListenerManager;




    public ConnectionListenerManager(Context context, SensorListenerManager sensorListenerManager, TextView eSenseConnection,
                                     TextView deviceNameTextView){
        this.context = context;
        this.eSenseConnection = eSenseConnection;
        this.deviceNameTextView = deviceNameTextView;
        this.sensorListenerManager = sensorListenerManager;
    }

    //manager = new ESenseManager(name, MainActivity.this.getApplicationContext(),eSenseConnectionListener);


    @Override
    public void onDeviceFound(ESenseManager manager) {
        Log.d(TAG, "onDeviceFound");
    }

    @Override
    public void onDeviceNotFound(ESenseManager manager) {
        Log.d(TAG, "onDeviceNotFound");
        Toast.makeText(context, "onDeviceNotFound", Toast.LENGTH_LONG).show();
       new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                eSenseConnection.setText("Disconnected");
                deviceNameTextView.setText(manager.getmDeviceName());
                Toast.makeText(context, "Device not Found !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onConnected(ESenseManager manager) {
        Log.d(TAG, "onConnected");
       new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void run() {

                eSenseConnection.setText("Connected");
                eSenseConnection.setTextColor(android.R.color.holo_green_dark);
                deviceNameTextView.setText(manager.getmDeviceName());
                Toast.makeText(context, "Device Connected !", Toast.LENGTH_SHORT).show();

            }
        });
      //  Looper.prepare();
       // Toast.makeText(context, "onConnected", Toast.LENGTH_LONG).show();
       // Looper.loop();
        /*runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Stuff that updates the UI
                deviceNameTextView.setText(manager.getmDeviceName());
                eSenseConnection.setText("Connected");
                eSenseConnection.setTextColor(Color.GREEN);

            }
        });*/

        manager.registerSensorListener(sensorListenerManager, samplingRate);

    }





    @Override
    public void onDisconnected(ESenseManager manager) {
        Log.d(TAG, "onDisconnected");
      /*  new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                sharedPrefEditor.putString("status", "disconnected");
                sharedPrefEditor.commit();

                connectionTextView.setText("Disconnected");
                //deviceNameTextView.setText(manager.getmDeviceName());
                Toast.makeText(context, "Device Disconnected !", Toast.LENGTH_SHORT).show();
            }
        });*/
        Toast.makeText(context, "Device Disconnected !", Toast.LENGTH_LONG).show();


    }
}
