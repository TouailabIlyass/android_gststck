package de.codecrafters.tableview.toolkit;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import de.codecrafters.tableview.providers.TableDataRowBackgroundProvider;

public final class TableDataRowBackgroundProviders {

    private static class AlternatingTableDataRowColorProvider implements TableDataRowBackgroundProvider<Object> {
        private final Drawable colorDrawableEven;
        private final Drawable colorDrawableOdd;

        public AlternatingTableDataRowColorProvider(int colorEven, int colorOdd) {
            this.colorDrawableEven = new ColorDrawable(colorEven);
            this.colorDrawableOdd = new ColorDrawable(colorOdd);
        }

        public Drawable getRowBackground(int rowIndex, Object rowData) {
            if (rowIndex % 2 == 0) {
                return this.colorDrawableEven;
            }
            return this.colorDrawableOdd;
        }
    }

    private static class AlternatingTableDataRowDrawableProvider implements TableDataRowBackgroundProvider<Object> {
        private final Drawable drawableEven;
        private final Drawable drawableOdd;

        public AlternatingTableDataRowDrawableProvider(Drawable drawableEven, Drawable drawableOdd) {
            this.drawableEven = drawableEven;
            this.drawableOdd = drawableOdd;
        }

        public Drawable getRowBackground(int rowIndex, Object rowData) {
            if (rowIndex % 2 == 0) {
                return this.drawableEven;
            }
            return this.drawableOdd;
        }
    }

    private static class SimpleTableDataRowColorProvider implements TableDataRowBackgroundProvider<Object> {
        private final Drawable colorDrawable;

        public SimpleTableDataRowColorProvider(int color) {
            this.colorDrawable = new ColorDrawable(color);
        }

        public Drawable getRowBackground(int rowIndex, Object rowData) {
            return this.colorDrawable;
        }
    }

    private static class SimpleTableDataRowDrawableProvider implements TableDataRowBackgroundProvider<Object> {
        private final Drawable drawable;

        public SimpleTableDataRowDrawableProvider(Drawable drawable) {
            this.drawable = drawable;
        }

        public Drawable getRowBackground(int rowIndex, Object rowData) {
            return this.drawable;
        }
    }

    private TableDataRowBackgroundProviders() {
    }

    public static TableDataRowBackgroundProvider<Object> alternatingRowColors(int colorEvenRows, int colorOddRows) {
        return new AlternatingTableDataRowColorProvider(colorEvenRows, colorOddRows);
    }

    public static TableDataRowBackgroundProvider<Object> alternatingRowDrawables(Drawable drawableEvenRows, Drawable drawableOddRows) {
        return new AlternatingTableDataRowDrawableProvider(drawableEvenRows, drawableOddRows);
    }

    public static TableDataRowBackgroundProvider<Object> similarRowColor(int color) {
        return new SimpleTableDataRowColorProvider(color);
    }

    public static TableDataRowBackgroundProvider<Object> similarRowDrawable(Drawable drawable) {
        return new SimpleTableDataRowDrawableProvider(drawable);
    }
}
