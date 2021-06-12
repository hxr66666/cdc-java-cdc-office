package cdc.office.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.util.function.Evaluation;

class CsvParserTest {
    protected static final Logger LOGGER = LogManager.getLogger(CsvParserTest.class);
    private static final char SEPARATOR = ';';
    private final List<Row> rows = new ArrayList<>();

    private final File filePlatform = new File("target", getClass().getSimpleName() + "-platform.txt");

    CsvParserTest() throws IOException {
        rows.add(Row.builder("V1", "V2").build());
        rows.add(Row.builder("Aaaa", "ààà").build());
        rows.add(Row.builder("Bbbb", "ÖöÏï").build());

        // Generate a Platform file
        try (PrintStream out = new PrintStream(filePlatform)) {
            for (final Row row : rows) {
                boolean first = true;
                for (final String value : row.getValues()) {
                    if (first) {
                        first = false;
                    } else {
                        out.print(SEPARATOR);
                    }
                    out.print(value);
                }
                out.println();
            }
            out.close();
        }

        // final FileEncoder.MainArgs margs = new FileEncoder.MainArgs();
        // margs.setEnabled(FileEncoder.MainArgs.Feature.VERBOSE, true);
        // margs.input = filePlatform;
        // margs.inputCharset = null;
        //
        // // Converts it to UTF-8
        // margs.output = fileUtf8;
        // margs.outputCharset = "UTF-8";
        // FileEncoder.execute(margs);
        //
        // // Converts it to UTF-16
        // margs.output = fileUtf16;
        // margs.outputCharset = "UTF-16";
        // FileEncoder.execute(margs);
    }

    private static final class Handler implements TableHandler {
        public List<Row> rows = new ArrayList<>();
        public int headers = 0;
        public int datas = 0;

        public Handler() {
            super();
        }

        @Override
        public void processBeginTable(String name,
                                      int numberOfRows) {
            // Ignore
            LOGGER.debug("processBeginTable({}, {})", name, numberOfRows);
        }

        @Override
        public Evaluation processHeader(Row header,
                                        RowLocation location) {
            LOGGER.debug("processHeader({}, {})", header, location);
            rows.add(header);
            headers++;
            return Evaluation.CONTINUE;
        }

        @Override
        public Evaluation processData(Row data,
                                      RowLocation location) {
            LOGGER.debug("processData({}, {})", data, location);
            rows.add(data);
            datas++;
            return Evaluation.CONTINUE;
        }

        @Override
        public void processEndTable(String name) {
            // Ignore
            LOGGER.debug("processEndTable({})", (Object) null);
        }
    }

    private void testParseFile(File file,
                               Charset charset) throws Exception {
        LOGGER.debug("testParseFile({}, {})", file, charset);

        final Handler handler = new Handler();
        final CsvParser parser = new CsvParser();
        parser.setSeparator(SEPARATOR);
        parser.parse(file, charset, handler, 1);
        assertEquals(rows, handler.rows);
        assertEquals(1, handler.headers);
        assertEquals(2, handler.datas);
    }

    @Test
    void testParseFile() throws Exception {
        testParseFile(filePlatform, Charset.defaultCharset());
        // testParseFile(fileUtf8, "UTF-8");
        // testParseFile(fileUtf16, "UTF-16");
    }
}