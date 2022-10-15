package cdc.office.ss.tools;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.tools.KeyedSheetDiff;
import cdc.util.files.Files;

class KeyedSheetDiffTest {
    private static void check(String filename1,
                              String filename2,
                              String ext,
                              boolean lineMarkColumn,
                              KeyedSheetDiff.MainArgs.Feature... features) throws IOException {
        final KeyedSheetDiff.MainArgs margs = new KeyedSheetDiff.MainArgs();
        margs.file1 = new File(filename1);
        margs.file2 = new File(filename2);

        final String ext1 = Files.getExtension(margs.file1);
        final String ext2 = Files.getExtension(margs.file2);

        margs.output = new File("target",
                                "diff-" + ext1 + "-" + ext2
                                        + "-" + (lineMarkColumn ? "line-mark" : "no-line-mark")
                                        + toString(features)
                                        + "." + ext);
        margs.keys.add("ID");
        margs.features.addAll(features);
        margs.lineMarkColumn = lineMarkColumn ? "Diff" : null;
        KeyedSheetDiff.execute(margs);
    }

    private static String toString(KeyedSheetDiff.MainArgs.Feature... features) {
        final StringBuilder builder = new StringBuilder();
        for (final KeyedSheetDiff.MainArgs.Feature feature : features) {
            builder.append("-");
            builder.append(feature.name().toLowerCase());
        }
        return builder.toString();
    }

    @Test
    void testNonMatchingHeaders() throws IOException {
        assertThrows(IllegalArgumentException.class,
                     () -> {
                         check("src/test/resources/ksd-test1-file1.csv",
                               "src/test/resources/ksd-test1-file2.csv",
                               "csv",
                               false);
                     });
    }

    @Test
    void testInvalidSheet1() throws IOException {
        final KeyedSheetDiff.MainArgs margs = new KeyedSheetDiff.MainArgs();
        margs.file1 = new File("src/test/resources/ksd-test-invalid-sheet-file1.xlsx");
        margs.file2 = new File("src/test/resources/ksd-test-invalid-sheet-file2.xlsx");
        margs.output = new File("target", "ksd-test-invalid-sheet.xlsx");
        margs.keys.add("ID");
        margs.sheet1 = "XXX";
        assertThrows(IOException.class,
                     () -> KeyedSheetDiff.execute(margs));
    }

    @Test
    void testInvalidSheet2() throws IOException {
        final KeyedSheetDiff.MainArgs margs = new KeyedSheetDiff.MainArgs();
        margs.file1 = new File("src/test/resources/ksd-test-invalid-sheet-file1.xlsx");
        margs.file2 = new File("src/test/resources/ksd-test-invalid-sheet-file2.xlsx");
        margs.output = new File("target", "ksd-test-invalid-sheet.xlsx");
        margs.keys.add("ID");
        margs.sheet2 = "XXX";
        assertThrows(IOException.class,
                     () -> KeyedSheetDiff.execute(margs));
    }

    @Test
    void testEmptySheet1() throws IOException {
        final KeyedSheetDiff.MainArgs margs = new KeyedSheetDiff.MainArgs();
        margs.file1 = new File("src/test/resources/ksd-test-empty-sheet.xlsx");
        margs.file2 = new File("src/test/resources/ksd-test-non-empty-sheet.xlsx");
        margs.output = new File("target", "ksd-test-empty-sheet.xlsx");
        margs.keys.add("ID");
        assertThrows(IllegalArgumentException.class,
                     () -> KeyedSheetDiff.execute(margs));
    }

    @Test
    void testEmptySheet2() throws IOException {
        final KeyedSheetDiff.MainArgs margs = new KeyedSheetDiff.MainArgs();
        margs.file1 = new File("src/test/resources/ksd-test-non-empty-sheet.xlsx");
        margs.file2 = new File("src/test/resources/ksd-test-empty-sheet.xlsx");
        margs.output = new File("target", "ksd-test-empty-sheet.xlsx");
        margs.keys.add("ID");
        assertThrows(IllegalArgumentException.class,
                     () -> KeyedSheetDiff.execute(margs));
    }
}