package de.codecrafters.tableview;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.codecrafters.tableview.providers.SortStateViewProvider;
import de.codecrafters.tableview.toolkit.SortStateViewProviders;
import java.util.HashMap;
import java.util.Map;

class SortableTableHeaderView extends TableHeaderView {
    private static final String LOG_TAG = SortableTableHeaderView.class.toString();
    private SortStateViewProvider sortStateViewProvider = SortStateViewProviders.darkArrows();
    private final Map<Integer, SortState> sortStates = new HashMap();
    private final Map<Integer, ImageView> sortViews = new HashMap();

    public SortableTableHeaderView(Context context) {
        super(context);
    }

    public void resetSortViews() {
        for (Integer column : this.sortStates.keySet()) {
            int column2 = column.intValue();
            SortState columnSortState = (SortState) this.sortStates.get(Integer.valueOf(column2));
            if (columnSortState != SortState.NOT_SORTABLE) {
                columnSortState = SortState.SORTABLE;
            }
            this.sortStates.put(Integer.valueOf(column2), columnSortState);
        }
        for (Integer column3 : this.sortStates.keySet()) {
            column2 = column3.intValue();
            ImageView sortView = (ImageView) this.sortViews.get(Integer.valueOf(column2));
            int imageRes = this.sortStateViewProvider.getSortStateViewResource((SortState) this.sortStates.get(Integer.valueOf(column2)));
            sortView.setImageResource(imageRes);
            if (imageRes == 0) {
                sortView.setVisibility(8);
            } else {
                sortView.setVisibility(0);
            }
        }
    }

    public void setSortState(int columnIndex, SortState state) {
        ImageView sortView = (ImageView) this.sortViews.get(Integer.valueOf(columnIndex));
        if (sortView == null) {
            String str = LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SortView not found for columnIndex with index ");
            stringBuilder.append(columnIndex);
            Log.e(str, stringBuilder.toString());
            return;
        }
        this.sortStates.put(Integer.valueOf(columnIndex), state);
        int imageRes = this.sortStateViewProvider.getSortStateViewResource(state);
        sortView.setImageResource(imageRes);
        if (imageRes == 0) {
            sortView.setVisibility(8);
        } else {
            sortView.setVisibility(0);
        }
    }

    public SortStateViewProvider getSortStateViewProvider() {
        return this.sortStateViewProvider;
    }

    public void setSortStateViewProvider(SortStateViewProvider provider) {
        this.sortStateViewProvider = provider;
        resetSortViews();
    }

    protected void renderHeaderViews() {
        removeAllViews();
        for (int columnIndex = 0; columnIndex < this.adapter.getColumnCount(); columnIndex++) {
            LinearLayout headerContainerLayout = (LinearLayout) this.adapter.getLayoutInflater().inflate(C0342R.layout.sortable_header, this, false);
            headerContainerLayout.setOnClickListener(new InternalHeaderClickListener(columnIndex, getHeaderClickListeners()));
            View headerView = this.adapter.getHeaderView(columnIndex, headerContainerLayout);
            if (headerView == null) {
                headerView = new TextView(getContext());
            }
            ((FrameLayout) headerContainerLayout.findViewById(C0342R.id.container)).addView(headerView);
            int imageRes = this.sortStateViewProvider.getSortStateViewResource(SortState.NOT_SORTABLE);
            ImageView sortView = (ImageView) headerContainerLayout.findViewById(C0342R.id.sort_view);
            sortView.setImageResource(imageRes);
            if (imageRes == 0) {
                sortView.setVisibility(8);
            } else {
                sortView.setVisibility(0);
            }
            this.sortViews.put(Integer.valueOf(columnIndex), sortView);
            addView(headerContainerLayout, new LayoutParams(0, -2, (float) this.adapter.getColumnWeight(columnIndex)));
        }
        resetSortViews();
    }
}
