package cdc.office.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of Row.
 *
 * @author Damien Carbonne
 *
 */
final class RowImpl implements Row {
    private final List<String> values;

    RowImpl(List<? extends Object> values) {
        final List<String> tmp = new ArrayList<>();
        for (final Object value : values) {
            tmp.add(value == null ? null : value.toString());
        }
        this.values = Collections.unmodifiableList(tmp);
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof RowImpl)) {
            return false;
        }
        final RowImpl other = (RowImpl) object;
        return values.equals(other.values);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append('[');
        for (int column = 0; column < getColumnsCount(); column++) {
            if (column > 0) {
                builder.append(';');
            }
            builder.append(getValue(column, ""));
        }
        builder.append(']');

        return builder.toString();
    }
}