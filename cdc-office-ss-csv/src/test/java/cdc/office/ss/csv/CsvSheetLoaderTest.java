package cdc.office.ss.csv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import cdc.office.ss.SheetLoader;
import cdc.office.tables.Row;

class CsvSheetLoaderTest {
    private static final Logger LOGGER = LogManager.getLogger(CsvSheetLoaderTest.class);

    @Test
    void testLoadCsv() throws IOException {
        final File file = new File("src/test/resources/file1.csv");
        LOGGER.debug("file {}", file.exists());
        final SheetLoader loader = new SheetLoader();
        final List<Row> rows = loader.load(file, null, 0);
        for (final Row row : rows) {
            LOGGER.debug(row);
        }
        assertTrue(true);
    }
}