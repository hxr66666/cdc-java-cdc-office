package cdc.office.ss.odf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.ss.WorkbookWriterTestSupport;

class OdfWorkbookWriterTest extends WorkbookWriterTestSupport {
    @Test
    void testTypesFileOds() throws IOException {
        testTypesFile(new File("target/" + getClass().getSimpleName() + "-types.ods"),
                      10,
                      100);
        assertTrue(true);
    }

    @Test
    void testTypesFileFastOds() throws IOException {
        testTypesFile(new File("target/" + getClass().getSimpleName() + "-types-fast.ods"),
                      10,
                      100,
                      f -> {
                          f.setEnabled(WorkbookWriterFactory.Hint.ODS_FAST, true);
                      },
                      WorkbookWriterFeatures.STANDARD_BEST);
        assertTrue(true);
    }

    @Test
    void testTypesOutputStreamOds() throws IOException {
        testTypesOutputStream(new File("target/" + getClass().getSimpleName() + "-os-types.ods"),
                              10,
                              100);
        assertTrue(true);
    }

    @Test
    void testTypesOutputStreamFastOds() throws IOException {
        testTypesOutputStream(new File("target/" + getClass().getSimpleName() + "-os-types-fast.ods"),
                              10,
                              100,
                              f -> {
                                  f.setEnabled(WorkbookWriterFactory.Hint.ODS_FAST, true);
                              },
                              WorkbookWriterFeatures.STANDARD_BEST);
        assertTrue(true);
    }
}