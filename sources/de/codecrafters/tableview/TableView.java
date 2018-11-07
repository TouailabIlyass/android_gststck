package de.codecrafters.tableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import de.codecrafters.tableview.colorizers.TableDataRowColorizer;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.listeners.TableHeaderClickListener;
import de.codecrafters.tableview.providers.TableDataRowBackgroundProvider;
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TableView<T> extends LinearLayout {
    private static final int DEFAULT_COLUMN_COUNT = 4;
    private static final int DEFAULT_HEADER_COLOR = -3355444;
    private static final int DEFAULT_HEADER_ELEVATION = 1;
    private static final int ID_DATA_VIEW = 101010;
    private static final String LOG_TAG = TableView.class.getName();
    private TableColumnModel columnModel;
    private final Set<TableDataClickListener<T>> dataClickListeners;
    private final Set<TableDataLongClickListener<T>> dataLongClickListeners;
    private TableDataRowBackgroundProvider<? super T> dataRowBackgroundProvider;
    private int headerColor;
    private int headerElevation;
    protected TableDataAdapter<T> tableDataAdapter;
    private ListView tableDataView;
    private TableHeaderAdapter tableHeaderAdapter;
    private TableHeaderView tableHeaderView;

    private class InternalDataClickListener implements OnItemClickListener {
        private InternalDataClickListener() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            informAllListeners(i);
        }

        private void informAllListeners(int rowIndex) {
            T clickedObject = TableView.this.tableDataAdapter.getItem(rowIndex);
            for (TableDataClickListener<T> listener : TableView.this.dataClickListeners) {
                try {
                    listener.onDataClicked(rowIndex, clickedObject);
                } catch (Throwable t) {
                    String access$300 = TableView.LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Caught Throwable on listener notification: ");
                    stringBuilder.append(t.toString());
                    Log.w(access$300, stringBuilder.toString());
                }
            }
        }
    }

    private class InternalDataLongClickListener implements OnItemLongClickListener {
        private InternalDataLongClickListener() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int rowIndex, long id) {
            return informAllListeners(rowIndex);
        }

        private boolean informAllListeners(int rowIndex) {
            T clickedObject = TableView.this.tableDataAdapter.getItem(rowIndex);
            boolean isConsumed = false;
            for (TableDataLongClickListener<T> listener : TableView.this.dataLongClickListeners) {
                try {
                    isConsumed |= listener.onDataLongClicked(rowIndex, clickedObject);
                } catch (Throwable t) {
                    String access$300 = TableView.LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Caught Throwable on listener notification: ");
                    stringBuilder.append(t.toString());
                    Log.w(access$300, stringBuilder.toString());
                }
            }
            return isConsumed;
        }
    }

    private class DefaultTableDataAdapter extends TableDataAdapter<T> {
        public DefaultTableDataAdapter(Context context) {
            super(context, TableView.this.columnModel, new ArrayList());
        }

        public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
            return new TextView(getContext());
        }
    }

    private class DefaultTableHeaderAdapter extends TableHeaderAdapter {
        public DefaultTableHeaderAdapter(Context context) {
            super(context, TableView.this.columnModel);
        }

        public View getHeaderView(int columnIndex, ViewGroup parentView) {
            TextView view = new TextView(getContext());
            view.setText(" ");
            view.setPadding(20, 40, 20, 40);
            return view;
        }
    }

    private class EditModeTableDataAdapter extends TableDataAdapter<T> {
        private static final float TEXT_SIZE = 16.0f;

        public EditModeTableDataAdapter(Context context) {
            super(context, TableView.this.columnModel, new ArrayList());
        }

        public View getCellView(int rowIndex, int columnIndex, ViewGroup parent) {
            TextView textView = new TextView(getContext());
            textView.setText(getResources().getString(C0342R.string.default_cell, new Object[]{Integer.valueOf(columnIndex), Integer.valueOf(rowIndex)}));
            textView.setPadding(20, 10, 20, 10);
            textView.setTextSize(TEXT_SIZE);
            return textView;
        }

        public int getCount() {
            return 50;
        }
    }

    private class EditModeTableHeaderAdapter extends TableHeaderAdapter {
        private static final float TEXT_SIZE = 18.0f;

        public EditModeTableHeaderAdapter(Context context) {
            super(context, TableView.this.columnModel);
        }

        public View getHeaderView(int columnIndex, ViewGroup parentView) {
            TextView textView = new TextView(getContext());
            textView.setText(getResources().getString(C0342R.string.default_header, new Object[]{Integer.valueOf(columnIndex)}));
            textView.setPadding(20, 40, 20, 40);
            textView.setTypeface(textView.getTypeface(), 1);
            textView.setTextSize(TEXT_SIZE);
            return textView;
        }
    }

    public TableView(Context context) {
        this(context, null);
    }

    public TableView(Context context, AttributeSet attributes) {
        this(context, attributes, 16842868);
    }

    public TableView(Context context, AttributeSet attributes, int styleAttributes) {
        super(context, attributes, styleAttributes);
        this.dataLongClickListeners = new HashSet();
        this.dataClickListeners = new HashSet();
        this.dataRowBackgroundProvider = TableDataRowBackgroundProviders.similarRowColor(0);
        setOrientation(1);
        setAttributes(attributes);
        setupTableHeaderView(attributes);
        setupTableDataView(attributes, styleAttributes);
    }

    protected void setHeaderView(TableHeaderView headerView) {
        this.tableHeaderView = headerView;
        this.tableHeaderView.setAdapter(this.tableHeaderAdapter);
        this.tableHeaderView.setBackgroundColor(this.headerColor);
        this.tableHeaderView.setId(C0342R.id.table_header_view);
        if (getChildCount() == 2) {
            removeViewAt(0);
        }
        addView(this.tableHeaderView, 0);
        setHeaderElevation(this.headerElevation);
        forceRefresh();
    }

    public void setHeaderBackground(int resId) {
        this.tableHeaderView.setBackgroundResource(resId);
    }

    public void setHeaderBackgroundColor(int color) {
        this.tableHeaderView.setBackgroundColor(color);
    }

    public void setHeaderElevation(int elevation) {
        ViewCompat.setElevation(this.tableHeaderView, (float) elevation);
    }

    @Deprecated
    public void setDataRowColorizer(TableDataRowColorizer<? super T> colorizer) {
        setDataRowBackgroundProvider(new TableDataRowBackgroundColorProvider(colorizer));
    }

    public void setDataRowBackgroundProvider(TableDataRowBackgroundProvider<? super T> backgroundProvider) {
        this.dataRowBackgroundProvider = backgroundProvider;
        this.tableDataAdapter.setRowBackgroundProvider(this.dataRowBackgroundProvider);
    }

    public void addDataClickListener(TableDataClickListener<T> listener) {
        this.dataClickListeners.add(listener);
    }

    public void addDataLongClickListener(TableDataLongClickListener<T> listener) {
        this.dataLongClickListeners.add(listener);
    }

    @Deprecated
    public void removeTableDataClickListener(TableDataClickListener<T> listener) {
        this.dataClickListeners.remove(listener);
    }

    public void removeDataClickListener(TableDataClickListener<T> listener) {
        this.dataClickListeners.remove(listener);
    }

    public void removeDataLongClickListener(TableDataLongClickListener<T> listener) {
        this.dataLongClickListeners.remove(listener);
    }

    public void addHeaderClickListener(TableHeaderClickListener listener) {
        this.tableHeaderView.addHeaderClickListener(listener);
    }

    @Deprecated
    public void removeHeaderListener(TableHeaderClickListener listener) {
        this.tableHeaderView.removeHeaderClickListener(listener);
    }

    public void removeHeaderClickListener(TableHeaderClickListener listener) {
        this.tableHeaderView.removeHeaderClickListener(listener);
    }

    public TableHeaderAdapter getHeaderAdapter() {
        return this.tableHeaderAdapter;
    }

    public void setHeaderAdapter(TableHeaderAdapter headerAdapter) {
        this.tableHeaderAdapter = headerAdapter;
        this.tableHeaderAdapter.setColumnModel(this.columnModel);
        this.tableHeaderView.setAdapter(this.tableHeaderAdapter);
        forceRefresh();
    }

    public TableDataAdapter<T> getDataAdapter() {
        return this.tableDataAdapter;
    }

    public void setDataAdapter(TableDataAdapter<T> dataAdapter) {
        this.tableDataAdapter = dataAdapter;
        this.tableDataAdapter.setColumnModel(this.columnModel);
        this.tableDataAdapter.setRowBackgroundProvider(this.dataRowBackgroundProvider);
        this.tableDataView.setAdapter(this.tableDataAdapter);
        forceRefresh();
    }

    public int getColumnCount() {
        return this.columnModel.getColumnCount();
    }

    public void setColumnCount(int columnCount) {
        this.columnModel.setColumnCount(columnCount);
        forceRefresh();
    }

    public void setColumnWeight(int columnIndex, int columnWeight) {
        this.columnModel.setColumnWeight(columnIndex, columnWeight);
        forceRefresh();
    }

    public int getColumnWeight(int columnIndex) {
        return this.columnModel.getColumnWeight(columnIndex);
    }

    public void setSaveEnabled(boolean enabled) {
        super.setSaveEnabled(enabled);
        this.tableHeaderView.setSaveEnabled(enabled);
        this.tableDataView.setSaveEnabled(enabled);
    }

    private void forceRefresh() {
        if (this.tableHeaderView != null) {
            this.tableHeaderView.invalidate();
        }
        if (this.tableDataView != null) {
            this.tableDataView.invalidate();
        }
    }

    private void setAttributes(AttributeSet attributes) {
        TypedArray styledAttributes = getContext().obtainStyledAttributes(attributes, C0342R.styleable.TableView);
        this.headerColor = styledAttributes.getInt(C0342R.styleable.TableView_tableView_headerColor, DEFAULT_HEADER_COLOR);
        this.headerElevation = styledAttributes.getInt(C0342R.styleable.TableView_tableView_headerElevation, 1);
        this.columnModel = new TableColumnModel(styledAttributes.getInt(C0342R.styleable.TableView_tableView_columnCount, 4));
        styledAttributes.recycle();
    }

    private void setupTableHeaderView(AttributeSet attributes) {
        if (isInEditMode()) {
            this.tableHeaderAdapter = new EditModeTableHeaderAdapter(getContext());
        } else {
            this.tableHeaderAdapter = new DefaultTableHeaderAdapter(getContext());
        }
        setHeaderView(new TableHeaderView(getContext()));
    }

    private void setupTableDataView(AttributeSet attributes, int styleAttributes) {
        LayoutParams dataViewLayoutParams = new LayoutParams(getWidthAttribute(attributes), -1);
        if (isInEditMode()) {
            this.tableDataAdapter = new EditModeTableDataAdapter(getContext());
        } else {
            this.tableDataAdapter = new DefaultTableDataAdapter(getContext());
        }
        this.tableDataAdapter.setRowBackgroundProvider(this.dataRowBackgroundProvider);
        this.tableDataView = new ListView(getContext(), attributes, styleAttributes);
        this.tableDataView.setOnItemClickListener(new InternalDataClickListener());
        this.tableDataView.setOnItemLongClickListener(new InternalDataLongClickListener());
        this.tableDataView.setLayoutParams(dataViewLayoutParams);
        this.tableDataView.setAdapter(this.tableDataAdapter);
        this.tableDataView.setId(C0342R.id.table_data_view);
        addView(this.tableDataView);
    }

    private int getWidthAttribute(AttributeSet attributes) {
        TypedArray ta = getContext().obtainStyledAttributes(attributes, new int[]{16842996});
        int layoutWidth = ta.getLayoutDimension(0, -1);
        ta.recycle();
        return layoutWidth;
    }
}
