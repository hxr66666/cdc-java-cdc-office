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
    public void processBegin(String name,
                             int numberOfRows) {
        delegate.processBegin(name, numberOfRows);
    }

    @Override
    public void processEnd() {
        delegate.processEnd();
    }
}