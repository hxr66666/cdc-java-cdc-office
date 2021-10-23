package cdc.office.tables.diff;

/**
 * Enumeration of possible comparison results.
 *
 * @author Damien Carbonne
 */
public enum RowDiffKind {
    /** Contents are the same. */
    SAME,
    /** Content was added. */
    ADDED,
    /** Content was removed. */
    REMOVED,
    /** Content was changed. */
    CHANGED
}