package cdc.office.tables;

import cdc.util.function.Evaluation;

public class TableRowsCounter implements TableHandler {
    private int rows = 0;

    public TableRowsCounter() {
        super();
    }

    public void reset() {
        rows = 0;
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        rows++;
        return Evaluation.CONTINUE;
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        rows++;
        return Evaluation.CONTINUE;
    }

    public int getNumberOfRows() {
        return rows;
    }
}