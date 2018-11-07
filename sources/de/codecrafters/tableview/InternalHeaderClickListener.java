package de.codecrafters.tableview;

import android.view.View;
import android.view.View.OnClickListener;
import de.codecrafters.tableview.listeners.TableHeaderClickListener;
import java.util.Set;

class InternalHeaderClickListener implements OnClickListener {
    private final int columnIndex;
    private final Set<TableHeaderClickListener> listeners;

    public InternalHeaderClickListener(int columnIndex, Set<TableHeaderClickListener> listeners) {
        this.columnIndex = columnIndex;
        this.listeners = listeners;
    }

    public void onClick(View view) {
        informHeaderListeners();
    }

    private void informHeaderListeners() {
        for (TableHeaderClickListener listener : this.listeners) {
            try {
                listener.onHeaderClicked(this.columnIndex);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
