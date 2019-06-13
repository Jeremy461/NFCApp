package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.*;

import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class SenderActivity extends AppCompatActivity implements OutcomingNfcManager.NfcActivity {

    private TextView tvOutcomingMessage;
    private EditText etOutcomingMessage;
    private Button btnSetOutcomingMessage;

    private NfcAdapter nfcAdapter;
    private OutcomingNfcManager outcomingNfccallback;

    private PackageManager packageManager;

    private TwitterLoginButton loginButton;

    private String username;
    private TwitterSession session;
    private long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);

        setContentView(R.layout.activity_sender);

        packageManager = this.getPackageManager();
        if (!isNfcSupported()) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT).show();
        }

        if (isPackageInstalled("com.linkedin.android", packageManager)) {
            TextView textView = findViewById(R.id.linkedin);
            textView.setText("LinkedIn is installed");
        }

        if (isPackageInstalled("com.twitter.android", packageManager)) {
            TextView textView = findViewById(R.id.twitter);
            textView.setText("Twitter is installed");
        }

        initViews();

        // encapsulate sending logic in a separate class
        this.outcomingNfccallback = new OutcomingNfcManager(this);
        this.nfcAdapter.setOnNdefPushCompleteCallback(outcomingNfccallback, this);
        this.nfcAdapter.setNdefPushMessageCallback(outcomingNfccallback, this);

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;
                userID = result.data.getId();
                login(session);
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
                Toast.makeText(SenderActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login(TwitterSession session) {
        username = session.getUserName();
        TextView textView = findViewById(R.id.twitterLoggedIn);
        textView.setText(username + " is now logged in on Twitter");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loginButton.onActivityResult(requestCode, resultCode, data);
    }


    private void initViews() {
        this.tvOutcomingMessage = findViewById(R.id.linkedin);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private boolean isNfcSupported() {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        return this.nfcAdapter != null;
    }

    @Override
    public String getOutcomingMessage() {
        return String.valueOf(userID);
    }

    @Override
    public void signalResult() {
        // this will be triggered when NFC message is sent to a device.
        // should be triggered on UI thread. We specify it explicitly
        // cause onNdefPushComplete is called from the Binder thread
        runOnUiThread(() ->
                Toast.makeText(SenderActivity.this, R.string.message_beaming_complete, Toast.LENGTH_SHORT).show());
    }

    //Check if package is installed on device
    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {

        boolean found = true;

        try {
            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            found = false;
        }

        return found;
    }
}
