package com.ponomarenko.flashlight;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int MAIL_REQUEST = 1110;
    private Camera camera;
    private CameraManager camManager;
    private String cameraId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        } else {
            initializeSwitcherBtn();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share_app:
                shareApp();
                break;

            case R.id.meu_item_contact_us:
                startEmailClient();
                break;
        }
        return true;
    }

    private void startEmailClient() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + TextUtils.join(",", new String[]{getString(R.string.developer_email)})));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        startActivityForResult(Intent.createChooser(intent, "Send message to admin via:"), MAIL_REQUEST);

    }

    private void shareApp() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.url_google_play));
        startActivity(Intent.createChooser(intent, "Share"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initializeSwitcherBtn();

                } else {
                    finish();
                }
            }
        }
    }

    private void initializeSwitcherBtn() {
        boolean hasSystemFeature = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasSystemFeature) {
            Toast.makeText(this, "Flashlight wasn't found", Toast.LENGTH_SHORT).show();
            return;
        }

        CheckBox switcherCheckbox = (CheckBox) findViewById(R.id.switcher_checkbox);
        switcherCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean state) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    switchFlashlightSdkMore23(state);
                } else {
                    turnOnFlashlightForSDKLess23(state);
                }
            }
        });
    }

    private void turnOnFlashlightForSDKLess23(boolean state) {
        if (state) {
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
        } else {
            camera.stopPreview();
            camera.release();
        }

    }

    void switchFlashlightSdkMore23(boolean state) {
        if (camManager == null) {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, state);   //Turn ON
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
