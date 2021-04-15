package cdc.office.tables;

import java.util.function.BiPredicate;

import cdc.util.function.Evaluation;
import cdc.util.lang.Checks;

/**
 * Table Filter that filters rows and delegates processing to another handler.
 *
 * @author Damien Carbonne
 *
 */
public class TableRowsFilter extends AbstractTableFilter {
    private final BiPredicate<Row, RowLocation> predicate;
    private final RowLocation.Builder location = RowLocation.builder();

    public TableRowsFilter(TableHandler delegate,
                           BiPredicate<Row, RowLocation> predicate) {
        super(delegate);
        Checks.isNotNull(predicate, "predicate");

        this.predicate = predicate;
    }

    public final BiPredicate<Row, RowLocation> getPredicate() {
        return predicate;
    }

    @Override
    public Evaluation processHeader(Row header,
                                    RowLocation location) {
        if (predicate.test(header, location)) {
            this.location.incrementNumbers(TableSection.HEADER);
            return delegate.processHeader(header, this.location.build());
        } else {
            return Evaluation.CONTINUE;
        }
    }

    @Override
    public Evaluation processData(Row data,
                                  RowLocation location) {
        if (predicate.test(data, location)) {
            this.location.incrementNumbers(TableSection.DATA);
            return delegate.processData(data, this.location.build());
        } else {
            return Evaluation.CONTINUE;
        }
    }
}