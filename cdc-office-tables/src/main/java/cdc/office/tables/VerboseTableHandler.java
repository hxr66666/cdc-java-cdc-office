package cdc.office.tables;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.util.function.Evaluation;

public class VerboseTableHandler implements TableHandler {
    private static final Logger LOGGER = LogManager.getLogger(VerboseTableHandler.class);
    private final TableHandler delegate;
    private final Level level;

    public VerboseTableHandler(TableHandler delegate,
                               Level level) {
        this.delegate = delegate;
        this.level = level;
    }

    public VerboseTableHandler(TableHandler delegate) {
        this(delegate, Level.INFO);
    }

    @Override
    public void processBegin(String name,
                             int numberOfRows) {
        LOGGER.log(level, "processBegin({}, {})", name, numberOfRows);
        delegate.processBegin(name, numberOfRows);
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        LOGGER.log(level, "processHeader({}, {})", header, location);
        return delegate.processHeader(header, location);
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        LOGGER.log(level, "processData({}, {})", data, location);
        return delegate.processData(data, location);
    }

    @Override
    public void processEnd() {
        LOGGER.log(level, "processEnd()");
        delegate.processEnd();
    }
}