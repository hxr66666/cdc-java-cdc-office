package cdc.office.ss.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.WorkbookKind;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.util.lang.ExceptionWrapper;

/**
 * Streaming implementation of SheetParser for Excel files.
 * <p>
 * It can be used with xlsx/xlsm formats.
 *
 * @author Damien Carbonne
 *
 */
public class PoiStreamSheetParser implements SheetParser {
    protected static final Logger LOGGER = LogManager.getLogger(PoiStreamSheetParser.class);

    public PoiStreamSheetParser() {
        super();
    }

    public PoiStreamSheetParser(SheetParserFactory factory,
                                WorkbookKind kind) {
        this();
    }

    @Override
    public void parse(File file,
                      String password,
                      int headers,
                      TableHandler handler) throws IOException {
        try (OPCPackage opcPackage = OPCPackage.open(file.getPath(), PackageAccess.READ)) {
            final StreamParser parser = new StreamParser(opcPackage);
            parser.process(headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(InputStream in,
                      WorkbookKind kind,
                      String password,
                      int headers,
                      TableHandler handler) throws IOException {
        try (OPCPackage opcPackage = OPCPackage.open(in)) {
            final StreamParser parser = new StreamParser(opcPackage);
            parser.process(headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(File file,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException {
        try (OPCPackage opcPackage = OPCPackage.open(file.getPath(), PackageAccess.READ)) {
            final StreamParser parser = new StreamParser(opcPackage);
            parser.process(sheetName, headers, handler);
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
        try (OPCPackage opcPackage = OPCPackage.open(file.getPath(), PackageAccess.READ)) {
            final StreamParser parser = new StreamParser(opcPackage);
            parser.process(sheetIndex, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(InputStream in,
                      WorkbookKind kind,
                      String password,
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException {
        try (OPCPackage opcPackage = OPCPackage.open(in)) {
            final StreamParser parser = new StreamParser(opcPackage);
            parser.process(sheetName, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    @Override
    public void parse(InputStream in,
                      WorkbookKind kind,
                      String password,
                      int sheetIndex,
                      int headers,
                      TableHandler handler) throws IOException {
        try (OPCPackage opcPackage = OPCPackage.open(in)) {
            final StreamParser parser = new StreamParser(opcPackage);
            parser.process(sheetIndex, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private static class StreamParser {
        private final OPCPackage opcPackage;

        public StreamParser(OPCPackage opcPackage) {
            this.opcPackage = opcPackage;
            LOGGER.warn("Cannot estimate number of rows");
        }

        public void process(int headers,
                            TableHandler handler) throws Exception {
            final ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.opcPackage);
            final XSSFReader xssfReader = new XSSFReader(this.opcPackage);
            final StylesTable styles = xssfReader.getStylesTable();
            final XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (iter.hasNext()) {
                try (final InputStream stream = iter.next()) {
                    handler.processBegin(iter.getSheetName(), -1);
                    processSheet(styles,
                                 strings,
                                 new ExcelSheetHandler(headers, handler),
                                 stream);
                    handler.processEnd();
                }
            }
        }

        public void process(String sheetName,
                            int headers,
                            TableHandler handler) throws Exception {
            final ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.opcPackage);
            final XSSFReader xssfReader = new XSSFReader(this.opcPackage);
            final StylesTable styles = xssfReader.getStylesTable();
            final XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            boolean found = false;
            while (!found && iter.hasNext()) {
                try (final InputStream stream = iter.next()) {
                    if (sheetName.equals(iter.getSheetName())) {
                        found = true;
                        handler.processBegin(iter.getSheetName(), -1);
                        processSheet(styles,
                                     strings,
                                     new ExcelSheetHandler(headers, handler),
                                     stream);
                        handler.processEnd();
                    }
                }
            }
        }

        public void process(int sheetIndex,
                            int headers,
                            TableHandler handler) throws Exception {
            final ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.opcPackage);
            final XSSFReader xssfReader = new XSSFReader(this.opcPackage);
            final StylesTable styles = xssfReader.getStylesTable();
            final XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            boolean found = false;
            int index = 0;
            while (!found && iter.hasNext()) {
                try (final InputStream stream = iter.next()) {
                    if (sheetIndex == index) {
                        found = true;
                        handler.processBegin(iter.getSheetName(), -1);
                        processSheet(styles,
                                     strings,
                                     new ExcelSheetHandler(headers, handler),
                                     stream);
                        handler.processEnd();
                    }
                }
                index++;
            }
        }

        private static void processSheet(Styles styles,
                                         SharedStrings strings,
                                         SheetContentsHandler sheetHandler,
                                         InputStream sheetInputStream) throws IOException, SAXException {
            final DataFormatter formatter = new DataFormatter();
            final InputSource sheetSource = new InputSource(sheetInputStream);
            try {
                final XMLReader sheetParser = XMLHelper.newXMLReader();
                final ContentHandler handler =
                        new XSSFSheetXMLHandler(styles,
                                                null,
                                                strings,
                                                sheetHandler,
                                                formatter,
                                                false);
                sheetParser.setContentHandler(handler);
                sheetParser.parse(sheetSource);
            } catch (final ParserConfigurationException e) {
                throw new SAXException("SAX parser appears to be broken - " + e.getMessage());
            }
        }

        private static class ExcelSheetHandler implements SheetContentsHandler {
            private final int headers;
            private final TableHandler handler;
            private final Row.Builder row = Row.builder();
            private final RowLocation.Builder location = RowLocation.builder();
            private int previousRowIndex = -1;
            private int currentCol = -1;
            private boolean active = true;

            public ExcelSheetHandler(int headers,
                                     TableHandler handler) {
                this.headers = headers;
                this.handler = handler;
            }

            @Override
            public void startRow(int rowNum) {
                row.clear();

                for (int index = previousRowIndex; active && index < rowNum - 1; index++) {
                    location.incrementNumbers(headers);
                    active = TableHandler.processRow(handler, row.build(), location.build()).isContinue();
                }
                location.incrementNumbers(headers);
                previousRowIndex = rowNum;

                currentCol = -1;
            }

            @Override
            public void endRow(int rowNum) {
                if (active) {
                    active = TableHandler.processRow(handler, row.build(), location.build()).isContinue();
                }
            }

            @Override
            public void cell(String cellReference,
                             String formattedValue,
                             XSSFComment comment) {

                final int col;
                if (cellReference == null) {
                    col = currentCol;
                } else {
                    final CellAddress addr = new CellAddress(cellReference);
                    col = addr.getColumn();
                }

                // Did we miss any cells?
                final int missedCols = col - currentCol - 1;
                for (int i = 0; i < missedCols; i++) {
                    row.addValue(null);
                }
                currentCol = col;
                row.addValue(formattedValue);
            }
        }
    }
}