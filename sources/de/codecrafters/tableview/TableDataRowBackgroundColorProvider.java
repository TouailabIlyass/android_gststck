package de.codecrafters.tableview;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import de.codecrafters.tableview.colorizers.TableDataRowColorizer;
import de.codecrafters.tableview.providers.TableDataRowBackgroundProvider;

@Deprecated
class TableDataRowBackgroundColorProvider<T> implements TableDataRowBackgroundProvider<T> {
    private final TableDataRowColorizer<T> colorizer;

    public TableDataRowBackgroundColorProvider(TableDataRowColorizer<T> colorizer) {
        this.colorizer = colorizer;
    }

    public Drawable getRowBackground(int rowIndex, T rowData) {
        return new ColorDrawable(this.colorizer.getRowColor(rowIndex, rowData));
    }
}
