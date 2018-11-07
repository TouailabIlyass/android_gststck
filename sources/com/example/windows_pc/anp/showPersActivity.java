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

public class showPersActivity extends Fragment {
    private ArrayList<Personnel> list;
    private ListView lst_view;
    private PersAdapter prAd;

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("List des Personnels");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(C0336R.layout.activity_show_pers, container, false);
        this.lst_view = (ListView) v.findViewById(C0336R.id.list_pers);
        this.list = AdminDB.getAllPersonnel(false);
        this.prAd = new PersAdapter(getActivity(), this.list, false);
        this.lst_view.setAdapter(this.prAd);
        return v;
    }
}
