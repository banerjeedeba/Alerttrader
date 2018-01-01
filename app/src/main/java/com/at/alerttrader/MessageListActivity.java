package com.at.alerttrader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.at.alerttrader.adapter.MessageAdapter;
import com.at.alerttrader.db.DBHelper;
import com.at.alerttrader.model.MessageModel;
import com.at.alerttrader.service.MyFirebaseMessagingService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MessageListActivity extends BaseActivity {

    //private ListView listView;
    private ArrayList<MessageModel> models;
    private MessageAdapter adapter;
    private RecyclerView mMessageRecycler;
    private EditText message_text;
    private Button send_button;
    private LinearLayout inputLayout;
    private Context context;
    private boolean noMessage;
    private static final int REFRESH_INTERVAL = 5;
    private static final String TAG = "MessageListActivity";
    boolean doubleBackToExitPressedOnce = false;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        setContentView(R.layout.activity_message_list);
        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        message_text = findViewById(R.id.message_text);
        send_button = findViewById(R.id.button_chatbox_send);
        inputLayout = findViewById(R.id.layout_chatbox);


        message_text.setVisibility(View.GONE);
        send_button.setVisibility(View.GONE);
        inputLayout.setVisibility(View.GONE);
        mMessageRecycler.setPadding(0,0,0,0);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d(TAG, "auth: " + (mAuth!=null));
        Log.d(TAG, "mGoogleSignInClient: " + (mGoogleSignInClient!=null));


        Intent i = getIntent();
        String msg = i.getStringExtra("messageText");
        Log.d(TAG, "message received in MessageListActivity: " + msg);
        if(msg!=null){
            saveMessage(msg,false);
        }
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        //Get the application next refresh time
        long refreshTime = ((ApplicationConstant)this.getApplication()).getNextUpdate();
        //Toast.makeText(context,"stored time: "+refreshTime,Toast.LENGTH_LONG).show();
        GregorianCalendar currentTime = new GregorianCalendar();
        //refreshTimeCal.add(Calendar.MINUTE,5);
        if(currentTime.getTimeInMillis()>=refreshTime){
            if(refreshTime != 0){
                currentTime.add(Calendar.MINUTE,(REFRESH_INTERVAL*(-1)));
                //Toast.makeText(context,"delete: ",Toast.LENGTH_LONG).show();
                //delete old messages
                dbHelper.deleteMessage(db,currentTime.getTimeInMillis());
                currentTime.add(Calendar.MINUTE,(REFRESH_INTERVAL*2));
            } else {
                currentTime.add(Calendar.MINUTE,REFRESH_INTERVAL);
            }

            refreshTime = currentTime.getTimeInMillis();
            ((ApplicationConstant)this.getApplication()).setNextUpdate(refreshTime);
            //Toast.makeText(context,"refresh time: "+refreshTime,Toast.LENGTH_LONG).show();
        }

        //FirebaseApp.initializeApp(this);


        setupUI(findViewById(R.id.layout_chatbox));
        Log.d("Alert Trader","message list Activity 3");
        //listView = (ListView) findViewById(R.id.msg_list);


        models = new ArrayList<MessageModel>();


        Cursor messages = dbHelper.getMessages(db);
        if(messages.moveToFirst()){
            do{
                String message = messages.getString(messages.getColumnIndex("message"));
                String messageTime = messages.getString(messages.getColumnIndex("message_time"));

                models.add(new MessageModel(message , getformattedDate(messageTime,"dd/MM/yyyy hh:mm a")));
            }while(messages.moveToNext());
        } else {
            models.add(new MessageModel("No message available!!" , " "));
            noMessage = true;
        }


        adapter = new MessageAdapter(this, models);
        mMessageRecycler.setAdapter(adapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.scrollToPosition(models.size()-1);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String value= intent.getStringExtra("message");
                    saveMessage(value,true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        Log.d("Alert Trader","message list Activity 2");
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(MessageListActivity.this).registerReceiver((receiver),
                new IntentFilter(MyFirebaseMessagingService.REQUEST_ACCEPT)
        );
    }



    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(MessageListActivity.this).unregisterReceiver(receiver);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            mAuth.signOut();
            mGoogleSignInClient.signOut();

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

    public void sendMessage(View v){

        String msg = message_text.getText().toString();
        saveMessage(msg,true);
    }

    private void saveMessage(String msg, boolean fromButton){
        Log.d(TAG, "saveMessage msg : " + msg);
        if(msg!=null && msg.trim().length()>0){

            DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String userName = mAuth.getCurrentUser().getEmail();
            long msgTime = new Date().getTime();
            long count = dbHelper.putMessages(msg,new Date().getTime(), "R", userName, db);
            if(count>0 && fromButton){
                int index = 0;
                if(models!=null){
                    index = models.size();
                }
                if(noMessage){
                    index = 0;
                    models.remove(0);
                    mMessageRecycler.removeViewAt(0);
                    adapter.notifyItemRemoved(0);
                    adapter.notifyItemRangeChanged(0, models.size());
                    adapter.notifyDataSetChanged();
                    noMessage = false;
                }
                models.add(index,new MessageModel(msg,getformattedDate(String.valueOf(msgTime),"dd/MM/yyyy hh:mm a")));
                adapter.notifyItemInserted(index);
                mMessageRecycler.scrollToPosition(index);
            }
            //Toast.makeText(MessageListActivity.this,"insert "+count,Toast.LENGTH_LONG).show();
        }
        message_text.setText("");
    }

    private String getformattedDate(String timeInMillies, String formatString){
        Calendar calendar = Calendar.getInstance();
        long timeInMilliesLong = 0;
        String formattedDate = "";
        if(timeInMillies != null && timeInMillies.trim().length()>0){
            try{
                timeInMilliesLong = Long.parseLong(timeInMillies);
                calendar.setTimeInMillis(timeInMilliesLong);
                SimpleDateFormat format = null;
                if(formatString != null && formatString.trim().length()>0){
                    format = new SimpleDateFormat(formatString);
                } else {
                    format = new SimpleDateFormat();
                }
                format.setCalendar(calendar);
                formattedDate = format.format(calendar.getTime());

            }catch (Exception e){
                Log.d(TAG, "getformattedDate: invalid value passed in timeInMillies: "+timeInMillies);
            }
        }
        return formattedDate;
    }


}
