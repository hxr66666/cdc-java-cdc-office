package cdc.office.ss;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.tables.MemoryTableHandler;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableSection;
import cdc.util.function.Evaluation;
import cdc.util.lang.DateUtils;
import cdc.util.strings.StringUtils;

/**
 * Test generation and parsing of sheets using different formats and options.
 *
 * @author Damien Carbonne
 */
public class SheetTestSupport {
    protected static final Logger LOGGER = LogManager.getLogger(SheetTestSupport.class);
    private static final Calendar CAL = Calendar.getInstance();
    static {
        CAL.set(2020, 00, 01, 14, 25, 33);
    }
    private static final Date DATE = CAL.getTime();
    private static final LocalDateTime LOCAL_DATE_TIME = DateUtils.asLocalDateTime(DATE);
    private static final LocalDate LOCAL_DATE = DateUtils.asLocalDate(DATE);
    private static final LocalTime LOCAL_TIME = DateUtils.asLocalTime(DATE);

    private static final String[] HEADER = {
            "Empty",
            "String",
            "Boolean",
            "Boolean",
            "Byte",
            "Short",
            "Integer",
            // "Long",
            "Double",
            "Float",
            "Date",
            "Local Date Time",
            "Local Date",
            "Local Time",
            "Empty",
            "Empty",
            "Empty"
    };

    private static final Object[] DATA_IN = {
            null,
            "Text\nText",
            Boolean.TRUE,
            Boolean.FALSE,
            Byte.MAX_VALUE,
            Short.MAX_VALUE,
            Integer.MAX_VALUE,
            Double.valueOf(10.5),
            Float.valueOf(10.5f),
            DATE,
            LOCAL_DATE_TIME,
            LOCAL_DATE,
            LOCAL_TIME,
            null, // For CSV test
            null,
            null
    };

    private static final String[] DATA_OUT = {
            null,
            "Text\nText",
            "TRUE",
            "FALSE",
            Byte.toString(Byte.MAX_VALUE),
            Short.toString(Short.MAX_VALUE),
            Integer.toString(Integer.MAX_VALUE),
            String.format("%f", 10.5),
            String.format("%f", 10.5),
            "2020/01/01 14:25:33",
            "2020/01/01 14:25:33",
            "2020/01/01",
            "14:25:33"
    };

    private static final Object[][][] ROWS_INS = {
            { null },
            { null, null },
            { HEADER },
            { null, HEADER },
            { null, HEADER, null },
            { null, HEADER, null, null },
            { null, HEADER, null, null, DATA_IN },
            { HEADER, DATA_IN, null, DATA_IN },
            { null, HEADER, DATA_IN, null, DATA_IN, null }
    };

    private static final String[][][] ROWS_OUTS = {
            {},
            {},
            { HEADER },
            { null, HEADER },
            { null, HEADER },
            { null, HEADER },
            { null, HEADER, null, null, DATA_OUT },
            { HEADER, DATA_OUT, null, DATA_OUT },
            { null, HEADER, DATA_OUT, null, DATA_OUT }
    };

    private static class Handler extends MemoryTableHandler {
        int processBeginCount = 0;
        int processHeaderCount = 0;
        int processDataCount = 0;
        int processEndCount = 0;

        public Handler() {
            super();
        }

        @Override
        public void processBegin(String name,
                                 int numberOfRows) {
            LOGGER.info("processBegin({}, {})", name, numberOfRows);
            processBeginCount++;
            super.processBegin(name, numberOfRows);
        }

        @Override
        public Evaluation processHeader(Row header,
                                        RowLocation location) {
            LOGGER.info("processHeader({}, {})", header, location);
            processHeaderCount++;
            return super.processHeader(header, location);
        }

        @Override
        public Evaluation processData(Row data,
                                      RowLocation location) {
            LOGGER.info("processData({}, {})", data, location);
            processDataCount++;
            return super.processData(data, location);
        }

        @Override
        public void processEnd() {
            LOGGER.info("processEnd()");
            processEndCount++;
            super.processEnd();
        }

    }

    private static void generate(File file,
                                 Object[][] rows,
                                 Consumer<WorkbookWriterFactory> factoryConsumer) throws IOException {
        LOGGER.info("==========================================");
        LOGGER.info("generate {}", file);
        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        factoryConsumer.accept(factory);
        try (final WorkbookWriter<?> writer = factory.create(file, WorkbookWriterFeatures.DEFAULT)) {
            writer.beginSheet("Sheet");
            for (final Object[] row : rows) {
                writer.beginRow(TableSection.DATA);
                if (row == null) {
                    writer.addEmptyCell(); // For CSV tests
                } else {
                    for (final Object value : row) {
                        writer.addCell(value);
                    }
                }
            }
        }
    }

    private static void parse(File file,
                              String[][] expected,
                              Consumer<SheetParserFactory> factoryConsumer) throws Exception {
        LOGGER.info("parse {}", file);
        final SheetParserFactory factory = new SheetParserFactory();
        factoryConsumer.accept(factory);
        final SheetParser parser = factory.create(file);
        LOGGER.info("parser: {}", parser.getClass().getCanonicalName());
        final Handler handler = new Handler();
        parser.parse(file, null, "Sheet", 1, handler);

        assertEquals(1, handler.processBeginCount);
        assertEquals(1, handler.processEndCount);
        assertEquals(expected.length, handler.getRowsCount());

        if (expected.length > 0) {
            assertEquals(1, handler.processHeaderCount);
            assertEquals(1, handler.getHeaderRowsCount());
            assertEquals(expected.length - 1, handler.processDataCount);
            assertEquals(expected.length - 1, handler.getDataRowsCount());
        } else {
            assertEquals(0, handler.processHeaderCount);
            assertEquals(0, handler.getHeaderRowsCount());
            assertEquals(0, handler.processDataCount);
            assertEquals(0, handler.getDataRowsCount());
        }

        for (int index = 0; index < expected.length; index++) {
            final String[] exp = expected[index];
            final Row found = handler.getRow(index);
            if (exp == null) {
                assertSame(0, found.getColumnsCount());
            } else if (exp == HEADER) {
                assertArrayEquals(HEADER, found.getValues().toArray());
            } else {
                assertEquals(DATA_OUT.length, found.getColumnsCount());
                for (int colIndex = 0; colIndex < DATA_IN.length; colIndex++) {
                    if (DATA_IN[colIndex] == null) {
                        assertTrue(StringUtils.isNullOrEmpty(found.getValue(colIndex)));
                    } else {
                        if (DATA_IN[colIndex].getClass().equals(Double.class)) {
                            final double in = ((Double) DATA_IN[colIndex]).doubleValue();
                            final double out = NumberFormat.getInstance().parse(DATA_OUT[colIndex]).doubleValue();
                            assertEquals(out, in);
                        } else if (DATA_IN[colIndex].getClass().equals(Float.class)) {
                            final float in = ((Float) DATA_IN[colIndex]).floatValue();
                            final float out = NumberFormat.getInstance().parse(DATA_OUT[colIndex]).floatValue();
                            assertEquals(out, in);
                        } else {
                            assertEquals(DATA_OUT[colIndex], found.getValue(colIndex));
                        }
                    }
                }
            }
        }
    }

    protected static void check(String suffix,
                                Consumer<WorkbookWriterFactory> wbwFactoryConsumer,
                                Consumer<SheetParserFactory> parserFactoryConsumer) throws Exception {
        for (int index = 0; index < ROWS_INS.length; index++) {
            final File file = new File("target", SheetTestSupport.class.getSimpleName() + "-" + index + suffix);
            generate(file,
                     ROWS_INS[index],
                     wbwFactoryConsumer);
            parse(file,
                  ROWS_OUTS[index],
                  parserFactoryConsumer);
        }
    }
}