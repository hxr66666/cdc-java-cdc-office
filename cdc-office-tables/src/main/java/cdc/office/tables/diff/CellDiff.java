package cdc.office.tables.diff;

import java.util.Objects;

/**
 * Class used to store the comparison of two cells.
 * <table>
 * <caption></caption>
 * <thead>
 * <tr>
 * <td></td>
 * <td>null</td>
 * <td>""</td>
 * <td>"foo"</td>
 * <td>"bar"</td>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>null</td>
 * <td>SAME</td>
 * <td>ADDED</td>
 * <td>ADDED</td>
 * <td>ADDED</td>
 * </tr>
 * <tr>
 * <td>""</td>
 * <td>REMOVED</td>
 * <td>SAME</td>
 * <td>CHANGED</td>
 * <td>CHANGED</td>
 * </tr>
 * <tr>
 * <td>"foo"</td>
 * <td>REMOVED</td>
 * <td>CHANGED</td>
 * <td>SAME</td>
 * <td>CHANGED</td>
 * </tr>
 * <tr>
 * <td>"bar"</td>
 * <td>REMOVED</td>
 * <td>CHANGED</td>
 * <td>CHANGED</td>
 * <td>SAME</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <ul>
 * <li>{@code CellDiff(null, null) = SAME}
 * <li>{@code CellDiff(null, "") = ADDED}
 * <li>{@code CellDiff(null, "foo") = ADDED}
 * <li>{@code CellDiff("", null) = REMOVED}
 * <li>{@code CellDiff("", "") = SAME}
 * <li>{@code CellDiff("", "foo") = CHANGED}
 * <li>{@code CellDiff("foo", null) = REMOVED}
 * <li>{@code CellDiff("foo", "") = CHANGED}
 * <li>{@code CellDiff("foo", "foo") = SAME}
 * <li>{@code CellDiff("foo", "bar") = CHANGED}
 * </ul>
 *
 * @author Damien Carbonne
 */
public class CellDiff {
    private final CellDiffKind kind;
    private final String left;
    private final String right;

    public CellDiff(String left,
                    String right) {
        this.left = left;
        this.right = right;
        if (Objects.equals(left, right)) {
            if (left == null) {
                this.kind = CellDiffKind.NULL;
            } else {
                this.kind = CellDiffKind.SAME;
            }
        } else if (left == null) {
            this.kind = CellDiffKind.ADDED;
        } else if (right == null) {
            this.kind = CellDiffKind.REMOVED;
        } else {
            this.kind = CellDiffKind.CHANGED;
        }
    }

    /**
     * @return The difference kind.
     */
    public CellDiffKind getKind() {
        return kind;
    }

    /**
     * @return The left value.
     */
    public String getLeft() {
        return left;
    }

    /**
     * @return The right value.
     */
    public String getRight() {
        return right;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind,
                            left,
                            right);

    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CellDiff)) {
            return false;
        }
        final CellDiff other = (CellDiff) object;
        return this.kind == other.kind
                && Objects.equals(this.left, other.left)
                && Objects.equals(this.right, other.right);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(kind);
        builder.append(" '");
        switch (kind) {
        case ADDED:
        case NULL:
        case SAME:
            builder.append(right);
            break;
        case CHANGED:
            builder.append(left);
            builder.append("' '");
            builder.append(right);
            break;
        case REMOVED:
            builder.append(left);
            break;
        }
        builder.append("']");
        return builder.toString();
    }
}