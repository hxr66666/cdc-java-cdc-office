package cdc.office.demos;

import java.io.File;
import java.io.IOException;

import cdc.office.tools.KeyedSheetDiff;
import cdc.office.tools.KeyedSheetDiff.MainArgs.Feature;
import cdc.util.files.Files;

public class KeyedSheetDiffDemo {
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

    public static void main(String... args) throws IOException {
        final String file1 = "src/main/resources/file1";
        final String csv1 = file1 + ".csv";
        final String ods1 = file1 + ".ods";
        final String xls1 = file1 + ".xls";
        // final String xlsm1 = file1 + ".xlsm";
        // final String xlsx1 = file1 + ".xlsx";

        final String file2 = "src/main/resources/file2";
        final String csv2 = file2 + ".csv";
        final String ods2 = file2 + ".ods";
        // final String xls2 = file2 + ".xls";
        // final String xlsm2 = file2 + ".xlsm";
        final String xlsx2 = file2 + ".xlsx";

        checkAll(csv1, csv2, "csv");
        checkAll(csv1, csv2, "xls");
        checkAll(csv1, csv2, "xlsx");
        checkAll(xls1, csv2, "csv");
        checkAll(xls1, csv2, "xls");
        checkAll(xls1, csv2, "xlsx");
        checkAll(xls1, xlsx2, "csv");
        checkAll(xls1, xlsx2, "xls");
        checkAll(xls1, xlsx2, "xlsx");
        checkAll(ods1, ods2, "ods");
    }
}