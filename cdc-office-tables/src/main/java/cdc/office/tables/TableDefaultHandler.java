package cdc.office.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.util.function.Evaluation;

public class TableDefaultHandler implements TableHandler {
    private static final Logger LOGGER = LogManager.getLogger(TableDefaultHandler.class);

    @Override
    public void processBegin(String name,
                             int numberOfRows) {
        LOGGER.debug("processBegin({})", name);
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        LOGGER.debug("processHeader({}, {})", header, location);
        return Evaluation.CONTINUE;
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        LOGGER.debug("processData({}, {})", data, location);
        return Evaluation.CONTINUE;
    }

    @Override
    public void processEnd() {
        LOGGER.debug("processEnd()");
    }
}