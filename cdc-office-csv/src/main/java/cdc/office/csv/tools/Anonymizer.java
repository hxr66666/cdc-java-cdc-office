package cdc.office.csv.tools;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.csv.CsvParser;
import cdc.office.csv.CsvWriter;
import cdc.office.csv.tools.AbstractFilter.BaseMainArgs.BaseFeature;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.util.cli.AbstractMainSupport;
import cdc.util.cli.FeatureMask;
import cdc.util.cli.OptionEnum;
import cdc.util.function.Evaluation;
import cdc.util.lang.ExceptionWrapper;
import cdc.util.strings.StringAnonymizer;
import cdc.util.strings.StringConversion;

/**
 * Class used to anonymize selected columns.
 * <p>
 * One can:
 * <ul>
 * <li>limit column length.
 * <li>replace characters by a specified character or jam characters (generated random characters).
 * <li>preserve white spaces.
 * <li>preserve specified characters.
 * </ul>
 *
 * @author Damien Carbonne
 */
public final class Anonymizer extends AbstractFilter<Anonymizer.MainArgs> {
    protected static final Logger LOGGER = LogManager.getLogger(Anonymizer.class);

    private Anonymizer(MainArgs margs) {
        super(margs);
    }

    public static class MainArgs extends AbstractFilter.BaseMainArgs {
        public enum Feature implements OptionEnum {
            PRESERVE_WHITESPACES("preserve-whitespaces", "If set, white spaces are perserved."),
            JAM("jam", "If set, characters are jammed instead of being replaced by the same character.");

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

        /** Columns (1-based) to anonymize. */
        public final Set<Integer> columns = new HashSet<>();
        /** Replacement character. */
        public char replacement = 'X';
        /** Characters to preserve. */
        public String preservedCharacters = null;
        /** Max column size. */
        public int maxLength = -1;

        protected final FeatureMask<Feature> features = new FeatureMask<>();

        public final void setEnabled(Feature feature,
                                     boolean enabled) {
            features.setEnabled(feature, enabled);
        }

        public final boolean isEnabled(Feature feature) {
            return features.isEnabled(feature);
        }
    }

    private final class Handler implements TableHandler {
        final CsvWriter writer;
        final StringAnonymizer anonymizer = new StringAnonymizer();

        public Handler() throws IOException {
            this.writer = new CsvWriter(margs.output, margs.outputCharset);
            this.writer.setSeparator(margs.outputSeparator);
            this.anonymizer.setReplacement(margs.replacement);
            this.anonymizer.setPreservedCharacters(margs.preservedCharacters);
            this.anonymizer.setMaxLength(margs.maxLength);
            this.anonymizer.setEnabled(StringAnonymizer.Feature.PRESERVE_WHITESPACES,
                                       margs.isEnabled(MainArgs.Feature.PRESERVE_WHITESPACES));
            this.anonymizer.setEnabled(StringAnonymizer.Feature.JAM, margs.isEnabled(MainArgs.Feature.JAM));
        }

        @Override
        public void processBegin(String name,
                                 int numberOfRows) {
            // Ignore
        }

        @Override
        public Evaluation processHeader(Row header,
                                        RowLocation location) {
            try {
                writer.writeln(header.getValues());
            } catch (final IOException e) {
                throw ExceptionWrapper.wrap(e);
            }
            return Evaluation.CONTINUE;
        }

        @Override
        public Evaluation processData(Row data,
                                      RowLocation location) {
            try {
                for (int column = 0; column < data.getColumnsCount(); column++) {
                    final String value = data.getValue(column);
                    if (margs.columns.contains(column + 1)) {
                        writer.write(anonymizer.anonymize(value));
                    } else {
                        writer.write(value);
                    }
                }
                writer.writeln();
            } catch (final IOException e) {
                throw ExceptionWrapper.wrap(e);
            }
            return Evaluation.CONTINUE;
        }

        @Override
        public void processEnd() {
            try {
                writer.close();
            } catch (final IOException e) {
                throw ExceptionWrapper.wrap(e);
            }
            if (margs.isEnabled(BaseFeature.VERBOSE)) {
                LOGGER.info("Generated '{}' (charset: {})", margs.output, margs.getOutputCharset());
            }
        }
    }

    protected void execute() throws IOException {
        final Handler handler = new Handler();
        final CsvParser parser = new CsvParser();
        parser.setSeparator(margs.inputSeparator);
        if (margs.isEnabled(BaseFeature.VERBOSE)) {
            LOGGER.info("Load '{}' (charset: {})", margs.input, margs.getInputCharset());
        }
        parser.parse(margs.input,
                     margs.inputCharset,
                     handler,
                     margs.isEnabled(BaseFeature.HAS_HEADER) ? 1 : 0);
    }

    public static void execute(MainArgs margs) throws IOException {
        final Anonymizer instance = new Anonymizer(margs);
        instance.execute();
    }

    public static void main(String[] args) {
        final MainSupport support = new MainSupport();
        support.main(args);
    }

    private static class MainSupport extends AbstractMainSupport<MainArgs, Void> {
        private static final String REPLACEMENT = "replacement";
        private static final String PRESERVE_CHARS = "preserve-chars";
        private static final String MAX_LENGTH = "max-length";

        public MainSupport() {
            super(Anonymizer.class, LOGGER);
        }

        @Override
        protected String getVersion() {
            return cdc.util.Config.VERSION;
        }

        @Override
        protected void addSpecificOptions(Options options) {
            addSpecificBaseOptions(options);
            options.addOption(Option.builder()
                                    .longOpt(COLUMNS)
                                    .desc("Columns (1-based) to anonymize.")
                                    .hasArgs()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(REPLACEMENT)
                                    .desc("Optional replacement character (default: 'X').")
                                    .hasArgs()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(PRESERVE_CHARS)
                                    .desc("Optional characters to preserve (default: none).")
                                    .hasArgs()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(MAX_LENGTH)
                                    .desc("Optional max length of values (default: -1).")
                                    .hasArg()
                                    .build());

            AbstractMainSupport.addNoArgOptions(options, MainArgs.Feature.class);
        }

        @Override
        protected MainArgs analyze(CommandLine cl) throws ParseException {
            final MainArgs margs = new MainArgs();
            AbstractFilter.analyze(cl, margs);
            for (final String s : cl.getOptionValues(COLUMNS)) {
                try {
                    final int number = StringConversion.asInt(s);
                    margs.columns.add(number);
                } catch (final Exception e) {
                    throw new ParseException(e.getMessage());
                }
            }
            margs.replacement = getValueAsChar(cl, REPLACEMENT, 'X');
            margs.preservedCharacters = cl.getOptionValue(PRESERVE_CHARS);
            margs.maxLength = getValueAsInt(cl, MAX_LENGTH, -1);
            AbstractMainSupport.setMask(cl, MainArgs.Feature.class, margs.features::setEnabled);
            return margs;
        }

        @Override
        protected Void execute(MainArgs margs) throws Exception {
            Anonymizer.execute(margs);
            return null;
        }
    }
}