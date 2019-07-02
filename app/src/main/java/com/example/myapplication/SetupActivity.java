package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class SetupActivity extends Activity {

    private EditText etname, etphon,etmail;
    private static final String VCF_DIRECTORY = "/vcf_directory";
    private File vcfFile;

    // Storage Permissions
    int PERMISSION_ALL = 1;
    private static final int REQUEST_PERMISSIONS = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);
        this.initContactForm();
    }

    private void initContactForm() {
        etname = findViewById(R.id.et_name);
        etphon = findViewById(R.id.et_phone);
        etmail = findViewById(R.id.et_email);
        Button btn = findViewById(R.id.save_button);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File vdfdirectory = new File(
                            Environment.getExternalStorageDirectory() + VCF_DIRECTORY);
                    if (!vdfdirectory.exists()) {
                        vdfdirectory.mkdirs();
                        vdfdirectory.createNewFile();
                    }

                    File vcfFile = new File(vdfdirectory, "android_" + Calendar.getInstance().getTimeInMillis() + ".vcf");

                    FileWriter fw = new FileWriter(vcfFile);
                    fw.write("BEGIN:VCARD\r\n");
                    fw.write("VERSION:3.0\r\n");
                    fw.write("FN:" + etname.getText().toString() + "\r\n");
                    fw.write("TEL;TYPE=WORK,VOICE:" + etphon.getText().toString() + "\r\n");
                    fw.write("EMAIL;TYPE=PREF,INTERNET:" + etmail.getText().toString() + "\r\n");
                    fw.write("END:VCARD\r\n");
                    fw.flush();
                    fw.close();

//                    Intent i = new Intent(); //this will import vcf in contact list
//                    i.setAction(android.content.Intent.ACTION_VIEW);
//                    i.setDataAndType(Uri.fromFile(vcfFile), "text/x-vcard");
//                    startActivity(i);
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}

