package jp.lopezlab.aoyagi.movesensedatacollection.activity_02_settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import jp.lopezlab.aoyagi.movesensedatacollection.R;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection.EsenseAct;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_03_recording.RecordActivity;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_04_file_list.FileListActivity;

public class InputActivity extends AppCompatActivity {

    public static final String INT_USER_ID="jp.lopezlab.aoyagi.movesensedatacollection.user_id";
    public static final String INT_SAMPLE_RATE="jp.lopezlab.aoyagi.movesensedatacollection.sample_rate";

    private EditText editTextUserId;
    private Spinner spinnerSampleRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int userId = sharedPreferences.getInt(INT_USER_ID, 0);
        editTextUserId = findViewById(R.id.editTextUserId);
        editTextUserId.setText(String.valueOf(userId));

        int sampleRate = sharedPreferences.getInt(INT_SAMPLE_RATE, 52);
        spinnerSampleRate = findViewById(R.id.spinnerSampleRate);
        ArrayAdapter<CharSequence> adapterSampleRate = ArrayAdapter.createFromResource(this, R.array.sample_rate_array, android.R.layout.simple_spinner_item);
        adapterSampleRate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSampleRate.setAdapter(adapterSampleRate);
        spinnerSampleRate.setSelection(adapterSampleRate.getPosition(String.valueOf(sampleRate)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_file_options_list,menu);
        return true;
    }

    public void onClick(View view){
        if (view.getId() == R.id.start_button) {
            saveSettings();
            int userId = 0;
            if (!editTextUserId.getText().toString().equals("")) {
                userId = Integer.parseInt(editTextUserId.getText().toString());
            }


            int sampleRate = Integer.parseInt(spinnerSampleRate.getSelectedItem().toString());
            Intent intent = new Intent(InputActivity.this, EsenseAct.class);
            intent.putExtra(INT_USER_ID, userId);
            intent.putExtra(INT_SAMPLE_RATE, sampleRate);
            startActivity(intent);
        } else if (view.getId() == R.id.button_plus) {
            if (!editTextUserId.getText().toString().equals("")) {
                int currentId = Integer.parseInt(editTextUserId.getText().toString());
                editTextUserId.setText(String.valueOf(++currentId));
            } else {
                editTextUserId.setText(String.valueOf(1));
            }
        } else if (view.getId() == R.id.button_minus){
            if (!editTextUserId.getText().toString().equals("")) {
                int currentId = Integer.parseInt(editTextUserId.getText().toString());
                editTextUserId.setText(String.valueOf(--currentId));
            } else {
                editTextUserId.setText(String.valueOf(1));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveSettings();
    }


    private void saveSettings() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (!editTextUserId.getText().toString().equals("")) {
            editor.putInt(INT_USER_ID, Integer.parseInt(editTextUserId.getText().toString()));
        }
        editor.putInt(INT_SAMPLE_RATE, Integer.parseInt(spinnerSampleRate.getSelectedItem().toString()));
        editor.apply();
    }

    //menu setting
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId=item.getItemId();
        switch (itemId){
            case R.id.file_list:
                Intent intent = new Intent(InputActivity.this, FileListActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

}