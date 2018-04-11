package com.at.alerttrader;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.at.alerttrader.model.User;
import com.at.alerttrader.util.DateUtil;
import com.at.alerttrader.util.StringUtils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class AuthActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "AuthActivity";


    View mRootView;
    SignInButton btnGoogleSignIn;
    private String messageText;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in);
        btnGoogleSignIn.setSize(SignInButton.SIZE_WIDE);
        mRootView = findViewById(R.id.root);

        Intent i = getIntent();
        String message = i.getStringExtra("message");
        Log.d(TAG, "message received: " + message);
        /*if(message!=null && message.trim().length()>0){
            messageText = message;
            Log.d(TAG, "message set: " + messageText);
        }*/

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateUserDB(null);
                    /*if(messageText!=null){
                        Log.d(TAG, "message sent to MessageListActivity: " + messageText);
                        i.putExtra("messageText",messageText);
                    }*/

                }
            }, 2000);

        } else {
            btnGoogleSignIn.setVisibility(View.VISIBLE);
        }

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(view);
            }
        });
    }

    public void signIn(View v) {
        showProgressDialog();
        btnGoogleSignIn.setEnabled(false);
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        //.setTheme(R.style.AppTheme)
                        .setLogo(R.drawable.ic_launcher_foreground)
                        .setAvailableProviders(selectedProviders)
                        //.setTosUrl(getSelectedTosUrl())
                        //.setPrivacyPolicyUrl(getSelectedPrivacyPolicyUrl())
                        .setIsSmartLockEnabled(false)
                        .setAllowNewEmailAccounts(true)
                        .build(),
                RC_SIGN_IN);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        hideProgressDialog();
        btnGoogleSignIn.setEnabled(true);
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                updateUserDB(response);
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar("Sign In Cancelled!!");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar("No internet connection!");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar("Unknown error occured!"+resultCode);
                    Log.d(TAG, "result code: "+resultCode + response);
                    return;
                }
            }

        }
    }

    private void showSnackbar(String errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
        if (false){//mGoogleScopeYoutubeData.isChecked()) {
            result.add("https://www.googleapis.com/auth/youtube.readonly");
        }
        if (false){//mGoogleScopeDriveFile.isChecked()) {
            result.add(Scopes.DRIVE_FILE);
        }
        return result;
    }

    private void updateUserDB(final IdpResponse response){
        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        //Log.d(TAG, "user: "+ fbuser.getUid() );
        db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .whereEqualTo("id", fbuser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size()>0){
                                //User already exist
                               // Log.d(TAG, "task size: "+ task.getResult().size() );
                                for(QueryDocumentSnapshot document : task.getResult()) {
                                    user = document.toObject(User.class);
                                    DocumentReference userRef = db.collection("Users").document(fbuser.getUid());
                                    userRef.update("lastLogin", DateUtil.dateToString(new Date(),null));

                                    switch (user.getStatus()){
                                        case User.STATUS_APPROVED:
                                            //Approved users
                                            String expDate = user.getValidTill();
                                            boolean expired = false;
                                            if(StringUtils.isEmpty(expDate)){
                                                expired = true;
                                            }
                                            try {
                                                Date expiryDate = DateUtil.stringToDate(expDate,null);
                                                Calendar expiryCalendar = new GregorianCalendar();
                                                expiryCalendar.setTime(expiryDate);
                                                if(expiryCalendar.before(new GregorianCalendar())){
                                                    expired = true;
                                                }
                                            } catch (ParseException e) {
                                                expired = true;
                                            }
                                            if(expired){
                                                FirebaseMessaging.getInstance().unsubscribeFromTopic("marketalert");
                                                userRef.update("status",User.STATUS_EXPIRED);
                                                startActivity(RegisterActivity.createIntent(AuthActivity.this,User.STATUS_EXPIRED,user.getDisplayName()));
                                                finish();
                                                break;
                                            }
                                            FirebaseMessaging.getInstance().subscribeToTopic("marketalert");
                                            if("alerttrader17@gmail.com".equalsIgnoreCase(user.getEmail())) isAdmin=true;
                                            startActivity(MessageListActivity.createIntent(AuthActivity.this, response,messageText,isAdmin));
                                            finish();
                                            break;
                                        case User.STATUS_EXPIRED:
                                            //Register page
                                            startActivity(RegisterActivity.createIntent(AuthActivity.this,user.getStatus(),user.getDisplayName()));
                                            finish();
                                            break;
                                        case User.STATUS_PENDING:
                                            //Pending approval page
                                            startActivity(RegisterActivity.createIntent(AuthActivity.this,user.getStatus(),user.getDisplayName()));
                                            finish();
                                            break;
                                    }

                                }
                            } else {
                                //Insert new User
                                User user = new User();
                                user.setEmail(fbuser.getEmail());
                                user.setDisplayName(fbuser.getDisplayName());
                                user.setId(fbuser.getUid());
                                user.setLastLogin(DateUtil.dateToString(new Date(),null));
                                user.setStatus(User.STATUS_EXPIRED);
                                db.collection("Users").document(fbuser.getUid()).set(user);
                                Log.d(TAG, "user: "+fbuser.getUid()+" email: "+fbuser.getEmail()+" name: "+fbuser.getDisplayName());
                                //Register page
                                startActivity(RegisterActivity.createIntent(AuthActivity.this,user.getStatus(),user.getDisplayName()));
                                finish();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
