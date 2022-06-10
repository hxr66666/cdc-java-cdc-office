package cdc.office.demos;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DemosTest {
    @Test
    void testCsvDemo() throws Exception {
        CsvDemo.main();
        assertTrue(true);
    }

    @Test
    void testDataValidationDemo() throws Exception {
        DataValidationDemo.main();
        assertTrue(true);
    }

    @Test
    void testKeyedSheetDiffDemo() throws Exception {
        KeyedSheetDiffDemo.main();
        assertTrue(true);
    }

    @Test
    void testRichTextDemo() throws Exception {
        RichTextDemo.main();
        assertTrue(true);
    }
}