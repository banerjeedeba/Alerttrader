package com.at.alerttrader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.at.alerttrader.ApprovalActivity;
import com.at.alerttrader.R;
import com.at.alerttrader.model.MessageModel;
import com.at.alerttrader.model.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserHolder>{

    private Context context;
    private ArrayList<User> models;
    private LayoutInflater inflater;
    private static String TAG = "UserAdapter";
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public UserAdapter(Context context, ArrayList<User> models) {
        this.context = context;
        this.models = models;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  inflater.inflate(R.layout.item_user, parent, false);
        Log.d(TAG,"onCreateViewHolder");
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        User user = (User) models.get(position);
        ((UserHolder) holder).bind(user);

        holder.setItemClickListner(new ItemClickListner() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                User userObj = models.get(position);
                context.startActivity(ApprovalActivity.createIntent(context,userObj.getId(),userObj.getDisplayName(),userObj.getEmail()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_MESSAGE_RECEIVED;
    }
}
