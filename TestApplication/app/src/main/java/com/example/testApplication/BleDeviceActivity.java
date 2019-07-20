package com.example.testApplication;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleDeviceActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private static final String TAG = BleDeviceActivity.class.getSimpleName();

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    //    private final String LIST_VALUE = "VALUE";
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
    private TextView deviceAddressTextView, connectionStateTextView;
    private Button connectionButton;
    private ExpandableListView servicesListView;
    // state of connecting gatt server
    private boolean mConnected = false;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName = "null", mDeviceAddress;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
        new ArrayList<>();
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
                updateConnectionState(R.string.title_connect);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "disconnect");
                mConnected = false;
                updateConnectionState(R.string.title_disconnect);
                connectionButton.setText(R.string.title_connect);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "Services Discovered");

                List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

                if (gattServices == null) return;

                String uuid;
                String unknownServiceString = getResources().getString(R.string.unknown_service);
                String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
                ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
                ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                    = new ArrayList<>();
                mGattCharacteristics = new ArrayList<>();

                Log.d(TAG, "gattServices\n" + gattServices);
                for (BluetoothGattService gattService : gattServices) {
                    HashMap<String, String> currentServiceData = new HashMap<>();
                    uuid = gattService.getUuid().toString();
                    currentServiceData.put(
                        LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
                    currentServiceData.put(LIST_UUID, uuid);
                    gattServiceData.add(currentServiceData);

                    ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                        new ArrayList<>();
                    List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                    ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<>();

                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                        mBluetoothLeService.readCharacteristic(gattCharacteristic);

                    Log.d(TAG, "gattCharacteristics" + gattCharacteristics);
                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        charas.add(gattCharacteristic);
                        HashMap<String, String> currentCharaData = new HashMap<>();
                        uuid = gattCharacteristic.getUuid().toString();
                        currentCharaData.put(
                            LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));

                        currentCharaData.put(LIST_UUID, uuid);
                        gattCharacteristicGroupData.add(currentCharaData);
                    }
                    mGattCharacteristics.add(charas);
                    gattCharacteristicData.add(gattCharacteristicGroupData);
                }
                Log.d(TAG, "gattServiceData\n" + gattServiceData);
                Log.d(TAG, "gattCharacteristicData\n" + gattCharacteristicData);

                // TODO groupView 클릭시 다른 group childView 내용이 변경되는 문제
                DeviceExpandableListAdapter gattServiceAdapter = new DeviceExpandableListAdapter(BleDeviceActivity.this, gattServiceData, gattCharacteristicData);
                servicesListView.setAdapter(gattServiceAdapter);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "Data Available");
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void setLayout() {
        deviceAddressTextView = findViewById(R.id.deviceAddress_textView);
        deviceAddressTextView.setText(getString(R.string.label_device_address).concat(mDeviceAddress));
        connectionStateTextView = findViewById(R.id.connectionState_textView);
        connectionButton = findViewById(R.id.connection_button);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnected) {
                    mBluetoothLeService.disconnect();
                    connectionStateTextView.setText(R.string.title_disconnect);
                    connectionButton.setText(R.string.title_connect);
                } else {
                    mBluetoothLeService.connect(mDeviceAddress);
                    connectionStateTextView.setText(R.string.title_connecting);
                    connectionButton.setText(R.string.title_disconnect);
                }
            }
        });

        servicesListView = findViewById(R.id.services_listView);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionStateTextView.setText(resourceId);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        getSupportActionBar().setTitle(mDeviceName);

        setLayout();

        Intent gattServiceIntent = new Intent(BleDeviceActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}
