package cdc.office.ss.excel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.ss.WorkbookWriterTestSupport;

class ExcelWorkbookWriterTest extends WorkbookWriterTestSupport {
    @Test
    void testTypesFileXlsx() throws IOException {
        testTypesFile(new File("target/" + getClass().getSimpleName() + "-types.xlsx"),
                      10,
                      100);
        assertTrue(true);
    }

    @Test
    void testTypesFileXls() throws IOException {
        testTypesFile(new File("target/" + getClass().getSimpleName() + "-types.xls"),
                      10,
                      100);
        assertTrue(true);
    }

    @Test
    void testTypesFileXlsm() throws IOException {
        testTypesFile(new File("target/" + getClass().getSimpleName() + "-types.xlsm"),
                      10,
                      100);
        assertTrue(true);
    }

    @Test
    void testTypesOutputStreamXlsx() throws IOException {
        testTypesOutputStream(new File("target/" + getClass().getSimpleName() + "-os-types.xlsx"),
                              10,
                              100);
        assertTrue(true);
    }

    @Test
    void testTypesOutputStreamXls() throws IOException {
        testTypesOutputStream(new File("target/" + getClass().getSimpleName() + "-os-types.xls"),
                              10,
                              100);
        assertTrue(true);
    }

    @Test
    void testTypesOutputStreamXlsm() throws IOException {
        testTypesOutputStream(new File("target/" + getClass().getSimpleName() + "-os-types.xlsm"),
                              10,
                              100);
        assertTrue(true);
    }

    @Test
    void testLongTextXlsxDefault() throws IOException {
        testLongText(new File("target/" + getClass().getSimpleName() + "-long-text-default.xlsx"),
                     10,
                     100,
                     f -> {
                         // Ignore
                     },
                     WorkbookWriterFeatures.DEFAULT);
        assertTrue(true);
    }

    @Test
    void testLongTextXlsDefault() throws IOException {
        testLongText(new File("target/" + getClass().getSimpleName() + "-long-text-default.xls"),
                     10,
                     100,
                     f -> {
                         // Ignore
                     },
                     WorkbookWriterFeatures.DEFAULT);
        assertTrue(true);
    }

    @Test
    void testLongTextXlsmDefault() throws IOException {
        testLongText(new File("target/" + getClass().getSimpleName() + "-long-text-default.xlsm"),
                     10,
                     100,
                     f -> {
                         // Ignore
                     },
                     WorkbookWriterFeatures.DEFAULT);
        assertTrue(true);
    }

    @Test
    void testLongTextXlsxStandard() throws IOException {
        testLongText(new File("target/" + getClass().getSimpleName() + "-long-text-standard.xlsx"),
                     10,
                     100,
                     f -> {
                         // Ignore
                     },
                     WorkbookWriterFeatures.STANDARD_FAST);
        assertTrue(true);
    }

    @Test
    void testLongTextXlsStandard() throws IOException {
        testLongText(new File("target/" + getClass().getSimpleName() + "-long-text-standard.xls"),
                     10,
                     100,
                     f -> {
                         // Ignore
                     },
                     WorkbookWriterFeatures.STANDARD_FAST);
        assertTrue(true);
    }

    @Test
    void testLongTextXlsmStandard() throws IOException {
        testLongText(new File("target/" + getClass().getSimpleName() + "-long-text-standard.xlsm"),
                     10,
                     100,
                     f -> {
                         // Ignore
                     },
                     WorkbookWriterFeatures.STANDARD_FAST);
        assertTrue(true);
    }

    @Test
    void testDataValidationXlsx() throws IOException {
        testDataValidation(new File("target/" + getClass().getSimpleName() + "-data-validation.xlsx"));
        assertTrue(true);
    }
}