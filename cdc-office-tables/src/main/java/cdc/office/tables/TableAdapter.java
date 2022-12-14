package cdc.office.tables;

import cdc.util.function.Evaluation;

public class TableAdapter implements TableHandler {
    @Override
    public void processBeginTable(String name,
                                  int numberOfRows) {
        // Ignore
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        // Ignore
        return Evaluation.CONTINUE;
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        // Ignore
        return Evaluation.CONTINUE;
    }

    @Override
    public void processEndTable(String name) {
        // Ignore
    }
}