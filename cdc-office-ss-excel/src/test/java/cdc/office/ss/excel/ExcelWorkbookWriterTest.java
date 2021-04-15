package cdc.office.ss.excel;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.ss.WorkbookWriterTestSupport;

class ExcelWorkbookWriterTest extends WorkbookWriterTestSupport {
    @Test
    void testTypesXlsx() throws IOException {
        testTypes(new File("target/" + getClass().getSimpleName() + "-types.xlsx"),
                  10,
                  100);
    }

    @Test
    void testTypesXls() throws IOException {
        testTypes(new File("target/" + getClass().getSimpleName() + "-types.xls"),
                  10,
                  100);
    }

    @Test
    void testTypesXlsm() throws IOException {
        testTypes(new File("target/" + getClass().getSimpleName() + "-types.xlsm"),
                  10,
                  100);
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
    }
}