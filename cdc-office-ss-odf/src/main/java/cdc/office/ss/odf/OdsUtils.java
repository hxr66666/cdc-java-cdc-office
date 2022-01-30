package cdc.office.ss.odf;

import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.dom.element.table.TableTableCellElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class OdsUtils {
    // private static final Logger LOGGER = LogManager.getLogger(OdsUtils.class);

    private OdsUtils() {
    }

    // public static Row addRow(Table table) {
    // final Row row = table.appendRow();
    // return row;
    // }

    // public static Cell setCell(Table table,
    // int colIndex,
    // int rowIndex,
    // String value) {
    // final Cell cell = table.getCellByPosition(colIndex, rowIndex);
    // cell.setStringValue(value);
    // return cell;
    // }

    // public static Cell addCell(Row row,
    // String value) {
    // final Cell cell = row.getCellByIndex(row.getCellCount());
    // cell.setStringValue(value);
    // return cell;
    // }
    //
    // public static Cell addCell(Row row,
    // String styleName,
    // String value) {
    // final Cell cell = row.getCellByIndex(row.getCellCount());
    // cell.setStringValue(value);
    // cell.setCellStyleName(styleName);
    // return cell;
    // }
    //
    // public static void addCells(Row row,
    // String... values) {
    // for (final String value : values) {
    // addCell(row, value);
    // }
    // }
    //
    // public static void addCells(Row row,
    // List<String> values) {
    // for (final String value : values) {
    // addCell(row, value);
    // }
    // }

    public static int getColumnsCount(OdfTableRow row) {
        int result = row.getTable().getColumnCount(); // This is costly
        final NodeList cells = row.getOdfElement().getChildNodes();
        if (cells != null && cells.getLength() > 0) {
            final int cellLen = cells.getLength();
            for (int i = cellLen - 1; i >= 0; i--) {
                final Node cell = cells.item(i);
                if (cell instanceof TableTableCellElement) {
                    if (!cell.hasChildNodes()) {
                        // last cell is empty - remove it from counter
                        result -= ((TableTableCellElement) cell).getTableNumberColumnsRepeatedAttribute();
                    } else {
                        // get first non-empty cell from the end, break
                        break;
                    }
                }
            }
        }
        return result;
    }
}