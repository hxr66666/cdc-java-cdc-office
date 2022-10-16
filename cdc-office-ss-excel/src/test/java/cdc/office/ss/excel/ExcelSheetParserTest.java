package cdc.office.ss.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.tables.MemoryTableHandler;

class ExcelSheetParserTest {
    private static final Logger LOGGER = LogManager.getLogger(ExcelSheetParserTest.class);

    private static void testNormal(String filename,
                                   int total,
                                   int headers,
                                   SheetParserFactory.Feature... features) throws IOException {
        LOGGER.info("features {}", (Object[]) features);
        final File file = new File("src/test/resources/", filename);
        final SheetParserFactory factory = new SheetParserFactory();
        for (final SheetParserFactory.Feature feature : features) {
            factory.setEnabled(feature, true);
        }
        final SheetParser parser = factory.create(file);
        final MemoryTableHandler handler = new MemoryTableHandler(false);
        parser.parse(file, null, 0, headers, handler);

        LOGGER.info("total rows: {}", handler.getRows().size());
        LOGGER.info("header rows: {}", handler.getHeaderRowsCount());
        LOGGER.info("date rows: {}", handler.getDataRowsCount());
        LOGGER.info("empty trailing rows: {}", handler.getEmptyTrailingRowsCount());

        assertEquals(total, handler.getRowsCount());
        assertEquals(headers, handler.getHeaderRowsCount());
        assertEquals(total - headers, handler.getDataRowsCount());
        assertEquals(0, handler.getEmptyTrailingRowsCount());
    }

    @ParameterizedTest
    @ValueSource(strings = { "normal.xlsx", "normal.xls" })
    void testPoiStandard0(String filename) throws IOException {
        testNormal(filename,
                   5,
                   0,
                   SheetParserFactory.Feature.POI_STANDARD);
    }

    @ParameterizedTest
    @ValueSource(strings = { "normal.xlsx", "normal.xls" })
    void testPoiStreaming0(String filename) throws IOException {
        testNormal(filename,
                   5,
                   0,
                   SheetParserFactory.Feature.POI_STREAMING);
    }

    @ParameterizedTest
    @ValueSource(strings = { "normal.xlsx", "normal.xls" })
    void testPoiSax0(String filename) throws IOException {
        testNormal(filename,
                   5,
                   0,
                   SheetParserFactory.Feature.POI_SAX);
    }

    @ParameterizedTest
    @ValueSource(strings = { "normal.xlsx", "normal.xls" })
    void testPoiStandard1(String filename) throws IOException {
        testNormal(filename,
                   5,
                   1,
                   SheetParserFactory.Feature.POI_STANDARD);
    }

    @ParameterizedTest
    @ValueSource(strings = { "normal.xlsx", "normal.xls" })
    void testPoiStreaming1(String filename) throws IOException {
        testNormal(filename,
                   5,
                   1,
                   SheetParserFactory.Feature.POI_STREAMING);
    }

    @ParameterizedTest
    @ValueSource(strings = { "normal.xlsx", "normal.xls" })
    void testPoiSax1(String filename) throws IOException {
        testNormal(filename,
                   5,
                   1,
                   SheetParserFactory.Feature.POI_SAX);
    }
}