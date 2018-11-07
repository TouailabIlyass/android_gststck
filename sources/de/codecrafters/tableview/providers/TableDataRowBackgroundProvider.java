package de.codecrafters.tableview.providers;

import android.graphics.drawable.Drawable;

public interface TableDataRowBackgroundProvider<T> {
    Drawable getRowBackground(int i, T t);
}
