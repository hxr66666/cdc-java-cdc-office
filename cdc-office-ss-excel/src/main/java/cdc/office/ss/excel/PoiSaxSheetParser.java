package cdc.office.ss.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.WorkbookKind;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.office.tables.TablesHandler;
import cdc.util.function.Evaluation;
import cdc.util.lang.ExceptionWrapper;

public class PoiSaxSheetParser implements SheetParser {
    protected static final Logger LOGGER = LogManager.getLogger(PoiSaxSheetParser.class);

    public PoiSaxSheetParser() {
        super();
    }

    public PoiSaxSheetParser(SheetParserFactory factory,
                             WorkbookKind kind) {
        this();
        LOGGER.warn("Cannot estimate number of rows");
    }

    @Override
    public void parse(File file,
                      String password,
                      int headers,
                      TablesHandler handler) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(file.getPath(), PackageAccess.READ)) {
            parse(file.getPath(), pkg, headers, handler);
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
        try (OPCPackage pkg = OPCPackage.open(in)) {
            parse(systemId, pkg, headers, handler);
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
        try (OPCPackage pkg = OPCPackage.open(file.getPath(), PackageAccess.READ)) {
            parse(file.getPath(), pkg, sheetName, headers, handler);
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
        try (OPCPackage pkg = OPCPackage.open(file.getPath(), PackageAccess.READ)) {
            parse(file.getPath(), pkg, sheetIndex, headers, handler);
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
                      String sheetName,
                      int headers,
                      TableHandler handler) throws IOException {
        try (OPCPackage pkg = OPCPackage.open(in)) {
            parse(systemId, pkg, sheetName, headers, handler);
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
        try (OPCPackage pkg = OPCPackage.open(in)) {
            parse(systemId, pkg, sheetIndex, headers, handler);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private static void parse(String systemId,
                              OPCPackage pkg,
                              int headers,
                              TablesHandler handler) throws IOException {
        try {
            final XSSFReader r = new XSSFReader(pkg);
            final SharedStrings sst = r.getSharedStringsTable();
            final StylesTable styles = r.getStylesTable();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("styles:");
                LOGGER.debug(styles);
                LOGGER.debug("number formats: {}", styles.getNumDataFormats());
                for (final Map.Entry<Short, String> entry : styles.getNumberFormats().entrySet()) {
                    LOGGER.debug("   number format: {}", entry);
                }
            }

            handler.processBeginTables(systemId);

            final XMLReader parser = fetchSheetParser(headers, handler, sst, styles);

            final XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) r.getSheetsData();
            while (sheets.hasNext()) {
                LOGGER.debug("Processing new sheet");
                try (InputStream sheet = sheets.next()) {
                    final InputSource sheetSource = new InputSource(sheet);
                    handler.processBeginTable(sheets.getSheetName(), -1);
                    parser.parse(sheetSource);
                    handler.processEndTable(sheets.getSheetName());
                }
                LOGGER.debug("Processed sheet");
            }

            handler.processEndTables(systemId);

        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private static void parse(String systemId,
                              OPCPackage pkg,
                              String sheetName,
                              int headers,
                              TableHandler handler) throws IOException {
        try {
            TablesHandler.processBeginTables(handler, systemId);

            final XSSFReader r = new XSSFReader(pkg);
            final SharedStrings sst = r.getSharedStringsTable();
            final StylesTable styles = r.getStylesTable();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("styles:");
                LOGGER.debug(styles);
                LOGGER.debug("number formats: {}", styles.getNumDataFormats());
                for (final Map.Entry<Short, String> entry : styles.getNumberFormats().entrySet()) {
                    LOGGER.debug("   number format: {}", entry);
                }
            }

            final XMLReader parser = fetchSheetParser(headers, handler, sst, styles);

            final XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) r.getSheetsData();
            boolean found = false;
            while (!found && sheets.hasNext()) {
                LOGGER.debug("Processing new sheet");
                try (InputStream sheet = sheets.next()) {
                    if (sheetName == null || sheets.getSheetName().equals(sheetName)) {
                        if (sheetName != null) {
                            found = true;
                        }
                        final InputSource sheetSource = new InputSource(sheet);
                        handler.processBeginTable(sheets.getSheetName(), -1);
                        parser.parse(sheetSource);
                        handler.processEndTable(sheets.getSheetName());
                    }
                }
                LOGGER.debug("Processed sheet");
            }

            TablesHandler.processEndTables(handler, systemId);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private static void parse(String systemId,
                              OPCPackage pkg,
                              int sheetIndex,
                              int headers,
                              TableHandler handler) throws IOException {
        try {
            TablesHandler.processBeginTables(handler, systemId);

            final XSSFReader r = new XSSFReader(pkg);
            final SharedStrings sst = r.getSharedStringsTable();
            final StylesTable styles = r.getStylesTable();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("styles:");
                LOGGER.debug(styles);
                LOGGER.debug("number formats: {}", styles.getNumDataFormats());
                for (final Map.Entry<Short, String> entry : styles.getNumberFormats().entrySet()) {
                    LOGGER.debug("   number format: {}", entry);
                }
            }

            final XMLReader parser = fetchSheetParser(headers, handler, sst, styles);

            final XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) r.getSheetsData();
            int index = -1;
            while (sheets.hasNext() && index < sheetIndex) {
                index++;
                LOGGER.debug("Processing new sheet");
                try (InputStream sheet = sheets.next()) {
                    if (index == sheetIndex) {
                        final InputSource sheetSource = new InputSource(sheet);
                        handler.processBeginTable(sheets.getSheetName(), -1);
                        parser.parse(sheetSource);
                        handler.processEndTable(sheets.getSheetName());
                    }
                }
                LOGGER.debug("Processed sheet");
            }

            TablesHandler.processEndTables(handler, systemId);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw ExceptionWrapper.wrap(e);
        }
    }

    private static XMLReader fetchSheetParser(int headers,
                                              TableHandler handler,
                                              SharedStrings sst,
                                              Styles styles) throws SAXException, ParserConfigurationException {
        final XMLReader parser = XMLHelper.newXMLReader();
        final SheetHandler sheetHandler = new SheetHandler(headers, handler, sst, styles);
        parser.setContentHandler(sheetHandler);
        return parser;
    }

    private static class SheetHandler extends DefaultHandler {
        private final int headers;
        private final TableHandler handler;
        private final SharedStrings sst;
        private final Styles styles;
        private String lastContents; // TODO Use StringBuilder
        private boolean nextIsString;
        private boolean inlineStr;
        private final LruCache<Integer, String> lruCache = new LruCache<>(50);
        private short formatIndex = -1;
        private String formatString = null;
        private int previousRowIndex = -1;
        private int previousColumnIndex = -1;
        private final Row.Builder r = Row.builder();
        private final RowLocation.Builder location = RowLocation.builder();
        private boolean active = true;

        private static class LruCache<A, B> extends LinkedHashMap<A, B> {
            private static final long serialVersionUID = 1L;
            private final int maxEntries;

            public LruCache(int maxEntries) {
                super(maxEntries + 1, 1.0f, true);
                this.maxEntries = maxEntries;
            }

            @Override
            protected boolean removeEldestEntry(Map.Entry<A, B> eldest) {
                return super.size() > maxEntries;
            }
        }

        // private static String toString(Attributes atts) {
        // final StringBuilder builder = new StringBuilder();
        // for (int index = 0; index < atts.getLength(); index++) {
        // builder.append(" " + atts.getLocalName(index) + ":" + atts.getValue(index));
        // }
        // return builder.toString();
        // }

        public SheetHandler(int headers,
                            TableHandler handler,
                            SharedStrings sst,
                            Styles styles) {
            this.headers = headers;
            this.handler = handler;
            this.sst = sst;
            this.styles = styles;
        }

        private void addCell(String content) {
            if (content == null) {
                r.addValue(null);
            } else {
                // TODO format content
                r.addValue(content);
            }
        }

        private Evaluation publishRow() throws SAXException {
            try {
                return TableHandler.processRow(handler, r.build(), location.build());
            } catch (final Exception e) {
                throw new SAXException(e);
            }
        }

        @Override
        public void startElement(String uri,
                                 String localName,
                                 String name,
                                 Attributes attributes) throws SAXException {
            // c => cell
            if (name.equals("c")) {
                // Print the cell reference
                final String cellTypeStr = attributes.getValue("t");
                final XssfDataType cellType = XssfDataType.from(cellTypeStr);
                final String cellRefStr = attributes.getValue("r");
                final CellAddress addr = new CellAddress(cellRefStr);
                final String cellStyleStr = attributes.getValue("s");
                final int rowIndex = addr.getRow();
                final int columnIndex = addr.getColumn();
                if (previousRowIndex != rowIndex) {
                    if (active && location.getGlobalNumber() >= 0) {
                        active = publishRow().isContinue();
                    }
                    location.incrementNumbers(headers);
                    r.clear();
                    this.previousColumnIndex = -1;
                }
                for (int index = previousRowIndex; active && index < rowIndex - 1; index++) {
                    LOGGER.debug("Added missing row");
                    location.incrementNumbers(headers);
                    active = publishRow().isContinue();
                }

                if (previousRowIndex != rowIndex && previousRowIndex % 10000 == 0) {
                    LOGGER.info("processed: {}", previousRowIndex);
                }
                previousRowIndex = rowIndex;

                for (int index = previousColumnIndex; index < columnIndex - 1; index++) {
                    LOGGER.debug("Added missing cell");
                    addCell(null);
                }

                LOGGER.debug("{} ({})", addr, cellType);
                if (cellType == null) {
                    this.previousColumnIndex++;
                    addCell(null);
                }
                previousColumnIndex = columnIndex;

                // Figure out if the value is an index in the SST
                nextIsString = cellType == XssfDataType.SST_STRING;
                inlineStr = cellType == XssfDataType.INLINE_STRING;

                if (cellType == XssfDataType.NUMBER && styles != null) {
                    final XSSFCellStyle style;
                    if (cellStyleStr != null) {
                        final int styleIndex = Integer.parseInt(cellStyleStr);
                        style = styles.getStyleAt(styleIndex);
                    } else if (styles.getNumCellStyles() > 0) {
                        style = styles.getStyleAt(0);
                    } else {
                        style = null;
                    }

                    if (style != null) {
                        this.formatIndex = style.getDataFormat();
                        this.formatString = style.getDataFormatString();
                        if (this.formatString == null) {
                            this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
                        }
                        LOGGER.debug("format: {} '{}'", formatIndex, formatString);
                    }
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        @Override
        public void endElement(String uri,
                               String localName,
                               String name) throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if (nextIsString) {
                final Integer idx = Integer.valueOf(lastContents);
                lastContents = lruCache.get(idx);
                if (lastContents == null && !lruCache.containsKey(idx)) {
                    lastContents = sst.getItemAt(idx).toString();
                    lruCache.put(idx, lastContents);
                }
                nextIsString = false;
            }

            // v => contents of a cell
            // Output after we've seen the string contents
            if (name.equals("v") || (inlineStr && name.equals("c"))) {
                // TODO format content
                addCell(lastContents);
            }
        }

        @Override
        public void characters(char[] ch,
                               int start,
                               int length) throws SAXException {
            lastContents += new String(ch, start, length);
        }

        @Override
        public void endDocument() throws SAXException {
            // Publish last row
            if (active && previousRowIndex >= 0) {
                active = publishRow().isContinue();
            }
        }
    }
}