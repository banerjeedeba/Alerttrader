package com.at.alerttrader;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SplashActivity";

    TextView splashText;
    SignInButton btnGoogleSignIn;
    Button btnSignOut;
    //private GoogleSignInClient mGoogleSignInClient;
    private String messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE);
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(view);
            }
        });
        btnSignOut = findViewById(R.id.btn_sign_out);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();*/
                //Log.d(TAG, "mGoogleSignInClient: " + (gso.getAccount().get));
        //FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        String message = i.getStringExtra("message");
        Log.d(TAG, "message received: " + message);
        if(message!=null && message.trim().length()>0){
            messageText = message;
            Log.d(TAG, "message set: " + messageText);
        }
    }


    public void signIn(View v) {
        Log.d(TAG, "signIn");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        //Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Log.d(TAG, "signIn Intent"+(signInIntent!=null));
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.d(TAG, "signIn startActivityForResult");
    }

    public void signOut(View v) {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });

    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            btnGoogleSignIn.setVisibility(View.GONE);
            //btnSignOut.setVisibility(View.VISIBLE);
            //TODO wait for few sec navigate to msg activity

            FirebaseMessaging.getInstance().subscribeToTopic("alert");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent i=new Intent(SplashActivity.this,MessageListActivity.class);
                    if(messageText!=null){
                        Log.d(TAG, "message sent to MessageListActivity: " + messageText);
                        i.putExtra("messageText",messageText);
                    }
                    startActivity(i);
                    //finish();
                }
            }, 2000);
        } else {
            btnGoogleSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
        }
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult before super" + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult" + requestCode);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            if(resultCode==RESULT_OK){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    Log.d(TAG, "Google Sign In was successful");
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.d(TAG, "Google sign in failed", e);
                    Toast.makeText(SplashActivity.this, "Google Authentication failed."+e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // [START_EXCLUDE]
                    updateUI(null);
                    // [END_EXCLUDE]
                }
            } else {
                Log.d(TAG, "Result code not ok: "+resultCode);
                Toast.makeText(SplashActivity.this, "Result code not ok: "+resultCode,
                        Toast.LENGTH_SHORT).show();
                // [START_EXCLUDE]
                updateUI(null);
            }

        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SplashActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    // [END auth_with_google]
}
