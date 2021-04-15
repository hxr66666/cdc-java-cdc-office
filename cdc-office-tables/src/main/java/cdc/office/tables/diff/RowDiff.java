package cdc.office.tables.diff;

import java.util.ArrayList;
import java.util.List;

import cdc.office.tables.Header;
import cdc.office.tables.Row;
import cdc.util.lang.Checks;
import cdc.util.lang.UnexpectedValueException;

/**
 * Class used to compare two rows.
 *
 * @author Damien Carbonne
 *
 */
public class RowDiff {
    private final List<CellDiff> diffs = new ArrayList<>();

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
        Checks.isTrue(leftRow.getColumnsCount() <= leftHeader.size(), "left row is too large");
        Checks.isTrue(rightRow.getColumnsCount() <= rightHeader.size(), "right row is too large");

        for (int rightCol = 0; rightCol < rightHeader.size(); rightCol++) {
            final String name = rightHeader.getNames().get(rightCol);
            final int leftCol = leftHeader.getIndex(name);
            if (leftCol >= 0) {
                diffs.add(new CellDiff(leftRow.getValue(leftCol), rightRow.getValue(rightCol, null)));
            } else {
                diffs.add(new CellDiff(null, rightRow.getValue(rightCol, null)));
            }
        }

        for (int leftCol = 0; leftCol < leftRow.getColumnsCount(); leftCol++) {
            final String name = leftHeader.getNames().get(leftCol);
            if (!rightHeader.contains(name)) {
                diffs.add(new CellDiff(leftRow.getValue(leftCol), null));
            }
        }
    }

    /**
     * Compares cells of two rows.
     * <p>
     * Cells with the same column number are compared.
     *
     * @param leftRow The left row.
     * @param rightRow The right row.
     */
    public RowDiff(Row leftRow,
                   Row rightRow) {
        final int min = Math.min(leftRow.getColumnsCount(), rightRow.getColumnsCount());
        final int max = Math.max(leftRow.getColumnsCount(), rightRow.getColumnsCount());

        for (int column = 0; column < min; column++) {
            diffs.add(new CellDiff(leftRow.getValue(column), rightRow.getValue(column)));
        }
        if (max > min) {
            if (leftRow.getColumnsCount() == max) {
                for (int col = min; col < max; col++) {
                    diffs.add(new CellDiff(leftRow.getValue(col), null));
                }
            } else {
                for (int col = min; col < max; col++) {
                    diffs.add(new CellDiff(null, rightRow.getValue(col)));
                }
            }
        }
    }

    public List<CellDiff> getDiffs() {
        return diffs;
    }

    public boolean containsDifferences() {
        for (final CellDiff diff : diffs) {
            if (diff.getKind() != DiffKind.SAME) {
                return true;
            }
        }
        return false;
    }

    public DiffKind getKind() {
        int added = 0;
        int changed = 0;
        int same = 0;
        int removed = 0;
        for (final CellDiff diff : diffs) {
            switch (diff.getKind()) {
            case ADDED:
                added++;
                break;
            case CHANGED:
                changed++;
                break;
            case REMOVED:
                removed++;
                break;
            case SAME:
                same++;
                break;
            default:
                throw new UnexpectedValueException(diff.getKind());
            }
        }
        final int total = added + changed + same + removed;
        if (added == total) {
            return DiffKind.ADDED;
        } else if (removed == total) {
            return DiffKind.REMOVED;
        } else if (same == total || total == 0) {
            return DiffKind.SAME;
        } else {
            return DiffKind.CHANGED;
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append('[');
        builder.append(getKind());
        for (final CellDiff diff : getDiffs()) {
            builder.append(' ').append(diff);
        }
        builder.append(']');
        return builder.toString();
    }
}