package cdc.office.ss.excel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import cdc.office.ss.SheetLoader;
import cdc.office.tables.Row;

class ExcelSheetLoaderTest {
    private static final Logger LOGGER = LogManager.getLogger(ExcelSheetLoaderTest.class);

    @Test
    void testLoadXlsx() throws IOException {
        final File file = new File("src/test/resources/file1.xlsx");
        LOGGER.debug("file {}", file.exists());
        final SheetLoader loader = new SheetLoader();
        final List<Row> rows = loader.load(file, null, 0);
        for (final Row row : rows) {
            LOGGER.debug(row);
        }
        assertTrue(true);
    }

    @Test
    void testLoadXlsm() throws IOException {
        final File file = new File("src/test/resources/file1.xlsm");
        LOGGER.debug("file {}", file.exists());
        final SheetLoader loader = new SheetLoader();
        final List<Row> rows = loader.load(file, null, 0);
        for (final Row row : rows) {
            LOGGER.debug(row);
        }
        assertTrue(true);
    }

    @Test
    void testLoadXls() throws IOException {
        final File file = new File("src/test/resources/file1.xls");
        LOGGER.debug("file {}", file.exists());
        final SheetLoader loader = new SheetLoader();
        final List<Row> rows = loader.load(file, null, 0);
        for (final Row row : rows) {
            LOGGER.debug(row);
        }
        assertTrue(true);
    }
}