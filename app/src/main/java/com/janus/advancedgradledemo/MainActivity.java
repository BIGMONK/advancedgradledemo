package com.janus.advancedgradledemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switch (BuildConfig.OEM) {
            case "OEA":

                break;

            case "OEB":
                break;
        }
        Log.d(TAG, "onCreate: "+BuildConfig.TTT);
    }
}
