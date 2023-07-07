package jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.lopezlab.aoyagi.movesensedatacollection.R;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_02_settings.InputActivity;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording.AngularVelocity;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording.CsvLogger;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording.FormatHelper;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording.RecordActivity;
import jp.lopezlab.aoyagi.movesensedatacollection.esenselib.*;

public class EsenseAct extends AppCompatActivity {


    private Button eSenseConnectBTM;
    private TextView connectionTextView;
    private TextView deviceNameTextView;

    private TextView tvSystemTime;

    private Button e_record;
    private boolean isRecording=false;

    private long timestamp;

    private String name = "eSense-0056";
    //eSense-0056
   // private String name = "eSense-1658";
    private int timeout = 30000;
    private String TAG = "Esense";
    private static final int PERMISSION_REQUEST_CODE = 200;


    ConnectionListenerManager connectionListenerManager;
    SensorListenerManager sensorListenerManager;

    ESenseManager eSenseManager;

    ESenseEvent Eevent;


    private int sampleRate;
    private int userId;
    private CsvLogger mCsvLogger;
    String dataDirPath;

    private File file;
    private long first_time;
    private long first_TimeStamp;
    private long last_TimeStamp;

    private boolean MoveTimeOk;

    private boolean sameTime;

    private MdsSubscription mdsSubscription;

    private final SimpleDateFormat dataFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private final SimpleDateFormat dataFormat2 = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    private final String ANGULAR_VELOCITY_PATH = "Meas/Gyro/";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esense_connect);

        deviceNameTextView = (TextView) findViewById(R.id.esenseText);
        eSenseConnectBTM = (Button) findViewById(R.id.esense_connect);
        connectionTextView = (TextView) findViewById(R.id.e_connection);
        tvSystemTime = (TextView) findViewById(R.id.times);

        e_record = (Button) findViewById(R.id.e_record);

       sensorListenerManager = new SensorListenerManager(this);



       Intent intent=getIntent();
       userId=intent.getIntExtra(InputActivity.INT_USER_ID,0);
       //sampleRate=intent.getIntExtra(InputActivity.INT_SAMPLE_RATE,52);
        sampleRate =52;



       StringBuilder sb = new StringBuilder();
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss_a");
        String currentTimestamp = formatter.format(date);
        sb.append(String.format(Locale.getDefault(), "%03d", userId)).append("_").append(currentTimestamp).append(".csv");
        dataDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator;
        file=new File(dataDirPath,sb.toString());
        mCsvLogger = new CsvLogger(file);





        connectionListenerManager = new ConnectionListenerManager(this, sensorListenerManager, connectionTextView, deviceNameTextView);
        eSenseManager = new ESenseManager(name, EsenseAct.this.getApplicationContext(), connectionListenerManager);
        if (!checkPermission()) {
            requestPermission();
        } else {
            Log.d(TAG, "Permission already granted..");
        }





        eSenseConnectBTM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                eSenseManager.connect(timeout);
            }
        });

        e_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isRecording){
                    sensorListenerManager.setTimeOk(true);
                    MoveTimeOk = true;
                    subscribeToSensor(ConnectedMovesense.movesense.getSerial(),sampleRate);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    sensorListenerManager.setTimeOk(true);
                    sensorListenerManager.startDataCollection();

                    isRecording = true;
                    e_record.setText("Stop");
                    first_time=System.currentTimeMillis();
                }else{
                    isRecording = false;
                    sensorListenerManager.stopDataCollection();
                    mCsvLogger.finishSavingLogs();
                    unsubscribe();
                    finish();
                    e_record.setText("Record");
                }

            }
        });








    }



    private boolean checkPermission() {

        int locationResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int writeResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return locationResult == PackageManager.PERMISSION_GRANTED &&
                writeResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION,
                WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);

    }


    public void subscribeToSensor(String deviceSerial, int sampleRate) {
        if (mdsSubscription != null) {
            unsubscribe();
        }


        // パラメータの作成
        String strContract = "{\"Uri\": \"" + deviceSerial + ANGULAR_VELOCITY_PATH +sampleRate+  "\"}";
        //String strContract2 = "{\"Uri\": \"" + deviceSerial + accUri + "\"}";

        Log.d(TAG, strContract);
        //Log.d(TAG, strContract2);

        mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                FormatHelper.formatContractToJson(deviceSerial, ANGULAR_VELOCITY_PATH + "52"), new MdsNotificationListener() {

                    // センサ値を受け取るメソッド
                    @Override
                    public void onNotification(String data) {

                        AngularVelocity angularVelocity = new Gson().fromJson(data, AngularVelocity.class);

                        if(MoveTimeOk){
                            first_TimeStamp = angularVelocity.body.timestamp;
                            MoveTimeOk = false;
                        }


                        if (angularVelocity != null) {

                           //final int sampleRate = Integer.parseInt(rate);
                           final float sampleInterval = 1000.0f / 52f;
                            long time=System.currentTimeMillis()-first_time;
                            tvSystemTime.setText(dataFormat.format(time));

                            mCsvLogger.appendHeader("Time,TimeStamp,m_gyroX,m_gyroY,m_gyroZ");
                            AngularVelocity.Array arrayData = null;
                            for (int i = 0; i < angularVelocity.body.array.length; i++) {
                               /* try {
                                    Thread.sleep(20-1000/52);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                } */
                                arrayData = angularVelocity.body.array[i];
                                last_TimeStamp = angularVelocity.body.timestamp - first_TimeStamp;
                                //+ Math.round(sampleInterval * i)

                                mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                        "%s,%d,%.4f,%.4f,%.4f, ", dataFormat2.format(new Date()),last_TimeStamp+ Math.round(sampleInterval * i),
                                        arrayData.x, arrayData.y, arrayData.z));
                            }
                        }
                    }


                    @Override
                    public void onError(MdsException error) {
                        Log.e(TAG, "subscription onError(): ", error);
                        unsubscribe();
                        onDestroy();
                    }
                });

       /* mdsSubscription2 = Mds.builder().build(this).subscribe(Constants.URI_EVENTLISTENER, strContract, new MdsNotificationListener() {

            // センサ値を受け取るメソッド2
            @Override
            public void onNotification(String data) {
                MovesenseAccDataResponse accResponse = new Gson().fromJson(data, MovesenseAccDataResponse.class);
                if (accResponse != null && accResponse.body.array.length > 0) {
                    AccDataModel accData = new AccDataModel(
                            (float) accResponse.body.array[0].x,
                            (float) accResponse.body.array[0].y,
                            (float) accResponse.body.array[0].z,
                            accResponse.body.timestamp,
                            System.currentTimeMillis());

                    xCsvLogger.appendHeader("Sensor time (ms),System time (ms),X (m/s^2),Y (m/s^2),Z (m/s^2)");
                    xCsvLogger.appendLine(String.format(Locale.getDefault(), "%d,%d,%.6f,%.6f,%.6f", accData.sensorTime, accData.systemTime, accData.x, accData.y, accData.z));

                }
            }

            @Override
            public void onError(MdsException error) {
                Log.e(TAG, "subscription onError(): ", error);
                unsubscribe();
                onDestroy();
            }
        }); */


    }


    // Subscription の解除
    public void unsubscribe() {
        if (mdsSubscription != null) {
            mdsSubscription.unsubscribe();
            mdsSubscription = null;
        }

    }

}
