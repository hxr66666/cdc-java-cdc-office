package cdc.office.benches;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.tables.TableSection;
import cdc.util.time.Chronometer;

public final class WorkbookWriterPerf {
    private static final Logger LOGGER = LogManager.getLogger(WorkbookWriterPerf.class);

    private WorkbookWriterPerf() {
    }

    private static void test(File file,
                             int sheetsCount,
                             int rowsCount) throws IOException {
        LOGGER.info("Generate {}", file);
        final Chronometer chrono = new Chronometer();
        chrono.start();
        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        factory.setEnabled(WorkbookWriterFactory.Hint.POI_STREAMING, true);
        try (final WorkbookWriter<?> writer = factory.create(file, WorkbookWriterFeatures.STANDARD_FAST)) {
            for (int sheetIndex = 0; sheetIndex < sheetsCount; sheetIndex++) {
                writer.beginSheet("Sheet " + sheetIndex);

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

                for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
                    writer.beginRow(TableSection.DATA);
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
        LOGGER.info("Generated {} {}", file, chrono);
    }

    public static void main(String[] args) throws IOException {
        final int[] rowsCounts = { 0, 1, 10, 100, 1000, 10000, 100000, 1000000 };

        for (final int rowsCount : rowsCounts) {
            for (final WorkbookKind kind : WorkbookKind.values()) {
                if (kind != WorkbookKind.ODS) {
                    final File file =
                            new File("target",
                                     WorkbookWriterPerf.class.getSimpleName() + "-" + rowsCount + "." + kind.getExtension());
                    try {
                        test(file, 1, rowsCount);
                    } catch (final IllegalArgumentException e) {
                        LOGGER.error("Failed to generate {}", file);
                        LOGGER.catching(e);
                    }
                }
            }
        }
    }
}