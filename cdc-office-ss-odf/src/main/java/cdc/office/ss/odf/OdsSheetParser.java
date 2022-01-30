package cdc.office.ss.odf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.WorkbookKind;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.office.tables.TablesHandler;
import cdc.util.lang.ExceptionWrapper;

/**
 * Implementation of SheetParser for Open Office files.
 *
 * @author Damien Carbonne
 *
 */
public class OdsSheetParser implements SheetParser {
    private static final Logger LOGGER = LogManager.getLogger(OdsSheetParser.class);

    public OdsSheetParser() {
        super();
    }

    public OdsSheetParser(SheetParserFactory factory,
                          WorkbookKind kind) {
        this();
    }

    @Override
    public void parse(File file,
                      String password,
                      int headers,
                      TablesHandler handler) throws IOException {
        // TODO use password ?
        try (final OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(file)) {
            parse(file.getPath(), doc, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String password,
                      int headers,
                      TablesHandler handler) throws IOException {
        // TODO use password ?
        try (final OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(in)) {
            parse(systemId, doc, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(File file,
                      String sheetName,
                      String password,
                      int headers,
                      TableHandler handler) throws IOException {
        // TODO use password ?
        try (final OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(file)) {
            parse(file.getPath(), doc, sheetName, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(File file,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException {
        // TODO use password ?
        try (final OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(file)) {
            parse(file.getPath(), doc, sheetIndex, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String sheetName,
                      String password,
                      int headers,
                      TableHandler handler) throws IOException {
        // TODO use password ?
        try (final OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(in)) {
            parse(systemId, doc, sheetName, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
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
        // TODO use password ?
        try (final OdfSpreadsheetDocument doc = OdfSpreadsheetDocument.loadDocument(in)) {
            parse(systemId, doc, sheetIndex, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private static void parse(String systemId,
                              OdfSpreadsheetDocument doc,
                              int headers,
                              TablesHandler handler) throws IOException {
        handler.processBeginTables(systemId);
        for (final OdfTable table : doc.getTableList(false)) {
            parse(table, headers, handler);
        }
        handler.processEndTables(systemId);
    }

    private static void parse(String systemId,
                              OdfSpreadsheetDocument doc,
                              String sheetName,
                              int headers,
                              TableHandler handler) throws IOException {
        final OdfTable table;
        if (sheetName == null) {
            table = doc.getTableList(false).get(0);
        } else {
            table = doc.getTableByName(sheetName);
        }

        TablesHandler.processBeginTables(handler, systemId);
        parse(table, headers, handler);
        TablesHandler.processEndTables(handler, systemId);
    }

    private static void parse(String systemId,
                              OdfSpreadsheetDocument doc,
                              int sheetIndex,
                              int headers,
                              TableHandler handler) throws IOException {
        final OdfTable table = doc.getTableList(false).get(sheetIndex);
        TablesHandler.processBeginTables(handler, systemId);
        parse(table, headers, handler);
        TablesHandler.processEndTables(handler, systemId);
    }

    private static void parse(OdfTable table,
                              int headers,
                              TableHandler handler) {
        handler.processBeginTable(table.getTableName(), -1); // TODO
        final Row.Builder r = Row.builder();
        final RowLocation.Builder location = RowLocation.builder();

        final List<OdfTableRow> rows = table.getRowList();
        boolean active = true;
        for (int rindex = 0; rindex < rows.size() && active; rindex++) {
            final OdfTableRow row = rows.get(rindex);
            final int cellsCount = OdsUtils.getColumnsCount(row);
            location.incrementNumbers(headers);
            r.clear();
            for (int cindex = 0; cindex < cellsCount; cindex++) {
                final OdfTableCell cell = row.getCellByIndex(cindex);
                r.addValue(toString(cell));
            }
            active = TableHandler.processRow(handler, r.build(), location.build()).isContinue();
        }
        handler.processEndTable(table.getTableName());
    }

    private static String toString(OdfTableCell cell) {
        LOGGER.debug("toString({})", cell);
        if (cell == null) {
            return null;
        } else {
            final String type = cell.getValueType();
            if (type == null) {
                return null;
            } else {
                switch (type) {
                case "boolean":
                    final Boolean b = cell.getBooleanValue();
                    if (b == null) {
                        return null;
                    } else if (b.booleanValue()) {
                        return "TRUE";
                    } else {
                        return "FALSE";
                    }
                case "currency":
                case "date":
                    // TODO
                    return cell.getDisplayText();
                case "float":
                    final Double d = cell.getDoubleValue();
                    if (d == null) {
                        return null;
                    } else {
                        return String.format("%f", d);
                    }
                case "percentage":
                    // TODO
                    return cell.getDisplayText();
                case "string":
                    return cell.getStringValue();
                case "time":
                case "void":
                    // TODO
                default:
                    return cell.getDisplayText();
                }
            }
        }
    }
}