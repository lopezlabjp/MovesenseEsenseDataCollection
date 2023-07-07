package jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Sheet;

import jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording.CsvLogger;
import jp.lopezlab.aoyagi.movesensedatacollection.esenselib.ESenseConfig;
import jp.lopezlab.aoyagi.movesensedatacollection.esenselib.ESenseSensorListener;
import jp.lopezlab.aoyagi.movesensedatacollection.esenselib.ESenseEvent;


public class SensorListenerManager implements ESenseSensorListener {

    private final String TAG = "SensorListenerManager";

    private double[] gyro;
    private double[] accel;
    private boolean dataCollecting;
    private boolean TimeStampOk;
    private long timeStamp;
    private long first_timeStamp;
    int rowIndex;

    private final SimpleDateFormat dataFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    public String sentTIme(){
       return dataFormat.format(new Date());
    }


    Context context;
    String dataDirPath;
    String sensorDataFile;
    File excelFile;
    CsvLogger eCsvLogger;

    Sheet excelSheet;



    ESenseConfig eSenseConfig;


    public SensorListenerManager(Context context ){
        this.context = context;
        eSenseConfig = new ESenseConfig();
        excelFile = null;
        rowIndex = 1;


        excelSheet = null;

        dataDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download" + File.separator;



    }



    public void setTimeOk(boolean TimeIsOk){
        this.TimeStampOk = TimeIsOk;
    }


    @Override
    public void onSensorChanged(ESenseEvent evt) {
        Log.d(TAG, "onSensorChanged()");


        if(TimeStampOk){
            first_timeStamp = evt.getTimestamp();
            TimeStampOk = false;
        }

        if (dataCollecting){
            accel = evt.convertAccToG(eSenseConfig);
            gyro = evt.convertGyroToDegPerSecond(eSenseConfig);

            timeStamp = evt.getTimestamp() - first_timeStamp;
           // float sampleInterval = 1000.0f / 52f;

            eCsvLogger.appendHeader("Time,TimeStamp,gyroX,gyroY,gyroZ");
            eCsvLogger.appendLine(String.format(Locale.getDefault(),
                        "%s,%d,%.6f,%.6f,%.6f ",dataFormat.format(new Date()), timeStamp,
                        gyro[0],gyro[1],gyro[2]));





           /* Log.d(TAG, "first:"+String.valueOf(first_timeStamp));
            Log.d(TAG, "timestamp:"+String.valueOf(timeStamp));

       /*     eCsvLogger.appendHeader("Time,TimeStamp,(m/s^2),Y (m/s^2),Z (m/s^2),gyroX,gyroY,gyroZ");
            eCsvLogger.appendLine(String.format(Locale.getDefault(),
                    "%s,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f ",dataFormat.format(new Date()), timeStamp,
                    accel[0], accel[1], accel[2],gyro[0],gyro[1],gyro[2])); */



            String sensorData = " Time : " + timeStamp +" gyro : " + gyro[0] + " " + gyro[1] + " " + gyro[2];
            Log.d(TAG, sensorData);

        }


      /* if (dataCollecting){

            if(excelSheet != null){
                rowIndex++;

                timeStamp = evt.getTimestamp();
                accel = evt.convertAccToG(eSenseConfig);
                gyro = evt.convertGyroToDegPerSecond(eSenseConfig);

                Row dataRow = excelSheet.createRow(rowIndex);
                Cell dataCell = null;
                dataCell = dataRow.createCell(0);
                dataCell.setCellValue(timeStamp);

                dataCell = dataRow.createCell(1);
                dataCell.setCellValue(accel[0]);

                dataCell = dataRow.createCell(2);
                dataCell.setCellValue(accel[1]);

                dataCell = dataRow.createCell(3);
                dataCell.setCellValue(accel[2]);

                dataCell = dataRow.createCell(4);
                dataCell.setCellValue(gyro[0]);

                dataCell = dataRow.createCell(5);
                dataCell.setCellValue(gyro[1]);

                dataCell = dataRow.createCell(6);
                dataCell.setCellValue(gyro[2]);



                String sensorData = " Time : " + timeStamp
                        + " accel : " + accel[0] + " " + accel[1] + " " + accel[2] + " gyro : " + gyro[0] + " " + gyro[1] + " " + gyro[2];
                Log.d(TAG, sensorData);
            }
        } */



    }

   /* public void setColumnWidth(Sheet sheet){
        sheet.setColumnWidth(0, (15 * 300));
        sheet.setColumnWidth(1, (15 * 300));
        sheet.setColumnWidth(2, (15 * 300));
        sheet.setColumnWidth(3, (15 * 300));
        sheet.setColumnWidth(4, (15 * 300));
        sheet.setColumnWidth(5, (15 * 300));
        sheet.setColumnWidth(6, (15 * 300));
        sheet.setColumnWidth(7, (15 * 300));
        sheet.setColumnWidth(8, (15 * 300));

    } */


    public void startDataCollection(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss_a", Locale.getDefault());
        String currentDateTime = simpleDateFormat.format(new Date());


        sensorDataFile = "Esense_" + currentDateTime + ".csv";
        excelFile = new File(dataDirPath, sensorDataFile);
        eCsvLogger = new CsvLogger(excelFile);
        dataCollecting = true;



       /* SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss_a", Locale.getDefault());
        String currentDateTime = simpleDateFormat.format(new Date());

        sensorDataFile = "Esense_" + currentDateTime + ".csv";
        excelFile = new File(context.getFilesDir(), sensorDataFile);


        excelWorkbook = new HSSFWorkbook();
        excelSheet = excelWorkbook.createSheet("Esense");

        setColumnWidth(excelSheet);
        dataCollecting = true; */

    }

    public void stopDataCollection() {
        dataCollecting = false;
        eCsvLogger.finishSavingLogs();
    }

      /*  rowIndex = 1;
        dataCollecting = false;
        FileOutputStream accelOutputStream = null;

        try {
            accelOutputStream = new FileOutputStream(excelFile);
            excelWorkbook.write(accelOutputStream);

            Log.w(TAG, "Writing excelFile : " + excelFile);
        } catch (IOException e) {
            Log.w(TAG, "Error writing : " + excelFile, e);
        } catch (Exception e) {
            Log.w(TAG, "Failed to save data file", e);
        } finally {
            try {
                if (null != accelOutputStream){
                    accelOutputStream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }*/

}
