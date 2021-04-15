package cdc.office.ss.excel;

import org.junit.jupiter.api.Test;

import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.SheetTestSupport;

/**
 * Test generation and parsing of sheets using different formats and options.
 *
 * @author Damien Carbonne
 *
 */
class ExcelSheetTest extends SheetTestSupport {
    @Test
    void testXlsDefault() throws Exception {
        check("-default.xls",
              factory -> {
                  // Ignore
              },
              factory -> {
                  // Ignore
              });
    }

    @Test
    void testXlsxDefault() throws Exception {
        check("-default.xlsx",
              factory -> {
                  // Ignore
              },
              factory -> {
                  // Ignore
              });
    }

    @Test
    void testXlsmDefault() throws Exception {
        check("-default.xlsm",
              factory -> {
                  // Ignore
              },
              factory -> {
                  // Ignore
              });
    }

    @Test
    void testXlsxStandard() throws Exception {
        check("-standard.xlsx",
              factory -> {
                  // Ignore
              },
              factory -> {
                  factory.setEnabled(SheetParserFactory.Feature.POI_STANDARD, true);
              });
    }

    @Test
    void testXlsmStandard() throws Exception {
        check("-standard.xlsm",
              factory -> {
                  // Ignore
              },
              factory -> {
                  factory.setEnabled(SheetParserFactory.Feature.POI_STANDARD, true);
              });
    }

    @Test
    void testXlsxStreaming() throws Exception {
        check("-streaming.xlsx",
              factory -> {
                  // Ignore
              },
              factory -> {
                  factory.setEnabled(SheetParserFactory.Feature.POI_STREAMING, true);
              });
    }

    @Test
    void testXlsmStreaming() throws Exception {
        check("-streaming.xlsm",
              factory -> {
                  // Ignore
              },
              factory -> {
                  factory.setEnabled(SheetParserFactory.Feature.POI_STREAMING, true);
              });
    }
}