/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.testApplication;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    // Generic Access
    public static String GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    // Generic Attribute
    public static String GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb";

    // Tx Power
    public static String TX_POWER = "00001804-0000-1000-8000-00805f9b34fb";
    public static String TX_POWER_LEVEL = "00002a07-0000-1000-8000-00805f9b34fb";
    public static String APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String PERIPHERAL_PREFERRED_CONNECTION_PARAMS = "00002a04-0000-1000-8000-00805f9b34fb";

    // Immediate Alert
    public static String IMMEDIATE_ALERT = "00001802-0000-1000-8000-00805f9b34fb";
    public static String ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb";

    // Link Loss
    public static String LINK_LOSS = "00001803-0000-1000-8000-00805f9b34fb";

    // Battery Service
    public static String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    // Heart Rate
    public static String HEART_RATE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";

    // Nordic UART Service
    public static String NORDIC_UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String TX_CHARACTERISTIC = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String RX_CHARACTERISTIC = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    private static HashMap<String, String> attributes = new HashMap<>();

    static {
        // Nordic nRF5x Proximity Services.
        attributes.put(GENERIC_ACCESS, "Generic Access");
        attributes.put(GENERIC_ATTRIBUTE, "Generic Attribute");
        attributes.put(TX_POWER, "Tx Power");
        attributes.put(IMMEDIATE_ALERT, "Immediate Alert");
        attributes.put(LINK_LOSS, "Link Loss");
        attributes.put(NORDIC_UART_SERVICE, "Nordic UART Service");

        attributes.put(BATTERY_SERVICE, "Battery Service");

        // Nordic nRF5x Proximity Characteristics
        attributes.put(DEVICE_NAME, "Device Name");
        attributes.put(APPEARANCE, "Appearance");
        attributes.put(PERIPHERAL_PREFERRED_CONNECTION_PARAMS, "Peripheral Preferred Connection Parameters");
        attributes.put(TX_POWER_LEVEL, "Tx Power Level");
        attributes.put(ALERT_LEVEL, "Alert Level");
        attributes.put(BATTERY_LEVEL, "Battery Level");
        attributes.put(TX_CHARACTERISTIC, "Tx Characteristic");
        attributes.put(RX_CHARACTERISTIC, "Rx Characteristic");



        // Heart Rate Monitor
        attributes.put(HEART_RATE, "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
