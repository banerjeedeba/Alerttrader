package com.at.alerttrader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.at.alerttrader.R;
import com.at.alerttrader.model.MessageModel;

import java.util.ArrayList;

/**
 * Created by lenovo on 14-12-2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

    private Context context;
    private ArrayList<MessageModel> models;
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private LayoutInflater inflater;

    public MessageAdapter(Context context, ArrayList<MessageModel> models) {
        this.context = context;
        this.models = models;
        inflater = LayoutInflater.from(context);
        Log.d("Alert Trader","MessageAdapter");
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =  inflater.inflate(R.layout.item_message_received, parent, false);
        Log.d("Alert Trader","onCreateViewHolder");
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        Log.d("Alert Trader","onBindViewHolder");
        MessageModel message = (MessageModel) models.get(position);
        ((MessageHolder) holder).bind(message);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    /*@Override
    public View getView(int i, View view, ViewGroup viewGroup) {


        *//*if(view == null){
            view = View.inflate(context , R.layout.list_item, null);
        }*//*





        msgText.setText(model.getMessageText());
        msgTimeText.setText(model.getMessageTime());

        return view;
    }*/
}
