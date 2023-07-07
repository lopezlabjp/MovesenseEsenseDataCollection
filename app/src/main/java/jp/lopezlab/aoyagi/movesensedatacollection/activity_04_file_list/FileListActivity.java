package jp.lopezlab.aoyagi.movesensedatacollection.activity_04_file_list;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import jp.lopezlab.aoyagi.movesensedatacollection.R;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_02_settings.InputActivity;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_05_chart.ChartActivity;

public class FileListActivity extends AppCompatActivity {

    private final String TAG="FileListActivity";
    private final int REQUEST_CODE_DELETE = 100;

    private ArrayList<String> fileList;
    private ListView lvFileList;
    private TextView tvNoFile;
    private ArrayAdapter<String> adapter;
    private Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);

        Log.d(TAG,"onCreate method");
        context=getApplicationContext();
        //UI
        lvFileList=findViewById(R.id.lv_file_list);
        tvNoFile=findViewById(R.id.tv_no_file);

        //file_list
        fileList=new ArrayList<>();


        updateFileList();
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,fileList);
        lvFileList.setAdapter(adapter);


        lvFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename=(String)parent.getItemAtPosition(position);
                Intent intent=new Intent(FileListActivity.this, ChartActivity.class);
                intent.putExtra("FILE_NAME",filename);
                startActivityForResult(intent,REQUEST_CODE_DELETE);
            }
        });

        //戻るバー
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void updateFileList(){
        fileList.clear();
        //csvファイルだけ読み込むだけのフィルタを作成する
        FilenameFilter filter=new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if(s.indexOf(".csv")!=-1){
                    return true;
                }else{
                    return false;
                }
            }
        };

        File[] files=new File(context.getFilesDir().getPath()).listFiles(filter);
        Arrays.sort(files);
        for (int i=0;i< files.length;i++){
            Log.d(TAG, files[i].getName());
            fileList.add(files[i].getName());
        }

        if (fileList.size() == 0) {
            lvFileList.setVisibility(View.GONE);
            tvNoFile.setVisibility(View.VISIBLE);
        } else {
            lvFileList.setVisibility(View.VISIBLE);
            tvNoFile.setVisibility(View.GONE);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId=item.getItemId();
        boolean returnVal=true;
        //戻る時の処理
        if(itemId==android.R.id.home){
            finish();
        }
        else{
            returnVal=super.onOptionsItemSelected(item);
        }
        return returnVal;
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult");
        Log.d(TAG, String.valueOf(resultCode));
        Log.d(TAG, String.valueOf(requestCode));
        if(resultCode== RESULT_OK && requestCode==REQUEST_CODE_DELETE&&data!=null) {
            boolean isDelete = data.getBooleanExtra("FILE_DELETE", false);
            String filename = data.getStringExtra("FILE_NAME");
            if (isDelete) {
                File file = new File(context.getFilesDir().getPath(), filename);
                file.delete();

                updateFileList();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show();
            }
        }
    }
}