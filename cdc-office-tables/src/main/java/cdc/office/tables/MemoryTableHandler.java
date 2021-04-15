package cdc.office.tables;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.util.function.Evaluation;

public class MemoryTableHandler implements TableHandler {
    private static final Logger LOGGER = LogManager.getLogger(MemoryTableHandler.class);
    private final List<Row> rows = new ArrayList<>();
    private int headers = 0;

    public int getHeaderRowsCount() {
        return headers;
    }

    public int getDataRowsCount() {
        return rows.size() - getHeaderRowsCount();
    }

    public boolean hasHeaders() {
        return headers > 0;
    }

    public int getRowsCount() {
        return rows.size();
    }

    public Row getRow(int index) {
        return rows.get(index);
    }

    public List<Row> getRows() {
        return rows;
    }

    @Override
    public void processBegin(String name,
                             int numberOfRows) {
        LOGGER.trace("processBegin({}, {})", name, numberOfRows);
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
    public void processEnd() {
        LOGGER.trace("processEnd()");
        // Ignore
    }
}