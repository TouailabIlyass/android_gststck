package com.example.windows_pc.anp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class PersAdapter extends BaseAdapter {
    private boolean isspiner;
    private ArrayList<Personnel> list;
    private Context mc;

    public PersAdapter(Context mc, ArrayList<Personnel> list, boolean isspiner) {
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
        return (long) ((Personnel) this.list.get(position)).getMat();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.isspiner) {
            View v = View.inflate(this.mc, C0336R.layout.item_pers_sp, null);
            TextView func = (TextView) v.findViewById(C0336R.id.func_pers_sp);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(((Personnel) this.list.get(position)).getNom());
            stringBuilder.append(" ");
            stringBuilder.append(((Personnel) this.list.get(position)).getPrenom());
            func.setText(stringBuilder.toString());
            v.setTag(Integer.valueOf(((Personnel) this.list.get(position)).getMat()));
            return v;
        }
        v = View.inflate(this.mc, C0336R.layout.item_pers, null);
        TextView lastname = (TextView) v.findViewById(C0336R.id.pers_prenom);
        TextView func2 = (TextView) v.findViewById(C0336R.id.pers_fonct);
        ((TextView) v.findViewById(C0336R.id.pers_nom)).setText(((Personnel) this.list.get(position)).getNom());
        lastname.setText(((Personnel) this.list.get(position)).getPrenom());
        func2.setText(((Personnel) this.list.get(position)).getFonction());
        v.setTag(Integer.valueOf(((Personnel) this.list.get(position)).getMat()));
        return v;
    }
}
