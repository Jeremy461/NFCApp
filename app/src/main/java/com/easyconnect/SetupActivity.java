package com.easyconnect;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class SetupActivity extends Activity {

    private EditText etname, etphon,etmail;
    private Button btn;
    private static final String VCF_DIRECTORY = "/vcf_directory";
    private File vcfFile;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        etname = findViewById(R.id.et_name);
        etphon = findViewById(R.id.et_phone);
        etmail = findViewById(R.id.et_email);
        btn = findViewById(R.id.save_button);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        verifyStoragePermissions(SetupActivity.this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("My prefs", MODE_PRIVATE).edit();

                String name = etname.getText().toString();
                String phone = etphon.getText().toString();
                String email = etmail.getText().toString();
                editor.putString("name", name);
                editor.putString("phone", phone);
                editor.putString("email", email);
                editor.apply();

                Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("phone", phone);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        SharedPreferences prefs = getSharedPreferences("My prefs", MODE_PRIVATE);
        String name = prefs.getString("name", null);
        if (name != null) {
            etname.setText(name);
            etphon.setText(prefs.getString("phone", null));
            etmail.setText(prefs.getString("email", null));
            btn.performClick();
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
