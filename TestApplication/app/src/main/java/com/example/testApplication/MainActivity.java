package com.example.testApplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button motorButton, touchButton, LEDButton, buzzerButton, BLEButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        motorButton = findViewById(R.id.motor_button);
        touchButton = findViewById(R.id.touch_button);
        LEDButton = findViewById(R.id.LED_button);
        buzzerButton = findViewById(R.id.buzzer_button);
        BLEButton = findViewById(R.id.BLE_button);

        motorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MotorActivity.class));
            }
        });
        touchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TouchActivity.class));
            }
        });
        LEDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LEDActivity.class));
            }
        });
        buzzerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), BuzzerActivity.class));
            }
        });
        BLEButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), BleActivity.class));
            }
        });


    }
}
