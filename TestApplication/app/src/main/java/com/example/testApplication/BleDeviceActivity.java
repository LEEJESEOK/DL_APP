package com.example.testApplication;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BleDeviceActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_CHARACTERISTIC_TYPE = "CHARACTERISTIC_TYPE";

    private static final boolean STATE_STOP = false;
    private static final boolean STATE_RUNNING = true;

    private static final String TAG = BleDeviceActivity.class.getSimpleName();

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private SimpleDateFormat currentDateFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat elapsedDateFormat = new SimpleDateFormat("SSS");
    private TextView deviceAddressTextView, connectionStateTextView;
    private Button connectionButton;
    private TextView stateTextView;
    private Button readDataButton;
    private TextView logTextView;
    private Button clearButton;
    private EditText messageEditText;
    private Button sendButton;
    // state of connecting gatt server
    private String mDeviceName = "null", mDeviceAddress = "null";
    private boolean mConnected = false;
    private boolean mReading = STATE_RUNNING;
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
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
        new ArrayList<>();
    private Date date;
    private String formatDate;

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

            if (action.equals(BluetoothLeService.ACTION_GATT_CONNECTED)) {
                Log.d(TAG, "Connect");
                mConnected = true;
                updateConnectionState(R.string.title_connect);


            } else if (action.equals(BluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                Log.d(TAG, "disconnect");
                mConnected = false;
                updateConnectionState(R.string.title_disconnect);
                connectionButton.setText(R.string.title_connect);
                if (mReading) {
                    readDataButton.callOnClick();
                }

            } else if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {
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
                logMessage("Service Discovered");

                if (!mBluetoothLeService.setHeartRateNotification(mReading)) {
                    Log.e(TAG, "Failed to setHeartRateNotification");
                    mReading = STATE_STOP;
                } else
                    Log.d(TAG, "Succeed to setHeartRateNotification");
            } else if (action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)) {
                Log.d(TAG, "Data Available");

                final String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, "data : " + data);

                if (intent.hasExtra(EXTRAS_CHARACTERISTIC_TYPE) && intent.getStringExtra(EXTRAS_CHARACTERISTIC_TYPE).equals(GattAttributes.HEART_RATE_MEASUREMENT))
                    logMessage("[Rx]Heart Rate : " + data);
                else if (intent.hasExtra(EXTRAS_CHARACTERISTIC_TYPE) && intent.getStringExtra(EXTRAS_CHARACTERISTIC_TYPE).equals(GattAttributes.DEVICE_NAME))
                    logMessage("[Rx]Device Name : " + data);
                else
                    logMessage("[Rx]Unknown Data : " + data);
            } else if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_HRM)) {
                //TODO Heart Rate service Not found / readDataButton event
                Log.w(TAG, "DEVICE_DOES_NOT_SUPPORT_HRM");
                Toast.makeText(BleDeviceActivity.this, "DEVICE_DOES_NOT_SUPPORT_HRM", Toast.LENGTH_SHORT).show();
                logMessage("Can not connected Heart rate monitor");


                readDataButton.callOnClick();
            } else if (action.equals(BluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                Log.w(TAG, "DEVICE_DOES_NOT_SUPPORT_UART");
                Toast.makeText(BleDeviceActivity.this, "DEVICE_DOES_NOT_SUPPORT_UART", Toast.LENGTH_SHORT).show();
                logMessage("Can not connected Nordic UART Service");
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

        stateTextView = findViewById(R.id.readState_textView);

        readDataButton = findViewById(R.id.readData_button);
        readDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mReading) {
                    mReading = STATE_STOP;
                    readDataButton.setText(R.string.read_data);
                    stateTextView.setText(R.string.stop);
                } else {
                    if (!mConnected) {
                        Log.d(TAG, "" + getString(R.string.title_disconnect));
                        Toast.makeText(BleDeviceActivity.this,
                            getString(R.string.title_disconnect), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mReading = STATE_RUNNING;
                    readDataButton.setText(R.string.stop);
                    stateTextView.setText(R.string.reading);
                }
                mBluetoothLeService.setHeartRateNotification(mReading);
            }
        });

        messageEditText = findViewById(R.id.message_editText);

        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String message = messageEditText.getText().toString();
                    byte[] value;
                    value = message.getBytes("UTF-8");
                    mBluetoothLeService.writeRXCharacteristic(value);
                    messageEditText.setText("");
                    logMessage("[Tx]message : " + message);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        logTextView = findViewById(R.id.log_textView);
//        logTextView.setMovementMethod(new ScrollingMovementMethod());

        clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logTextView.setText("");
                Toast.makeText(BleDeviceActivity.this, "clear log", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionStateTextView.setText(resourceId);
                logMessage("" + getString(resourceId));
            }
        });
    }

    private void logMessage(String msg) {
        long currentTime = System.currentTimeMillis();
        date = new Date(currentTime);
        formatDate = currentDateFormat.format(date);
        logTextView.append("[" + formatDate + "] / " + msg + "\n");
        //TODO log 스크롤
//        final int scrollAmount = logTextView.getLayout().getLineTop(logTextView.getLineCount()) - logTextView.getHeight();
//
//        Log.d(TAG, "logMessage : logTextView.getLayout().getLineTop(logTextView.getLineCount())" + logTextView.getLayout().getLineTop(logTextView.getLineCount()));
//        Log.d(TAG, "logMessage : logTextView.getHeight()" + logTextView.getHeight());
//        Log.d(TAG, "logMessage : scrollAmount" + scrollAmount);
//
//        if (scrollAmount > 0)
//            logTextView.scrollTo(0, scrollAmount);
    }

    private void logElapsed(long elapsed) {
        date = new Date(elapsed);
        formatDate = elapsedDateFormat.format(date);
        logTextView.append("(" + formatDate + "ms)\n");
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
