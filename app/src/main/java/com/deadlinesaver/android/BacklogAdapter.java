package com.deadlinesaver.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BacklogAdapter extends RecyclerView.Adapter<BacklogAdapter.ViewHolder> {

    private List<Backlog> mBacklogList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View backlogView;
        RadioButton radioButton;
        TextView backlogTextView;

        public ViewHolder(View view) {
            super(view);
            backlogView = view;
            radioButton = (RadioButton) view.findViewById(R.id.backlog_radio_button);
            backlogTextView = (TextView) view.findViewById(R.id.backlog_name_text);
        }
    }

    public BacklogAdapter(List<Backlog> backlogList) {
        mBacklogList = backlogList;
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
                Backlog backlog = mBacklogList.get(position);
                clickOnButton(holder.radioButton, backlog);
            }
        });

        //set a listener to the toggle
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Backlog backlog = mBacklogList.get(position);
                clickOnButton(holder.radioButton, backlog);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Backlog backlog = mBacklogList.get(position);
        holder.backlogTextView.setText(backlog.getBacklogName());
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
            radioButton.toggle();
            backlog.setDone(true);
        }
    }
}
