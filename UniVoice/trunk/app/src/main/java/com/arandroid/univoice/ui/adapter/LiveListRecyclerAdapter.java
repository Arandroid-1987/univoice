package com.arandroid.univoice.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arandroid.univoice.model.Filterable;
import com.arandroid.univoice.model.User;
import com.arandroid.univoice.ui.ItemTouchHelperAdapter;
import com.arandroid.univoice.ui.viewholder.BaseViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Created by MeringoloRo on 23/04/2017.
 */

public class LiveListRecyclerAdapter<M extends Filterable, VH extends BaseViewHolder<M>> extends RecyclerView.Adapter<VH> implements ItemTouchHelperAdapter {
    private ArrayList<M> filteredList;
    private ArrayList<M> unfilteredList;
    private Activity context;
    private LayoutInflater inflater;
    private Class<M> mClass;
    private Class<VH> vhClass;
    private int itemLayout;
    private int alternativeLayout;
    private EventListener<QuerySnapshot> listener;
    private Query query;
    private M lastRemoved;
    private String filter;
    private M highlighted;
    private boolean highlightedRequest;

    private static final int ALTERNATE_LAYOUT = 0;
    private static final int SMALL_LAYOUT = 1;
    private View.OnClickListener itemClickListener;

    public LiveListRecyclerAdapter(Activity context, Class<M> mClass, Class<VH> vhClass, int itemLayout, Query query) {
        this(context, mClass, vhClass, itemLayout, -1, query, null);
        this.highlightedRequest = false;
    }

    public LiveListRecyclerAdapter(Activity context, Class<M> mClass, Class<VH> vhClass, int itemLayout, int alternativeLayout, Query query) {
        this(context, mClass, vhClass, itemLayout, alternativeLayout, query, null);
        this.highlightedRequest = false;
    }

    public LiveListRecyclerAdapter(Activity context, Class<M> mClass, Class<VH> vhClass, int itemLayout, int alternativeLayout, Query query, View.OnClickListener itemClickListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mClass = mClass;
        this.vhClass = vhClass;
        this.itemLayout = itemLayout;
        this.alternativeLayout = alternativeLayout;
        this.filteredList = new ArrayList<>();
        this.unfilteredList = new ArrayList<>();
        this.query = query;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = itemLayout;
        if (viewType == ALTERNATE_LAYOUT) {
            layout = alternativeLayout;
        }
        View v = inflater.inflate(layout, parent, false);
        try {
            Constructor<VH> constructor = vhClass.getConstructor(View.class);
            return constructor.newInstance(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (alternativeLayout != -1) {
            if (!highlightedRequest) {
                if (position == filteredList.size() - 1) {
                    return ALTERNATE_LAYOUT;
                }
            } else {
                final M model = filteredList.get(position);
                if (model.equals(highlighted)) {
                    return ALTERNATE_LAYOUT;
                }
            }
        }
        return SMALL_LAYOUT;
    }

    public void setHighlighted(M highlighted) {
        this.highlighted = highlighted;
        this.highlightedRequest = true;
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        final M model = filteredList.get(position);
        holder.bindToModel(model, context);
        if (itemClickListener != null) {
            holder.setOnItemClickListener(itemClickListener);
        }
        holder.onItemRemoved(m -> {
            int index = filteredList.indexOf(m);
            remove(index);
        });
    }

    private void remove(int index) {
        lastRemoved = filteredList.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void startListening() {
        if (listener == null) {
            initListener();
        }
        query.addSnapshotListener(listener);
    }

    private void initListener() {
        listener = (documentSnapshot, e) -> {
            if (documentSnapshot != null) {
                for (DocumentChange dc : documentSnapshot.getDocumentChanges()) {
                    M voice = dc.getDocument().toObject(mClass);
                    if (voice instanceof User) {
                        User user = (User) voice;
                        String uid = FirebaseAuth.getInstance().getUid();
                        if (!user.getUid().equals(uid)) {
                            switch (dc.getType()) {
                                case ADDED:
                                    unfilteredList.add(voice);
                                    break;
                                case MODIFIED:
                                    unfilteredList.remove(voice);
                                    unfilteredList.add(voice);
                                    break;
                                case REMOVED:
                                    unfilteredList.remove(voice);
                                    break;
                            }
                        }
                    } else {
                        switch (dc.getType()) {
                            case ADDED:
                                unfilteredList.add(voice);
                                break;
                            case MODIFIED:
                                unfilteredList.remove(voice);
                                unfilteredList.add(voice);
                                break;
                            case REMOVED:
                                unfilteredList.remove(voice);
                                break;
                        }
                    }
                }
            }
            applyFilter();
            notifyDataSetChanged();
        };
    }

    public void stopListening() {
        if (listener != null) {
            listener = null;
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        remove(position);
    }

    public M getLastRemoved() {
        return lastRemoved;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        applyFilter();
        notifyDataSetChanged();
    }

    public String getFilter() {
        return filter;
    }

    private void applyFilter() {
        filteredList.clear();
        if (filter == null) {
            filteredList.addAll(unfilteredList);
        } else {
            for (M m : unfilteredList) {
                if (m.isCompliant(filter)) {
                    filteredList.add(m);
                }
            }
        }
    }

    public void setItemClickListener(View.OnClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ArrayList<M> getCurrentList() {
        return filteredList;
    }
}
