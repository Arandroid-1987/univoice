package com.arandroid.univoice.ui.viewholder;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.arandroid.univoice.core.FirebaseAsync;

import java.io.Serializable;

/**
 * Created by MeringoloRo on 29/04/2017.
 */

public abstract class BaseViewHolder<M> extends RecyclerView.ViewHolder implements Serializable {
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindToModel(@NonNull M model, Activity context);

    public abstract void onItemRemoved(FirebaseAsync<M> callback);

    public abstract void setOnItemClickListener(View.OnClickListener onItemClickListener);

}
