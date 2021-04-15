package cdc.office.csv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;

import cdc.office.tables.TableRowsCounter;

public final class CsvUtils {
    private CsvUtils() {
    }

    /**
     * Return whether a string value needs to be escaped. This is the case when:
     * <ul>
     * <li>the string contains '"' character,</li>
     * <li>the string contains the separator character,</li>
     * <li>the string has multiple lines.</li>
     * </ul>
     *
     * @param text The string to test.
     * @param separator The separator that should be used.
     * @return whether text needs to be escaped or not.
     */
    public static boolean needsEscape(String text,
                                      char separator) {
        return text != null
                && (text.indexOf('"') >= 0
                        || text.indexOf(separator) >= 0
                        || text.contains("\n"));
    }

    /**
     * Escape a string and add external quotes ('"') if asked. Internal
     * processing replaces '"' by 2 '"'.
     *
     * @param text The string to escape.
     * @param addExternalQuotes If true, external quotes are added.
     * @return The escaped string.
     */
    public static String escape(String text,
                                boolean addExternalQuotes) {
        if (text == null) {
            return null;
        } else {
            final StringBuilder buffer = new StringBuilder();
            if (addExternalQuotes) {
                buffer.append('"');
            }
            for (int index = 0; index < text.length(); index++) {
                final char c = text.charAt(index);
                if (c == '"') {
                    buffer.append("\"\"");
                } else {
                    buffer.append(c);
                }
            }
            if (addExternalQuotes) {
                buffer.append('"');
            }

            return buffer.toString();
        }
    }

    /**
     * Escape a string if necessary.
     *
     * @param text The string to escape.
     * @param separator The separator to be used.
     * @return The input string or its escaped version.
     */
    public static String escapeIfNecessary(String text,
                                           char separator) {
        if (needsEscape(text, separator)) {
            return escape(text, true);
        } else {
            return text;
        }
    }

    public static void print(CharSequence[] items,
                             char separator,
                             PrintWriter out) {
        boolean first = true;
        for (final CharSequence cs : items) {
            if (!first) {
                out.print(separator);
            }
            if (cs != null) {
                out.print(escapeIfNecessary(cs.toString(), separator));
            }
            first = false;
        }
    }

    public static void print(CharSequence[] items,
                             char separator,
                             PrintStream out) {
        boolean first = true;
        for (final CharSequence cs : items) {
            if (!first) {
                out.print(separator);
            }
            if (cs != null) {
                out.print(escapeIfNecessary(cs.toString(), separator));
            }
            first = false;
        }
    }

    public static int getNumberOfCsvRows(Reader reader,
                                         char separator) throws IOException {
        final CsvParser parser = new CsvParser(separator).setVoidHandler();
        final TableRowsCounter counter = new TableRowsCounter();
        parser.parse(reader, counter, 0);
        return counter.getNumberOfRows();
    }

    public static int getNumberOfCsvRows(InputStream in,
                                         String charset,
                                         char separator) throws IOException {
        final CsvParser parser = new CsvParser(separator).setVoidHandler();
        final TableRowsCounter counter = new TableRowsCounter();
        parser.parse(in, charset, counter, 0);
        return counter.getNumberOfRows();
    }

    public static int getNumberOfCsvRows(File file,
                                         String charset,
                                         char separator) throws IOException {
        final CsvParser parser = new CsvParser(separator).setVoidHandler();
        final TableRowsCounter counter = new TableRowsCounter();
        parser.parse(file, charset, counter, 0);
        return counter.getNumberOfRows();
    }
}