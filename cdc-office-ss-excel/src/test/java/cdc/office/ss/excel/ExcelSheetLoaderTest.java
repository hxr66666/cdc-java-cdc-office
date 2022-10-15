package cdc.office.ss.excel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import cdc.office.ss.SheetLoader;
import cdc.office.ss.SheetParserFactory;
import cdc.office.tables.Row;

class ExcelSheetLoaderTest {
    private static final Logger LOGGER = LogManager.getLogger(ExcelSheetLoaderTest.class);

    private static void test(String filename,
                             SheetParserFactory.Feature... features) throws IOException {
        final File file = new File("src/test/resources/", filename);
        LOGGER.debug("test({}, {})", file, features);
        final SheetLoader loader = new SheetLoader();
        for (final SheetParserFactory.Feature feature : features) {
            loader.getFactory().setEnabled(feature, true);
        }
        final List<Row> rows = loader.load(file, null, 0);
        for (final Row row : rows) {
            LOGGER.debug(row);
            for (final String s : row.getValues()) {
                assertFalse(s.contains("+"), "Unexpexted content; " + s);
            }
        }
        assertTrue(true);
    }

    private static void testInvalidSheetName(String filename,
                                             String sheetName,
                                             SheetParserFactory.Feature... features) throws IOException {
        final File file = new File("src/test/resources/", filename);
        final SheetLoader loader = new SheetLoader();
        for (final SheetParserFactory.Feature feature : features) {
            loader.getFactory().setEnabled(feature, true);
        }
        assertThrows(IOException.class,
                     () -> loader.load(file, null, sheetName));
    }

    private static void testInvalidSheetIndex(String filename,
                                              SheetParserFactory.Feature... features) throws IOException {
        final File file = new File("src/test/resources/", filename);
        final SheetLoader loader = new SheetLoader();
        for (final SheetParserFactory.Feature feature : features) {
            loader.getFactory().setEnabled(feature, true);
        }
        assertThrows(IOException.class,
                     () -> loader.load(file, null, 100));
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testLoad(String filename) throws IOException {
        test(filename);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testLoadPoiSax(String filename) throws IOException {
        test(filename,
             SheetParserFactory.Feature.POI_SAX);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testLoadPoiStandard(String filename) throws IOException {
        test(filename,
             SheetParserFactory.Feature.POI_STANDARD);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testLoadPoiStandardEvaluateFormula(String filename) throws IOException {
        test(filename,
             SheetParserFactory.Feature.POI_STANDARD,
             SheetParserFactory.Feature.EVALUATE_FORMULA);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testLoadPoiStreaming(String filename) throws IOException {
        test(filename,
             SheetParserFactory.Feature.POI_STREAMING);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testInvalidSheetNamePoiSax(String filename) throws IOException {
        testInvalidSheetName(filename,
                             "XXX",
                             SheetParserFactory.Feature.POI_SAX);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testInvalidSheetNamePoiStandard(String filename) throws IOException {
        testInvalidSheetName(filename,
                             "XXX",
                             SheetParserFactory.Feature.POI_STANDARD);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testInvalidSheetNamePoiStreaming(String filename) throws IOException {
        testInvalidSheetName(filename,
                             "XXX",
                             SheetParserFactory.Feature.POI_STREAMING);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testInvalidSheetIndexPoiSax(String filename) throws IOException {
        testInvalidSheetIndex(filename,
                              SheetParserFactory.Feature.POI_SAX);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testInvalidSheetIndexPoiStandard(String filename) throws IOException {
        testInvalidSheetIndex(filename,
                              SheetParserFactory.Feature.POI_STANDARD);
    }

    @ParameterizedTest
    @ValueSource(strings = { "file1.xlsx", "file1.xlsm", "file1.xls" })
    void testInvalidSheetIndexPoiStreaming(String filename) throws IOException {
        testInvalidSheetIndex(filename,
                              SheetParserFactory.Feature.POI_STREAMING);
    }
}