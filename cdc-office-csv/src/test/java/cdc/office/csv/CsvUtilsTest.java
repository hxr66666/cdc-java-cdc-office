package cdc.office.csv;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CsvUtilsTest {
    @Test
    void test() throws IOException {
        assertSame(4,
                   CsvUtils.getNumberOfCsvRows(new File("src/test/resources/data01.csv"),
                                               StandardCharsets.UTF_8.name(),
                                               ';'));
    }

    static interface CsvWriterConsumer {
        public void accept(CsvWriter writer) throws IOException;
    }

    private static void checkGetNumberOfRows(int expected,
                                             CsvWriterConsumer consumer) throws IOException {
        final StringWriter sw = new StringWriter();
        final CsvWriter writer = new CsvWriter(sw);
        consumer.accept(writer);
        writer.flush();
        final Reader reader = new StringReader(sw.toString());
        assertSame(expected, CsvUtils.getNumberOfCsvRows(reader, ';'));
    }

    @Test
    void testEmpty() throws IOException {
        checkGetNumberOfRows(0, w -> {
            // Ignore
        });
    }

    @Test
    void testSimple() throws IOException {
        checkGetNumberOfRows(1, w -> {
            w.write("C1", "C2");
        });
        checkGetNumberOfRows(1, w -> {
            w.write("C1", "C2");
            w.writeln();
        });
        checkGetNumberOfRows(1, w -> {
            w.write("C1", "C2");
            w.writeln();
            w.write("");
        });

        checkGetNumberOfRows(2, w -> {
            w.write("C1", "C2");
            w.writeln();
            w.write("", "");
        });
        checkGetNumberOfRows(2, w -> {
            w.write("C1", "C2");
            w.writeln();
            w.writeln();
        });
    }

    @Test
    void testEscape() throws IOException {
        checkGetNumberOfRows(1, w -> {
            w.write("C1", "\"C2");
        });
        checkGetNumberOfRows(1, w -> {
            w.write("C1", "\"C2\n");
        });
        checkGetNumberOfRows(1, w -> {
            w.write("C1", "\"C2\nC2");
        });

        checkGetNumberOfRows(1, w -> {
            w.write("C1", "C2\nC2");
        });
    }
}