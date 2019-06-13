package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class MainActivity extends Activity {

    private TwitterLoginButton twitterLoginButton;
    private ImageView linkedinLoginButton;
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
        if (isPackageInstalled("com.linkedin.android", packageManager)) {
            linkedinLoginButton = findViewById(R.id.linkedin_login_button);
            linkedinLoginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    LISessionManager.getInstance(getApplicationContext()).init(MainActivity.this, buildScope(), new AuthListener() {
                        @Override
                        public void onAuthSuccess() {
                            Toast.makeText(MainActivity.this, "Linkedin login successfull", Toast.LENGTH_LONG).show();
                            String accessToken = LISessionManager.getInstance(getApplicationContext()).getSession().getAccessToken().toString();
                        }

                        @Override
                        public void onAuthError(LIAuthError error) {
                            Log.e("Error", error.toString());
                        }
                    }, true);
                }
            });
            linkedinLoginButton.setVisibility(View.VISIBLE);
        }

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
        username = session.getUserName();

        Toast.makeText(MainActivity.this, "Logged in to Twitter", Toast.LENGTH_LONG).show();
        twitterLoginButton.setVisibility(View.INVISIBLE);
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
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);

    }

    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE);
    }
}
