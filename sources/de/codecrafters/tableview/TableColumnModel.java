package de.codecrafters.tableview;

import java.util.HashMap;
import java.util.Map;

public class TableColumnModel {
    private static final int DEFAULT_COLUMN_WEIGHT = 1;
    private int columnCount;
    private final Map<Integer, Integer> columnWeights = new HashMap();

    public TableColumnModel(int columnCount) {
        this.columnCount = columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public void setColumnWeight(int columnIndex, int columnWeight) {
        this.columnWeights.put(Integer.valueOf(columnIndex), Integer.valueOf(columnWeight));
    }

    public int getColumnWeight(int columnIndex) {
        Integer columnWeight = (Integer) this.columnWeights.get(Integer.valueOf(columnIndex));
        if (columnWeight == null) {
            columnWeight = Integer.valueOf(1);
        }
        return columnWeight.intValue();
    }

    public int getColumnWeightSum() {
        int weightSum = 0;
        for (int columnIndex = 0; columnIndex < this.columnCount; columnIndex++) {
            weightSum += getColumnWeight(columnIndex);
        }
        return weightSum;
    }
}
