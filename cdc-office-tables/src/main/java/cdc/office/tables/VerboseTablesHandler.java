package cdc.office.tables;

import org.apache.logging.log4j.Level;

public class VerboseTablesHandler extends VerboseTableHandler implements TablesHandler {
    private final TablesHandler delegate;

    public VerboseTablesHandler(TablesHandler delegate,
                                Level level) {
        super(delegate, level);
        this.delegate = delegate;
    }

    @Override
    public void processBeginTables(String systemId) {
        logger.log(level, "processBeginTables({})", systemId);
        delegate.processBeginTables(systemId);
    }

    @Override
    public void processEndTables(String systemId) {
        logger.log(level, "processEndTables({})", systemId);
        delegate.processEndTables(systemId);
    }
}