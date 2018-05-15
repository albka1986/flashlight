package com.ponomarenko.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int MAIL_REQUEST = 1110;
    public static final int PERMISSION_REQUEST = 1;
    private Camera camera;
    private CameraManager camManager;
    private String cameraId;
    private CheckBox switcherCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switcherCheckbox = findViewById(R.id.switcher_checkbox);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST);
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

            case R.id.exit_btn:
                closeApplication();
                break;

            default:
                //do nothing
                break;
        }
        return true;
    }

    private void closeApplication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.finishAndRemoveTask();
        } else {
            this.finishAffinity();
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                initializeSwitcherBtn();

            } else {
                finish();
            }
        }
    }

    private void initializeSwitcherBtn() {
        boolean hasSystemFeature = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasSystemFeature) {
            Toast.makeText(this, "Flashlight wasn't found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && camManager == null) {
            camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (cameraId == null) {
                try {
                    if (camManager != null) {
                        cameraId = camManager.getCameraIdList()[0];
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        switcherCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean state) {

                turnFlashLight(state);
            }
        });
    }

    private void turnFlashLight(boolean state) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            turnOnAfterSdk23(state);
        } else {
            turnOnLessSdk23(state);
        }
    }

    private void turnOnLessSdk23(boolean state) {
        if (state) {
            try {
                camera = Camera.open();
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "turnOnLessSdk23: ", e);
            }
        } else {
            camera.stopPreview();
            camera.release();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void turnOnAfterSdk23(boolean state) {
        try {
            camManager.setTorchMode(cameraId, state);   //Turn ON-OFF
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
