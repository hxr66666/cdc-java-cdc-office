package cdc.office.ss.csv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.WorkbookWriterTestSupport;

class CsvWorkbookWriterTest extends WorkbookWriterTestSupport {
    @Test
    void testTypesFileCsv() throws IOException {
        testTypesFile(new File("target/" + getClass().getSimpleName() + "-types.csv"),
                      10,
                      100);
        assertTrue(true);
    }

    @Test
    void testTypesOutputStreamCsv() throws IOException {
        testTypesOutputStream(new File("target/" + getClass().getSimpleName() + "-os-types.csv"),
                              10,
                              100);
        assertTrue(true);
    }
}