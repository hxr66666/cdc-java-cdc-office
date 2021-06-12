package cdc.office.tables;

import cdc.util.lang.Checks;

public abstract class AbstractTableFilter implements TableHandler {
    protected final TableHandler delegate;

    protected AbstractTableFilter(TableHandler delegate) {
        Checks.isNotNull(delegate, "delegate");
        this.delegate = delegate;
    }

    public final TableHandler getDelegate() {
        return delegate;
    }

    @Override
    public void processBeginTable(String name,
                                  int numberOfRows) {
        delegate.processBeginTable(name, numberOfRows);
    }

    @Override
    public void processEndTable(String name) {
        delegate.processEndTable(name);
    }
}