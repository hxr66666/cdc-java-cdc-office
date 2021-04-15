package cdc.office.ss.tools;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.tools.KeyedSheetDiff.MainArgs.Feature;
import cdc.util.files.Files;

class KeyedSheetDiffTest {

    private static String toString(Feature... features) {
        final StringBuilder builder = new StringBuilder();
        for (final Feature feature : features) {
            builder.append("-");
            builder.append(feature.name().toLowerCase());
        }
        return builder.toString();
    }

    private static void check(String filename1,
                              String filename2,
                              String ext,
                              boolean lineMarkColumn,
                              Feature... features) throws IOException {
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

    private static void checkAll(String filename1,
                                 String filename2,
                                 String ext) throws IOException {
        check(filename1,
              filename2,
              ext,
              false);
        check(filename1,
              filename2,
              ext,
              true);
        check(filename1,
              filename2,
              ext,
              false,
              Feature.NO_UNCHANGED_LINES);
        check(filename1,
              filename2,
              ext,
              true,
              Feature.NO_UNCHANGED_LINES);
        check(filename1,
              filename2,
              ext,
              false,
              Feature.NO_ADDED_OR_REMOVED_MARKS);
        check(filename1,
              filename2,
              ext,
              true,
              Feature.NO_ADDED_OR_REMOVED_MARKS);
        check(filename1,
              filename2,
              ext,
              false,
              Feature.NO_ADDED_OR_REMOVED_MARKS,
              Feature.SORT_LINES);
        check(filename1,
              filename2,
              ext,
              true,
              Feature.NO_ADDED_OR_REMOVED_MARKS,
              Feature.SORT_LINES);
        check(filename1,
              filename2,
              ext,
              false,
              Feature.NO_ADDED_OR_REMOVED_MARKS,
              Feature.NO_UNCHANGED_LINES);
        check(filename1,
              filename2,
              ext,
              true,
              Feature.NO_ADDED_OR_REMOVED_MARKS,
              Feature.NO_UNCHANGED_LINES,
              Feature.SYNTHESIS);
        check(filename1,
              filename2,
              ext,
              true,
              Feature.NO_COLORS,
              Feature.SORT_LINES);
    }

    @Test
    void testCsvCsvCsv() throws IOException {
        checkAll("src/test/resources/file1.csv",
                 "src/test/resources/file2.csv",
                 "csv");
    }

    @Test
    void testCsvCsvXls() throws IOException {
        checkAll("src/test/resources/file1.csv",
                 "src/test/resources/file2.csv",
                 "xls");
    }

    @Test
    void testCsvCsvXlsx() throws IOException {
        checkAll("src/test/resources/file1.csv",
                 "src/test/resources/file2.csv",
                 "xlsx");
    }

    @Test
    void testXlsCsvCsv() throws IOException {
        checkAll("src/test/resources/file1.xls",
                 "src/test/resources/file2.csv",
                 "csv");
    }

    @Test
    void testXlsCsvXls() throws IOException {
        checkAll("src/test/resources/file1.xls",
                 "src/test/resources/file2.csv",
                 "xls");
    }

    @Test
    void testXlsCsvXlsx() throws IOException {
        checkAll("src/test/resources/file1.xls",
                 "src/test/resources/file2.csv",
                 "xlsx");
    }

    @Test
    void testXlsXlsxCsv() throws IOException {
        checkAll("src/test/resources/file1.xls",
                 "src/test/resources/file2.xlsx",
                 "csv");
    }

    @Test
    void testXlsXlsxXls() throws IOException {
        checkAll("src/test/resources/file1.xls",
                 "src/test/resources/file2.xlsx",
                 "xls");
    }

    @Test
    void testXlsXlsxXlsx() throws IOException {
        checkAll("src/test/resources/file1.xls",
                 "src/test/resources/file2.xlsx",
                 "xlsx");
    }

    @Test
    void testOdsOdsOds() throws IOException {
        checkAll("src/test/resources/file1.ods",
                 "src/test/resources/file2.ods",
                 "ods");
    }
}