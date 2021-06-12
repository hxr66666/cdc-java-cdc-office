package cdc.office.tables;

public interface TablesHandler extends TableHandler {
    public default void processBeginTables(String systemId) {
        // Ignore
    }

    public default void processEndTables(String systemId) {
        // Ignore
    }

    public static void processBeginTables(TableHandler handler,
                                          String systemId) {
        if (handler instanceof TablesHandler) {
            ((TablesHandler) handler).processBeginTables(systemId);
        }
    }

    public static void processEndTables(TableHandler handler,
                                        String systemId) {
        if (handler instanceof TablesHandler) {
            ((TablesHandler) handler).processEndTables(systemId);
        }
    }
}