package jp.lopezlab.aoyagi.movesensedatacollection.activity_05_chart;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import jp.lopezlab.aoyagi.movesensedatacollection.R;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording.MovesenseAccDataResponse;

public class ChartActivity extends AppCompatActivity {

    private final String TAG="ChartActivity";
    private String filename;
    private LineChart mChart;
    private LineChart hzChart;
    private String [] labels=new String[]{
            "AccX",
            "AccY",
            "AccZ"
    };

    private ArrayList<Float> xDataList;
    private ArrayList<Float> yDataList;
    private ArrayList<Float> zDataList;
    private ArrayList<Float> timeDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Intent intent=getIntent();
        filename=intent.getStringExtra("FILE_NAME");
        //戻るバー
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //csvFileを読み込む
        readCsvFile(filename);

        //グラフを表示
        plotChart(timeDataList,xDataList,yDataList,zDataList);
        //Hzを表示
        plotHz(timeDataList);

    }

    //ファイルを読み込む
    private void readCsvFile(String file){
        try {
            FileInputStream fileInputStream=openFileInput(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream,"UTF-8"));
            String line;
            //要素数0:systemTime,1:x,2:y,3:z
            String[] data;
            //最初の一行は読み込まない
            boolean isFirst=true;
            boolean isTimeFirst=true;
            double firstTime=0;
            double time=0;
            //systemCurrentと3軸加速度
            timeDataList=new ArrayList<>();
            xDataList =new ArrayList<>();
            yDataList =new ArrayList<>();
            zDataList =new ArrayList<>();

            while((line=reader.readLine())!=null) {
                data = line.split(",");
//                Log.i("ChartActivity",data[3].getClass().getSimpleName());
                if (isFirst) {
                    isFirst = false;
                } else {
                    //csvのデータを格納
                    if (isTimeFirst){
                        firstTime= Double.parseDouble(data[0]);
                        isTimeFirst=false;
                    }
                    time=(Double.parseDouble(data[0])-firstTime)/1000;
                    Log.i(TAG, String.valueOf(time));
                    timeDataList.add((float) time);
                    xDataList.add(Float.valueOf(data[2]));
                    yDataList.add(Float.valueOf(data[3]));
                    zDataList.add(Float.valueOf(data[4]));
                }
            }


        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void plotChart(ArrayList<Float>timeList,ArrayList<Float>xList,ArrayList<Float>yList,ArrayList<Float>zList){
        //グラフに関する記述
        //データをEntryに格納
        ArrayList<Entry>xEntryList=new ArrayList<>();
        ArrayList<Entry>yEntryList=new ArrayList<>();
        ArrayList<Entry>zEntryList=new ArrayList<>();
        if(timeList.size()!=0){
            for (int i=0;i<timeList.size();i++){
                xEntryList.add(new Entry(timeList.get(i),xList.get(i)));
                yEntryList.add(new Entry(timeList.get(i),yList.get(i)));
                zEntryList.add(new Entry(timeList.get(i),zList.get(i)));
                Log.d(TAG, String.valueOf(timeList.get(i)));
            }
        }
        //3軸加速度を表示するために、LineDataSetを格納するListを作成
        ArrayList<ILineDataSet>lineDataSets=new ArrayList<>();
        //EntryのListをDataSetに格納
        LineDataSet xLineDataSet=new LineDataSet(xEntryList,labels[0]);
        LineDataSet yLineDataSet=new LineDataSet(yEntryList,labels[1]);
        LineDataSet zLineDataSet=new LineDataSet(zEntryList,labels[2]);

        //DataSet(グラフ)のフォーマットを指定
        xLineDataSet.setColor(getResources().getColor(android.R.color.holo_red_light));
        xLineDataSet.setLineWidth(2.0f);
        xLineDataSet.setDrawCircles(false);
        xLineDataSet.setDrawValues(false);

        yLineDataSet.setColor(getResources().getColor(android.R.color.holo_green_light));
        yLineDataSet.setLineWidth(2.0f);
        yLineDataSet.setDrawCircles(false);
        yLineDataSet.setDrawValues(false);

        zLineDataSet.setColor(getResources().getColor(android.R.color.holo_blue_light));
        zLineDataSet.setLineWidth(2.0f);
        zLineDataSet.setDrawCircles(false);
        zLineDataSet.setDrawValues(false);

        //リストに格納
        lineDataSets.add(xLineDataSet);
        lineDataSets.add(yLineDataSet);
        lineDataSets.add(zLineDataSet);
        //DataSet（各グラフ）をDataに格納
        LineData lineData=new LineData(lineDataSets);


        //DataをChartに格納
        mChart=findViewById(R.id.LineChart_acc);
        mChart.setData(lineData);
        //Chartのフォーマットを指定
        mChart.getDescription().setEnabled(false);//説明文を表示しない
//        mChart.setDrawGridBackground(true);//Grid背景色

        //凡例
        Legend legend=mChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);//グラフの上下
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);//グラフの左右
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);//並び方
        legend.setDrawInside(true);//グラフの中に配置するか
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(5f);

        //軸に関する記述
        //x軸
        XAxis xAxis=mChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(Color.BLACK);

        //y軸(左側)
        YAxis yAxis=mChart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setTextColor(Color.BLACK);

        //y軸(右側)
        mChart.getAxisRight().setEnabled(false);//右側の目盛りを表示させるかさせないか

        mChart.invalidate();
    }

    private void plotHz(ArrayList<Float>time){
        int isFirstTime=0;
        int sampleCount=0;
        ArrayList<Entry> secEntryList=new ArrayList<>();
        for (float t:time){
            if(isFirstTime!=(int) t){
                Log.i(TAG, "\nTime:"+String.valueOf(isFirstTime)+" s\nHz:"+String.valueOf(sampleCount)+" hz");
                secEntryList.add(new Entry(isFirstTime,sampleCount));
                isFirstTime=(int)t;
                sampleCount=0;
            }else{
                Log.i(TAG, String.valueOf(t));
                sampleCount++;
            }
        }
        hzChart=findViewById(R.id.LineChart_hz);
        if (isFirstTime==0){
            hzChart.setVisibility(View.GONE);
            TextView tvNotHz=findViewById(R.id.tv_not_Hz);
            tvNotHz.setVisibility(View.VISIBLE);
        }else {
            //DataSetにEntryを格納
            //グラフのフォーマット指定
            LineDataSet lineDataSet=new LineDataSet(secEntryList,"Hz");
            lineDataSet.getColor(getResources().getColor(android.R.color.holo_orange_light));
            lineDataSet.setLineWidth(2.0f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);

            //LineData
            LineData lineData=new LineData(lineDataSet);
            hzChart.setData(lineData);
            hzChart.getDescription().setEnabled(false);

            //凡例
            Legend legend=hzChart.getLegend();
            legend.setEnabled(true);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);//グラフの上下
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);//グラフの左右
            legend.setDrawInside(true);//グラフの中に配置するか
            legend.setTextColor(Color.BLACK);
            legend.setTextSize(5f);

            //軸に関する記述
            //x軸
            XAxis xAxis=hzChart.getXAxis();
            xAxis.setEnabled(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawLabels(true);
            xAxis.setTextColor(Color.BLACK);

            //y軸(左側)
            YAxis yAxis=hzChart.getAxisLeft();
            yAxis.setEnabled(true);
            yAxis.setTextColor(Color.BLACK);


            //y軸(右側)
            hzChart.getAxisRight().setEnabled(false);//右側の目盛りを表示させるかさせないか

            hzChart.invalidate();

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_chart_options_list,menu);
        return true;
    }



    //メニューバー
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId=item.getItemId();
        boolean returnVal=true;
        //削除するか戻るか
        switch (itemId){
            case R.id.menu_delete_file:
//                ダイアログを表示
                DeleteFileDialogFragment dialogFragment=new DeleteFileDialogFragment(filename,ChartActivity.this);
                dialogFragment.show(getSupportFragmentManager(),"DeleteFileDialogFragment");
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }


}