package cdc.office.tables;

import cdc.util.function.Evaluation;

/**
 * Implementation of {@link TableHandler} that does nopthing.
 *
 * @author Damien Carbonne
 */
public class VoidTableHandler implements TableHandler {
    public static final VoidTableHandler INSTANCE = new VoidTableHandler();

    protected VoidTableHandler() {
    }

    public VoidTableHandler(TableHandler delegate) {
        // Ignore
    }

    @Override
    public void processBeginTable(String name,
                                  int numberOfRows) {
        // Ignore
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        return Evaluation.CONTINUE;
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        return Evaluation.CONTINUE;
    }

    @Override
    public void processEndTable(String name) {
        // Ignore
    }
}