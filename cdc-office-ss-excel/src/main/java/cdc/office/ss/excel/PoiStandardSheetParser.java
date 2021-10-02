package cdc.office.ss.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.SheetParserFactory.Feature;
import cdc.office.ss.WorkbookKind;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.office.tables.TablesHandler;
import cdc.util.lang.BlackHole;

/**
 * Standard implementation of SheetParser for Excel files.
 * <p>
 * It can be used with xls and xlsx formats.
 *
 * @author Damien Carbonne
 *
 */
public class PoiStandardSheetParser implements SheetParser {
    protected static final Logger LOGGER = LogManager.getLogger(PoiStandardSheetParser.class);
    private final DataFormatter df = new DataFormatter();
    private final boolean evaluateFormula;

    public PoiStandardSheetParser() {
        this.evaluateFormula = false;
    }

    public PoiStandardSheetParser(SheetParserFactory factory,
                                  WorkbookKind kind) {
        this.evaluateFormula = factory.isEnabled(Feature.EVALUATE_FORMULA);
        BlackHole.discard(kind);
    }

    @Override
    public void parse(File file,
                      String password,
                      int headers,
                      TablesHandler handler) throws IOException {
        // Open the file in read only mode
        try (final Workbook workbook = WorkbookFactory.create(file, password, true)) {
            parse(file.getPath(), workbook, headers, handler);
        }
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String password,
                      int headers,
                      TablesHandler handler) throws IOException {
        try (final Workbook workbook = WorkbookFactory.create(in, password)) {
            parse(systemId, workbook, headers, handler);
        }
    }

    @Override
    public void parse(File file,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException {
        // Open the file in read only mode
        try (final Workbook workbook = WorkbookFactory.create(file, password, true)) {
            parse(file.getPath(), workbook, sheetName, headers, handler);
        }
    }

    @Override
    public void parse(File file,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException {
        // Open the file in read only mode
        try (final Workbook workbook = WorkbookFactory.create(file, password, true)) {
            parse(file.getPath(), workbook, sheetIndex, headers, handler);
        }
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException {
        try (final Workbook workbook = WorkbookFactory.create(in, password)) {
            parse(systemId, workbook, sheetName, headers, handler);
        }
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException {
        try (final Workbook workbook = WorkbookFactory.create(in, password)) {
            parse(systemId, workbook, sheetIndex, headers, handler);
        }
    }

    private void parse(String systemId,
                       Workbook workbook,
                       int headers,
                       TablesHandler handler) {
        handler.processBeginTables(systemId);
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            final Sheet sheet = workbook.getSheetAt(index);
            parse(systemId, sheet, headers, handler);
        }
        handler.processEndTables(systemId);
    }

    private void parse(String systemId,
                       Workbook workbook,
                       String sheetName,
                       int headers,
                       TableHandler handler) {
        final Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Invalid sheet name: " + sheetName);
        }
        parse(systemId, sheet, headers, handler);
    }

    private void parse(String systemId,
                       Workbook workbook,
                       int sheetIndex,
                       int headers,
                       TableHandler handler) {
        final Sheet sheet = workbook.getSheetAt(sheetIndex);
        parse(systemId, sheet, headers, handler);
    }

    private static int getNumberOfRows(Sheet sheet) {
        final int last = sheet.getLastRowNum();
        return last < 0 ? -1 : last + 1;// TODO check
    }

    private void parse(String systemId,
                       Sheet sheet,
                       int headers,
                       TableHandler handler) {
        TablesHandler.processBeginTables(handler, systemId);
        handler.processBeginTable(sheet.getSheetName(), getNumberOfRows(sheet));

        final FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();

        final Row.Builder r = Row.builder();
        final RowLocation.Builder location = RowLocation.builder();
        int previousRowIndex = -1;
        boolean active = true;
        for (final org.apache.poi.ss.usermodel.Row row : sheet) {
            r.clear();

            final int rowIndex = row.getRowNum();
            for (int index = previousRowIndex; active && index < rowIndex - 1; index++) {
                location.incrementNumbers(headers);
                active = TableHandler.processRow(handler, r.build(), location.build()).isContinue();
            }
            location.incrementNumbers(headers);
            previousRowIndex = rowIndex;

            int previousColumnIndex = -1;
            for (final Cell cell : row) {
                final int columnIndex = cell.getColumnIndex();
                for (int index = previousColumnIndex; index < columnIndex - 1; index++) {
                    r.addValue(null);
                }
                r.addValue(toString(cell, evaluator));
                previousColumnIndex = columnIndex;
            }
            if (active) {
                active = TableHandler.processRow(handler, r.build(), location.build()).isContinue();
            }
        }
        handler.processEndTable(sheet.getSheetName());
        TablesHandler.processEndTables(handler, systemId);
    }

    /**
     * Formats the cell content.
     *
     * @param cell The cell.
     * @param evaluator The evaluator. Used when cell contains a formula.
     * @return The cell content as a string. Formula calls are evaluated.
     */
    private String toString(Cell cell,
                            FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        } else {
            if (evaluateFormula && cell.getCellType() == CellType.FORMULA) {
                // Retrieve cached value
                // We could also evaluate formula
                switch (cell.getCachedFormulaResultType()) {
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell, null)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        return Double.toString(cell.getNumericCellValue());
                    }
                case STRING:
                    return cell.getRichStringCellValue().getString();
                default:
                    return "";
                }
            } else {
                return df.formatCellValue(cell, evaluator);
            }
        }
    }
}