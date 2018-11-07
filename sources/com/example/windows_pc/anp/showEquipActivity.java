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

public class showEquipActivity extends Fragment {
    private EquipAdapter eqAd;
    private ArrayList<Equipement> list;
    private ListView lst_view;

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("List des Equipements");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(C0336R.layout.activity_show_equip, container, false);
        this.lst_view = (ListView) v.findViewById(C0336R.id.list_view);
        this.list = AdminDB.getAllEquipement(false);
        this.eqAd = new EquipAdapter(getActivity(), this.list, false);
        this.lst_view.setAdapter(this.eqAd);
        return v;
    }
}
