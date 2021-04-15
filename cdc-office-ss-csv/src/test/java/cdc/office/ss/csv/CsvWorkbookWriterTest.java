package cdc.office.ss.csv;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.WorkbookWriterTestSupport;

class CsvWorkbookWriterTest extends WorkbookWriterTestSupport {
    @Test
    void testTypesCsv() throws IOException {
        testTypes(new File("target/" + getClass().getSimpleName() + "-types.csv"),
                  10,
                  100);
    }
}