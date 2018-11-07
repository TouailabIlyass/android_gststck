package de.codecrafters.tableview;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class TableHeaderAdapter {
    private TableColumnModel columnModel;
    private final Context context;

    public abstract View getHeaderView(int i, ViewGroup viewGroup);

    public TableHeaderAdapter(Context context) {
        this(context, 0);
    }

    protected TableHeaderAdapter(Context context, int columnCount) {
        this(context, new TableColumnModel(columnCount));
    }

    protected TableHeaderAdapter(Context context, TableColumnModel columnModel) {
        this.context = context;
        this.columnModel = columnModel;
    }

    public Context getContext() {
        return this.context;
    }

    public LayoutInflater getLayoutInflater() {
        return (LayoutInflater) getContext().getSystemService("layout_inflater");
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    protected void setColumnModel(TableColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    protected TableColumnModel getColumnModel() {
        return this.columnModel;
    }

    protected void setColumnCount(int columnCount) {
        this.columnModel.setColumnCount(columnCount);
    }

    protected int getColumnCount() {
        return this.columnModel.getColumnCount();
    }

    protected void setColumnWeight(int columnIndex, int columnWeight) {
        this.columnModel.setColumnWeight(columnIndex, columnWeight);
    }

    protected int getColumnWeight(int columnIndex) {
        return this.columnModel.getColumnWeight(columnIndex);
    }

    protected int getColumnWeightSum() {
        return this.columnModel.getColumnWeightSum();
    }
}
