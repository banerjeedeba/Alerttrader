package com.at.alerttrader;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.at.alerttrader.model.User;
import com.at.alerttrader.util.StringUtils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends BaseActivity {

    TextView userName;
    TextView userMessage;
    Button registerBtn;
    String displayName;
    String status;
    String welcomeText="Welcome";
    boolean doubleBackToExitPressedOnce = false;
    private static final String congratulationMsg = "Congratulations!! \n\nYour request for market alert has been submitted successfully. You will start receiving alerts once Admin approves your request. \n\nPlease contact Admin for further details. ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = findViewById(R.id.UserName);
        userMessage = findViewById(R.id.Message);
        registerBtn = findViewById(R.id.RegisterBtn);

        Intent i = getIntent();
        status = i.getStringExtra("status");
        displayName = i.getStringExtra("displayName");
        if(!StringUtils.isEmpty(displayName)){
            welcomeText += " " + displayName.split(" ")[0] + "!";
        } else {
            welcomeText += "!!";
        }
        updateUI(status);
    }

    private void updateUI(String status){
        switch(status){
            case User.STATUS_EXPIRED:
                userName.setText(welcomeText);
                userMessage.setText("Remain updated with latest market alert new!\n\nPress the Register button below to subscribe for the latest market alerts.");
                registerBtn.setVisibility(View.VISIBLE);
                registerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showProgressDialog();
                        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Users").document(fbuser.getUid()).update("status",User.STATUS_PENDING)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                userName.setText("Greetings!!");
                                                userMessage.setText(congratulationMsg);
                                                registerBtn.setVisibility(View.GONE);
                                                hideProgressDialog();
                                            }
                                        }, 1000);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this,"Something went wrong! Try Again!",Toast.LENGTH_LONG).show();
                                        hideProgressDialog();
                                    }
                                });

                    }
                });
                break;
            default:    //User.STATUS_PENDING
                userName.setText(welcomeText);
                userMessage.setText(congratulationMsg);
                registerBtn.setVisibility(View.GONE);
                break;
        }
    }


    public static Intent createIntent(
            Context context,String status, String displayName) {

        Intent startIntent = new Intent();

        if(status!=null && status.trim().length()>0){
            startIntent.putExtra("status",status);
        }

        if(displayName!=null && displayName.trim().length()>0){
            startIntent.putExtra("displayName",displayName);
        }

        return startIntent.setClass(context, RegisterActivity.class);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            //startActivity(new Intent(MyActivity.this, SignInActivity.class));
                            //finish();
                            //Toast.makeText(MessageListActivity.this, "Signed out!!", Toast.LENGTH_SHORT).show();
                        }
                    });

            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
