package com.example.windows_pc.anp;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;

public class AddOperActivity extends Fragment {
    private Button add;
    private Button cancel;
    private Button date;
    private OnDateSetListener dateSetListener;
    private TextView dateTxt;
    private Button date_arr;
    private OnDateSetListener date_arrSetListener;
    private TextView date_arrTxt;
    private Button date_dep;
    private OnDateSetListener date_depSetListener;
    private TextView date_depTxt;
    private EquipAdapter eqAd;
    private ArrayList<Equipement> listequip;
    private ArrayList<Personnel> listper;
    private EditText nom_navire;
    private EditText operation;
    private PersAdapter prAd;
    private EditText qte;
    private Spinner sp_equip;
    private Spinner sp_pers;
    private Spinner sp_shift;
    private EditText tonnage;
    private EditText type;

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$1 */
    class C03241 implements OnClickListener {
        C03241() {
        }

        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(AddOperActivity.this.getContext(), 16973940, AddOperActivity.this.dateSetListener, c.get(1), c.get(2), c.get(5));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            dialog.show();
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$2 */
    class C03252 implements OnClickListener {
        C03252() {
        }

        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(AddOperActivity.this.getContext(), 16973940, AddOperActivity.this.date_arrSetListener, c.get(1), c.get(2), c.get(5));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            dialog.show();
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$3 */
    class C03263 implements OnClickListener {
        C03263() {
        }

        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(AddOperActivity.this.getContext(), 16973940, AddOperActivity.this.date_depSetListener, c.get(1), c.get(2), c.get(5));
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            dialog.show();
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$4 */
    class C03274 implements OnDateSetListener {
        C03274() {
        }

        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            month++;
            TextView access$300 = AddOperActivity.this.dateTxt;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(year);
            stringBuilder.append("-");
            stringBuilder.append(month);
            stringBuilder.append("-");
            stringBuilder.append(day);
            access$300.setText(stringBuilder.toString());
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$5 */
    class C03285 implements OnDateSetListener {
        C03285() {
        }

        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            month++;
            TextView access$400 = AddOperActivity.this.date_arrTxt;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(year);
            stringBuilder.append("-");
            stringBuilder.append(month);
            stringBuilder.append("-");
            stringBuilder.append(day);
            access$400.setText(stringBuilder.toString());
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$6 */
    class C03296 implements OnDateSetListener {
        C03296() {
        }

        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            month++;
            TextView access$500 = AddOperActivity.this.date_depTxt;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(year);
            stringBuilder.append("-");
            stringBuilder.append(month);
            stringBuilder.append("-");
            stringBuilder.append(day);
            access$500.setText(stringBuilder.toString());
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$7 */
    class C03307 implements OnClickListener {
        C03307() {
        }

        public void onClick(View v) {
            try {
                String obj = AddOperActivity.this.operation.getText().toString();
                String str = obj;
                Operation operation = r3;
                Operation operation2 = new Operation(0, str, AddOperActivity.this.dateTxt.getText().toString(), AddOperActivity.this.sp_shift.getSelectedItem().toString(), AddOperActivity.this.type.getText().toString(), AddOperActivity.this.nom_navire.getText().toString(), AddOperActivity.this.date_arrTxt.getText().toString(), AddOperActivity.this.date_depTxt.getText().toString(), Double.valueOf(AddOperActivity.this.tonnage.getText().toString()).doubleValue(), Integer.valueOf(AddOperActivity.this.qte.getText().toString()).intValue(), (int) AddOperActivity.this.sp_pers.getSelectedItemId(), (int) AddOperActivity.this.sp_equip.getSelectedItemId());
                if (!AdminDB.insert(operation)) {
                    Toast.makeText(AddOperActivity.this.getActivity(), "erreur", 1).show();
                }
            } catch (Exception e) {
                Toast.makeText(AddOperActivity.this.getActivity(), e.getMessage(), 1).show();
            }
        }
    }

    /* renamed from: com.example.windows_pc.anp.AddOperActivity$8 */
    class C03318 implements OnClickListener {
        C03318() {
        }

        public void onClick(View v) {
            Main2Activity.Home();
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Ajouter Operation");
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(C0336R.layout.activity_add_oper, container, false);
        this.operation = (EditText) v.findViewById(C0336R.id.operationTxt);
        this.nom_navire = (EditText) v.findViewById(C0336R.id.nom_navireTxt);
        this.type = (EditText) v.findViewById(C0336R.id.type_marchendise);
        this.tonnage = (EditText) v.findViewById(C0336R.id.tonnage_marchendise);
        this.qte = (EditText) v.findViewById(C0336R.id.qte_marchendise);
        this.dateTxt = (TextView) v.findViewById(C0336R.id.operationDateTxt);
        this.date_arrTxt = (TextView) v.findViewById(C0336R.id.operationDate_arrTxt);
        this.date_depTxt = (TextView) v.findViewById(C0336R.id.operationDate_depTxt);
        this.sp_shift = (Spinner) v.findViewById(C0336R.id.shift_spinner);
        this.sp_pers = (Spinner) v.findViewById(C0336R.id.pers_spinner);
        this.sp_equip = (Spinner) v.findViewById(C0336R.id.equip_spinner);
        this.add = (Button) v.findViewById(C0336R.id.add_oper);
        this.cancel = (Button) v.findViewById(C0336R.id.cancel_oper);
        this.date = (Button) v.findViewById(C0336R.id.operationDate);
        this.date_arr = (Button) v.findViewById(C0336R.id.date_arr);
        this.date_dep = (Button) v.findViewById(C0336R.id.date_dep);
        this.date.setOnClickListener(new C03241());
        this.date_arr.setOnClickListener(new C03252());
        this.date_dep.setOnClickListener(new C03263());
        this.dateSetListener = new C03274();
        this.date_arrSetListener = new C03285();
        this.date_depSetListener = new C03296();
        this.add.setOnClickListener(new C03307());
        this.cancel.setOnClickListener(new C03318());
        this.listper = AdminDB.getAllPersonnel(true);
        this.prAd = new PersAdapter(getActivity(), this.listper, true);
        this.sp_pers.setAdapter(this.prAd);
        this.listequip = AdminDB.getAllEquipement(true);
        this.eqAd = new EquipAdapter(getActivity(), this.listequip, true);
        this.sp_equip.setAdapter(this.eqAd);
        ArrayList<String> listShift = new ArrayList();
        listShift.add("shift1");
        listShift.add("shift2");
        listShift.add("shift3");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), C0336R.layout.support_simple_spinner_dropdown_item, listShift);
        arrayAdapter.setDropDownViewResource(C0336R.layout.support_simple_spinner_dropdown_item);
        this.sp_shift.setAdapter(arrayAdapter);
        return v;
    }
}
