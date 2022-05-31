package cdc.office.ss;

public class CellAddressRange {
    private final int firstRow;
    private final int lastRow;
    private final int firstColumn;
    private final int lastColumn;

    public CellAddressRange(int firstRow,
                            int lastRow,
                            int firstColumn,
                            int lastColumn) {
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public int getLastRow() {
        return lastRow;
    }

    public int getLastRow(WorkbookKind kind) {
        return lastRow >= 0 ? lastRow : kind.getMaxRows() - 1;
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public int getLastColumn(WorkbookKind kind) {
        return lastColumn >= 0 ? lastColumn : kind.getMaxColumns() - 1;
    }

}