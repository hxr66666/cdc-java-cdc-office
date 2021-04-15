package cdc.office.ss.csv;

import org.junit.jupiter.api.Test;

import cdc.office.ss.SheetTestSupport;

/**
 * Test generation and parsing of sheets using different formats and options.
 *
 * @author Damien Carbonne
 *
 */
public class CsvSheetTest extends SheetTestSupport {

    @Test
    public void testCsvDefault() throws Exception {
        check("-default.csv",
              (factory) -> {
                  // Ignore
              },
              (factory) -> {
                  // Ignore
              });
    }
}