package com.at.alerttrader;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.at.alerttrader.adapter.MessageAdapter;
import com.at.alerttrader.adapter.UserAdapter;
import com.at.alerttrader.model.MessageModel;
import com.at.alerttrader.model.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ApproveActivity extends BaseActivity  {

    ArrayList<User> models=new ArrayList<>();
    private boolean noMessage;
    private UserAdapter adapter;
    private RecyclerView mUserRecycler;
    private static final String TAG = "ApproveActivity";

    public static Intent createIntent(
            Context context) {

        Intent startIntent = new Intent();

        return startIntent.setClass(context, ApproveActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve);

        mUserRecycler = findViewById(R.id.reyclerview_user_list);
        mUserRecycler.setPadding(0,0,0,0);

        models.add(new User("","","Loading..","","",""));
        noMessage = true;
        adapter = new UserAdapter(this, models);
        mUserRecycler.setAdapter(adapter);
        mUserRecycler.setLayoutManager(new LinearLayoutManager(this));
        mUserRecycler.scrollToPosition(models.size()-1);
        Log.d(TAG,mUserRecycler.toString());
        db = FirebaseFirestore.getInstance();

        db.collection("Users")
        .whereEqualTo("status", User.STATUS_PENDING)
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                models.remove(0);
                mUserRecycler.removeViewAt(0);
                adapter.notifyItemRemoved(0);
                adapter.notifyItemRangeChanged(0, models.size());
                adapter.notifyDataSetChanged();
                if (task.isSuccessful()) {
                    Log.d(TAG,"success");
                    if (task.getResult().size() > 0) {
                        Log.d(TAG,"result");
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            Log.d(TAG,"user"+user.getDisplayName());
                            models.add(user);
                        }

                    }else {
                        models.add(new User("","","No pending approvals!!","","",""));
                        noMessage = true;
                    }
                }else {
                    models.add(new User("","","An error occurred while fetching users!!","","",""));
                    noMessage = true;
                }
                //adapter = new MessageAdapter(MessageListActivity.this, models);
                //mMessageRecycler.setAdapter(adapter);
                //mMessageRecycler.setLayoutManager(new LinearLayoutManager(MessageListActivity.this));
                adapter.notifyItemInserted(0);
                mUserRecycler.scrollToPosition(models.size()-1);
            }
        });



    }

    @Override
    public void onBackPressed() {
        startActivity(MessageListActivity.createIntent(ApproveActivity.this, null,null,true));
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
