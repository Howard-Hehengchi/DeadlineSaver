package com.deadlinesaver.android.recyclerview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.R;
import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.fragments.DoneFragment;
import com.deadlinesaver.android.fragments.UndoneFragment;
import com.google.android.material.snackbar.Snackbar;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.List;

public class BacklogAdapter extends RecyclerView.Adapter<BacklogAdapter.ViewHolder> implements onMoveAndSwipeListener {

    private Context mContext;
    private View mView;
    private List<Backlog> mBacklogList;
    private boolean isUndone = true;

    /**
     * 最近被删除的待办事项的各项数据（用于复原
     */
    private String lastBacklogName;
    private int lastBacklogId;
    private boolean lastBacklogIsDone;

    @Override
    public void onItemMove(int fromPos, int toPos) {
        Collections.swap(mBacklogList, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
    }

    /**
     * 删除事件，并给用户撤销的机会
     * @param position 位置
     */
    @Override
    public void onItemRemove(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("您准备删除这一事件")
                .setMessage("确定执行该操作吗？")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeItem(position);

                        Snackbar.make(mView.getRootView(), "事件已删除", Snackbar.LENGTH_LONG)
                                .setAction("撤销", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        restoreItem();
                                        Toast.makeText(mContext, "已撤销", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notifyItemRangeChanged(0, mBacklogList.size());
                    }
                });
        builder.show();
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

    public BacklogAdapter(Context context,View view, List<Backlog> backlogList, boolean isUndone) {
        mContext = context;
        mView = view;
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
                        backlog.save();
                        holder.isFirstClick = false;
                    } else {
                        clickOnButton(holder.radioButton, backlog);
                        holder.isFirstClick = true;
                    }

                    checkItem(backlog, position);
                }
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
            backlog.save();
        } else {
            radioButton.setChecked(true);
            backlog.setDone(true);
            backlog.save();
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

    private void removeItem(int position) {
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

    private void restoreItem() {
        Backlog lastBacklog = new Backlog(lastBacklogName, lastBacklogIsDone);
        mBacklogList.add(lastBacklog);
        notifyItemRangeChanged(0, mBacklogList.size());
        lastBacklog.save();
    }
}