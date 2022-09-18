package cdc.office.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;

import cdc.io.utils.NonCloseableOutputStream;
import cdc.io.utils.NonCloseableWriter;
import cdc.office.tables.Row;
import cdc.util.strings.StringConversion;

/**
 * Utility used to write CSV files.
 * <p>
 * Cell separator can be controlled.<br>
 * End of line can be controlled.<br>
 * Escaping end multi line cells are supported.
 *
 * @author Damien Carbonne
 *
 */
public class CsvWriter implements Flushable, Closeable {
    /** The default separator that should be used. */
    public static final char DEFAULT_SEPARATOR = ';';

    private String lineSeparator = System.getProperty("line.separator");
    /** The writer. */
    private final Writer writer;
    private char separator = DEFAULT_SEPARATOR;

    private enum Status {
        START_LINE,
        IN_LINE
    }

    private Status status = Status.START_LINE;

    /**
     * Creates a CsvWriter from a writer.
     * <p>
     * <b>Note:</b> buffering is used.<br>
     * <b>WARNING:</b> {@code writer} shall be closed by its owner.
     *
     * @param writer The writer.
     */
    public CsvWriter(Writer writer) {
        this.writer = new NonCloseableWriter(writer);
    }

    /**
     * Creates a CsvWriter from an output stream and a charset.
     * <p>
     * <b>Note:</b> buffering is used.<br>
     * <b>WARNING:</b> {@code out} shall be closed by its owner.
     *
     * @param out The output stream.
     * @param charset The charset.
     */
    public CsvWriter(OutputStream out,
                     Charset charset) {
        if (charset == null) {
            this.writer = new BufferedWriter(new OutputStreamWriter(new NonCloseableOutputStream(out)));
        } else {
            this.writer = new BufferedWriter(new OutputStreamWriter(new NonCloseableOutputStream(out), charset));
        }
    }

    /**
     * Creates a CsvWriter from an output stream and a default charset.
     * <p>
     * <b>Note:</b> buffering is used.<br>
     * <b>WARNING:</b> {@code out} shall be closed by its owner.
     *
     * @param out The output stream.
     */
    public CsvWriter(OutputStream out) {
        this(out, null);
    }

    /**
     * Creates a CsvWriter from a PrintStream and a charset.
     * <p>
     * <b>Note:</b> buffering is used.<br>
     * <b>WARNING:</b> {@code s} shall be closed by its owner.
     *
     * @param s The PrintStream.
     * @param charset The charset.
     */
    @Deprecated(since = "2022-02-12", forRemoval = true)
    public CsvWriter(PrintStream s,
                     Charset charset) {
        if (charset == null) {
            this.writer = new BufferedWriter(new OutputStreamWriter(s));
        } else {
            this.writer = new BufferedWriter(new OutputStreamWriter(s, charset));
        }
    }

    /**
     * Creates a CsvWriter from a PrintStream and a default charset.
     * <p>
     * <b>Note:</b> buffering is used.<br>
     * <b>WARNING:</b> {@code s} shall be closed by its owner.
     *
     * @param s The PrintStream.
     */
    @Deprecated(since = "2022-02-12", forRemoval = true)
    public CsvWriter(PrintStream s) {
        this(s, null);
    }

    /**
     * Creates a CSvWriter from a file name and a charset.
     * <p>
     * <b>Note:</b> buffering is used.
     *
     * @param filename The file name.
     * @param charset The charset.
     * @param append If {@code true}, then bytes will be written
     *            to the end of the file rather than the beginning.
     * @throws IOException When an IO error occurs.
     */
    @Deprecated(since = "2022-02-12", forRemoval = true)
    public CsvWriter(String filename,
                     Charset charset,
                     boolean append)
            throws IOException {
        if (charset == null) {
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, append)));
        } else {
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, append), charset));
        }
    }

    /**
     * Creates a CSvWriter from a file name and a charset.
     * <p>
     * <b>Note:</b> buffering is used.
     *
     * @param filename The file name.
     * @param charset The charset.
     * @throws IOException When an IO error occurs.
     */
    @Deprecated(since = "2022-02-12", forRemoval = true)
    public CsvWriter(String filename,
                     Charset charset)
            throws IOException {
        this(filename, charset, false);
    }

    /**
     * Creates a CSvWriter from a file name and a default charset.
     * <p>
     * <b>Note:</b> buffering is used.
     *
     * @param filename The file name.
     * @param append If {@code true}, then bytes will be written
     *            to the end of the file rather than the beginning.
     * @throws IOException When an IO error occurs.
     */
    @Deprecated(since = "2022-02-12", forRemoval = true)
    public CsvWriter(String filename,
                     boolean append)
            throws IOException {
        this(filename, null, append);
    }

    /**
     * Creates a CSvWriter from a file name and a default charset.
     * <p>
     * <b>Note:</b> buffering is used.
     *
     * @param filename The file name.
     * @throws IOException When an IO error occurs.
     */
    @Deprecated(since = "2022-02-12", forRemoval = true)
    public CsvWriter(String filename) throws IOException {
        this(filename, null, false);
    }

    /**
     * Creates a CsvWriter from a file and a charset.
     * <p>
     * Buffering is used.
     *
     * @param file The file.
     * @param charset The charset.
     * @param append If true, then bytes will be written
     *            to the end of the file rather than the beginning.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter(File file,
                     Charset charset,
                     boolean append)
            throws IOException {
        if (charset == null) {
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append)));
        } else {
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset));
        }
    }

    /**
     * Creates a CsvWriter from a file and charset.
     * <p>
     * Buffering is used.
     *
     * @param file The file.
     * @param charset The charset.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter(File file,
                     Charset charset)
            throws IOException {
        this(file, charset, false);
    }

    /**
     * Creates a CsvWriter from a file and a default charset.
     * <p>
     * Buffering is used.
     *
     * @param file The file.
     * @param append If true, then bytes will be written
     *            to the end of the file rather than the beginning.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter(File file,
                     boolean append)
            throws IOException {
        this(file, null, append);
    }

    /**
     * Creates a CsvWriter from a file and a default charset.
     * <p>
     * Buffering is used.
     *
     * @param file The file.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter(File file) throws IOException {
        this(file, null, false);
    }

    /**
     * @return The used cell separator.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Sets the cell separator.
     *
     * @param sep The separator.
     * @return This CsvWriter.
     */
    public CsvWriter setSeparator(char sep) {
        this.separator = sep;
        return this;
    }

    /**
     * @return The used line separator.
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the line separator to use.
     *
     * @param sep The line separator.
     * @return This CsvWriter.
     */
    public CsvWriter setLineSeparator(String sep) {
        this.lineSeparator = sep;
        return this;
    }

    /**
     * Writes the line separator (moves to next line).
     *
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter writeln() throws IOException {
        writer.write(lineSeparator);
        status = Status.START_LINE;
        return this;
    }

    /**
     * Writes a CharSequence cell and controls its escaping.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @param forceEscape If true, cell is escaped. If false, cell is escaped if necessary.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(CharSequence value,
                           boolean forceEscape) throws IOException {
        if (value == null) {
            return write((String) null);
        } else {
            return write(value.toString(), forceEscape);
        }
    }

    /**
     * Writes a CharSequence cell, escaping it if necessary.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(CharSequence value) throws IOException {
        return write(value, false);
    }

    /**
     * Writes a String cell and controls its escaping.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @param forceEscape If true, cell is escaped. If false, cell is escaped if necessary.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(String value,
                           boolean forceEscape) throws IOException {
        if (status == Status.IN_LINE) {
            writer.write(separator);
        } else {
            status = Status.IN_LINE;
        }
        final String tmp = forceEscape ? CsvUtils.escape(value, true) : CsvUtils.escapeIfNecessary(value, separator);
        if (tmp != null) {
            writer.write(tmp);
        }
        return this;
    }

    /**
     * Writes a String cell, escaping it if necessary.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(String value) throws IOException {
        return write(value, false);
    }

    /**
     * Writes a collection of String cells and controls their escaping.
     * <p>
     * If necessary, a cell separator is first appended.
     * A cell separator is inserted between consecutive cells.
     *
     * @param values The cells.
     * @param forceEscape If true, all cells are escaped. If false, each cell is escaped if necessary.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(Collection<String> values,
                           boolean forceEscape) throws IOException {
        for (final String value : values) {
            write(value, forceEscape);
        }
        return this;
    }

    /**
     * Writes a collection of String cells, escaping them if necessary.
     * <p>
     * If necessary, a cell separator is first appended.<br>
     * A cell separator is inserted between consecutive cells.<br>
     * Cells are escaped if necessary.
     *
     * @param values The cells.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(Collection<String> values) throws IOException {
        return write(values, false);
    }

    /**
     * Writes a collection of String cells, controlling their escaping, then moves to next line.
     * <p>
     * If necessary, a cell separator is first appended.
     * A cell separator is inserted between consecutive cells.
     *
     * @param values The cells.
     * @param forceEscape If true, all cells are escaped. If false, each cell is escaped if necessary.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter writeln(Collection<String> values,
                             boolean forceEscape) throws IOException {
        write(values, forceEscape);
        return writeln();
    }

    /**
     * Writes a collection of cells, escaping them if necessary, then moves to next line.
     * <p>
     * If necessary, a cell separator is first appended.<br>
     * A cell separator is inserted between consecutive cells.<br>
     * Cells are escaped if necessary.
     *
     * @param values The cells.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter writeln(Collection<String> values) throws IOException {
        return writeln(values, false);
    }

    public CsvWriter writeln(Row row) throws IOException {
        return writeln(row.getValues());
    }

    /**
     * Writes an array of String cells and controls their escaping.
     * <p>
     * If necessary, a cell separator is first appended.
     * A cell separator is inserted between consecutive cells.
     *
     * @param forceEscape If true, all cells are escaped. If false, each cell is escaped if necessary.
     * @param values The cells.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(boolean forceEscape,
                           String... values) throws IOException {
        for (final String value : values) {
            write(value, forceEscape);
        }
        return this;
    }

    /**
     * Writes an array of String cells, escaping them if necessary.
     * <p>
     * If necessary, a cell separator is first appended.
     * A cell separator is inserted between consecutive cells.
     *
     * @param values The cells.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(String... values) throws IOException {
        return write(false, values);
    }

    /**
     * Writes an array of String cells, controlling their escaping, then moves to next line.
     * <p>
     * If necessary, a cell separator is first appended.
     * A cell separator is inserted between consecutive cells.
     *
     * @param forceEscape If true, all cells are escaped. If false, each cell is escaped if necessary.
     * @param values The cells.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter writeln(boolean forceEscape,
                             String... values) throws IOException {
        write(forceEscape, values);
        return writeln();
    }

    /**
     * Writes an array of String cells, escaping if necessary, then moves to next line.
     * <p>
     * If necessary, a cell separator is first appended.
     * A cell separator is inserted between consecutive cells.
     *
     * @param values The cells.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter writeln(String... values) throws IOException {
        return writeln(false, values);
    }

    /**
     * Writes a boolean cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(boolean value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    /**
     * Writes a long cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(long value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    /**
     * Writes an int cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(int value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    /**
     * Writes a short cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(short value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    /**
     * Writes a byte cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(byte value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    /**
     * Writes a double cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(double value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    /**
     * Writes a float cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public CsvWriter write(float value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    /**
     * Writes an enum cell.
     * <p>
     * If necessary, a cell separator is first appended.
     *
     * @param <E> The enum type.
     * @param value The cell.
     * @return This CsvWriter.
     * @throws IOException When an IO error occurs.
     */
    public <E extends Enum<E>> CsvWriter write(E value) throws IOException {
        return write(StringConversion.asString(value), false);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        status = null;
        writer.close();
    }
}