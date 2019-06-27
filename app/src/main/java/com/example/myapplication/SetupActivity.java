package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

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
                try {
                    File vdfdirectory = new File(
                            Environment.getExternalStorageDirectory() + VCF_DIRECTORY);
                    if (!vdfdirectory.exists()) {
                        vdfdirectory.mkdirs();
                        vdfdirectory.createNewFile();
                    }

                    Log.d("LOG", vdfdirectory.getAbsolutePath());

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

                    Toast.makeText(SetupActivity.this, "Created!", Toast.LENGTH_SHORT).show();

//                    Intent i = new Intent(); //this will import vcf in contact list
//                    i.setAction(android.content.Intent.ACTION_VIEW);
//                    i.setDataAndType(Uri.fromFile(vcfFile), "text/x-vcard");
//                    startActivity(i);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
