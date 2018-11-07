package de.codecrafters.tableview;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.codecrafters.tableview.providers.TableDataRowBackgroundProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TableDataAdapter<T> extends ArrayAdapter<T> {
    private static final String LOG_TAG = TableDataAdapter.class.getName();
    private TableColumnModel columnModel;
    private final List<T> data;
    private TableDataRowBackgroundProvider<? super T> rowBackgroundProvider;

    public abstract View getCellView(int i, int i2, ViewGroup viewGroup);

    public TableDataAdapter(Context context, T[] data) {
        this(context, 0, new ArrayList(Arrays.asList(data)));
    }

    public TableDataAdapter(Context context, List<T> data) {
        this(context, 0, (List) data);
    }

    protected TableDataAdapter(Context context, int columnCount, List<T> data) {
        this(context, new TableColumnModel(columnCount), (List) data);
    }

    protected TableDataAdapter(Context context, TableColumnModel columnModel, List<T> data) {
        super(context, -1, data);
        this.columnModel = columnModel;
        this.data = data;
    }

    public T getRowData(int rowIndex) {
        return getItem(rowIndex);
    }

    public List<T> getData() {
        return this.data;
    }

    public Context getContext() {
        return super.getContext();
    }

    public LayoutInflater getLayoutInflater() {
        return (LayoutInflater) getContext().getSystemService("layout_inflater");
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public final View getView(int rowIndex, View convertView, ViewGroup parent) {
        LinearLayout rowView = new LinearLayout(getContext());
        rowView.setLayoutParams(new LayoutParams(-1, -2));
        rowView.setGravity(16);
        T rowData = null;
        try {
            rowData = getItem(rowIndex);
        } catch (IndexOutOfBoundsException e) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No row date available for row with index ");
            stringBuilder.append(rowIndex);
            stringBuilder.append(". ");
            stringBuilder.append("Caught Exception: ");
            stringBuilder.append(e.getMessage());
            Log.w(str, stringBuilder.toString());
        }
        if (VERSION.SDK_INT < 16) {
            rowView.setBackgroundDrawable(this.rowBackgroundProvider.getRowBackground(rowIndex, rowData));
        } else {
            rowView.setBackground(this.rowBackgroundProvider.getRowBackground(rowIndex, rowData));
        }
        int widthUnit = parent.getWidth() / this.columnModel.getColumnWeightSum();
        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
            View cellView = getCellView(rowIndex, columnIndex, rowView);
            if (cellView == null) {
                cellView = new TextView(getContext());
            }
            LinearLayout.LayoutParams cellLayoutParams = new LinearLayout.LayoutParams(this.columnModel.getColumnWeight(columnIndex) * widthUnit, -2);
            cellLayoutParams.weight = (float) this.columnModel.getColumnWeight(columnIndex);
            cellView.setLayoutParams(cellLayoutParams);
            rowView.addView(cellView);
        }
        return rowView;
    }

    protected void setRowBackgroundProvider(TableDataRowBackgroundProvider<? super T> rowbackgroundProvider) {
        this.rowBackgroundProvider = rowbackgroundProvider;
    }

    protected TableColumnModel getColumnModel() {
        return this.columnModel;
    }

    protected void setColumnModel(TableColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    protected int getColumnCount() {
        return this.columnModel.getColumnCount();
    }

    protected void setColumnCount(int columnCount) {
        this.columnModel.setColumnCount(columnCount);
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
