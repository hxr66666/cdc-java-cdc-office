package cdc.office.tables;

import cdc.util.function.Evaluation;

/**
 * Interface implemented by classes that can handle table-like parsing events for one table.
 * <p>
 * The caller <b>MUST</b> proceed in this order:
 * <ol>
 * <li>{@link #processBeginTable(String, int)}
 * <li>{@link #processHeader(Row, RowLocation)}*
 * <li>{@link #processData(Row, RowLocation)}*
 * <li>{@link #processEndTable(String)}
 * </ol>
 *
 * @author D. Carbonne
 */
public interface TableHandler {
    /**
     * Called when the parsing of a table starts.
     * <p>
     * Default implementation does nothing.
     *
     * @param name The table/sheet name may be {@code null}.
     * @param numberOfRows The number of rows of the table,
     *            or a negative number if that can not be determined.
     */
    public default void processBeginTable(String name,
                                          int numberOfRows) {
        // Ignore
    }

    /**
     * Called when a header row is read.
     *
     * @param header The header row.
     * @param location The row location.
     * @return Whether parsing should continue or stop.
     */
    public Evaluation processHeader(Row header,
                                    RowLocation location);

    /**
     * Called when a data row is read.
     *
     * @param data The data row.
     * @param location The row location.
     * @return Whether parsing should continue or stop.
     */
    public Evaluation processData(Row data,
                                  RowLocation location);

    /**
     * Called when the table has been fully read.
     * <p>
     * This is also called even when parsing has been interrupted.<br>
     * Default implementation does nothing.
     *
     * @param name The table/sheet name may be {@code null}.
     */
    public default void processEndTable(String name) {
        // Ignore
    }

    public static Evaluation processRow(TableHandler handler,
                                        Row row,
                                        RowLocation location) {
        if (location.isHeader()) {
            return handler.processHeader(row, location);
        } else {
            return handler.processData(row, location);
        }
    }
}