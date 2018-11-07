package de.codecrafters.tableview.colorizers;

@Deprecated
public interface TableDataRowColorizer<T> {
    int getRowColor(int i, T t);
}
