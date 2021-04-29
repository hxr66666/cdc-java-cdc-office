package cdc.office.csv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.io.txt.LineParser;
import cdc.io.txt.LinesHandler;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.office.tables.TableSection;
import cdc.util.function.Evaluation;
import cdc.util.lang.Checks;
import cdc.util.lang.ImplementationException;
import cdc.util.lang.InvalidStateException;

/**
 * Parsing of csv files. Special handling of text is done. However, one must not
 * use '"' as a field separator. One must indicate whether the file has a header
 * or not (this information is not automatically elaborated).
 *
 * @author Damien Carbonne
 */
public class CsvParser {
    private static final Logger LOGGER = LogManager.getLogger(CsvParser.class);
    private char separator;
    private boolean verbose = false;
    private boolean countRows = false;
    private boolean voidHandler = false;

    /**
     * Creates a parser with ';' separator.
     */
    public CsvParser() {
        separator = ';';
    }

    /**
     * Creates a parser with a specified separator.
     *
     * @param separator the separator to use.
     */
    public CsvParser(char separator) {
        this.separator = separator;
    }

    /**
     * Sets the used separator.
     *
     * @param separator the separator to use.
     * @return This parser.
     */
    public CsvParser setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    public CsvParser countRows(boolean countRows) {
        this.countRows = countRows;
        return this;
    }

    CsvParser setVoidHandler() {
        this.voidHandler = true;
        return this;
    }

    /**
     * Sets the verbosity level of the parser.
     *
     * @param verbose indicates whether the parsing must be verbose or not.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private void traceBegin(Object object) {
        if (verbose) {
            LOGGER.info("Load: '{}' ... ", object);
        }
    }

    private void traceEnd() {
        if (verbose) {
            LOGGER.info("Done");
        }
    }

    private LinesHandler createLinesHandler(TableHandler handler,
                                            int headers,
                                            int numberOfRows) {
        if (voidHandler) {
            return new VoidHandler(handler, headers, numberOfRows);
        } else {
            return new Handler(handler, headers, numberOfRows);
        }
    }

    /**
     * Parses a Reader.
     * <p>
     * If {@code reader} supports mark, the number of lines is computed.
     *
     * @param reader The reader to parse.
     * @param handler The table handler to use.
     * @param headers The number of header lines.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(Reader reader,
                      TableHandler handler,
                      int headers) throws IOException {
        final int numberOfRows;
        if (countRows) {
            if (reader.markSupported()) {
                reader.mark(Integer.MAX_VALUE);
                numberOfRows = CsvUtils.getNumberOfCsvRows(reader, separator);
                reader.reset();
            } else {
                LOGGER.warn("Cannot count rows, reader does not support marks.");
                numberOfRows = -1;
            }
        } else {
            numberOfRows = -1;
        }
        LineParser.parse(reader, createLinesHandler(handler, headers, numberOfRows));
    }

    /**
     * Parses an InputStream.
     *
     * @param in The input stream.
     * @param charset The charset to use.
     * @param handler The table handler to use.
     * @param headers The number of header lines.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(InputStream in,
                      String charset,
                      TableHandler handler,
                      int headers) throws IOException {
        final int numberOfRows;
        if (countRows) {
            if (in.markSupported()) {
                in.mark(Integer.MAX_VALUE);
                numberOfRows = CsvUtils.getNumberOfCsvRows(in, charset, separator);
                in.reset();
            } else {
                LOGGER.warn("Cannot count rows, stream does not support marks.");
                numberOfRows = -1;
            }
        } else {
            numberOfRows = -1;
        }
        LineParser.parse(in, charset, createLinesHandler(handler, headers, numberOfRows));
    }

    /**
     * Parses an InputStream using default charset.
     *
     * @param in The input stream.
     * @param handler The table handler to use.
     * @param headers The number of header lines.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(InputStream in,
                      TableHandler handler,
                      int headers) throws IOException {
        parse(in, Charset.defaultCharset().name(), handler, headers);
    }

    /**
     * Parses a file.
     *
     * @param file The input file.
     * @param charset The charset to use.
     * @param handler The table handler to use.
     * @param headers The number of header lines.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(File file,
                      String charset,
                      TableHandler handler,
                      int headers) throws IOException {
        traceBegin(file);
        final int numberORows;
        if (countRows) {
            numberORows = CsvUtils.getNumberOfCsvRows(file, charset, separator);
        } else {
            numberORows = -1;
        }
        LineParser.parse(file, charset, createLinesHandler(handler, headers, numberORows));
        traceEnd();
    }

    /**
     * Parses a file using default charset.
     *
     * @param file The input file.
     * @param handler The table handler to use.
     * @param headers The number of header lines.
     * @throws IOException When an IO exception occurs.
     */
    public void parse(File file,
                      TableHandler handler,
                      int headers) throws IOException {
        parse(file, null, handler, headers);
    }

    private enum Status {
        /** Currently no line is processed */
        LINE_OUT,
        /**
         * Currently, a standard field (does not start with '"') is processed.
         */
        FIELD_IN_STD,
        /** Currently, a special field (starting with '"') is processed. */
        FIELD_IN_SPEC,
        /** A quote ('"') is processed while in special field. */
        FIELD_IN_QUOTE,
        /** A field has just been left. */
        FIELD_OUT
    }

    /**
     * Parsing of input lines, one after the other.
     *
     * @author D. Carbonne
     *
     */
    private class Handler implements LinesHandler {
        private final TableHandler tableHandler;
        private final int headers;
        private final int numberOfRows;
        private Status currentStatus = Status.LINE_OUT;
        private final StringBuilder currentValue = new StringBuilder();
        private final Row.Builder row = Row.builder();
        private final RowLocation.Builder location = RowLocation.builder();
        private Evaluation evaluation = Evaluation.CONTINUE;

        Handler(TableHandler handler,
                int headers,
                int numberOfRows) {
            Checks.isNotNull(handler, "handler");
            Checks.isTrue(headers >= 0, "invalid headers");
            this.tableHandler = handler;
            this.headers = headers;
            this.numberOfRows = numberOfRows;
        }

        private void implementationError() {
            throw new ImplementationException("unhandled status: " + currentStatus);
        }

        @Override
        public void processBegin() {
            row.clear();
            tableHandler.processBegin(null, numberOfRows);
        }

        @Override
        public void processEnd() {
            switch (currentStatus) {
            case FIELD_IN_QUOTE:
            case FIELD_IN_SPEC:
            case FIELD_IN_STD:
                throw new InvalidStateException("Invalid status (" + currentStatus + ") at the end of parsing.");

            case FIELD_OUT:
            case LINE_OUT:
                break;

            default:
                implementationError();
                break;
            }
            tableHandler.processEnd();
        }

        @Override
        public Evaluation processLine(String line,
                                      int number) {
            for (int i = 0; i < line.length(); i++) {
                // current character
                final char c = line.charAt(i);
                if (c == '"') {
                    switch (currentStatus) {
                    case LINE_OUT:
                    case FIELD_OUT:
                        // skip c
                        setStatus(Status.FIELD_IN_SPEC);
                        break;

                    case FIELD_IN_STD:
                        // This should be an error?
                        appendChar(c);
                        break;

                    case FIELD_IN_SPEC:
                        setStatus(Status.FIELD_IN_QUOTE);
                        break;

                    case FIELD_IN_QUOTE:
                        appendChar('"');
                        setStatus(Status.FIELD_IN_SPEC);
                        break;

                    default:
                        implementationError();
                        break;
                    }
                } else if (c == separator) {
                    switch (currentStatus) {
                    case LINE_OUT:
                    case FIELD_IN_STD:
                    case FIELD_OUT:
                    case FIELD_IN_QUOTE:
                        addValue();
                        break;

                    case FIELD_IN_SPEC:
                        appendChar(c);
                        break;

                    default:
                        implementationError();
                        break;
                    }

                } else {
                    switch (currentStatus) {
                    case LINE_OUT:
                    case FIELD_OUT:
                        appendChar(c);
                        setStatus(Status.FIELD_IN_STD);
                        break;

                    case FIELD_IN_STD:
                    case FIELD_IN_SPEC:
                        appendChar(c);
                        // Don't change status
                        break;

                    case FIELD_IN_QUOTE:
                        // Error
                        appendChar(c);
                        setStatus(Status.FIELD_IN_SPEC);
                        break;

                    default:
                        implementationError();
                        break;
                    }
                }
            }

            // End of line processing
            switch (currentStatus) {

            case FIELD_IN_STD:
            case FIELD_IN_QUOTE:
            case FIELD_OUT:
                addValue(); // Possibly add an empty one
                flushLineIfAny();
                break;

            case FIELD_IN_SPEC:
                appendNewLine();
                break;

            case LINE_OUT:
                flushLineIfAny();
                break;

            default:
                assert (false);
                break;
            }
            return evaluation;
        }

        private void setStatus(Status status) {
            currentStatus = status;
        }

        private void appendChar(char c) {
            currentValue.append(c);
        }

        private void appendNewLine() {
            currentValue.append('\n');
        }

        private void addValue() {
            row.addValue(currentValue.toString());
            currentValue.setLength(0);
            setStatus(Status.FIELD_OUT);
        }

        private void flushLineIfAny() {
            location.incrementNumbers(headers);
            if (location.getSection() == TableSection.HEADER) {
                evaluation = tableHandler.processHeader(row.build(),
                                                        location.build());
            } else {
                evaluation = tableHandler.processData(row.build(),
                                                      location.build());
            }

            row.clear();
            currentValue.setLength(0);
            setStatus(Status.LINE_OUT);
        }
    }

    private class VoidHandler implements LinesHandler {
        private final TableHandler tableHandler;
        private final int headers;
        private final int numberOfRows;
        private Status currentStatus = Status.LINE_OUT;
        private final RowLocation.Builder location = RowLocation.builder();
        private Evaluation evaluation = Evaluation.CONTINUE;

        VoidHandler(TableHandler handler,
                    int headers,
                    int numberOfRows) {
            Checks.isNotNull(handler, "handler");
            Checks.isTrue(headers >= 0, "invalid headers");
            this.tableHandler = handler;
            this.headers = headers;
            this.numberOfRows = numberOfRows;
        }

        private void implementationError() {
            throw new ImplementationException("unhandled status: " + currentStatus);
        }

        @Override
        public void processBegin() {
            tableHandler.processBegin(null, numberOfRows);
        }

        @Override
        public void processEnd() {
            switch (currentStatus) {
            case FIELD_IN_QUOTE:
            case FIELD_IN_SPEC:
            case FIELD_IN_STD:
                throw new InvalidStateException("Invalid status (" + currentStatus + ") at the end of parsing.");

            case FIELD_OUT:
            case LINE_OUT:
                break;

            default:
                implementationError();
                break;
            }
            tableHandler.processEnd();
        }

        @Override
        public Evaluation processLine(String line,
                                      int number) {
            for (int i = 0; i < line.length(); i++) {
                // current character
                final char c = line.charAt(i);
                if (c == '"') {
                    switch (currentStatus) {
                    case LINE_OUT:
                    case FIELD_OUT:
                        // skip c
                        setStatus(Status.FIELD_IN_SPEC);
                        break;

                    case FIELD_IN_STD:
                        // This should be an error?
                        break;

                    case FIELD_IN_SPEC:
                        setStatus(Status.FIELD_IN_QUOTE);
                        break;

                    case FIELD_IN_QUOTE:
                        setStatus(Status.FIELD_IN_SPEC);
                        break;

                    default:
                        implementationError();
                        break;
                    }
                } else if (c == separator) {
                    switch (currentStatus) {
                    case LINE_OUT:
                    case FIELD_IN_STD:
                    case FIELD_OUT:
                    case FIELD_IN_QUOTE:
                        setStatus(Status.FIELD_OUT);
                        break;

                    case FIELD_IN_SPEC:
                        break;

                    default:
                        implementationError();
                        break;
                    }

                } else {
                    switch (currentStatus) {
                    case LINE_OUT:
                    case FIELD_OUT:
                        setStatus(Status.FIELD_IN_STD);
                        break;

                    case FIELD_IN_STD:
                    case FIELD_IN_SPEC:
                        // Don't change status
                        break;

                    case FIELD_IN_QUOTE:
                        // Error
                        setStatus(Status.FIELD_IN_SPEC);
                        break;

                    default:
                        implementationError();
                        break;
                    }
                }
            }

            // End of line processing
            switch (currentStatus) {

            case FIELD_IN_STD:
            case FIELD_IN_QUOTE:
            case FIELD_OUT:
                setStatus(Status.FIELD_OUT);
                flushLineIfAny();
                break;

            case FIELD_IN_SPEC:
                break;

            case LINE_OUT:
                flushLineIfAny();
                break;

            default:
                assert (false);
                break;
            }
            return evaluation;
        }

        private void setStatus(Status status) {
            currentStatus = status;
        }

        private void flushLineIfAny() {
            location.incrementNumbers(headers);
            if (location.getSection() == TableSection.HEADER) {
                evaluation = tableHandler.processHeader(null, null);
            } else {
                evaluation = tableHandler.processData(null, null);
            }
            setStatus(Status.LINE_OUT);
        }
    }
}