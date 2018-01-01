package com.at.alerttrader.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.at.alerttrader.R;
import com.at.alerttrader.model.MessageModel;

/**
 * Created by lenovo on 14-12-2017.
 */

public class MessageHolder extends RecyclerView.ViewHolder {

    TextView msgText,msgTimeText;

    MessageHolder(View itemView){
        super(itemView);
        msgText = (TextView)itemView.findViewById(R.id.text_message_body);
        msgTimeText = (TextView) itemView.findViewById(R.id.text_message_time);
    }

    void bind(MessageModel model) {
        msgText.setText(model.getMessageText());
        msgTimeText.setText(model.getMessageTime());
        Log.d("Alert Trader", model.getMessageText());
    }
}
