package cdc.office.tables.diff;

import java.util.ArrayList;
import java.util.List;

import cdc.office.tables.Header;
import cdc.office.tables.Row;
import cdc.util.lang.Checks;

/**
 * Class used to compare two rows.
 *
 * @author Damien Carbonne
 */
public class RowDiff {
    // private static final Logger LOGGER = LogManager.getLogger(RowDiff.class);
    private final List<CellDiff> diffs = new ArrayList<>();

    /**
     * Compare cells that have the same header name.
     *
     * @param leftHeader The left header.
     * @param leftRow The left row.
     * @param rightHeader The right header.
     * @param rightRow The right row.
     * @throws IllegalArgumentException When {@code leftRow} has more elements than {@code leftHeader}
     *             or {@code rightRow} has more elements than {@code rightHeader}.
     */
    public RowDiff(Header leftHeader,
                   Row leftRow,
                   Header rightHeader,
                   Row rightRow) {
        Checks.isTrue(leftRow.size() <= leftHeader.size(), "left row is too large");
        Checks.isTrue(rightRow.size() <= rightHeader.size(), "right row is too large");

        // Start comparison with right names
        for (int rightCol = 0; rightCol < rightHeader.size(); rightCol++) {
            final String name = rightHeader.getNameAt(rightCol);
            final int leftCol = leftHeader.getIndex(name);
            final CellDiff diff;
            if (leftCol >= 0) {
                diff = new CellDiff(leftRow.getValue(leftCol), rightRow.getValue(rightCol, null));
            } else {
                diff = new CellDiff(null, rightRow.getValue(rightCol, null));
            }
            // LOGGER.info("right {}: {}", name, diff);
            diffs.add(diff);
        }

        // Finish comparison with left specific names
        for (int leftCol = 0; leftCol < leftHeader.size(); leftCol++) {
            final String name = leftHeader.getNameAt(leftCol);
            if (!rightHeader.contains(name)) {
                final CellDiff diff = new CellDiff(leftRow.getValue(leftCol), null);
                // LOGGER.info("specific left {}: {}", name, diff);
                diffs.add(diff);
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
            diffs.add(new CellDiff(leftRow.getValue(column), rightRow.getValue(column)));
        }
        if (max > min) {
            if (leftRow.size() == max) {
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
            if (diff.getKind().isNeitherSameNorNull()) {
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
        int nul = 0;
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
            case NULL:
                nul++;
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
        for (final CellDiff diff : getDiffs()) {
            builder.append(' ').append(diff);
        }
        builder.append(']');
        return builder.toString();
    }
}