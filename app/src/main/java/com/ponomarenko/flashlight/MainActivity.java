package com.ponomarenko.flashlight;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {

    private CameraManager camManager;
    private CheckBox switcherCheckbox;
    private String cameraId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("yes", "yes");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    }
                    boolean hasSystemFeature = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
                    if (hasSystemFeature) {
                        initializeSwitcherBtn();
                    }

                } else {
                    Log.d("yes", "no");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initializeSwitcherBtn() {
        switcherCheckbox = (CheckBox) findViewById(R.id.switcher_checkbox);
        switcherCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    turnOnFlashlight();
                } else {
                    turnOffFlashlight();
                }
            }
        });
    }

    void turnOnFlashlight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, true);   //Turn ON
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    void turnOffFlashlight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                camManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }
}
