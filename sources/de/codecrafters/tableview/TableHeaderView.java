package de.codecrafters.tableview;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.codecrafters.tableview.listeners.TableHeaderClickListener;
import java.util.HashSet;
import java.util.Set;

class TableHeaderView extends LinearLayout {
    protected TableHeaderAdapter adapter;
    private final Set<TableHeaderClickListener> listeners = new HashSet();

    public TableHeaderView(Context context) {
        super(context);
        setOrientation(0);
        setGravity(16);
        setLayoutParams(new LayoutParams(-1, -2));
    }

    public void setAdapter(TableHeaderAdapter adapter) {
        this.adapter = adapter;
        renderHeaderViews();
    }

    public void invalidate() {
        renderHeaderViews();
        super.invalidate();
    }

    protected void renderHeaderViews() {
        removeAllViews();
        for (int columnIndex = 0; columnIndex < this.adapter.getColumnCount(); columnIndex++) {
            View headerView = this.adapter.getHeaderView(columnIndex, this);
            if (headerView == null) {
                headerView = new TextView(getContext());
            }
            headerView.setOnClickListener(new InternalHeaderClickListener(columnIndex, getHeaderClickListeners()));
            addView(headerView, new LayoutParams(0, -2, (float) this.adapter.getColumnWeight(columnIndex)));
        }
    }

    protected Set<TableHeaderClickListener> getHeaderClickListeners() {
        return this.listeners;
    }

    public void addHeaderClickListener(TableHeaderClickListener listener) {
        this.listeners.add(listener);
    }

    public void removeHeaderClickListener(TableHeaderClickListener listener) {
        this.listeners.remove(listener);
    }
}
