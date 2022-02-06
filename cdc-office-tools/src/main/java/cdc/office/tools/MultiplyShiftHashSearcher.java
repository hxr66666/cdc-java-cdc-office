package cdc.office.tools;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.tables.TableSection;
import cdc.util.cli.AbstractMainSupport;
import cdc.util.cli.FeatureMask;
import cdc.util.cli.OptionEnum;
import cdc.util.time.Chronometer;

public final class MultiplyShiftHashSearcher {
    protected static final Logger LOGGER = LogManager.getLogger(MultiplyShiftHashSearcher.class);
    private final MainArgs margs;

    private final BitSet set = new BitSet();

    /** Number of solutions. */
    private long count = 0L;

    /** Number of solutions indexed with the maximum hash code. */
    private final long[] counts;

    /** Maximum possible number of entries (values different from 0) in counts. */
    private final int countsMaxEntries;

    /** Number of entries in counts. */
    private int countsEntries = 0;

    /** The set of characters for which a perfect hash function is searched. */
    private final char[] chars;

    /** Length of chars. */
    private final int length;

    /** Maximum acceptable hash code. */
    private final int maxAcceptableHashCode;

    /** Filler character. */
    private final char filler;

    private final boolean verbose;
    private final boolean showAll;
    private final boolean stopOnBest;

    private final int bestSize;

    private boolean bestSizeFound = false;

    private final WorkbookWriter<?> writer;

    public static class MainArgs {
        public String chars;
        public Character filler = null;
        public File output;
        public int minMultiplier = 1;
        public int maxMultiplier = Integer.MAX_VALUE;
        public double maxRatio = 4.0;

        protected final FeatureMask<Feature> features = new FeatureMask<>();

        public boolean isEnabled(Feature feature) {
            return features.isEnabled(feature);
        }

        public void setEnabled(Feature feature,
                               boolean enabled) {
            features.setEnabled(feature, enabled);
        }

        public void validate() throws ParseException {
            if (minMultiplier <= 0) {
                throw new ParseException("min multiplier too small.");
            }
            if (maxMultiplier <= 0) {
                throw new ParseException("max multiplier too small.");
            }
            if (maxRatio < 1.0) {
                throw new ParseException("max ratio too small.");
            }
        }

        /**
         * Enumeration of possible boolean options.
         */
        public enum Feature implements OptionEnum {
            VERBOSE("verbose", "Print messages."),
            SHOW_ALL("show-all",
                     "Show all matching (multiplier, shift) pairs. If disabled, show only one solution for each max hash code."),
            STOP_ON_BEST("stop-on-best",
                         "Stop searching when a solution whose size is the smallest power of 2 larger than the number of characters to encode has been found.");

            private final String name;
            private final String description;

            private Feature(String name,
                            String description) {
                this.name = name;
                this.description = description;
            }

            @Override
            public final String getName() {
                return name;
            }

            @Override
            public final String getDescription() {
                return description;
            }
        }
    }

    private MultiplyShiftHashSearcher(MainArgs margs) throws IOException {
        this.margs = margs;

        LOGGER.debug("original: {}", margs.chars);

        final Set<Character> tmp = new HashSet<>();
        final StringBuilder builder = new StringBuilder();
        final String decoded = decode(margs.chars);
        for (int index = 0; index < decoded.length(); index++) {
            final char c = decoded.charAt(index);
            if (!tmp.contains(c)) {
                tmp.add(c);
                builder.append(c);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("decoded: {}", decoded);
            LOGGER.debug("protected decoded: {}", encode(decoded));
        }

        this.chars = builder.toString().toCharArray();
        this.length = chars.length;

        this.bestSize = 1 << (32 - Integer.numberOfLeadingZeros(this.length - 1));

        this.maxAcceptableHashCode = (int) (this.length * margs.maxRatio) - 1;
        this.filler = margs.filler == null ? chars[0] : margs.filler;
        this.counts = new long[this.maxAcceptableHashCode + 1];
        this.countsMaxEntries = this.maxAcceptableHashCode - this.length + 2;
        this.verbose = margs.isEnabled(MainArgs.Feature.VERBOSE);
        this.showAll = margs.isEnabled(MainArgs.Feature.SHOW_ALL);
        this.stopOnBest = margs.isEnabled(MainArgs.Feature.STOP_ON_BEST);

        if (margs.output == null) {
            this.writer = null;
        } else {
            final WorkbookWriterFactory factory = new WorkbookWriterFactory();
            this.writer = factory.create(margs.output, WorkbookWriterFeatures.STANDARD_BEST);

            this.writer.beginSheet("Parameters");
            this.writer.addRow(TableSection.HEADER, "Parameter", "Value");
            this.writer.addRow(TableSection.DATA, "Chars", encode(decoded));
            this.writer.addRow(TableSection.DATA, "Chars Length", length);
            this.writer.addRow(TableSection.DATA, "Best Size", bestSize);
            this.writer.addRow(TableSection.DATA, "Max Ratio", margs.maxRatio);
            this.writer.addRow(TableSection.DATA, "Max Hash Code", maxAcceptableHashCode);
            this.writer.addRow(TableSection.DATA, "Filler", filler);
            for (final MainArgs.Feature feature : MainArgs.Feature.values()) {
                this.writer.addRow(TableSection.DATA, feature, margs.isEnabled(feature));
            }

            this.writer.beginSheet("Solutions");
            this.writer.addRow(TableSection.HEADER,
                               "Multiplier",
                               "Shift",
                               "Min Hash Code",
                               "Max Hash Code",
                               "Hash Table Size",
                               "Hash Table");
        }

    }

    private void log(String message) {
        if (writer == null || verbose) {
            LOGGER.info(message);
        }
    }

    private static String encode(char c) {
        return String.format("\\u%04x", (int) c);
    }

    private static String encode(String s) {
        final StringBuilder builder = new StringBuilder();
        for (int index = 0; index < s.length(); index++) {
            final char c = s.charAt(index);
            if (c == '\r') {
                builder.append("\\r");
            } else if (c == '\n') {
                builder.append("\\n");
            } else if (c == '\t') {
                builder.append("\\t");
            } else if (c == '\f') {
                builder.append("\\f");
            } else if (c == '\b') {
                builder.append("\\b");
            } else if (c == '\\') {
                builder.append("\\\\");
            } else if (c == '"') {
                builder.append("\\\"");
            } else if (Character.isWhitespace(c) || Character.isISOControl(c)
                    || c == '\u202F' || c == '\u00A0' || c == '\u303F' || c == '\uFEFF'
                    || c == '\u180E' || ('\u2000' <= c && c <= '\u200B')) {
                builder.append(encode(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String decode(String s) {
        final StringBuilder builder = new StringBuilder();
        int index = 0;
        while (index < s.length()) {
            final char c = s.charAt(index);
            if (c == '\\') {
                final char next = s.charAt(index + 1);
                if (next == '\\') {
                    builder.append('\\');
                    index++;
                } else if (next == 'n') {
                    builder.append('\n');
                    index++;
                } else if (next == 't') {
                    builder.append('\t');
                    index++;
                } else if (next == 'r') {
                    builder.append('\r');
                    index++;
                } else if (next == 'f') {
                    builder.append('\f');
                    index++;
                } else if (next == 'b') {
                    builder.append('\b');
                    index++;
                } else if (next == '"') {
                    builder.append('"');
                    index++;
                } else if (next == 'u') {
                    // expect XXXX
                    final String code = s.substring(index + 2, index + 6);
                    index += 5;
                    builder.append((char) Integer.parseInt(code, 16));
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                builder.append(c);
            }
            index++;
        }
        return builder.toString();
    }

    /**
     * Tests a multiplier and shift and returns {@code true} if they are usable.
     *
     * @param multiplier The multiplier.
     * @param shift The shift.
     * @return {@code true} if {@code multiplier} and {@code shift} are usable.
     * @throws IOException When an IO error occurs.
     */
    private boolean test(int multiplier,
                         int shift) throws IOException {
        set.clear();
        for (final char c : chars) {
            final int hash = (c * multiplier) >>> shift;
            if (hash < 0 || hash > maxAcceptableHashCode) {
                return false;
            }
            set.set(hash);
        }

        if (set.cardinality() == length) {
            // All characters have been hashed with a valid hash code
            count++;

            // Computes the maximum hash code
            final int minHashCode = set.nextSetBit(0);
            final int maxHashCode = set.length() - 1;

            if (maxHashCode < bestSize) {
                bestSizeFound = true;
            }

            final boolean isFirst = counts[maxHashCode] == 0;
            if (isFirst) {
                countsEntries++;
            }
            counts[maxHashCode]++;

            if (isFirst || showAll) {
                // Computes the hash table (must be a power of 2)
                final int size = 1 << (32 - Integer.numberOfLeadingZeros(maxHashCode));

                final char[] buffer = new char[size];
                for (int index = 0; index < size; index++) {
                    buffer[index] = filler;
                }
                for (final char c : chars) {
                    final int hash = (c * multiplier) >>> shift;
                    buffer[hash] = c;
                }
                final String hashTable = new String(buffer);

                if (writer == null || isFirst) {
                    log(String.format("multiplier: %9d shift: %2d min: %3d max: %3d size: %3d table: %s",
                                      multiplier,
                                      shift,
                                      minHashCode,
                                      maxHashCode,
                                      size,
                                      encode(hashTable)));
                }
                if (writer != null) {
                    writer.beginRow(TableSection.DATA);
                    writer.addCell(multiplier);
                    writer.addCell(shift);
                    writer.addCell(minHashCode);
                    writer.addCell(maxHashCode);
                    writer.addCell(size);
                    writer.addCell(encode(hashTable));
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static void test(String chars,
                            Character filler,
                            int multiplier,
                            int shift) {
        final MainArgs margs = new MainArgs();
        margs.chars = chars;
        margs.filler = filler;
        margs.output = null;

        try {
            final MultiplyShiftHashSearcher instance = new MultiplyShiftHashSearcher(margs);
            final boolean found = instance.test(multiplier, shift);
            if (!found) {
                LOGGER.info("Failed with multiplier: {} shift: {}", multiplier, shift);
            }
        } catch (final IOException e) {
            LOGGER.catching(e);
        }
    }

    private void execute() throws IOException {
        final Chronometer chrono = new Chronometer();
        chrono.start();
        final int maxShift = Integer.numberOfLeadingZeros(length - 1);
        boolean finished = false;
        for (int multiplier = margs.minMultiplier; !finished && multiplier > 0 && multiplier <= margs.maxMultiplier; multiplier++) {
            if (multiplier % 1000000 == 0 || multiplier == margs.minMultiplier || multiplier == margs.maxMultiplier) {
                log(String.format("%d %d %d/%d",
                                  multiplier,
                                  count,
                                  countsEntries,
                                  countsMaxEntries));
                if (writer != null) {
                    writer.flush();
                }
            }
            for (int shift = 0; !finished && shift <= maxShift; shift++) {
                test(multiplier, shift);
                finished = !showAll && (countsEntries == countsMaxEntries || (bestSizeFound && stopOnBest));
            }
        }

        chrono.suspend();

        log("Finished in " + chrono);

        if (bestSizeFound) {
            log("Found solution(s) for best size (" + bestSize + ")");
        }

        // Show the number of solutions found for each maximum hash code
        // This smallest possible maximum hash code is length - 1
        log("Number of solutions for max hash code between " + (length - 1) + " and " + maxAcceptableHashCode);
        for (int index = length - 1; index < counts.length; index++) {
            log(String.format("   %3d: %19d", index, counts[index]));
        }

        if (writer != null) {
            writer.close();
        }
    }

    public static void execute(MainArgs margs) throws IOException {
        final MultiplyShiftHashSearcher instance = new MultiplyShiftHashSearcher(margs);
        instance.execute();
    }

    public static void main(String[] args) {
        final MainSupport support = new MainSupport();
        support.main(args);
    }

    private static class MainSupport extends AbstractMainSupport<MainArgs, Void> {
        private static final String CHARS = "chars";
        private static final String FILLER = "filler";
        private static final String MIN_MULTIPLIER = "min-multiplier";
        private static final String MAX_MULTIPLIER = "max-multiplier";
        private static final String MAX_RATIO = "max-ratio";

        public MainSupport() {
            super(MultiplyShiftHashSearcher.class, LOGGER);
        }

        @Override
        protected String getVersion() {
            return Config.VERSION;
        }

        @Override
        protected boolean addArgsFileOption(Options options) {
            return true;
        }

        @Override
        protected void addSpecificOptions(Options options) {
            options.addOption(Option.builder()
                                    .longOpt(OUTPUT)
                                    .desc("Optional name of the output spreadsheet file (must end with a CSV, XLS, XLSX compliant extension).\n"
                                            + "Warning: only CSV format supports unlimited number of rows.")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(CHARS)
                                    .desc("Characters to hash. Escaping characters is possible: \\r \\n \\t \\b \\f \\\\ and \\uXXXX.")
                                    .hasArg()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(FILLER)
                                    .desc("Optional filler character.")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(MIN_MULTIPLIER)
                                    .desc("Optional min multiplier (default to 1).")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(MAX_MULTIPLIER)
                                    .desc("Optional max multiplier (default to " + Integer.MAX_VALUE + ").")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(MAX_RATIO)
                                    .desc("Optional max ratio (default to 4.0).")
                                    .hasArg()
                                    .build());

            addNoArgOptions(options, MainArgs.Feature.class);
        }

        @Override
        protected MainArgs analyze(CommandLine cl) throws ParseException {
            final MainArgs margs = new MainArgs();
            margs.output = getValueAsFile(cl, OUTPUT, null);
            margs.chars = getValueAsString(cl, CHARS, "");
            margs.filler = getValueAsChar(cl, FILLER, null);
            margs.minMultiplier = Math.max(getValueAsInt(cl, MIN_MULTIPLIER, 1), 1);
            margs.maxMultiplier = getValueAsInt(cl, MAX_MULTIPLIER, Integer.MAX_VALUE);
            margs.maxRatio = getValueAsDouble(cl, MAX_RATIO, 4.0);
            setMask(cl, MainArgs.Feature.class, margs.features::setEnabled);
            margs.validate();
            return margs;
        }

        @Override
        protected Void execute(MainArgs margs) throws IOException {
            MultiplyShiftHashSearcher.execute(margs);
            return null;
        }
    }
}