package cdc.office.tables;

import java.util.function.IntPredicate;

import cdc.util.function.Evaluation;

/**
 * Table Filter that filters columns and delegates processing to another handler.
 *
 * @author Damien Carbonne
 *
 */
public class TableColumnsFilter extends AbstractTableFilter {
    private final IntPredicate predicate;
    private final Row.Builder buffer = Row.builder();

    public TableColumnsFilter(TableHandler delegate,
                              IntPredicate predicate) {
        super(delegate);
        this.predicate = predicate;
    }

    public final IntPredicate getPredicate() {
        return predicate;
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        setBuffer(header);
        return delegate.processHeader(buffer.build(), location);
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        setBuffer(data);
        return delegate.processData(buffer.build(), location);
    }

    private void setBuffer(Row row) {
        buffer.clear();
        for (int index = 0; index < row.getColumnsCount(); index++) {
            if (predicate.test(index)) {
                buffer.addValue(row.getValue(index));
            }
        }
    }
}