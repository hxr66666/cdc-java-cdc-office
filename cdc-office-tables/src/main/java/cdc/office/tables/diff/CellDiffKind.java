package cdc.office.tables.diff;

/**
 * Enumeration of possible comparison results.
 *
 * @author Damien Carbonne
 */
public enum CellDiffKind {
    /** Contents are the same and null. */
    NULL,
    /** Contents are the same and not null. */
    SAME,
    /** Content was added. */
    ADDED,
    /** Content was removed. */
    REMOVED,
    /** Content was changed. */
    CHANGED;

    public boolean isSameOrNull() {
        return this == SAME || this == NULL;
    }

    public boolean isNeitherSameNorNull() {
        return !isSameOrNull();
    }
}