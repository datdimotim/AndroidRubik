package com.dimotim.kubsolver;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

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
        return Arrays.asList(
                new SolveEntry(new int[]{},"solved"),
                new SolveEntry(new int[]{6,12,1,12,1,16,6,2,11,2,4,9,5,1,10,17}, "uzor")
        );
    }

    public interface SolveListener{
        void onSolve(SolveEntry solveEntry);
    }

    @Value
    public static class SolveEntry{
        int[] hods;
        String label;

        @Override
        public String toString(){
            return label;
        }
    }
}

