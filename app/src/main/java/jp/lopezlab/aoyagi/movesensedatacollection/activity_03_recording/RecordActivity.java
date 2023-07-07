package jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.lopezlab.aoyagi.movesensedatacollection.Constants;
import jp.lopezlab.aoyagi.movesensedatacollection.R;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection.ConnectedMovesense;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection.ConnectionListenerManager;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection.EsenseAct;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection.SensorListenerManager;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_02_settings.InputActivity;
import jp.lopezlab.aoyagi.movesensedatacollection.esenselib.ESenseManager;

public class RecordActivity extends AppCompatActivity {

    private final String TAG="RecordActivity";
    SensorListenerManager sensorListenerManager;

    //UI

    private Button recordButton;
    private TextView tvSystemTime;

    private MdsSubscription mdsSubscription;

    private int userId;
    private int sampleRate;
    String dataDirPath;

    private File file;
   // private File file_x;





    private boolean isRecording=false;
    private CsvLogger mCsvLogger;
    private CsvLogger xCsvLogger;


    private final String ANGULAR_VELOCITY_PATH = "Meas/Gyro/";
    private final String ANGULAR_VELOCITY_INFO_PATH = "/Meas/Gyro/Info";
    public static final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private String rate;




    //SystemTime
    private long first_time;
    //分：秒.ミリ秒に変換
    ConnectionListenerManager connectionListenerManager;
    ESenseManager eSenseManager;
    private final SimpleDateFormat dataFormat = new SimpleDateFormat("mm:ss.SS", Locale.getDefault());

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        sensorListenerManager = new SensorListenerManager(this);


        //UI


        recordButton=findViewById(R.id.record_button);
        tvSystemTime=findViewById(R.id.tv_systemTime);


        //Input data
        Intent intent=getIntent();
        userId=intent.getIntExtra(InputActivity.INT_USER_ID,0);
        sampleRate=intent.getIntExtra(InputActivity.INT_SAMPLE_RATE,52);

        //file name
        // timestamp + device serial + data type,
        StringBuilder sb = new StringBuilder();
        //StringBuilder sx = new StringBuilder();

        // Get Current Timestamp in format suitable for file names (i.e. no : or other bad chars)
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss_a");
        String currentTimestamp = formatter.format(date);

        sb.append(String.format(Locale.getDefault(), "%03d", userId)).append("_").append(currentTimestamp).append(".csv");
        //sx.append(String.format(Locale.getDefault(), "%03d", userId)).append("_").append("Acc").append(currentTimestamp).append(".csv");

        dataDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Documents" + File.separator;



        Log.i(TAG,sb.toString());
        //manage file
        //Context context=getApplicationContext();
        file=new File(dataDirPath,sb.toString());
        //file_x=new File(context.getFilesDir(),sb.toString());
        mCsvLogger = new CsvLogger(file);
        //xCsvLogger = new CsvLogger(file_x);




    }



    public void onClick(View view){
        if(view.getId()==R.id.record_button){
            if(ConnectedMovesense.movesense!=null){
                if(!isRecording){
                    sensorListenerManager.startDataCollection();
                    subscribeToSensor(ConnectedMovesense.movesense.getSerial(),sampleRate);

                    isRecording=true;
                    recordButton.setText("Stop Recording");
                    first_time=System.currentTimeMillis();
                }else{
                    isRecording=false;
                    recordButton.setText("Start Recording");
                    sensorListenerManager.stopDataCollection();
                    mCsvLogger.finishSavingLogs();
                    unsubscribe();
                    finish();
                }

            }
        }
    }



    // Subscription の設定（serial でデバイスを指定、sensor で取得するセンサとサンプル周波数を含む定数を指定）
    public void subscribeToSensor(String deviceSerial, int sampleRate) {
        if (mdsSubscription != null) {
            unsubscribe();
        }
       /* if (mdsSubscription2 != null) {
            unsubscribe();
        }

       String accUri;

        switch (sampleRate) {
            case 13:
                accUri = Constants.URI_MEAS_ACC_13;

                break;
            case 26:
                accUri = Constants.URI_MEAS_ACC_26;

                break;
            case 52:
            default:
                accUri = Constants.URI_MEAS_ACC_52;

                break;
            case 102:
                accUri=Constants.URI_MEAS_ACC_104;

                break;


        }*/

        // パラメータの作成
        String strContract = "{\"Uri\": \"" + deviceSerial + ANGULAR_VELOCITY_PATH +sampleRate+  "\"}";
        //String strContract2 = "{\"Uri\": \"" + deviceSerial + accUri + "\"}";

        Log.d(TAG, strContract);
        //Log.d(TAG, strContract2);

        mdsSubscription = Mds.builder().build(this).subscribe(URI_EVENTLISTENER,
                FormatHelper.formatContractToJson(deviceSerial, ANGULAR_VELOCITY_PATH + sampleRate), new MdsNotificationListener() {

            // センサ値を受け取るメソッド
            @Override
            public void onNotification(String data) {

                AngularVelocity angularVelocity = new Gson().fromJson(data, AngularVelocity.class);


               if (angularVelocity != null) {

                    //final int sampleRate = Integer.parseInt(rate);
                    final float sampleInterval = 1000.0f / (float) sampleRate;
                    long time=System.currentTimeMillis()-first_time;
                    tvSystemTime.setText(dataFormat.format(time));

                    mCsvLogger.appendHeader("Timestamp (ms),X: (degree per second),Y: (degree per second),Z: (degree per second)");
                    AngularVelocity.Array arrayData = null;
                    for (int i = 0; i < angularVelocity.body.array.length; i++) {
                        arrayData = angularVelocity.body.array[i];

                        mCsvLogger.appendLine(String.format(Locale.getDefault(),
                                "%d,%.6f,%.6f,%.6f, ", angularVelocity.body.timestamp + Math.round(sampleInterval * i),
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