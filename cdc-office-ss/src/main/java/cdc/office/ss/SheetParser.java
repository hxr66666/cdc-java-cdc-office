package cdc.office.ss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import cdc.office.tables.TableHandler;

/**
 * Base interface of objects that can be used to parse spread sheets using a stream-like API.
 * <p>
 * <b>WARNING:</b> multi-sheet CSV files are currently parsed as a single table.
 *
 * @author Damien Carbonne
 *
 */
public interface SheetParser {
    /**
     * Parses a file and extracts all sheets.
     *
     * @param file The file.
     * @param password The optional password. Used by some implementations.
     * @param headers The number of header rows in the sheets.
     * @param handler The table handler.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(File file,
                      String password,
                      int headers,
                      TableHandler handler) throws IOException;

    /**
     * Parses an input stream and extracts all sheets.
     *
     * @param in The input stream.
     * @param kind The workbook kind.
     * @param password The optional password. Used by some implementations.
     * @param headers The number of header rows in the sheets.
     * @param handler The table handler.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(InputStream in,
                      WorkbookKind kind,
                      String password,
                      int headers,
                      TableHandler handler) throws IOException;

    /**
     * Parses a file and extracts one sheet.
     *
     * @param file The file.
     * @param password The optional password. Used by some implementations.
     * @param sheetName The sheet name. Used by multi-sheet implementations.
     * @param headers The number of header rows in the sheet.
     * @param handler The table handler.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(File file,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException;

    /**
     * Parses a file and extracts one sheet.
     *
     * @param file The file.
     * @param password The optional password. Used by some implementations.
     * @param sheetIndex The O-based sheet index. Used by multi-sheet implementations.
     * @param headers The number of header rows in the sheet.
     * @param handler The table handler.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(File file,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException;

    /**
     * Parses an input stream and extracts one sheet.
     *
     * @param in The input stream.
     * @param kind The workbook kind.
     * @param password The optional password. Used by some implementations.
     * @param sheetName The sheet name. Used by multi-sheet implementations.
     * @param headers The number of header rows in the sheet.
     * @param handler The table handler.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(InputStream in,
                      WorkbookKind kind,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException;

    /**
     * Parses an input stream and extracts one sheet.
     *
     * @param in The input stream.
     * @param kind The workbook kind.
     * @param password The optional password. Used by some implementations.
     * @param sheetIndex The 0-based sheet index. Used by multi-sheet implementations.
     * @param headers The number of header rows in the sheet.
     * @param handler The table handler.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(InputStream in,
                      WorkbookKind kind,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException;
}