package cdc.office.benches;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.csv.CsvUtils;
import cdc.office.csv.CsvWriter;
import cdc.util.files.Files;
import cdc.util.time.Chronometer;

public class CsvUtilsPerfs {
    private static final Logger LOGGER = LogManager.getLogger(CsvUtilsPerfs.class);

    private static final StringWriter BUFFER = new StringWriter();
    private static CsvWriter WRITER = new CsvWriter(BUFFER);

    private static void fill(CsvWriter writer,
                             int lines,
                             int columns) throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();
        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                if (c == 0) {
                    writer.write("A\nB");
                } else {
                    writer.write("Value");
                }
            }
            writer.writeln();
        }
        writer.flush();
        chrono.suspend();
        LOGGER.info("   Generated {}x{} in {}", lines, columns, chrono);
        WRITER.write(chrono.getElapsedSeconds());
    }

    private static void testInMemory(int lines,
                                     int columns) throws IOException {
        LOGGER.info("In Memory {}x{}", lines, columns);
        WRITER.write("Memory");
        WRITER.write(lines);
        WRITER.write(columns);
        final StringWriter sw = new StringWriter();
        try (final CsvWriter writer = new CsvWriter(sw)) {
            fill(writer, lines, columns);
            final Chronometer chrono = new Chronometer();

            chrono.start();
            final String s = sw.toString();
            final Reader reader = new StringReader(s);
            final int rows = CsvUtils.getNumberOfCsvRows(reader, ';');
            chrono.suspend();
            LOGGER.info("   Found {} in {}, length: {}", rows, chrono, s.length());
            WRITER.write(chrono.getElapsedSeconds());
            WRITER.writeln();
        }
    }

    private static void testInFile(int lines,
                                   int columns) throws IOException {
        LOGGER.info("In File {}x{}", lines, columns);
        WRITER.write("File");
        WRITER.write(lines);
        WRITER.write(columns);
        final File file = new File("target/perfs-" + lines + "-" + columns + ".csv");
        try (final CsvWriter writer = new CsvWriter(file)) {
            fill(writer, lines, columns);
            final Chronometer chrono = new Chronometer();

            chrono.start();
            final int rows = CsvUtils.getNumberOfCsvRows(file, null, ';');
            chrono.suspend();
            LOGGER.info("   Found {} in {}, length: {}", rows, chrono, Files.length(file));
            WRITER.write(chrono.getElapsedSeconds());
            WRITER.writeln();
        }
    }

    public static void main(String[] args) throws IOException {
        WRITER.writeln("Mode", "Lines", "Columns", "Generation", "Computation");

        final int[] columns = { 1, 2, 4, 8, 16 };
        for (int p = 0; p < 22; p++) {
            final int lines = (int) Math.pow(2, p);
            for (final int c : columns) {
                testInMemory(lines, c);
                testInFile(lines, c);
            }
        }

        LOGGER.info("\n" + BUFFER.toString());
    }
}