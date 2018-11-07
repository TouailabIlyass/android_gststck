package de.codecrafters.tableview;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import de.codecrafters.tableview.listeners.TableHeaderClickListener;
import de.codecrafters.tableview.providers.SortStateViewProvider;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SortableTableView<T> extends TableView<T> {
    private static final String LOG_TAG = SortableTableView.class.getName();
    public static final String SAVED_STATE_SORTED_COLUMN = "SAVED_STATE_SORTED_COLUMN";
    public static final String SAVED_STATE_SORTED_DIRECTION = "SAVED_STATE_SORTED_DIRECTION";
    public static final String SAVED_STATE_SUPER_STATE = "SAVED_STATE_SUPER";
    private final SortableTableHeaderView sortableTableHeaderView;
    private final SortingController sortingController;

    private class RecapSortingDataSetObserver extends DataSetObserver {
        private boolean initializedByMyself;

        private RecapSortingDataSetObserver() {
            this.initializedByMyself = null;
        }

        public void onChanged() {
            if (this.initializedByMyself) {
                this.initializedByMyself = false;
                return;
            }
            this.initializedByMyself = true;
            SortableTableView.this.sortingController.recapSorting();
        }
    }

    private class SortingController implements TableHeaderClickListener {
        private final Map<Integer, Comparator<T>> comparators;
        private boolean isSortedUp;
        private Comparator<T> sortedColumnComparator;
        private int sortedColumnIndex;

        private SortingController() {
            this.comparators = new HashMap();
            this.sortedColumnIndex = -1;
        }

        public void onHeaderClicked(int columnIndex) {
            if (this.comparators.containsKey(Integer.valueOf(columnIndex))) {
                this.sortedColumnComparator = getComparator(columnIndex);
                sortDataSFCT(this.sortedColumnComparator);
                setSortView(columnIndex);
                this.sortedColumnIndex = columnIndex;
                return;
            }
            String access$600 = SortableTableView.LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to sort column with index ");
            stringBuilder.append(columnIndex);
            stringBuilder.append(". Reason: no comparator set for this column.");
            Log.i(access$600, stringBuilder.toString());
        }

        public void sort(int columnIndex, boolean sortUp) {
            if (this.comparators.containsKey(Integer.valueOf(columnIndex))) {
                Comparator<T> columnComparator = (Comparator) this.comparators.get(Integer.valueOf(columnIndex));
                if (!sortUp) {
                    columnComparator = Collections.reverseOrder(columnComparator);
                }
                this.sortedColumnComparator = columnComparator;
                this.sortedColumnIndex = columnIndex;
                this.isSortedUp = sortUp;
                sortDataSFCT(columnComparator);
                setSortView(columnIndex);
                return;
            }
            String access$600 = SortableTableView.LOG_TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to sort column with index ");
            stringBuilder.append(columnIndex);
            stringBuilder.append(". Reason: no comparator set for this column.");
            Log.i(access$600, stringBuilder.toString());
        }

        private void setSortView(int columnIndex) {
            SortableTableView.this.sortableTableHeaderView.resetSortViews();
            if (this.isSortedUp) {
                SortableTableView.this.sortableTableHeaderView.setSortState(columnIndex, SortState.SORTED_ASC);
            } else {
                SortableTableView.this.sortableTableHeaderView.setSortState(columnIndex, SortState.SORTED_DESC);
            }
        }

        private void recapSorting() {
            sortDataSFCT(this.sortedColumnComparator);
        }

        private void sortDataSFCT(Comparator<T> comparator) {
            if (comparator != null) {
                Collections.sort(SortableTableView.this.tableDataAdapter.getData(), comparator);
                SortableTableView.this.tableDataAdapter.notifyDataSetChanged();
            }
        }

        private Comparator<T> getRawComparator(int columnIndex) {
            return (Comparator) this.comparators.get(Integer.valueOf(columnIndex));
        }

        private Comparator<T> getComparator(int columnIndex) {
            Comparator<T> columnComparator = (Comparator) this.comparators.get(Integer.valueOf(columnIndex));
            Comparator<T> comparator;
            if (this.sortedColumnIndex == columnIndex) {
                if (this.isSortedUp) {
                    comparator = Collections.reverseOrder(columnComparator);
                } else {
                    comparator = columnComparator;
                }
                this.isSortedUp = true ^ this.isSortedUp;
                return comparator;
            }
            comparator = columnComparator;
            this.isSortedUp = true;
            return comparator;
        }

        public void setComparator(int columnIndex, Comparator<T> columnComparator) {
            if (columnComparator == null) {
                this.comparators.remove(Integer.valueOf(columnIndex));
                SortableTableView.this.sortableTableHeaderView.setSortState(columnIndex, SortState.NOT_SORTABLE);
                return;
            }
            this.comparators.put(Integer.valueOf(columnIndex), columnComparator);
            SortableTableView.this.sortableTableHeaderView.setSortState(columnIndex, SortState.SORTABLE);
        }
    }

    public SortableTableView(Context context) {
        this(context, null);
    }

    public SortableTableView(Context context, AttributeSet attributes) {
        this(context, attributes, 16842868);
    }

    public SortableTableView(Context context, AttributeSet attributes, int styleAttributes) {
        super(context, attributes, styleAttributes);
        this.sortableTableHeaderView = new SortableTableHeaderView(context);
        this.sortableTableHeaderView.setBackgroundColor(-3355444);
        setHeaderView(this.sortableTableHeaderView);
        this.sortingController = new SortingController();
        this.sortableTableHeaderView.addHeaderClickListener(this.sortingController);
    }

    public void setDataAdapter(TableDataAdapter<T> dataAdapter) {
        dataAdapter.registerDataSetObserver(new RecapSortingDataSetObserver());
        super.setDataAdapter(dataAdapter);
    }

    public void setColumnComparator(int columnIndex, Comparator<T> columnComparator) {
        this.sortingController.setComparator(columnIndex, columnComparator);
    }

    public SortStateViewProvider getHeaderSortStateViewProvider() {
        return this.sortableTableHeaderView.getSortStateViewProvider();
    }

    public void setHeaderSortStateViewProvider(SortStateViewProvider provider) {
        this.sortableTableHeaderView.setSortStateViewProvider(provider);
    }

    public Comparator<T> getColumnComparator(int columnIndex) {
        return this.sortingController.getRawComparator(columnIndex);
    }

    public void sort(int columnIndex) {
        this.sortingController.onHeaderClicked(columnIndex);
    }

    public void sort(int columnIndex, boolean sortAscending) {
        this.sortingController.sort(columnIndex, sortAscending);
    }

    public void sort(Comparator<T> comparator) {
        this.sortingController.sortDataSFCT(comparator);
    }

    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(SAVED_STATE_SUPER_STATE, super.onSaveInstanceState());
        state.putBoolean(SAVED_STATE_SORTED_DIRECTION, this.sortingController.isSortedUp);
        state.putInt(SAVED_STATE_SORTED_COLUMN, this.sortingController.sortedColumnIndex);
        return state;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle savedState = (Bundle) state;
            Parcelable superState = savedState.getParcelable(SAVED_STATE_SUPER_STATE);
            boolean wasSortedUp = savedState.getBoolean(SAVED_STATE_SORTED_DIRECTION, false);
            int sortedColumnIndex = savedState.getInt(SAVED_STATE_SORTED_COLUMN, -1);
            super.onRestoreInstanceState(superState);
            this.sortingController.sort(sortedColumnIndex, wasSortedUp);
        }
    }
}
