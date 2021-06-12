package cdc.office.ss.csv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import cdc.office.csv.CsvParser;
import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.WorkbookKind;
import cdc.office.tables.TableHandler;
import cdc.office.tables.TablesHandler;

/**
 * Implementation of SheetParser for CSV files.
 *
 * @author Damien Carbonne
 *
 */
public class CsvSheetParser implements SheetParser {
    private final CsvParser parser;
    private final Charset charset;

    public CsvSheetParser(Charset charset,
                          char separator) {
        this.parser = new CsvParser(separator).countRows(true);
        this.charset = charset;
    }

    public CsvSheetParser(SheetParserFactory factory,
                          WorkbookKind kind) {
        this(factory.getCharset(),
             factory.getSeparator());
    }

    @Override
    public void parse(File file,
                      String password,
                      int headers,
                      TablesHandler handler) throws IOException {
        parser.parse(file, charset, handler, headers);
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String password,
                      int headers,
                      TablesHandler handler) throws IOException {
        parser.parse(in, systemId, charset, handler, headers);
    }

    @Override
    public void parse(File file,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException {
        parser.parse(file, charset, handler, headers);
    }

    @Override
    public void parse(File file,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException {
        parser.parse(file, charset, handler, headers);
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException {
        parser.parse(in, systemId, charset, handler, headers);
    }

    @Override
    public void parse(InputStream in,
                      String systemId,
                      WorkbookKind kind,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException {
        parser.parse(in, systemId, charset, handler, headers);
    }
}