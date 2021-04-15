package cdc.office.benches;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.office.tables.TableSection;
import cdc.util.function.Evaluation;
import cdc.util.time.Chronometer;

public class SheetParserPerf {
    protected static final Logger LOGGER = LogManager.getLogger(SheetParserPerf.class);

    private static void log(String message,
                            int size,
                            double seconds,
                            Class<?> cls,
                            Object[] features) {
        LOGGER.info(String.format("%-10s %8d %8.4f %s %s",
                                  message,
                                  size,
                                  seconds,
                                  cls.getSimpleName(),
                                  Arrays.toString(features)));
    }

    private static void generate(File file,
                                 int rowsCount,
                                 boolean asStrings) throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();
        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        factory.setEnabled(WorkbookWriterFactory.Hint.ODS_FAST, true);
        final Object[] features = factory.getHints();
        final Class<?> cls;
        try (final WorkbookWriter<?> writer = factory.create(file, WorkbookWriterFeatures.DEFAULT)) {
            cls = writer.getClass();
            writer.beginSheet("Sheet");

            if (rowsCount >= 1) {
                writer.beginRow(TableSection.HEADER);
                writer.addCell("Empty");
                writer.addCell("String");
                writer.addCell("Boolean");
                writer.addCell("Double");
                writer.addCell("Float");
                writer.addCell("Long");
                writer.addCell("Int");
                writer.addCell("Short");
                writer.addCell("Byte");
                writer.addCell("Date");
                writer.addCell("Local Date");
                writer.addCell("Local Time");
                writer.addCell("Local Date Time");
                writer.addCell("Enum");
            }

            // writer.beginRow(TableSection.DATA);
            // writer.beginRow(TableSection.DATA);
            // writer.beginRow(TableSection.DATA);
            // writer.beginRow(TableSection.DATA);

            for (int rowIndex = 0; rowIndex < rowsCount - 1; rowIndex++) {
                writer.beginRow(TableSection.DATA);
                if (asStrings) {
                    writer.addEmptyCell();
                    writer.addCell("0001");
                    writer.addCell("true");
                    writer.addCell("10.5");
                    writer.addCell("10.5");
                    writer.addCell("9223372036854775807");
                    writer.addCell("2147483647");
                    writer.addCell("32767");
                    writer.addCell("127");
                    writer.addCell("2019/11/28 10:36:09");
                    writer.addCell("2019/11/28");
                    writer.addCell("10:36:09");
                    writer.addCell("2019/11/28 10:36:09");
                    writer.addCell("CSV");
                } else {
                    writer.addEmptyCell();
                    writer.addCell("0001");
                    writer.addCell(true);
                    writer.addCell(10.5);
                    writer.addCell(10.5f);
                    writer.addCell(Long.MAX_VALUE);
                    writer.addCell(Integer.MAX_VALUE);
                    writer.addCell(Short.MAX_VALUE);
                    writer.addCell(Byte.MAX_VALUE);
                    writer.addCell(new Date());
                    writer.addCell(LocalDate.now());
                    writer.addCell(LocalTime.now());
                    writer.addCell(LocalDateTime.now());
                    writer.addCell(WorkbookKind.CSV);
                }
            }
        }
        chrono.suspend();
        log("Creation", rowsCount, chrono.getElapsedSeconds(), cls, features);
    }

    private static class Handler implements TableHandler {
        public int count = 0;

        public Handler() {
            super();
        }

        @Override
        public void processBegin(String name,
                                 int numberOfRows) {
            // Ignore
        }

        @Override
        public Evaluation processHeader(Row header,
                                        RowLocation location) {
            count++;
            return Evaluation.CONTINUE;
        }

        @Override
        public Evaluation processData(Row data,
                                      RowLocation location) {
            count++;
            return Evaluation.CONTINUE;
        }

        @Override
        public void processEnd() {
            // Ignore
        }
    }

    private static void test(File file,
                             int size) throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();
        final SheetParserFactory factory = new SheetParserFactory();
        // factory.setEnabled(SheetParserFactory.Feature.POI_STREAMING, false);
        // factory.setEnabled(SheetParserFactory.Feature.POI_SAX, true);
        final Object[] features = factory.getFeatures();

        final SheetParser parser = factory.create(file);
        final Handler handler = new Handler();
        parser.parse(file, null, 0, 1, handler);
        chrono.suspend();
        log("Parsing", handler.count, chrono.getElapsedSeconds(), parser.getClass(), features);
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) throws IOException {

        if (false) {
            final File file = new File("target", SheetParserPerf.class.getSimpleName() + "-big.csv");
            final int rowsCount = 10000000;
            generate(file, rowsCount, true);
            generate(file, rowsCount, false);
            test(file, rowsCount);
        }

        final int[] rowsCounts = { 0, 1, 10, 100, 1000, 10000, 65536, 100000, 1000000, 1048576 };
        final WorkbookKind[] kinds = { WorkbookKind.CSV, WorkbookKind.XLS, WorkbookKind.XLSX };
        for (final WorkbookKind kind : kinds) {
            LOGGER.info("=========================================");
            LOGGER.info("Test {}", kind);
            for (final int rowsCount : rowsCounts) {
                LOGGER.info("===========================");
                if (kind.getMaxRows() < 0 || rowsCount <= kind.getMaxRows()) {
                    final File file =
                            new File("target", SheetParserPerf.class.getSimpleName() + "-" + rowsCount + "." + kind.getExtension());
                    try {
                        generate(file, rowsCount, false);
                        test(file, rowsCount);
                    } catch (final IllegalArgumentException e) {
                        LOGGER.catching(e);
                    }
                } else {
                    LOGGER.info("Skip {} {}", kind, rowsCount);
                }
            }
        }
    }
}