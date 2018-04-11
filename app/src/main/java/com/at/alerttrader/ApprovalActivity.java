package com.at.alerttrader;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.at.alerttrader.model.User;
import com.at.alerttrader.util.DateUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ApprovalActivity extends BaseActivity {

    TextView approvalText;
    NumberPicker monthPicker;
    Button approvalBtn;

    String id;
    String name;
    String email;

    public static Intent createIntent(Context context,
                                      String id, String name, String email) {

        Intent startIntent = new Intent();
        if(id!=null && id.trim().length()>0){
            startIntent.putExtra("id",id);
        }

        if(name!=null && name.trim().length()>0){
            startIntent.putExtra("name",name);
        }

        if(email!=null && email.trim().length()>0){
            startIntent.putExtra("email",email);
        }
        return startIntent.setClass(context, ApprovalActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);

        approvalText = findViewById(R.id.approveTextView);
        monthPicker = findViewById(R.id.monthPicker);
        approvalBtn = findViewById(R.id.approveBtn);

        Intent i = getIntent();
        id = i.getStringExtra("id");
        name = i.getStringExtra("name");
        email = i.getStringExtra("email");

        approvalText.setText("Please select the number of month you want "+name+ " ("+email+") to be active.");

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(100);

        approvalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    int months = monthPicker.getValue();
                    Calendar currentDate = new GregorianCalendar();
                    currentDate.set(Calendar.HOUR,0);
                    currentDate.set(Calendar.MINUTE,0);
                    currentDate.set(Calendar.SECOND,0);
                    currentDate.add(Calendar.MONTH,months);
                    String expiryDate = DateUtil.dateToString(currentDate.getTime(),null);
                    showProgressDialog();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Users").document(id).update("status", User.STATUS_APPROVED,"validTill",expiryDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    ApprovalActivity.this.startActivity(ApproveActivity.createIntent(ApprovalActivity.this));
                                    finish();
                                    hideProgressDialog();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ApprovalActivity.this,"Something went wrong! Try Again!",Toast.LENGTH_LONG).show();
                                    hideProgressDialog();
                                }
                            });

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(ApproveActivity.createIntent(ApprovalActivity.this));
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
