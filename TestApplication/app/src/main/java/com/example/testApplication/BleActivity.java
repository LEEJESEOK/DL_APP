package com.example.testApplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BleActivity extends AppCompatActivity {
    private static final String TAG = BleActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_GPS = 2;

    private static final int STATE_STOP = 0;
    private static final int STATE_SCAN = 1;

    // ms 단위
    private static final int SCAN_PERIOD = 100000;
    // scan : true, stop : false
    private int scanButtonFlag = STATE_STOP;

    // state of connecting gatt server
    private boolean mConnected = false;
    private BluetoothAdapter mBluetoothAdapter;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private BluetoothLeScanner mBLEScanner;
    private Button scanButton;
    private Button disconnectButton;
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "Connect");

                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "disconnect");

                disconnectButton.callOnClick();

                mConnected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "Services Discovered");

                mBluetoothLeService.getSupportedGattServices();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "Data Available");

            }
        }
    };
    private ListView deviceListView;
    private ArrayList<HashMap<String, String>> deviceArrayList;
    private SimpleAdapter deviceListAdapter;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results)
                processResult(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
        }

        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Log.d(TAG, "Scan name : " + result.getDevice().getName() + "\n address : " + result.getDevice().getAddress());

                    HashMap<String, String> item = new HashMap<>();
                    boolean itemFlag = true;

                    // 이름 검색 안된 device
                    if (result.getDevice().getName() == null)
                        item.put("name", "unknown");
                    else
                        item.put("name", result.getDevice().getName());

                    item.put("address", result.getDevice().getAddress());

                    // 이미 검색된 기기(주소)일 경우 추가하지 않음, 이름이 확인될 경우 갱신
                    // contains 메소드로 수정 가능
                    for (HashMap<String, String> i : deviceArrayList)
                        if (Objects.requireNonNull(i.get("address")).equals(item.get("address"))) {
                            i.put("name", item.get("name"));
                            itemFlag = false;
                            break;
                        }

                    if (itemFlag)
                        deviceArrayList.add(item);

                    deviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    //TODO name, address filter
    private EditText searchEditText;

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void setLayout() {
        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanButtonFlag == STATE_STOP) { // scan start
                    startScan();

                    // SCAN_PERIOD 후에 stop scan
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopScan();
                        }
                    }, SCAN_PERIOD);
                } else // scan stop
                    stopScan();
            }
        });

        disconnectButton = findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectButton.setVisibility(View.GONE);
                mBluetoothLeService.disconnect();
                Toast.makeText(BleActivity.this, "disconnect", Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText = findViewById(R.id.search_editText);

        deviceArrayList = new ArrayList<>();

        deviceListAdapter = new SimpleAdapter(this, deviceArrayList, android.R.layout.simple_list_item_2, new String[]{"name", "address"},
            new int[]{android.R.id.text1, android.R.id.text2});

        deviceListView = findViewById(R.id.device_listView);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDeviceAddress = deviceArrayList.get(position).get("address");

                connectDevice(mDeviceAddress);
            }
        });
    }

    private void startScan() {
        scanButtonFlag = STATE_SCAN;
        scanButton.setText(R.string.stop);
        Log.d(TAG, "click scan button");

        deviceArrayList.clear();
        deviceListAdapter.notifyDataSetChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner.startScan(mScanCallback);
        }
    }

    private void stopScan() {
        scanButtonFlag = STATE_STOP;
        scanButton.setText(R.string.scan_button);
        Log.d(TAG, "click stop button");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner.stopScan(mScanCallback);
        }
    }

    private void connectDevice(final String address) {
        Log.d(TAG, "address : " + address);
        if (mBluetoothLeService.connect(address)) {
            Toast.makeText(BleActivity.this, "connect", Toast.LENGTH_SHORT).show();

            disconnectButton.setVisibility(View.VISIBLE);

            Log.d(TAG, "GATT Services" + mBluetoothLeService.getSupportedGattServices());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);


        PackageManager packageManager = getPackageManager();

        // Checks if BLE is supported on the device.
        if (!(packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // bluetooth scan accuracy increase
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "FINE LOCATION");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }


        mBluetoothLeService = new BluetoothLeService();

        // API level 21부터 'BluetoothLeScanner'를 통해 scan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }


        setLayout();

        Intent gattServiceIntent = new Intent(BleActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopScan();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            // 블루투스 실행 요청
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "Bluetooth unactivated");
                    finish();
                } else if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth activate");
                }
                break;
            // GPS 권한 요청
            case REQUEST_ENABLE_GPS:
                if (resultCode == RESULT_CANCELED)
                    Log.d(TAG, "GPS FINE LOCATION PERMISSION DENIED");
                finish();
                break;
        }
    }
}