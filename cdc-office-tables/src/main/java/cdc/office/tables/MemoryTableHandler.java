package cdc.office.tables;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.util.function.Evaluation;

/**
 * Implementation of {@link TableHandler} that stores rows.
 *
 * @author Damien Carbonne
 */
public class MemoryTableHandler implements TableHandler {
    private static final Logger LOGGER = LogManager.getLogger(MemoryTableHandler.class);
    private final boolean removeEmptyTrailingRows;
    private final List<Row> rows = new ArrayList<>();
    private int headers = 0;

    /**
     * Creates a MemoryTableHandler that removed empty trailing rows.
     * <p>
     * <b>WARNING:</b> this does not remove empty-like trailing rows.
     */
    public MemoryTableHandler() {
        this(true);
    }

    /**
     * Creates a MemoryTableHandler that can remove empty trailing rows.
     *
     * @param removeEmptyTrailingRows If {@code true}, empty (not empty-like) trailing rows are removed.
     */
    public MemoryTableHandler(boolean removeEmptyTrailingRows) {
        this.removeEmptyTrailingRows = removeEmptyTrailingRows;
    }

    /**
     * @return The number of empty trailing rows.
     */
    public int getEmptyTrailingRowsCount() {
        boolean active = true;
        int index = rows.size() - 1;
        while (index >= 0 && active) {
            final Row row = rows.get(index);
            if (row.isEmpty()) {
                index--;
            } else {
                // found a non-empty row
                // index is the index of this first non empty row
                active = false;
            }
        }

        // index+1 is the index of the first trailing empty row
        return rows.size() - index - 1;
    }

    /**
     * @return The number of empty-like trailing rows.
     */
    public int getEmptyLikeTrailingRowsCount() {
        boolean active = true;
        int index = rows.size() - 1;
        while (index >= 0 && active) {
            final Row row = rows.get(index);
            if (row.isEmptyLike()) {
                index--;
            } else {
                // found a non-empty row
                // index is the index of this first non empty row
                active = false;
            }
        }

        // index+1 is the index of the first trailing empty row
        return rows.size() - index - 1;
    }

    /**
     * Remove all trailing empty rows.
     * <p>
     * This should be called once all rows have been loaded.
     */
    public void removeEmptyTrailingtRows() {
        final int count = getEmptyTrailingRowsCount();
        if (count > 0) {
            rows.subList(rows.size() - count, rows.size()).clear();
        }
    }

    /**
     * Remove all trailing empty-like rows.
     * <p>
     * This should be called once all rows have been loaded.
     */
    public void removeEmptyLikeTrailingtRows() {
        final int count = getEmptyLikeTrailingRowsCount();
        if (count > 0) {
            rows.subList(rows.size() - count, rows.size()).clear();
        }
    }

    /**
     * @return The number of header rows.
     *         It should usually be 0 or 1, and is passed by the application.
     */
    public int getHeaderRowsCount() {
        return headers;
    }

    /**
     * @return The number of data rows.
     */
    public int getDataRowsCount() {
        return rows.size() - getHeaderRowsCount();
    }

    /**
     * @return {@code true} if there are some header rows.
     */
    public boolean hasHeaders() {
        return headers > 0;
    }

    /**
     * @return The total number of rows (header + data).
     */
    public int getRowsCount() {
        return rows.size();
    }

    /**
     * @param index The rows index (0-based).
     * @return The row at {@code index}.
     */
    public Row getRow(int index) {
        return rows.get(index);
    }

    /**
     * @return A list of all rows.
     */
    public List<Row> getRows() {
        return rows;
    }

    @Override
    public void processBeginTable(String name,
                                  int numberOfRows) {
        LOGGER.trace("processBeginTable({}, {})", name, numberOfRows);
        headers = 0;
        rows.clear();
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        LOGGER.trace("processHeader({}, {})", header, location);
        headers++;
        rows.add(header);
        return Evaluation.CONTINUE;
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        LOGGER.trace("processData({}, {})", data, location);
        rows.add(data);
        return Evaluation.CONTINUE;
    }

    @Override
    public void processEndTable(String name) {
        LOGGER.trace("processEndTable({})", name);
        if (removeEmptyTrailingRows) {
            removeEmptyTrailingtRows();
        }
    }
}