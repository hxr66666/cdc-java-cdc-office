package cdc.office.tables.diff;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    /** The left SystemId. */
    private final String leftSystemId;
    /** The right SystemId. */
    private final String rightSystemId;
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
    private final int leftIgnored;
    private final int rightIgnored;

    protected KeyedTableDiff(Builder builder) {
        this.leftSystemId = builder.leftSystemId;
        this.rightSystemId = builder.rightSystemId;
        this.leftHeader = Checks.isNotNull(builder.leftHeader, "leftHeader");
        this.rightHeader = Checks.isNotNull(builder.rightHeader, "rightHeader");
        this.keyNames = builder.keyNames;

        this.leftKeyColumns = buildKeyColumns(leftHeader, keyNames);
        this.rightKeyColumns = buildKeyColumns(rightHeader, keyNames);

        // The number of left rows that are empty and ignored
        int leftEmpty = 0;

        // Map from left keys to left rows
        final Map<CTupleN<String>, Row> leftMap = new HashMap<>();
        for (int number = 0; number < builder.leftRows.size(); number++) {
            final Row row = builder.leftRows.get(number);
            if (row.isEmpty()) {
                leftEmpty++;
            } else {
                // Ignore empty rows
                final CTupleN<String> key = getKey(Side.LEFT, row, number);
                if (leftMap.containsKey(key)) {
                    throw new InvalidDataException("Duplicate key " + key + locate(Side.LEFT, row, number));
                }
                leftMap.put(key, row);
            }
        }

        // The number of right rows that are empty and ignored
        int rightEmpty = 0;

        // Compare right rows to left ones
        for (int number = 0; number < builder.rightRows.size(); number++) {
            final Row right = builder.rightRows.get(number);
            if (right.isEmpty()) {
                rightEmpty++;
            } else {
                // Ignore empty rows
                final CTupleN<String> key = getKey(Side.RIGHT, right, number);
                final Row left = leftMap.getOrDefault(key, Row.EMPTY);
                if (diffs.containsKey(key)) {
                    throw new InvalidDataException("Duplicate key " + key + locate(Side.RIGHT, right, number));
                }

                final RowDiff diff = new RowDiff(leftHeader, left, rightHeader, right);
                diffs.put(key, diff);
                keys.add(key);
            }
        }

        // Add all left rows that are not in right rows
        for (int number = 0; number < builder.leftRows.size(); number++) {
            final Row left = builder.leftRows.get(number);
            if (!left.isEmpty()) {
                // Ignore empty rows
                final CTupleN<String> key = getKey(Side.LEFT, left, number);
                diffs.computeIfAbsent(key, k -> {
                    keys.add(k);
                    return new RowDiff(leftHeader, left, rightHeader, Row.EMPTY);
                });
            }
        }

        this.leftIgnored = leftEmpty;
        this.rightIgnored = rightEmpty;

        this.synthesis.compute(this);
    }

    private int[] getKeyColumns(Side side) {
        return side == Side.LEFT ? leftKeyColumns : rightKeyColumns;
    }

    private static int[] buildKeyColumns(Header header,
                                         List<String> keyNames) {
        final int[] result = new int[keyNames.size()];
        for (int index = 0; index < keyNames.size(); index++) {
            final int column = header.getMatchingIndex(keyNames.get(index));
            if (column < 0) {
                throw new IllegalArgumentException("Key '" + keyNames.get(index) + "' missing in " + header);
            }
            result[index] = column;
        }
        return result;
    }

    private String locate(Side side,
                          Row row,
                          int number) {
        final String systemId = getSystemId(side);
        final StringBuilder builder = new StringBuilder();
        builder.append(" in ")
               .append(side)
               .append(" row ")
               .append(row)
               .append(", line ")
               .append(number + 2);
        if (systemId != null) {
            builder.append(" (")
                   .append(systemId)
                   .append(')');
        }
        return builder.toString();
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

    /**
     * @param side The side.
     * @return The SystemId associated to {@code side}. May be {@code null}.
     */
    public String getSystemId(Side side) {
        return side == Side.LEFT ? leftSystemId : rightSystemId;
    }

    /**
     * @param side The side.
     * @return The {@link Header} associated to {@code side}.
     */
    public Header getHeader(Side side) {
        return side == Side.LEFT ? leftHeader : rightHeader;
    }

    /**
     * @return A list of key names.
     */
    public List<String> getKeyNames() {
        return keyNames;
    }

    /**
     * @return A list of all keys. Some may be left-only or right-only.
     */
    public List<CTupleN<String>> getKeys() {
        return keys;
    }

    /**
     * @param key The key.
     * @return The {@link RowDiff} associated to {@code key}.
     */
    public RowDiff getDiff(TupleN<String> key) {
        return diffs.get(key);
    }

    /**
     * @return A collections of all row differences.
     */
    public Collection<RowDiff> getDiffs() {
        return diffs.values();
    }

    /**
     * @param side The side.
     * @return The number of ignored rows on {@code side}.
     */
    public int getNumberOfIgnoredRows(Side side) {
        return side == Side.LEFT ? leftIgnored : rightIgnored;
    }

    public int getNumberOfIgnoredRows() {
        return leftIgnored + rightIgnored;
    }

    /**
     * @return A {@link Synthesis} of differences.
     */
    public Synthesis getSynthesis() {
        return synthesis;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder of {@link KeyedTableDiff}.
     */
    public static class Builder {
        private String leftSystemId;
        private String rightSystemId;
        private Header leftHeader;
        private Header rightHeader;
        private final List<String> keyNames = new ArrayList<>();
        private final List<Row> leftRows = new ArrayList<>();
        private final List<Row> rightRows = new ArrayList<>();

        protected Builder() {
        }

        public Builder leftSystemId(String systemId) {
            this.leftSystemId = systemId;
            return this;
        }

        public Builder rightSystemId(String systemId) {
            this.rightSystemId = systemId;
            return this;
        }

        public Builder header(Header header) {
            this.leftHeader = header;
            this.rightHeader = header;
            return this;
        }

        public Builder leftHeader(Header leftHeader) {
            this.leftHeader = leftHeader;
            return this;
        }

        public Builder rightHeader(Header rightHeader) {
            this.rightHeader = rightHeader;
            return this;
        }

        public Builder keyNames(List<String> keyNames) {
            this.keyNames.clear();
            this.keyNames.addAll(keyNames);
            return this;
        }

        public Builder keyNames(String... keyNames) {
            this.keyNames.clear();
            Collections.addAll(this.keyNames, keyNames);
            return this;
        }

        public Builder leftRows(List<Row> leftRows) {
            this.leftRows.clear();
            this.leftRows.addAll(leftRows);
            return this;
        }

        public Builder leftRows(Row... leftRows) {
            this.leftRows.clear();
            Collections.addAll(this.leftRows, leftRows);
            return this;
        }

        public Builder rightRows(List<Row> rightRows) {
            this.rightRows.clear();
            this.rightRows.addAll(rightRows);
            return this;
        }

        public Builder rightRows(Row... rightRows) {
            this.rightRows.clear();
            Collections.addAll(this.rightRows, rightRows);
            return this;
        }

        public KeyedTableDiff build() {
            return new KeyedTableDiff(this);
        }
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
            SAME
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
            this.columnNames.addAll(diffs.leftHeader.getSortedNames());
            for (final String name : diffs.rightHeader.getSortedNames()) {
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
                    for (final String name : diffs.rightHeader.getDeclaredNames()) {
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
                            cells[Action.SAME.ordinal()]++;
                            counts[Action.SAME.ordinal()]++;
                            break;
                        default:
                            throw new UnexpectedValueException(lcdiff.getDiff().getKind());
                        }
                    }
                    break;
                case REMOVED:
                    lines[Action.REMOVED.ordinal()]++;
                    cells[Action.REMOVED.ordinal()] += diffs.leftHeader.size();
                    for (final String name : diffs.leftHeader.getDeclaredNames()) {
                        final int[] counts = columnToCells.get(name);
                        counts[Action.REMOVED.ordinal()]++;
                    }
                    break;
                case SAME:
                    lines[Action.SAME.ordinal()]++;
                    // left and right header have the same size
                    cells[Action.SAME.ordinal()] += diffs.leftHeader.size();
                    for (final String name : diffs.leftHeader.getDeclaredNames()) {
                        final int[] counts = columnToCells.get(name);
                        counts[Action.SAME.ordinal()]++;
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