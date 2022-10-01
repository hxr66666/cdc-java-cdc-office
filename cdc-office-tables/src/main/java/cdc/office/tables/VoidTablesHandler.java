package cdc.office.tables;

/**
 * Implementation of {@link TablesHandler} that does nothing.
 *
 * @author Damien Carbonne
 */
public class VoidTablesHandler extends VoidTableHandler implements TablesHandler {
    public static final VoidTablesHandler INSTANCE = new VoidTablesHandler();

    protected VoidTablesHandler() {
    }

    @Override
    public void processBeginTables(String systemId) {
        // Ignore
    }

    @Override
    public void processEndTables(String systemId) {
        // Ignore
    }
}