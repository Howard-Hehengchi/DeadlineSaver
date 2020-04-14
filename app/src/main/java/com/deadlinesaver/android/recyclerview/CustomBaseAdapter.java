package com.deadlinesaver.android.recyclerview;

import androidx.recyclerview.widget.RecyclerView;

public abstract class CustomBaseAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements onMoveAndSwipeListener {

    @Override
    public abstract void onItemMove(int fromPos, int toPos);

    @Override
    public abstract void onItemRemove(int position);

}
