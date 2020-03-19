package com.deadlinesaver.android.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.BacklogAdapter;
import com.deadlinesaver.android.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class UndoneFragment extends Fragment {

    private static List<Backlog> backlogList_undone = new ArrayList<>();
    private static RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.undone_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.backlog_recycler_view_undone);

        //set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //set adapter
        final BacklogAdapter adapter = new BacklogAdapter(backlogList_undone, true);
        recyclerView.setAdapter(adapter);

        //长按时删除事件，并给用户撤销的机会
        adapter.setOnItemLongClickListener(new BacklogAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle("您准备删除这一事件")
                        .setMessage("确定执行该操作吗？")
                        .setCancelable(false)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                adapter.removeItem(position);

                                Snackbar.make(view, "事件已删除", Snackbar.LENGTH_LONG)
                                        .setAction("撤销", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                adapter.restoreItem();
                                                Toast.makeText(getActivity(), "已撤销", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.show();
            }
        });
        return view;
    }

    public static void addBacklog(final Backlog backlog, boolean isInitialize) {
        if (backlogList_undone.contains(backlog))
            return;
        backlogList_undone.add(backlog);
        if (!isInitialize) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
