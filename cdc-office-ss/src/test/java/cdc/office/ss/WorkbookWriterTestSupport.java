package cdc.office.ss;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.ss.ContentValidation.ErrorReaction;
import cdc.office.ss.ContentValidation.ValidationType;
import cdc.office.tables.TableSection;
import cdc.util.time.Chronometer;

public class WorkbookWriterTestSupport {
    private static final Logger LOGGER = LogManager.getLogger(WorkbookWriterTestSupport.class);

    protected static void testTypesFile(File file,
                                        int sheetsCount,
                                        int rowsCount) throws IOException {
        testTypesFile(file,
                      sheetsCount,
                      rowsCount,
                      f -> {
                          // Ignore
                      },
                      WorkbookWriterFeatures.DEFAULT);
    }

    protected static void testTypesOutputStream(File file,
                                                int sheetsCount,
                                                int rowsCount) throws IOException {
        testTypesOutputStream(file,
                              sheetsCount,
                              rowsCount,
                              f -> {
                                  // Ignore
                              },
                              WorkbookWriterFeatures.DEFAULT);
    }

    protected static void fill(WorkbookWriter<?> writer,
                               int sheetsCount,
                               int rowsCount) throws IOException {
        for (int sheetIndex = 0; sheetIndex < sheetsCount; sheetIndex++) {
            writer.beginSheet("Sheet " + sheetIndex);

            writer.beginRow(TableSection.HEADER);
            writer.addCell("Empty").addCellComment("Comment");
            writer.addCell("String").addCellComment("Comment");
            writer.addCell("Boolean").addCellComment("Comment");
            writer.addCell("Double").addCellComment("Comment");
            writer.addCell("Float").addCellComment("Comment");
            writer.addCell("Long").addCellComment("Comment");
            writer.addCell("Int").addCellComment("Comment");
            writer.addCell("Short").addCellComment("Comment");
            writer.addCell("Byte").addCellComment("Comment");
            writer.addCell("Date").addCellComment("Comment");
            writer.addCell("Local Date").addCellComment("Comment");
            writer.addCell("Local Time").addCellComment("Comment");
            writer.addCell("Local Date Time").addCellComment("Comment");
            writer.addCell("Enum").addCellComment("Comment");
            writer.addCell("Link With Label").addCellComment("Comment");
            writer.addCell("Link").addCellComment("Comment");

            for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
                writer.beginRow(TableSection.DATA);
                writer.addEmptyCell();
                writer.addCell("0001");
                writer.addCell(true);
                writer.addCell(rowIndex + 0.5);
                writer.addCell(rowIndex + 0.5f);
                writer.addCell(Long.MAX_VALUE - rowIndex);
                writer.addCell(Integer.MAX_VALUE - rowIndex);
                writer.addCell((short) (Short.MAX_VALUE - rowIndex));
                writer.addCell((byte) (Byte.MAX_VALUE - rowIndex));
                writer.addCell(new Date());
                writer.addCell(LocalDate.now());
                writer.addCell(LocalTime.now());
                writer.addCell(LocalDateTime.now());
                writer.addCell(WorkbookKind.values()[rowIndex % WorkbookKind.values().length]);
                try {
                    writer.addCell(new URL("https://gitlab.com/cdc-java/cdc-util").toURI(), "cdc-util");
                    writer.addCell(new URL("https://gitlab.com/cdc-java/cdc-util").toURI());
                } catch (final URISyntaxException e) {
                    LOGGER.catching(e);
                }
            }
        }
        writer.flush();
    }

    protected static void testTypesOutputStream(File file,
                                                int sheetsCount,
                                                int rowsCount,
                                                Consumer<WorkbookWriterFactory> factoryConsumer,
                                                WorkbookWriterFeatures features) throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();
        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        factoryConsumer.accept(factory);
        try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                WorkbookWriter<?> writer = factory.create(out, WorkbookKind.from(file), features)) {
            fill(writer, sheetsCount, rowsCount);
        }
        chrono.suspend();
        LOGGER.debug("Generated {} {}", file, chrono);
    }

    protected static void testTypesFile(File file,
                                        int sheetsCount,
                                        int rowsCount,
                                        Consumer<WorkbookWriterFactory> factoryConsumer,
                                        WorkbookWriterFeatures features) throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();
        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        factoryConsumer.accept(factory);
        try (final WorkbookWriter<?> writer = factory.create(file, features)) {
            fill(writer, sheetsCount, rowsCount);
        }
        chrono.suspend();
        LOGGER.debug("Generated {} {}", file, chrono);
    }

    private static String longText(int lines) {
        final StringBuilder sb = new StringBuilder();
        for (int index = 0; index < lines; index++) {
            if (index > 0) {
                sb.append("\n");
            }
            sb.append("Line ")
              .append(index + 1)
              .append(": The quick final brown fox jumps final over the final lazy dog. 01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
        }
        return sb.toString();
    }

    protected static void testLongText(File file,
                                       int sheetsCount,
                                       int rowsCount,
                                       Consumer<WorkbookWriterFactory> factoryConsumer,
                                       WorkbookWriterFeatures features) throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();
        final String text;
        if (features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS)
                || features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES)) {
            text = longText(1000);
        } else {
            text = longText(10);
        }
        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        factoryConsumer.accept(factory);
        try (final WorkbookWriter<?> writer = factory.create(file, features)) {
            for (int sheetIndex = 0; sheetIndex < sheetsCount; sheetIndex++) {
                writer.beginSheet("Sheet " + sheetIndex);

                writer.beginRow(TableSection.HEADER);
                writer.addCell("Column1");
                writer.addCell("Column2");

                for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
                    writer.beginRow(TableSection.DATA);
                    writer.addCell(text);
                    writer.addCell(text);
                }
            }
        }
        chrono.suspend();
        LOGGER.debug("Generated {} {}", file, chrono);
    }

    protected static void testDataValidation(File file) throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();

        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        final WorkbookWriterFeatures features = WorkbookWriterFeatures.STANDARD_BEST;
        try (final WorkbookWriter<?> writer = factory.create(file, features)) {
            writer.beginSheet("Sheet");
            writer.beginRow(TableSection.HEADER);
            writer.addCell("List");
            writer.addCell("Integer");

            writer.addContentValidation(ContentValidation.builder()
                                                         .help("List test", "Select: One, Two or Three")
                                                         .allowsEmptyCell(true)
                                                         .errorReaction(ErrorReaction.STOP)
                                                         .validationType(ValidationType.LIST)
                                                         .values("One", "Two", "Three")
                                                         .addRange(new CellAddressRange(1, -1, 0, 0))
                                                         .build());
        }
        chrono.suspend();
        LOGGER.debug("Generated {} {}", file, chrono);
    }
}