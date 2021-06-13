package cdc.office.tables;

/**
 * Interface implemented by classes that can handle parsing of several tables.
 *
 * @author Damien Carbonne
 */
public interface TablesHandler extends TableHandler {
    /**
     * Invoked when parsing of a group of tables starts.
     *
     * @param systemId The system id of the tables group.
     */
    public default void processBeginTables(String systemId) {
        // Ignore
    }

    /**
     * Invoked when parsing of a group of tables ends.
     *
     * @param systemId The system id of the tables group.
     */
    public default void processEndTables(String systemId) {
        // Ignore
    }

    /**
     * Invokes {@link #processBeginTables(String)} if a handler implements {@link TablesHandler}.
     *
     * @param handler The {@link TableHandler}.
     * @param systemId The system id.
     */
    public static void processBeginTables(TableHandler handler,
                                          String systemId) {
        if (handler instanceof TablesHandler) {
            ((TablesHandler) handler).processBeginTables(systemId);
        }
    }

    /**
     * Invokes {@link #processEndTables(String)} if a handler implements {@link TablesHandler}.
     *
     * @param handler The {@link TableHandler}.
     * @param systemId The system id.
     */
    public static void processEndTables(TableHandler handler,
                                        String systemId) {
        if (handler instanceof TablesHandler) {
            ((TablesHandler) handler).processEndTables(systemId);
        }
    }
}