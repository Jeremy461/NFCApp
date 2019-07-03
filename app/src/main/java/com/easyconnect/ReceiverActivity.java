package com.easyconnect;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.models.User;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;


public class ReceiverActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private NfcAdapter nfcAdapter;
    private TwitterSession session;
    private static final String VCF_DIRECTORY = "/vcf_directory";

    private String name, phone, email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        if (!isNfcSupported()) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_LONG).show();
            finish();
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }

        session = TwitterCore.getInstance().getSessionManager().getActiveSession();
    }

    // need to check NfcAdapter for nullability. Null means no NFC support on the device
    private boolean isNfcSupported() {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        return this.nfcAdapter != null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // also reading NFC message from here in case this activity is already started in order
        // not to start another instance of this activity
        receiveMessageFromDevice(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // foreground dispatch should be enabled here, as onResume is the guaranteed place where app
        // is in the foreground
        enableForegroundDispatch(this, this.nfcAdapter);
        receiveMessageFromDevice(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch(this, this.nfcAdapter);
    }

    private void receiveMessageFromDevice(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage inNdefMessage = (NdefMessage) parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord ndefRecord_0 = inNdefRecords[0];

            String inMessage = new String(ndefRecord_0.getPayload());

            String[] parts = inMessage.split("-");

            name = parts[0];
            phone = parts[1];
            email = parts[2];
            if (session != null ) {
                loadTwitterApi(Long.parseLong(parts[3]));
            }
            Log.d("LOG", name + phone + email + parts[0]);
            createVcard();
        }
    }

    private void createVcard() {
        try {
            File vdfdirectory = new File(
                    Environment.getExternalStorageDirectory() + VCF_DIRECTORY);
            if (!vdfdirectory.exists()) {
                vdfdirectory.mkdirs();
                vdfdirectory.createNewFile();
            }

            String filename = "android_" + Calendar.getInstance().getTimeInMillis() + ".vcf";
            File vcfFile = new File(vdfdirectory, filename);

            FileWriter fw = new FileWriter(vcfFile);
            fw.write("BEGIN:VCARD\r\n");
            fw.write("VERSION:3.0\r\n");
            fw.write("FN:" + name + "\r\n");
            fw.write("TEL;TYPE=WORK,VOICE:" + phone + "\r\n");
            fw.write("EMAIL:" + email + "\r\n");
            fw.write("END:VCARD\r\n");
            fw.flush();
            fw.close();

            Intent i = new Intent(); //this will import vcf in contact list
            i.setAction(android.content.Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(vcfFile), "text/x-vcard");
            startActivity(i);
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }
    }

    private void loadTwitterApi(long userID) {
        // Call the MyTwitterApiClient for friendships/create session new
        new MyTwitterApiClient(session).postCustomService().create(userID)
                .enqueue(new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        String username = result.data.name;
                        Toast.makeText(ReceiverActivity.this, "Now following " + username, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        Toast.makeText(ReceiverActivity.this, "Failed to add user as friend", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void enableForegroundDispatch(AppCompatActivity activity, NfcAdapter adapter) {

        // here we are setting up receiving activity for a foreground dispatch
        // thus if activity is already started it will take precedence over any other activity or app
        // with the same intent filters


        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException ex) {
            throw new RuntimeException("Check your MIME type");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public void disableForegroundDispatch(final AppCompatActivity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
}
