package com.example.windows_pc.anp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddEquipActivity extends Fragment {
    Button add;
    Button cancel;
    EditText edit;
    Spinner sp;

    /* renamed from: com.example.windows_pc.anp.AddEquipActivity$1 */
    class C03221 implements OnClickListener {
        C03221() {
        }

        public void onClick(View v) {
            if (AdminDB.addEquipement(new Equipement(0, AddEquipActivity.this.edit.getText().toString(), AddEquipActivity.this.sp.getSelectedItem().toString()))) {
                AddEquipActivity.this.startActivity(new Intent(AddEquipActivity.this.getActivity(), showEquipActivity.class));
                return;
            }
            Toast.makeText(AddEquipActivity.this.getActivity(), "erreur", 1).show();
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddEquipActivity$2 */
    class C03232 implements OnClickListener {
        C03232() {
        }

        public void onClick(View v) {
            Main2Activity.Home();
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Ajouter Equipement");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(C0336R.layout.activity_add_equip, container, false);
        this.edit = (EditText) v.findViewById(C0336R.id.machinetxt);
        this.add = (Button) v.findViewById(C0336R.id.add_equip);
        this.cancel = (Button) v.findViewById(C0336R.id.cancel_equip);
        this.sp = (Spinner) v.findViewById(C0336R.id.type_machine);
        this.sp.setAdapter(new EquipAdapter(getActivity(), AdminDB.getAllEquipement(true), true));
        this.add.setOnClickListener(new C03221());
        this.cancel.setOnClickListener(new C03232());
        return v;
    }
}
