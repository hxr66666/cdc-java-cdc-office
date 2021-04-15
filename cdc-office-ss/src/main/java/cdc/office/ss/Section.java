package cdc.office.ss;

public enum Section {
    /** Just in workbook, and not yet in a sheet. */
    WORKBOOK,
    /** Just in a sheet, and not yet in row. */
    SHEET,
    /** Just in a header row, and not yet in a header cell. */
    HEADER_ROW,
    /** Just in a data row, and not yet in a data cell. */
    DATA_ROW,
    /** In a header cell. */
    HEADER_CELL,
    /** In a data cell. */
    DATA_CELL
}