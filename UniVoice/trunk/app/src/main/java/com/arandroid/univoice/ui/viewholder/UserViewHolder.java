package com.arandroid.univoice.ui.viewholder;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arandroid.univoice.R;
import com.arandroid.univoice.core.FirebaseAsync;
import com.arandroid.univoice.model.User;
import com.arandroid.univoice.utils.ColorCommon;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class UserViewHolder extends BaseViewHolder<User> {
    private TextView phoneNumber;
    private ImageView userPicture;
    private TextView userPictureSymbol;

    public UserViewHolder(View itemView) {
        super(itemView);
        phoneNumber = itemView.findViewById(R.id.user_phone_number);
        userPicture = itemView.findViewById(R.id.user_picture);
        userPictureSymbol = itemView.findViewById(R.id.user_picture_symbol);
    }

    @Override
    public void bindToModel(@NonNull User model, Activity context) {
        itemView.setTag(model);
        if (model.getName() != null) {
            phoneNumber.setText(model.getName());
            int randomColor = ColorCommon.nextRandomColor(context);
            userPicture.setImageDrawable(new ColorDrawable(randomColor));

            if (randomColor == ContextCompat.getColor(context, R.color.material_color_blue)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_cyan)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_light_blue)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_green)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_light_green)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_lime)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_yellow)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_amber)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_orange)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_deep_orange)
                    || randomColor == ContextCompat.getColor(context, R.color.material_color_grey)) {
                userPictureSymbol.setTextColor(Color.BLACK);
            } else {
                userPictureSymbol.setTextColor(Color.WHITE);
            }
            userPictureSymbol.setText(model.getName().substring(0, 1));
        } else {
            phoneNumber.setText(model.getPhoneNumber());
            userPicture.setImageResource(R.drawable.default_user_icon);
            userPictureSymbol.setText("");
        }
    }

    @Override
    public void onItemRemoved(FirebaseAsync<User> callback) {

    }

    @Override
    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        itemView.setOnClickListener(onItemClickListener);
    }
}
