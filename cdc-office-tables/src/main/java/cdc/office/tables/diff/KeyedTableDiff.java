package cdc.office.tables.diff;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdc.office.tables.Header;
import cdc.office.tables.Row;
import cdc.tuples.CTupleN;
import cdc.tuples.TupleN;
import cdc.util.lang.Checks;
import cdc.util.lang.InvalidDataException;
import cdc.util.lang.UnexpectedValueException;

/**
 * Class used to compare two lists of rows identified by a key set.
 *
 * @author Damien Carbonne
 *
 */
public class KeyedTableDiff {
    // private static final Logger LOGGER = LogManager.getLogger(KeyedTableDiff.class);
    private final Header leftHeader;
    private final Header rightHeader;
    private final List<String> keyNames;
    private final int[] leftKeyColumns;
    private final int[] rightKeyColumns;
    private final List<CTupleN<String>> keys = new ArrayList<>();
    private final Map<CTupleN<String>, RowDiff> diffs = new HashMap<>();

    public KeyedTableDiff(Header leftHeader,
                          List<Row> leftData,
                          Header rightHeader,
                          List<Row> rightData,
                          String... keyNames) {
        Checks.isNotNull(leftHeader, "leftHeader");
        Checks.isNotNull(leftData, "leftData");
        Checks.isNotNull(rightHeader, "rightHeader");
        Checks.isNotNull(rightData, "rightData");
        Checks.isNotNullOrEmpty(keyNames, "keys");

        this.leftHeader = leftHeader;
        this.rightHeader = rightHeader;
        this.keyNames = Arrays.asList(keyNames);

        this.leftKeyColumns = buildKeyColumns(leftHeader, keyNames);
        this.rightKeyColumns = buildKeyColumns(rightHeader, keyNames);

        // Map from tuples to left rows
        final Map<CTupleN<String>, Row> leftMap = new HashMap<>();
        for (int number = 0; number < leftData.size(); number++) {
            final Row row = leftData.get(number);
            final CTupleN<String> key = getKey(Side.LEFT, row, number);
            if (leftMap.containsKey(key)) {
                throw new InvalidDataException("Duplicate key " + key + locate(Side.LEFT, row, number));
            }
            leftMap.put(key, row);
        }

        // Compare right rows to left ones
        for (int number = 0; number < rightData.size(); number++) {
            final Row right = rightData.get(number);
            final CTupleN<String> key = getKey(Side.RIGHT, right, number);
            final Row left = leftMap.getOrDefault(key, Row.EMPTY);
            if (diffs.containsKey(key)) {
                throw new InvalidDataException("Duplicate key " + key + locate(Side.RIGHT, right, number));
            }

            final RowDiff diff = new RowDiff(leftHeader, left, rightHeader, right);
            // LOGGER.info("{} -> {}", key, diff);
            diffs.put(key, diff);
            keys.add(key);
        }

        // Add all left rows that are not in right rows
        for (int number = 0; number < leftData.size(); number++) {
            final Row left = leftData.get(number);
            final CTupleN<String> key = getKey(Side.LEFT, left, number);
            if (!diffs.containsKey(key)) {
                diffs.put(key, new RowDiff(leftHeader, left, rightHeader, Row.EMPTY));
                keys.add(key);
            }
        }
    }

    public KeyedTableDiff(Header leftHeader,
                          List<Row> leftData,
                          Header rightHeader,
                          List<Row> rightData,
                          List<String> keyNames) {
        this(leftHeader,
             leftData,
             rightHeader,
             rightData,
             keyNames.toArray(new String[keyNames.size()]));
    }

    public KeyedTableDiff(Header header,
                          List<Row> leftData,
                          List<Row> rightData,
                          String... keyNames) {
        this(header,
             leftData,
             header,
             rightData,
             keyNames);
    }

    public KeyedTableDiff(Header header,
                          List<Row> leftData,
                          List<Row> rightData,
                          List<String> keyNames) {
        this(header,
             leftData,
             rightData,
             keyNames.toArray(new String[keyNames.size()]));
    }

    private int[] getKeyColumns(Side side) {
        return side == Side.LEFT ? leftKeyColumns : rightKeyColumns;
    }

    private static int[] buildKeyColumns(Header header,
                                         String... keyNames) {
        final int[] result = new int[keyNames.length];
        for (int index = 0; index < keyNames.length; index++) {
            final int column = header.getIndex(keyNames[index]);
            if (column < 0) {
                throw new IllegalArgumentException("Key '" + keyNames[index] + "' missing in " + header);
            }
            result[index] = column;
        }
        return result;
    }

    private static String locate(Side side,
                                 Row row,
                                 int number) {
        return " in " + side + " row " + row + ", line " + (number + 2);
    }

    private CTupleN<String> getKey(Side side,
                                   Row row,
                                   int number) {
        final int[] keyColumns = getKeyColumns(side);
        final String[] values = new String[keyColumns.length];
        for (int index = 0; index < keyColumns.length; index++) {
            final int column = keyColumns[index];
            final String value = row.getValue(column);
            values[index] = value;
        }
        return new CTupleN<>(values);
    }

    public Header getHeader(Side side) {
        return side == Side.LEFT ? leftHeader : rightHeader;
    }

    public List<String> getKeyNames() {
        return keyNames;
    }

    public List<CTupleN<String>> getKeys() {
        return keys;
    }

    public RowDiff getDiff(TupleN<String> key) {
        return diffs.get(key);
    }

    public Collection<RowDiff> getDiffs() {
        return diffs.values();
    }

    /**
     * Prints a synthesis of this diff.
     *
     * @param out The PrintStream.
     */
    public void printSynthesis(PrintStream out) {
        int addedLines = 0;
        int removedLines = 0;
        int unchangedLines = 0;
        int changedLines = 0;
        int addedCells = 0;
        int removedCells = 0;
        int unchangedCells = 0;
        int changedCells = 0;

        for (final RowDiff rdiff : getDiffs()) {
            switch (rdiff.getKind()) {
            case ADDED:
                addedLines++;
                break;
            case CHANGED:
                changedLines++;
                for (final CellDiff cdiff : rdiff.getDiffs()) {
                    switch (cdiff.getKind()) {
                    case ADDED:
                        addedCells++;
                        break;
                    case CHANGED:
                        changedCells++;
                        break;
                    case REMOVED:
                        removedCells++;
                        break;
                    case SAME:
                    case NULL:
                        unchangedCells++;
                        break;
                    default:
                        throw new UnexpectedValueException(cdiff.getKind());
                    }
                }
                break;
            case REMOVED:
                removedLines++;
                break;
            case SAME:
                unchangedLines++;
                break;
            default:
                throw new UnexpectedValueException(rdiff.getKind());
            }
        }
        out.println("Lines");
        out.println("   Added:     " + addedLines);
        out.println("   Removed:   " + removedLines);
        out.println("   Changed:   " + changedLines);
        out.println("   Unchanged: " + unchangedLines);
        out.println("Cells");
        out.println("   Added:     " + addedCells);
        out.println("   Removed:   " + removedCells);
        out.println("   Changed:   " + changedCells);
        out.println("   Unchanged: " + unchangedCells);
    }
}