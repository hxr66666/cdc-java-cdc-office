package cdc.office.tables;

/**
 * Rows utilities.
 *
 * @author Damien Carbonne
 *
 */
public final class Rows {
    private Rows() {
    }

    /**
     * Returns a string containing at most max columns of a row.
     *
     * @param row The row.
     * @param maxColumns The max number of columns to display.<br>
     *            A negative number means there is no filtering.
     * @return A string representation of {@code row} containing at most {@code maxColumns} columns.
     */
    public static String toExtract(Row row,
                                   int maxColumns) {
        final StringBuilder builder = new StringBuilder();
        for (int column = 0; (maxColumns < 0 || column < maxColumns) && column < row.getColumnsCount(); column++) {
            builder.append(" '");
            builder.append(row.getValue(column));
            builder.append('\'');
        }
        if (row.getColumnsCount() > maxColumns) {
            builder.append(" ...");
        }
        return builder.toString();
    }
}