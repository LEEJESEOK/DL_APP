package com.example.testApplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BleScanActivity extends AppCompatActivity {
    private static final String TAG = BleScanActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_GPS = 2;

    private static final boolean SCAN_STATE_STOP = false;
    private static final boolean SCAN_STATE_RUNNING = true;

    // ms 단위
    private static final int SCAN_PERIOD = 100000;
    // scan : true, stop : false
    private boolean mScanning = SCAN_STATE_STOP;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBLEScanner;
    private Button scanButton;
    //    private Button disconnectButton;
    //TODO name, address filter
    private EditText searchEditText;
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


    private void setLayout() {
        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning == SCAN_STATE_STOP) { // scan start
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

//        disconnectButton = findViewById(R.id.disconnect_button);
//        disconnectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                disconnectButton.setVisibility(View.GONE);
//                mBluetoothLeService.disconnect();
//                Toast.makeText(BleScanActivity.this, "disconnect", Toast.LENGTH_SHORT).show();
//            }
//        });

        //TODO name, address filter
        searchEditText = findViewById(R.id.search_editText);

        deviceArrayList = new ArrayList<>();
        deviceListAdapter = new SimpleAdapter(this, deviceArrayList, android.R.layout.simple_list_item_2, new String[]{"name", "address"},
                new int[]{android.R.id.text1, android.R.id.text2});
        deviceListView = findViewById(R.id.device_listView);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mDeviceAddress = deviceArrayList.get(position).get("address");
//                connectDevice(mDeviceAddress);
                String name, address;

                name = deviceArrayList.get(position).get("name");
                address = deviceArrayList.get(position).get("address");

                final Intent intent = new Intent(BleScanActivity.this, BleDeviceActivity.class);
                intent.putExtra(BleDeviceActivity.EXTRAS_DEVICE_NAME, name);
                intent.putExtra(BleDeviceActivity.EXTRAS_DEVICE_ADDRESS, address);
                if (mScanning)
                    stopScan();
                startActivity(intent);
            }
        });
    }

    private void startScan() {
        mScanning = SCAN_STATE_RUNNING;
        scanButton.setText(R.string.stop);
        Log.d(TAG, "click scan button");

        deviceArrayList.clear();
        deviceListAdapter.notifyDataSetChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner.startScan(mScanCallback);
        }
    }

    private void stopScan() {
        mScanning = SCAN_STATE_STOP;
        scanButton.setText(R.string.scan_button);
        Log.d(TAG, "click stop button");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner.stopScan(mScanCallback);
        }
    }

//    private void connectDevice(final String address) {
//        Log.d(TAG, "address : " + address);
//        if (mBluetoothLeService.connect(address)) {
//            Toast.makeText(BleScanActivity.this, "connect", Toast.LENGTH_SHORT).show();
//
//            disconnectButton.setVisibility(View.VISIBLE);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);


        // Checks if BLE is supported on the device.
        if (!(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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

        // API level 21부터 'BluetoothLeScanner'를 통해 scan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        setLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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