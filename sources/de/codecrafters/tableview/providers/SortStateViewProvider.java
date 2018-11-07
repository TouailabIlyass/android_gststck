package de.codecrafters.tableview.providers;

import de.codecrafters.tableview.SortState;

public interface SortStateViewProvider {
    public static final int NO_IMAGE = 0;

    int getSortStateViewResource(SortState sortState);
}
