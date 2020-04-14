package com.deadlinesaver.android.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deadlinesaver.android.db.Backlog;
import com.deadlinesaver.android.recyclerview.BacklogAdapter;
import com.deadlinesaver.android.R;
import com.deadlinesaver.android.recyclerview.ItemTouchCallback;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import static com.deadlinesaver.android.fragments.UndoneFragment.backlogList_undone;

public class DoneFragment extends Fragment {

    static List<Backlog> backlogList_done = new ArrayList<>();
    private static RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.done_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.backlog_recycler_view_done);

        //set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
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
            recyclerView.getAdapter().notifyItemRangeChanged(0, backlogList_done.size());
        }
    }

    public static void removeBacklogOfName(String backlogName) {
        Backlog chosenBacklog = null;
        for (Backlog backlog : backlogList_done) {
            if (backlog.getBacklogName().equals(backlogName)) {
                chosenBacklog = backlog;
                break;
            }
        }

        if (chosenBacklog != null) {
            backlogList_done.remove(chosenBacklog);
            recyclerView.getAdapter().notifyItemRangeChanged(0, backlogList_undone.size() + 1);
            LitePal.delete(Backlog.class, chosenBacklog.getId());
        }
    }
}
