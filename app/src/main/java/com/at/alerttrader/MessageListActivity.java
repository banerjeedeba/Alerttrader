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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.at.alerttrader.adapter.MessageAdapter;
import com.at.alerttrader.db.DBHelper;
import com.at.alerttrader.model.DBMessageModel;
import com.at.alerttrader.model.MessageModel;
import com.at.alerttrader.model.User;
import com.at.alerttrader.service.MyFirebaseMessagingService;
import com.at.alerttrader.util.DateUtil;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MessageListActivity extends BaseActivity {

    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";
    //private ListView listView;
    private ArrayList<MessageModel> models;
    private MessageAdapter adapter;
    private RecyclerView mMessageRecycler;
    private EditText message_text;
    private Button send_button;
    private LinearLayout inputLayout;
    private Context context;
    private boolean noMessage;
    private static final int REFRESH_INTERVAL = 2880;
    private static final String TAG = "MessageListActivity";
    boolean doubleBackToExitPressedOnce = false;
    BroadcastReceiver receiver;
    SQLiteDatabase sqLitedb;
    private static final int MENU_ITEM_APPROVE = 1;
    boolean isAdmin = false;

    public static Intent createIntent(
            Context context,
            IdpResponse idpResponse,
            String message,boolean isAdmin) {

        Intent startIntent = new Intent();
        if (idpResponse != null) {
            startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        }
        if(message!=null && message.trim().length()>0){
            startIntent.putExtra("messageText",message);
        }
        startIntent.putExtra("isAdmin",isAdmin);
        return startIntent.setClass(context, MessageListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        DBHelper dbHelper = new DBHelper(context);
        sqLitedb = dbHelper.getWritableDatabase();
        setContentView(R.layout.activity_message_list);
        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        message_text = findViewById(R.id.message_text);
        send_button = findViewById(R.id.button_chatbox_send);
        inputLayout = findViewById(R.id.layout_chatbox);


        message_text.setVisibility(View.GONE);
        send_button.setVisibility(View.GONE);
        inputLayout.setVisibility(View.GONE);
        //mMessageRecycler.setPadding(0,0,0,0);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d(TAG, "auth: " + (mAuth!=null));
        Log.d(TAG, "mGoogleSignInClient: " + (mGoogleSignInClient!=null));


        Intent i = getIntent();
        String msg = i.getStringExtra("messageText");
        isAdmin = i.getBooleanExtra("isAdmin",false);
        if(isAdmin){
            message_text.setVisibility(View.VISIBLE);
            send_button.setVisibility(View.VISIBLE);
            inputLayout.setVisibility(View.VISIBLE);
        }
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
                //dbHelper.deleteMessage(sqLitedb,currentTime.getTimeInMillis());
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

        //Access message from DB
        /*Cursor messages = dbHelper.getMessages(sqLitedb);
        if(messages.moveToFirst()){
            do{
                String message = messages.getString(messages.getColumnIndex("message"));
                String messageTime = messages.getString(messages.getColumnIndex("message_time"));

                models.add(new MessageModel(message , getformattedDate(messageTime,"dd/MM/yyyy hh:mm a")));
            }while(messages.moveToNext());
        } else {
            models.add(new MessageModel("No message available!!" , " "));
            noMessage = true;
        }*/

        //Access messages from FireStore
        db = FirebaseFirestore.getInstance();

        db.collection("message").orderBy("messageTimeInMillis").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        models.remove(0);
                        mMessageRecycler.removeViewAt(0);
                        adapter.notifyItemRemoved(0);
                        adapter.notifyItemRangeChanged(0, models.size());
                        adapter.notifyDataSetChanged();
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {

                                for(QueryDocumentSnapshot document : task.getResult()) {
                                    DBMessageModel message = document.toObject(DBMessageModel.class);
                                    MessageModel messageModel = new MessageModel(message.getMessageText(),getformattedDate(String.valueOf(message.getMessageTimeInMillis()),"dd/MM/yyyy hh:mm a"));
                                    models.add(messageModel);
                                }
                                noMessage = false;

                            }else {
                                models.add(new MessageModel("No message available!!" , " "));
                                noMessage = true;
                            }
                        }else {
                            models.add(new MessageModel("An error occurred while fetching messages!!" , " "));
                            noMessage = true;
                        }
                        //adapter = new MessageAdapter(MessageListActivity.this, models);
                        //mMessageRecycler.setAdapter(adapter);
                        //mMessageRecycler.setLayoutManager(new LinearLayoutManager(MessageListActivity.this));
                        adapter.notifyItemInserted(0);
                        mMessageRecycler.scrollToPosition(models.size()-1);
                    }
                });
        //registration.remove();

        models.add(new MessageModel("Loading.." , " "));
        noMessage = true;
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
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(MessageListActivity.this).unregisterReceiver(receiver);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            //mAuth.signOut();
            //mGoogleSignInClient.signOut();

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

    public void sendMessage(View v){

        String msg = message_text.getText().toString();
        saveMessage(msg,true);
    }

    private void saveMessage(final String msg, final boolean fromButton){
        Log.d(TAG, "saveMessage msg : " + msg);
        if(msg!=null && msg.trim().length()>0){

            /*DBHelper dbHelper = new DBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();*/
            String userName = mAuth.getCurrentUser().getEmail();
            final Date msgTime = new Date();
            DBMessageModel dbMessageModel = new DBMessageModel(msg,msgTime.getTime());
            MessageModel messageModel= new MessageModel(msg, DateUtil.dateToString(msgTime,null));
            db.collection("message")
                    .add(dbMessageModel)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            if(fromButton){
                                int index = 0;

                                if(models!=null){
                                    index = models.size();
                                }
                                Log.d(TAG,String.valueOf(noMessage));
                                if(noMessage){
                                    index = 0;
                                    models.remove(0);
                                    mMessageRecycler.removeViewAt(0);
                                    adapter.notifyItemRemoved(0);
                                    adapter.notifyItemRangeChanged(0, models.size());
                                    adapter.notifyDataSetChanged();
                                    noMessage = false;
                                }
                                models.add(index,new MessageModel(msg,getformattedDate(String.valueOf(msgTime.getTime()),"dd/MM/yyyy hh:mm a")));
                                adapter.notifyItemInserted(index);
                                mMessageRecycler.scrollToPosition(index);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MessageListActivity.this,"Error adding message!!",Toast.LENGTH_LONG);
                            Log.w(TAG, "Error adding message", e);
                        }
                    });

            //long count = dbHelper.putMessages(msg,new Date().getTime(), "R", userName, sqLitedb);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isAdmin){
            menu.add(Menu.NONE, MENU_ITEM_APPROVE, Menu.NONE, "Approve User");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_APPROVE:
                approveUser();
                return true;

            default:
                return false;
        }

    }

    private void approveUser() {
        startActivity(ApproveActivity.createIntent(MessageListActivity.this));
        finish();
    }
}


