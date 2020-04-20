package com.deadlinesaver.android.recyclerview;

import android.app.Activity;
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
import com.deadlinesaver.android.util.ToastUtil;
import com.deadlinesaver.android.util.VibrateUtil;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BacklogAdapter extends CustomBaseAdapter<BacklogAdapter.ViewHolder> {

    private Activity mActivity;
    private List<Backlog> mBacklogList;
    private boolean isUndone = true;

    /**
     * 最近被删除的待办事项的各项数据（用于复原
     */
    private String lastBacklogName;
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
        SweetAlertDialog dialog = new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("您准备删除这一事件")
                .setContentText("确定执行该操作吗？")
                .setConfirmText("是的，我很确定！")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        removeItem(position);

                        sweetAlertDialog.setTitleText("事件已删除！")
                                .setContentText("如果反悔请点击下方按钮撤销")
                                .setConfirmText("好的没事了")
                                .setConfirmClickListener(null)
                                .setCancelText("快撤销！")
                                .showCancelButton(true)
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        restoreItem();
                                        sweetAlertDialog.dismissWithAnimation();
                                        ToastUtil.showToast(mActivity, "已撤销", Toast.LENGTH_SHORT);
                                    }
                                })
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        notifyItemRangeChanged(0, mBacklogList.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView backlogTextView;
        boolean isFirstClick = true;

        public ViewHolder(View view) {
            super(view);
            radioButton = view.findViewById(R.id.backlog_radio_button);
            backlogTextView = view.findViewById(R.id.backlog_name_text);
        }
    }

    public BacklogAdapter(Activity activity,View view, List<Backlog> backlogList, boolean isUndone) {
        mActivity = activity;
        mBacklogList = backlogList;
        this.isUndone = isUndone;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.backlog_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                VibrateUtil.vibrate(mActivity, 50);
                return true;
            }
        });

        //set the item itself clickable
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != -1) {
                    Backlog backlogInDataBase = getBacklogInDataBase(mBacklogList.get(position));
                    clickOnButton(holder.radioButton, backlogInDataBase);

                    checkItem(backlogInDataBase, position);
                }
            }
        });

        //set a listener to the toggle
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != -1) {
                    Backlog backlogInDataBase = getBacklogInDataBase(mBacklogList.get(position));
                    if (holder.isFirstClick) {
                        backlogInDataBase.setDone(true);
                        backlogInDataBase.save();
                        holder.isFirstClick = false;
                    } else {
                        clickOnButton(holder.radioButton, backlogInDataBase);
                        holder.isFirstClick = true;
                    }

                    checkItem(backlogInDataBase, position);
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

    private Backlog getBacklogInDataBase(Backlog backlogInList) {
        return LitePal.find(Backlog.class, backlogInList.getId());
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