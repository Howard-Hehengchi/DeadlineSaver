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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.MyApplication;
import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.recyclerview.BacklogAdapter;
import com.deadlinesaver.android.R;
import com.deadlinesaver.android.recyclerview.ItemTouchCallback;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import static com.deadlinesaver.android.fragments.UndoneFragment.backlogList_undone;

public class DoneFragment extends Fragment {

    public static List<Backlog> backlogList_done = new ArrayList<>();
    private static RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.undone_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.backlog_recycler_view_undone);

        //set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //set adapter
        final BacklogAdapter adapter = new BacklogAdapter(getActivity(), view, backlogList_done, false);
        recyclerView.setAdapter(adapter);

        ItemTouchCallback callback = new ItemTouchCallback(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    public static void addBacklog(final Backlog backlog, boolean isInitialize) {
        for (Backlog backlogInList : backlogList_done) {
            if (backlogInList.getBacklogName().equals(backlog.getBacklogName()))
                return;
        }
        for (Backlog backlogInList : backlogList_undone) {
            if (backlogInList.getBacklogName().equals(backlog.getBacklogName()))
                return;
        }
        backlogList_done.add(backlog);
        if (!isInitialize) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
