package com.example.windows_pc.anp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class EquipAdapter extends BaseAdapter {
    private boolean isspiner;
    private ArrayList<Equipement> list;
    private Context mc;

    public EquipAdapter(Context mc, ArrayList<Equipement> list, boolean isspiner) {
        this.mc = mc;
        this.list = list;
        this.isspiner = isspiner;
    }

    public int getCount() {
        return this.list.size();
    }

    public Object getItem(int position) {
        return this.list.get(position);
    }

    public long getItemId(int position) {
        return (long) ((Equipement) this.list.get(position)).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.isspiner) {
            View v = View.inflate(this.mc, C0336R.layout.item_equip_sp, null);
            ((TextView) v.findViewById(C0336R.id.type_sp)).setText(((Equipement) this.list.get(position)).getMachine());
            v.setTag(Integer.valueOf(((Equipement) this.list.get(position)).getId()));
            return v;
        }
        v = View.inflate(this.mc, C0336R.layout.item_equip, null);
        TextView ty = (TextView) v.findViewById(C0336R.id.type);
        ((TextView) v.findViewById(C0336R.id.machine)).setText(((Equipement) this.list.get(position)).getMachine());
        ty.setText(((Equipement) this.list.get(position)).getType());
        v.setTag(Integer.valueOf(((Equipement) this.list.get(position)).getId()));
        return v;
    }
}
