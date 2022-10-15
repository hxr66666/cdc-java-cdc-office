package cdc.office.tables.diff;

import java.util.ArrayList;
import java.util.List;

import cdc.office.tables.Header;
import cdc.office.tables.Row;

/**
 * Class used to compare two rows.
 *
 * @author Damien Carbonne
 */
public class RowDiff {
    private final List<LocalizedCellDiff> diffs = new ArrayList<>();

    /**
     * Compare cells that have the same header name.
     *
     * @param leftHeader The left header.
     * @param leftRow The left row.
     * @param rightHeader The right header.
     * @param rightRow The right row.
     */
    public RowDiff(Header leftHeader,
                   Row leftRow,
                   Header rightHeader,
                   Row rightRow) {
        // Start comparison with right names
        for (int rightCol = 0; rightCol < rightHeader.size(); rightCol++) {
            final String name = rightHeader.getNameAt(rightCol);
            final int leftCol = leftHeader.getIndex(name);
            final CellDiff diff;
            if (leftCol >= 0) {
                // column name is present left and right
                diff = new CellDiff(leftRow.getValue(leftCol), rightRow.getValue(rightCol, null));
            } else {
                // column name is specific to right
                diff = new CellDiff(null, rightRow.getValue(rightCol, null));
            }
            diffs.add(new LocalizedCellDiff(diff, name));
        }

        // Finish comparison with left specific names
        for (int leftCol = 0; leftCol < leftHeader.size(); leftCol++) {
            final String name = leftHeader.getNameAt(leftCol);
            if (!rightHeader.contains(name)) {
                // column name is specific to left
                final CellDiff diff = new CellDiff(leftRow.getValue(leftCol), null);
                diffs.add(new LocalizedCellDiff(diff, name));
            }
        }
    }

    /**
     * Compares cells of two rows.
     * <p>
     * Cells with the same column index are compared.
     *
     * @param leftRow The left row.
     * @param rightRow The right row.
     */
    public RowDiff(Row leftRow,
                   Row rightRow) {
        final int min = Math.min(leftRow.size(), rightRow.size());
        final int max = Math.max(leftRow.size(), rightRow.size());

        for (int column = 0; column < min; column++) {
            final CellDiff diff = new CellDiff(leftRow.getValue(column), rightRow.getValue(column));
            diffs.add(new LocalizedCellDiff(diff, column));
        }
        if (max > min) {
            if (leftRow.size() == max) {
                for (int col = min; col < max; col++) {
                    final CellDiff diff = new CellDiff(leftRow.getValue(col), null);
                    diffs.add(new LocalizedCellDiff(diff, col));
                }
            } else {
                for (int col = min; col < max; col++) {
                    final CellDiff diff = new CellDiff(null, rightRow.getValue(col));
                    diffs.add(new LocalizedCellDiff(diff, col));
                }
            }
        }
    }

    public List<LocalizedCellDiff> getDiffs() {
        return diffs;
    }

    public boolean containsDifferences() {
        for (final LocalizedCellDiff diff : diffs) {
            if (diff.getDiff().getKind().isNeitherSameNorNull()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the row difference kind.
     * <table>
     * <caption></caption>
     * <thead>
     * <tr>
     * <td>NULL</td>
     * <td>SAME</td>
     * <td>CHANGED</td>
     * <td>ADDED</td>
     * <td>REMOVED</td>
     * <td></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>*</td>
     * <td>*</td>
     * <td>0</td>
     * <td>0</td>
     * <td>0</td>
     * <td>SAME</td>
     * </tr>
     * <tr>
     * <td>*</td>
     * <td>0</td>
     * <td>0</td>
     * <td>+</td>
     * <td>0</td>
     * <td>ADDED</td>
     * </tr>
     * <tr>
     * <td>*</td>
     * <td>0</td>
     * <td>0</td>
     * <td>0</td>
     * <td>+</td>
     * <td>REMOVED</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td></td>
     * <td></td>
     * <td></td>
     * <td></td>
     * <td>CHANGED</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @return The row difference kind.
     */
    public RowDiffKind getKind() {
        int added = 0;
        int changed = 0;
        int same = 0;
        int removed = 0;
        for (final LocalizedCellDiff diff : diffs) {
            switch (diff.getDiff().getKind()) {
            case ADDED:
                added++;
                break;
            case CHANGED:
                changed++;
                break;
            case REMOVED:
                removed++;
                break;
            case NULL:
                break;
            case SAME:
                same++;
                break;
            }
        }
        if (changed == 0) {
            if (added == 0 && removed == 0) {
                return RowDiffKind.SAME;
            } else if (same == 0) {
                if (added > 0 && removed == 0) {
                    return RowDiffKind.ADDED;
                } else if (added == 0/* && removed > 0 */) { // removed > 0
                    return RowDiffKind.REMOVED;
                } else {
                    return RowDiffKind.CHANGED;
                }
            } else {
                return RowDiffKind.CHANGED;
            }
        } else {
            return RowDiffKind.CHANGED;
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append('[');
        builder.append(getKind());
        for (final LocalizedCellDiff diff : getDiffs()) {
            builder.append(' ').append(diff);
        }
        builder.append(']');
        return builder.toString();
    }
}