package com.dimotim.kubsolver.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.Solution;
import com.dimotim.kubSolver.Uzors;
import com.dimotim.kubsolver.R;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Value;


public class SolveDialog extends DialogFragment {
    SolveListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (SolveListener) getActivity();
        } catch (ClassCastException e) {
            Log.e("SolveDialog", "onAttach: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        View view = inflater.inflate(R.layout.solve_fragment, parent);

        ListView listView = view.findViewById(R.id.solve_list_view);

        listView.setAdapter(new ArrayAdapter<>(
                view.getContext(),
                android.R.layout.simple_list_item_1,
                getEntries()
        ));

        listView.setOnItemClickListener((parent1, view1, position, id) -> {
            SolveEntry solveEntry = (SolveEntry) parent1.getItemAtPosition(position);
            listener.onSolve(solveEntry);
            dismiss();
        });

        return view;
    }

    private static List<SolveEntry> getEntries(){
        return Uzors.getInstance().getUzors().entrySet().stream()
                .map(kv -> new SolveEntry(kv.getKey(), kv.getValue()))
                .collect(Collectors.toList());
    }

    public interface SolveListener{
        void onSolve(SolveEntry solveEntry);
    }

    @Value
    public static class SolveEntry{
        String label;
        Solution solution;

        @Override
        public String toString(){
            return label;
        }
    }
}

