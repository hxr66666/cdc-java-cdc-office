package cdc.office.tables.diff;

import java.util.Objects;

import cdc.util.lang.UnexpectedValueException;

/**
 * Class used to store the comparison of two cells.
 *
 * @author Damien Carbonne
 */
public class CellDiff {
    private final DiffKind kind;
    private final String left;
    private final String right;

    public CellDiff(String left,
                    String right) {
        this.left = left;
        this.right = right;
        if (Objects.equals(left, right)) {
            this.kind = DiffKind.SAME;
        } else if (left == null) {
            this.kind = DiffKind.ADDED;
        } else if (right == null) {
            this.kind = DiffKind.REMOVED;
        } else {
            this.kind = DiffKind.CHANGED;
        }
    }

    public DiffKind getKind() {
        return kind;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(kind);
        builder.append(" '");
        switch (kind) {
        case ADDED:
        case SAME:
            builder.append(right);
            builder.append("']");
            break;
        case CHANGED:
            builder.append(left);
            builder.append("' '");
            builder.append(right);
            builder.append("']");
            break;
        case REMOVED:
            builder.append(left);
            builder.append("']");
            break;
        default:
            throw new UnexpectedValueException(kind);
        }
        return builder.toString();
    }
}