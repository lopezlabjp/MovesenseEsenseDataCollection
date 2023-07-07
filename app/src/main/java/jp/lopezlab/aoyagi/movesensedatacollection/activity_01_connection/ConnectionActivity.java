package jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.movesense.mds.Mds;
import com.movesense.mds.MdsConnectionListener;
import com.movesense.mds.MdsException;

import java.util.ArrayList;

import jp.lopezlab.aoyagi.movesensedatacollection.R;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_02_settings.InputActivity;

public class ConnectionActivity extends AppCompatActivity implements View.OnClickListener {


    private final String TAG = "MainActivity";



    private static final int REQUEST_ENABLE_BLUETOOTH = 1; // Bluetooth 設定のアクティビティに渡す
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 2;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 3;
    private static final int REQUEST_CODE_FINE_LOCATION = 4;

    // MDS
    private Mds mMds;
    private MovesenseModel connectedMovesense;

    private BluetoothAdapter bluetoothAdapter;

    // Scan UI
    private RecyclerView movesenseRecyclerView;
    private MovesenseAdapter movesenseAdapter;
    private ArrayList<MovesenseModel> movesenseInfoArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);



        movesenseRecyclerView = findViewById(R.id.movesense_recyclerView);
        movesenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration dividerItemDecorationDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        movesenseRecyclerView.addItemDecoration(dividerItemDecorationDecoration);

        //connectionListenerManager = new ConnectionListenerManager(this, sensorListenerManager, connectionState, esenseTextView);
       // eSenseManager = new ESenseManager(name, ConnectionActivity.this.getApplicationContext(), connectionListenerManager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 検出された端末情報を受信するための、BroadcastReceiver を登録
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // MDS オブジェクトの初期化
        mMds = Mds.builder().build(this);
        if (ConnectedMovesense.movesense != null) {
            mMds.disconnect(ConnectedMovesense.movesense.getSerial());
        }

        resetRecyclerView();
    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        if (buttonId == R.id.scanButton) {
            scanMovesense();
        } else if (buttonId == R.id.movesenseItem) {
            Log.d(TAG, "click!");
            unregisterReceiver(receiver);
            ConnectingDialog.INSTANCE.showDialog(this);
            connectMovesense((String) view.getTag());
            //startActivity(new Intent(ConnectionActivity.this, MainActivity.class));

        } else {
            Log.w(TAG, "Missing button id.");
        }
    }

    private void resetRecyclerView() {
        movesenseInfoArrayList = new ArrayList<>();
        movesenseAdapter = new MovesenseAdapter(movesenseInfoArrayList, this);
        movesenseRecyclerView.setAdapter(movesenseAdapter);
    }


    private void scanMovesense() {
        resetRecyclerView();

        // 端末の検出を開始
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_CODE_BLUETOOTH_SCAN);
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        showToast("scanning...");
    }

    // 端末を検出した時の BroadcastReceiver を作成
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (ActivityCompat.checkSelfPermission(ConnectionActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    ActivityCompat.requestPermissions(ConnectionActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                String deviceFullName = device.getName();

                // Movesense が見つかったときの処理
                if (deviceFullName != null && deviceFullName.contains("Movesense")) {
                    MovesenseModel movesense = new MovesenseModel(deviceFullName.split(" ")[1], device.getAddress());

                    if (!movesenseInfoArrayList.contains(movesense)) {
                        movesenseAdapter.add(movesense);
                    }
                    movesenseAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void connectMovesense(String deviceHardwareAddress) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_CODE_BLUETOOTH_SCAN);
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Movesense の MAC address と MdsConnectionListener を引数にして connect する
        mMds.connect(deviceHardwareAddress, new MdsConnectionListener() {
            @Override
            public void onConnect(String macAddress) {
                Log.d(TAG, "onConnect: " + macAddress);
            }
            // onConnect から数秒間して onConnectionComplete が呼ばれる
            @Override
            public void onConnectionComplete(String macAddress, String serial) {
                Log.d(TAG, "onConnectionComplete: " + macAddress);
                ConnectingDialog.INSTANCE.dismissDialog();
                connectedMovesense = new MovesenseModel(serial, macAddress);
                showToast("connected\nSerial: " + serial);
                ConnectedMovesense.movesense = new MovesenseModel(serial, macAddress);

                startActivity(new Intent(ConnectionActivity.this, InputActivity.class));
            }

            @Override
            public void onError(MdsException e) {
                Log.e(TAG, "onError:" + e);
                ConnectingDialog.INSTANCE.dismissDialog();
                showConnectionError(e);
            }

            @Override
            public void onDisconnect(String bleAddress) {
                Log.d(TAG, "onDisconnect: " + bleAddress);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectedMovesense != null) {
            mMds.disconnect(connectedMovesense.getAddress());
            connectedMovesense = null;
        }

        unregisterReceiver(receiver);
    }

    // Toast を表示する用のメソッド
    private void showToast(String text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void showConnectionError(MdsException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Connection Error:")
                .setMessage(e.getMessage());
        builder.create().show();
    }



}