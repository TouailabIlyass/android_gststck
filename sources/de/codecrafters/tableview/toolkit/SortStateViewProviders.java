package de.codecrafters.tableview.toolkit;

import de.codecrafters.tableview.C0342R;
import de.codecrafters.tableview.SortState;
import de.codecrafters.tableview.providers.SortStateViewProvider;

public final class SortStateViewProviders {

    private static class BrightSortStateViewProvider implements SortStateViewProvider {
        private BrightSortStateViewProvider() {
        }

        public int getSortStateViewResource(SortState state) {
            switch (state) {
                case SORTABLE:
                    return C0342R.mipmap.ic_light_sortable;
                case SORTED_ASC:
                    return C0342R.mipmap.ic_light_sorted_asc;
                case SORTED_DESC:
                    return C0342R.mipmap.ic_light_sorted_desc;
                default:
                    return 0;
            }
        }
    }

    private static class DarkSortStateViewProvider implements SortStateViewProvider {
        private DarkSortStateViewProvider() {
        }

        public int getSortStateViewResource(SortState state) {
            switch (state) {
                case SORTABLE:
                    return C0342R.mipmap.ic_dark_sortable;
                case SORTED_ASC:
                    return C0342R.mipmap.ic_dark_sorted_asc;
                case SORTED_DESC:
                    return C0342R.mipmap.ic_dark_sorted_desc;
                default:
                    return 0;
            }
        }
    }

    private SortStateViewProviders() {
    }

    public static SortStateViewProvider darkArrows() {
        return new DarkSortStateViewProvider();
    }

    public static SortStateViewProvider brightArrows() {
        return new BrightSortStateViewProvider();
    }
}
