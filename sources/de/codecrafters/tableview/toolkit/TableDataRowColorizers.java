package de.codecrafters.tableview.toolkit;

import de.codecrafters.tableview.colorizers.TableDataRowColorizer;

@Deprecated
public final class TableDataRowColorizers {

    private static class AlternatingTableDataRowColorizer implements TableDataRowColorizer<Object> {
        private final int colorEven;
        private final int colorOdd;

        public AlternatingTableDataRowColorizer(int colorEven, int colorOdd) {
            this.colorEven = colorEven;
            this.colorOdd = colorOdd;
        }

        public int getRowColor(int rowIndex, Object rowData) {
            if (rowIndex % 2 == 0) {
                return this.colorEven;
            }
            return this.colorOdd;
        }
    }

    private static class SimpleTableDataRowColorizer implements TableDataRowColorizer<Object> {
        private final int color;

        public SimpleTableDataRowColorizer(int color) {
            this.color = color;
        }

        public int getRowColor(int rowIndex, Object rowData) {
            return this.color;
        }
    }

    private TableDataRowColorizers() {
    }

    public static TableDataRowColorizer<Object> similarRowColor(int color) {
        return new SimpleTableDataRowColorizer(color);
    }

    public static TableDataRowColorizer<Object> alternatingRows(int colorEvenRows, int colorOddRows) {
        return new AlternatingTableDataRowColorizer(colorEvenRows, colorOddRows);
    }
}
