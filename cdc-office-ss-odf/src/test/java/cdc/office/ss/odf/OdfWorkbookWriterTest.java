package cdc.office.ss.odf;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.ss.WorkbookWriterTestSupport;

class OdfWorkbookWriterTest extends WorkbookWriterTestSupport {
    @Test
    void testTypesOds() throws IOException {
        testTypes(new File("target/" + getClass().getSimpleName() + "-types.ods"),
                  10,
                  100);
    }

    @Test
    void testTypesFastOds() throws IOException {
        testTypes(new File("target/" + getClass().getSimpleName() + "-types-fast.ods"),
                  10,
                  100,
                  f -> {
                      f.setEnabled(WorkbookWriterFactory.Hint.ODS_FAST, true);
                  },
                  WorkbookWriterFeatures.STANDARD_BEST);
    }
}