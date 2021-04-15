package cdc.office.tables;

import cdc.util.function.Evaluation;

/**
 * Set of operations that must be implemented by a text table (csv, xml or
 * others) handler.
 * <p>
 * The caller <b>MUST</b> proceed in this order:
 * <ol>
 * <li>{@code processBegin}
 * <li>{@code procesHeader?}
 * <li>{@code processData*}
 * <li>{@code processEnd}
 * </ol>
 *
 * @author D. Carbonne
 */
public interface TableHandler {
    /**
     * Called when the parsing of a table starts.<br>
     * Default implementation does nothing.
     *
     * @param name The table/sheet name may be {@code null}.
     * @param numberOfRows The number of rows of the table,
     *            or a negative number if that can not be determined.
     */
    public default void processBegin(String name,
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
     * This is called even when parsing has been interrupted.<br>
     * Default implementation does nothing.
     */
    public default void processEnd() {
        // Ignore
    }

    public static void processRow(TableHandler handler,
                                  Row row,
                                  RowLocation location) {
        if (location.isHeader()) {
            handler.processHeader(row, location);
        } else {
            handler.processData(row, location);
        }
    }
}