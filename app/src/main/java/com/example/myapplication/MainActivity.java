package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class MainActivity extends Activity {

    private TwitterLoginButton twitterLoginButton;
    private TextView twitterLoggedIn;
    private PackageManager packageManager;

    private String username;
    private TwitterSession session;
    private long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Twitter.initialize(this);

        setContentView(R.layout.activity_main);

        //Check installed applications
        packageManager = this.getPackageManager();
        if (isPackageInstalled("com.twitter.android", packageManager)) {
            twitterLoginButton = findViewById(R.id.twitter_login_button);
            twitterLoginButton.setVisibility(View.VISIBLE);

            //Handle Twitter login button
            twitterLoginButton.setCallback(new Callback<TwitterSession>() {
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
                    Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Handle SenderActivity button click
        ImageView img = findViewById(R.id.StartConnecting);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SenderActivity.class);
                intent.putExtra("UserID", userID);
                startActivity(intent);
            }
        });
    }

    private void login(TwitterSession session) {
        Toast.makeText(MainActivity.this, "Logged in to Twitter", Toast.LENGTH_LONG).show();
        twitterLoginButton.setVisibility(View.INVISIBLE);
        twitterLoggedIn = findViewById(R.id.twitter_logged_in);
        twitterLoggedIn.setVisibility(View.VISIBLE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

}
