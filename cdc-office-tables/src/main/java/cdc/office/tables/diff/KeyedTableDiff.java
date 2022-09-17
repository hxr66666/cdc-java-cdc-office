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
 * Class used to compare two lists of rows identified by a set of keys.
 *
 * @author Damien Carbonne
 */
public class KeyedTableDiff {
    /** The left header. */
    private final Header leftHeader;
    /** The right header. */
    private final Header rightHeader;
    /** The names of key columns. */
    private final List<String> keyNames;
    /** Indices in left header of key columns. */
    private final int[] leftKeyColumns;
    /** Indices in right header of key columns. */
    private final int[] rightKeyColumns;
    /**
     * The list of all keys.
     * <p>
     * Some may be left or right only, some may be common.
     */
    private final List<CTupleN<String>> keys = new ArrayList<>();
    /** Maps from keys to row differences. */
    private final Map<CTupleN<String>, RowDiff> diffs = new HashMap<>();
    /** The synthesis of differences. */
    private final Synthesis synthesis = new Synthesis();

    /**
     * Creates a KeyedTableDiff.
     *
     * @param leftHeader The left header.
     * @param leftRows The left rows.
     * @param rightHeader the right header.
     * @param rightRows The right rows.
     * @param keyNames The key names.
     */
    public KeyedTableDiff(Header leftHeader,
                          List<Row> leftRows,
                          Header rightHeader,
                          List<Row> rightRows,
                          String... keyNames) {
        Checks.isNotNull(leftHeader, "leftHeader");
        Checks.isNotNull(leftRows, "leftRows");
        Checks.isNotNull(rightHeader, "rightHeader");
        Checks.isNotNull(rightRows, "rightRows");
        Checks.isNotNullOrEmpty(keyNames, "keys");

        this.leftHeader = leftHeader;
        this.rightHeader = rightHeader;
        this.keyNames = Arrays.asList(keyNames);

        this.leftKeyColumns = buildKeyColumns(leftHeader, keyNames);
        this.rightKeyColumns = buildKeyColumns(rightHeader, keyNames);

        // Map from left keys to left rows
        final Map<CTupleN<String>, Row> leftMap = new HashMap<>();
        for (int number = 0; number < leftRows.size(); number++) {
            final Row row = leftRows.get(number);
            final CTupleN<String> key = getKey(Side.LEFT, row, number);
            if (leftMap.containsKey(key)) {
                throw new InvalidDataException("Duplicate key " + key + locate(Side.LEFT, row, number));
            }
            leftMap.put(key, row);
        }

        // Compare right rows to left ones
        for (int number = 0; number < rightRows.size(); number++) {
            final Row right = rightRows.get(number);
            final CTupleN<String> key = getKey(Side.RIGHT, right, number);
            final Row left = leftMap.getOrDefault(key, Row.EMPTY);
            if (diffs.containsKey(key)) {
                throw new InvalidDataException("Duplicate key " + key + locate(Side.RIGHT, right, number));
            }

            final RowDiff diff = new RowDiff(leftHeader, left, rightHeader, right);
            diffs.put(key, diff);
            keys.add(key);
        }

        // Add all left rows that are not in right rows
        for (int number = 0; number < leftRows.size(); number++) {
            final Row left = leftRows.get(number);
            final CTupleN<String> key = getKey(Side.LEFT, left, number);
            diffs.computeIfAbsent(key, k -> {
                keys.add(k);
                return new RowDiff(leftHeader, left, rightHeader, Row.EMPTY);
            });
        }

        this.synthesis.compute(this);
    }

    /**
     * Creates a KeyedTableDiff.
     *
     * @param leftHeader The left header.
     * @param leftRows The left rows.
     * @param rightHeader the right header.
     * @param rightRows The right rows.
     * @param keyNames The key names.
     */
    public KeyedTableDiff(Header leftHeader,
                          List<Row> leftRows,
                          Header rightHeader,
                          List<Row> rightRows,
                          List<String> keyNames) {
        this(leftHeader,
             leftRows,
             rightHeader,
             rightRows,
             keyNames.toArray(new String[keyNames.size()]));
    }

    /**
     * Creates a KeyedTableDiff.
     *
     * @param header The common header.
     * @param leftRows The left rows.
     * @param rightRows The right rows.
     * @param keyNames The key names.
     */
    public KeyedTableDiff(Header header,
                          List<Row> leftRows,
                          List<Row> rightRows,
                          String... keyNames) {
        this(header,
             leftRows,
             header,
             rightRows,
             keyNames);
    }

    /**
     * Creates a KeyedTableDiff.
     *
     * @param header The common header.
     * @param leftRows The left rows.
     * @param rightRows The right rows.
     * @param keyNames The key names.
     */
    public KeyedTableDiff(Header header,
                          List<Row> leftRows,
                          List<Row> rightRows,
                          List<String> keyNames) {
        this(header,
             leftRows,
             rightRows,
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

    public Synthesis getSynthesis() {
        return synthesis;
    }

    /**
     * Synthesis of differences (for statistics).
     * <p>
     * Numbers are computed for:
     * <ul>
     * <li>lines
     * <li>cells
     * <li>columns.
     * </ul>
     *
     * @author Damien Carbonne
     */
    public static class Synthesis {
        public enum Action {
            ADDED,
            REMOVED,
            CHANGED,
            UNCHANGED
        }

        /** Counts of lines. */
        private final int[] lines = new int[Action.values().length];
        /** Counts of cell. */
        private final int[] cells = new int[Action.values().length];
        /** Counts of cells by column. */
        private final Map<String, int[]> columnToCells = new HashMap<>();
        /** The list of all (left and right) column names. */
        private final List<String> columnNames = new ArrayList<>();

        private void compute(KeyedTableDiff diffs) {
            // retrieve all column names.
            this.columnNames.addAll(diffs.leftHeader.getNames());
            for (final String name : diffs.rightHeader.getNames()) {
                if (!columnNames.contains(name)) {
                    columnNames.add(name);
                }
            }
            // Initialize map
            for (final String name : columnNames) {
                columnToCells.put(name, new int[Action.values().length]);
            }

            for (final RowDiff rdiff : diffs.getDiffs()) {
                switch (rdiff.getKind()) {
                case ADDED:
                    lines[Action.ADDED.ordinal()]++;
                    cells[Action.ADDED.ordinal()] += diffs.rightHeader.size();
                    for (final String name : diffs.rightHeader.getNames()) {
                        final int[] counts = columnToCells.get(name);
                        counts[Action.ADDED.ordinal()]++;
                    }
                    break;
                case CHANGED:
                    lines[Action.CHANGED.ordinal()]++;
                    for (final LocalizedCellDiff lcdiff : rdiff.getDiffs()) {
                        final int[] counts = columnToCells.get(lcdiff.getKey());
                        switch (lcdiff.getDiff().getKind()) {
                        case ADDED:
                            cells[Action.ADDED.ordinal()]++;
                            counts[Action.ADDED.ordinal()]++;
                            break;
                        case CHANGED:
                            cells[Action.CHANGED.ordinal()]++;
                            counts[Action.CHANGED.ordinal()]++;
                            break;
                        case REMOVED:
                            cells[Action.REMOVED.ordinal()]++;
                            counts[Action.REMOVED.ordinal()]++;
                            break;
                        case SAME:
                        case NULL:
                            cells[Action.UNCHANGED.ordinal()]++;
                            counts[Action.UNCHANGED.ordinal()]++;
                            break;
                        default:
                            throw new UnexpectedValueException(lcdiff.getDiff().getKind());
                        }
                    }
                    break;
                case REMOVED:
                    lines[Action.REMOVED.ordinal()]++;
                    cells[Action.REMOVED.ordinal()] += diffs.leftHeader.size();
                    for (final String name : diffs.leftHeader.getNames()) {
                        final int[] counts = columnToCells.get(name);
                        counts[Action.REMOVED.ordinal()]++;
                    }
                    break;
                case SAME:
                    lines[Action.UNCHANGED.ordinal()]++;
                    // left and right header have the same size
                    cells[Action.UNCHANGED.ordinal()] += diffs.leftHeader.size();
                    for (final String name : diffs.leftHeader.getNames()) {
                        final int[] counts = columnToCells.get(name);
                        counts[Action.UNCHANGED.ordinal()]++;
                    }
                    break;
                default:
                    throw new UnexpectedValueException(rdiff.getKind());
                }
            }
        }

        public int getLinesCount(Action action) {
            return lines[action.ordinal()];
        }

        public int getCellsCount(Action action) {
            return cells[action.ordinal()];
        }

        public List<String> getColumnNames() {
            return columnNames;
        }

        public int getColumnCellsCount(String columnName,
                                       Action action) {
            return columnToCells.get(columnName)[action.ordinal()];
        }

        public void print(PrintStream out) {
            final String format = "   %-10s %d%n";
            out.println("Lines");
            for (final Synthesis.Action action : Synthesis.Action.values()) {
                out.printf(format, action + ":", getLinesCount(action));
            }
            out.println("Cells");
            for (final Synthesis.Action action : Synthesis.Action.values()) {
                out.printf(format, action + ":", getCellsCount(action));
            }
            for (final String name : getColumnNames()) {
                out.println("[" + name + "]");
                for (final Synthesis.Action action : Synthesis.Action.values()) {
                    out.printf(format, action + ":", getColumnCellsCount(name, action));
                }
            }
        }
    }
}