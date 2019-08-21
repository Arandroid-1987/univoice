package com.arandroid.univoice.ui.viewholder;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.arandroid.univoice.utils.UIUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arandroid.univoice.R;
import com.arandroid.univoice.core.FirebaseAsync;
import com.arandroid.univoice.model.Message;
import com.google.firebase.auth.FirebaseAuth;

public class MessageViewHolder extends BaseViewHolder<Message> {
    private TextView textView;
    private FloatingActionButton actionPlay;
    private FirebaseAuth mAuth;
    private final CardView cardView;

    public MessageViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.textView);
        actionPlay = itemView.findViewById(R.id.action_play);
        cardView = itemView.findViewById(R.id.card_view);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void bindToModel(@NonNull Message model, Activity context) {
        itemView.setTag(model);
        actionPlay.setTag(model);
        textView.setText(model.getMessage());
        if (model.getSenderUid().equals(mAuth.getUid())) {
            // l'ho inviato io
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.material_color_teal));
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
            params.leftMargin = UIUtils.pixel(context, 50);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            cardView.setLayoutParams(params);

            textView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }else{
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cardView.getLayoutParams();
            params.rightMargin = UIUtils.pixel(context, 50);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            cardView.setLayoutParams(params);

            textView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }
    }

    @Override
    public void onItemRemoved(FirebaseAsync<Message> callback) {

    }

    @Override
    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        cardView.setOnClickListener(onItemClickListener);
        actionPlay.setOnClickListener(onItemClickListener);
    }
}
