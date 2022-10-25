package cdc.office.benches;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.ss.WorkbookWriterFeatures.Feature;
import cdc.office.tables.TableSection;
import cdc.office.tools.KeyedSheetDiff;

public final class KsdBench {
    private static final Logger LOGGER = LogManager.getLogger(KsdBench.class);
    private static final String ID = "ID";
    private static final String SHEET = "Sheet";

    public static final WorkbookWriterFeatures FEATURES =
            WorkbookWriterFeatures.builder()
                                  .enable(Feature.AUTO_FILTER_COLUMNS)
                                  .build();

    private static void generate(File file,
                                 int rows) throws IOException {
        LOGGER.info("Generate {}", file);
        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        try (final WorkbookWriter<?> writer = factory.create(file, FEATURES)) {
            writer.beginSheet(SHEET);
            writer.beginRow(TableSection.HEADER);
            writer.addCells(ID, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
            final Random random = new Random();
            int row = 0;
            int count = 0;
            while (count < rows) {
                if (random.nextBoolean()) {
                    writer.beginRow(TableSection.DATA);
                    writer.addCell(row);
                    writer.addCells("a" + random.nextInt(2),
                                    "b" + random.nextInt(2),
                                    "c" + random.nextInt(2),
                                    "d" + random.nextInt(2),
                                    "e" + random.nextInt(2),
                                    "f" + random.nextInt(2),
                                    "g" + random.nextInt(2),
                                    "h" + random.nextInt(2),
                                    "i" + random.nextInt(2),
                                    "j" + random.nextInt(2));
                    count++;
                }
                row++;
            }
            writer.flush();
        }
        LOGGER.info("Done");
    }

    private static void run(int rows) throws IOException {
        final File file1 = new File("target/file1-" + rows + ".csv");
        generate(file1, rows);
        final File file2 = new File("target/file2-" + rows + ".csv");
        generate(file2, rows);

        final KeyedSheetDiff.MainArgs margs = new KeyedSheetDiff.MainArgs();
        margs.file1 = file1;
        margs.sheet1 = SHEET;
        margs.file2 = file2;
        margs.sheet2 = SHEET;
        margs.keys.add(ID);
        margs.features.add(KeyedSheetDiff.MainArgs.Feature.VERBOSE);
        margs.features.add(KeyedSheetDiff.MainArgs.Feature.SAVE_SYNTHESIS);
        margs.features.add(KeyedSheetDiff.MainArgs.Feature.SHOW_CHANGE_DETAILS);
        margs.features.add(KeyedSheetDiff.MainArgs.Feature.SORT_LINES);
        margs.lineMarkColumn = "Diff";
        margs.output = new File("target/output-" + rows + ".xlsx");
        KeyedSheetDiff.execute(margs);

    }

    public static void main(String[] args) throws IOException {
        run(100);
        run(200);
        run(500);
        run(1000);
        run(2000);
        run(5000);
        run(10000);
        run(20000);
        run(50000);
        run(100000);
        run(200000);
        run(500000);
    }
}