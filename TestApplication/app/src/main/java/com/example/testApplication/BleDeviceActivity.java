package com.example.testApplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;

public class BleDeviceActivity extends AppCompatActivity {
    private static final String TAG = BleDeviceActivity.class.getSimpleName();

    private ArrayList<String> services;
    private ArrayList<String> characteristics;

    HashMap<String, String> service, characteristic;

    private ExpandableListView servicesListView;

    private void setLayout() {
        servicesListView = findViewById(R.id.expandableListView);
        servicesListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });
        servicesListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });
        servicesListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {

            }
        });
        servicesListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device);

        services = new ArrayList<>();
        characteristics = new ArrayList<>();

        setLayout();

        // connect gatt

        // get services

        // get characteristics
    }
}
