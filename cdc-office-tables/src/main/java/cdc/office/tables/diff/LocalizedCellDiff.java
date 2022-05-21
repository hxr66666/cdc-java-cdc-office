package cdc.office.tables.diff;

import cdc.util.lang.Checks;

/**
 * Association of a CellDiff with a key or an index.
 *
 * @author Damien Carbonne
 */
public class LocalizedCellDiff {
    private final CellDiff diff;
    private final String key;
    private final int index;

    public LocalizedCellDiff(CellDiff diff,
                             String key) {
        this.diff = Checks.isNotNull(diff, "diff");
        this.key = key;
        this.index = -1;
    }

    public LocalizedCellDiff(CellDiff diff,
                             int index) {
        this.diff = Checks.isNotNull(diff, "diff");
        this.key = null;
        this.index = index;
    }

    public CellDiff getDiff() {
        return diff;
    }

    public String getKey() {
        return key;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        if (key == null) {
            return "[" + diff + " " + index + "]";
        } else {
            return "[" + diff + " " + key + "]";
        }
    }
}