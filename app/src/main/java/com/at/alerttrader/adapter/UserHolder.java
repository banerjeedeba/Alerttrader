package com.at.alerttrader.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.at.alerttrader.R;
import com.at.alerttrader.model.MessageModel;
import com.at.alerttrader.model.User;
import com.at.alerttrader.util.StringUtils;

public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    TextView userText;

    private ItemClickListner itemClickListner;

    UserHolder(View itemView){
        super(itemView);
        userText = (TextView)itemView.findViewById(R.id.text_user_body);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListner(ItemClickListner itemClickListner){
        this.itemClickListner = itemClickListner;
    }

    void bind(User model) {
        String text = model.getDisplayName();
        if(!StringUtils.isEmpty(model.getEmail())){
            text+= "\n"+model.getEmail();
        }
        userText.setText(text);
    }

    @Override
    public void onClick(View view) {
        itemClickListner.onClick(view,getAdapterPosition(),false);
    }
}
