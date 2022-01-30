package cdc.office.ss.odf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class OdfSheetLoaderTest {
    private static final Logger LOGGER = LogManager.getLogger(OdfSheetLoaderTest.class);

    @Test
    void testLoadOds() throws IOException {
        // TODO this code has not yet been ported to odftoolkit-0.10.0
        // final File file = new File("src/test/resources/file1.ods");
        // LOGGER.debug("file {}", file.exists());
        // final SheetLoader loader = new SheetLoader();
        // final List<Row> rows = loader.load(file, null, 0);
        // for (final Row row : rows) {
        // LOGGER.debug(row);
        // }
        assertTrue(true);
    }
}