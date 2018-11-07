package com.example.windows_pc.anp;

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

public class AddPersActivity extends Fragment {
    Button add;
    Button cancel;
    EditText nom;
    EditText prenom;
    Spinner sp;

    /* renamed from: com.example.windows_pc.anp.AddPersActivity$1 */
    class C03321 implements OnClickListener {
        C03321() {
        }

        public void onClick(View v) {
            if (!AdminDB.addPersone(new Personnel(0, AddPersActivity.this.nom.getText().toString(), AddPersActivity.this.prenom.getText().toString(), AddPersActivity.this.sp.getSelectedItem().toString()))) {
                Toast.makeText(AddPersActivity.this.getActivity(), "erreur", 1).show();
            }
            Toast.makeText(AddPersActivity.this.getActivity(), AddPersActivity.this.sp.getSelectedItem().toString(), 1).show();
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddPersActivity$2 */
    class C03332 implements OnClickListener {
        C03332() {
        }

        public void onClick(View v) {
            Main2Activity.Home();
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Ajouter Personnel");
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(C0336R.layout.activity_add_pers, container, false);
        this.nom = (EditText) v.findViewById(C0336R.id.firstname);
        this.prenom = (EditText) v.findViewById(C0336R.id.lastname);
        this.add = (Button) v.findViewById(C0336R.id.add_pers);
        this.cancel = (Button) v.findViewById(C0336R.id.cancel_pers);
        this.sp = (Spinner) v.findViewById(C0336R.id.type_fonct);
        this.sp.setAdapter(new PersAdapter(getActivity(), AdminDB.getAllPersonnel(true), true));
        this.add.setOnClickListener(new C03321());
        this.cancel.setOnClickListener(new C03332());
        return v;
    }
}
