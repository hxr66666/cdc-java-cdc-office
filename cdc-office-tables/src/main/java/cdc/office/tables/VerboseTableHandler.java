package cdc.office.tables;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.util.function.Evaluation;

public class VerboseTableHandler implements TableHandler {
    protected final Logger logger = LogManager.getLogger(getClass());
    private final TableHandler delegate;
    protected final Level level;

    public VerboseTableHandler(TableHandler delegate,
                               Level level) {
        this.delegate = delegate;
        this.level = level;
    }

    public VerboseTableHandler(TableHandler delegate) {
        this(delegate, Level.INFO);
    }

    @Override
    public void processBeginTable(String name,
                                  int numberOfRows) {
        logger.log(level, "processBeginTable({}, {})", name, numberOfRows);
        delegate.processBeginTable(name, numberOfRows);
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        logger.log(level, "processHeader({}, {})", header, location);
        return delegate.processHeader(header, location);
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        logger.log(level, "processData({}, {})", data, location);
        return delegate.processData(data, location);
    }

    @Override
    public void processEndTable(String name) {
        logger.log(level, "processEndTable({})", name);
        delegate.processEndTable(name);
    }
}