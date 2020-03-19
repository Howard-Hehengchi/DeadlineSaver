package com.deadlinesaver.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.fragments.DoneFragment;
import com.deadlinesaver.android.fragments.UndoneFragment;

import org.litepal.LitePal;

import java.util.List;

public class BacklogAdapter extends RecyclerView.Adapter<BacklogAdapter.ViewHolder> {

    private List<Backlog> mBacklogList;
    private boolean isUndone = true;

    /**
     * 最近被删除的待办事项的各项数据（用于复原
     */
    private String lastBacklogName;
    private int lastBacklogId;
    private boolean lastBacklogIsDone;

    public interface OnItemLongClickListener {
        void onLongClick(int position);
    }
    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View backlogView;
        RadioButton radioButton;
        TextView backlogTextView;
        boolean isFirstClick = true;

        public ViewHolder(View view) {
            super(view);
            backlogView = view;
            radioButton = (RadioButton) view.findViewById(R.id.backlog_radio_button);
            backlogTextView = (TextView) view.findViewById(R.id.backlog_name_text);
        }
    }

    public BacklogAdapter(List<Backlog> backlogList, boolean isUndone) {
        mBacklogList = backlogList;
        this.isUndone = isUndone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.backlog_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //set the item itself clickable
        holder.backlogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != -1) {
                    Backlog backlog = mBacklogList.get(position);
                    clickOnButton(holder.radioButton, backlog);

                    checkItem(backlog, position);
                }
            }
        });

        //set a listener to the toggle
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != -1) {
                    Backlog backlog = mBacklogList.get(position);
                    if (holder.isFirstClick) {
                        backlog.setDone(true);
                        holder.isFirstClick = false;
                    } else {
                        clickOnButton(holder.radioButton, backlog);
                        holder.isFirstClick = true;
                    }

                    checkItem(backlog, position);
                }
            }
        });

        holder.backlogView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onLongClick(position);
                }
                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Backlog backlog = mBacklogList.get(position);
        holder.backlogTextView.setText(backlog.getBacklogName());
        holder.radioButton.setChecked(backlog.isDone());


    }

    @Override
    public int getItemCount() {
        return mBacklogList.size();
    }

    /**
     * 将选中状态设置为相反的
     * @param radioButton 事件对应的按钮
     * @param backlog 事件类
     */
    private void clickOnButton(RadioButton radioButton, Backlog backlog){
        if (radioButton.isChecked()) {
            radioButton.setChecked(false);
            backlog.setDone(false);
        } else {
            radioButton.setChecked(true);
            backlog.setDone(true);
        }
    }

    /**
     * 判断当前事件的状态并执行相应的转移操作
     * @param backlog 事件类
     * @param position 位置
     */
    private void checkItem(Backlog backlog, int position) {
        if (isUndone) {
            if (backlog.isDone()) {
                mBacklogList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mBacklogList.size());
                DoneFragment.addBacklog(backlog, false);
            }
        } else {
            if (!backlog.isDone()) {
                mBacklogList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mBacklogList.size());
                UndoneFragment.addBacklog(backlog, false);
            }
        }
    }

    public void removeItem(int position) {
        //储存被删除事件的数据
        Backlog lastBacklog = mBacklogList.get(position);
        lastBacklogName = lastBacklog.getBacklogName();
        lastBacklogId = lastBacklog.getId();
        lastBacklogIsDone = lastBacklog.isDone();
        //删除事件
        mBacklogList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mBacklogList.size());
        LitePal.delete(Backlog.class, lastBacklog.getId());
    }

    public void restoreItem() {
        Backlog lastBacklog = new Backlog(lastBacklogName, lastBacklogIsDone);
        mBacklogList.add(lastBacklog);
        notifyItemRangeChanged(0, mBacklogList.size());
        lastBacklog.save();
    }
}