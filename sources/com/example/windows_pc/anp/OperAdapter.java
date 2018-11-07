package com.example.windows_pc.anp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class OperAdapter extends BaseAdapter {
    private ArrayList<Operation> list;
    private Context mc;

    public OperAdapter(Context mc, ArrayList<Operation> list) {
        this.mc = mc;
        this.list = list;
    }

    public int getCount() {
        return this.list.size();
    }

    public Object getItem(int position) {
        return this.list.get(position);
    }

    public long getItemId(int position) {
        return (long) ((Operation) this.list.get(position)).getId();
    }

    public View getView(int i, View convertView, ViewGroup parent) {
        View v = View.inflate(this.mc, C0336R.layout.item_opre, null);
        TextView date = (TextView) v.findViewById(C0336R.id.date_oper);
        TextView shift = (TextView) v.findViewById(C0336R.id.shift);
        TextView navire = (TextView) v.findViewById(C0336R.id.navire);
        TextView type = (TextView) v.findViewById(C0336R.id.type);
        ((TextView) v.findViewById(C0336R.id.operation)).setText(((Operation) this.list.get(i)).getNom());
        date.setText(((Operation) this.list.get(i)).getDate());
        shift.setText(((Operation) this.list.get(i)).getShift());
        navire.setText(((Operation) this.list.get(i)).getNom_navire());
        type.setText(((Operation) this.list.get(i)).getType());
        v.setTag(Integer.valueOf(((Operation) this.list.get(i)).getId()));
        return v;
    }
}
