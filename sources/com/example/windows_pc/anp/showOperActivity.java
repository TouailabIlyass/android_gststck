package com.example.windows_pc.anp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;

public class showOperActivity extends Fragment {
    private ArrayList<Operation> list;
    private ListView list_view;
    private OperAdapter opAd;

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("List des Operations");
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(C0336R.layout.activity_show_oper, container, false);
        this.list_view = (ListView) view.findViewById(C0336R.id.list_oper);
        this.list = AdminDB.getOp();
        this.opAd = new OperAdapter(getActivity(), this.list);
        this.list_view.setAdapter(this.opAd);
        return view;
    }
}
