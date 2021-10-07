package cdc.office.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.office.tables.TableSection;
import cdc.util.cli.AbstractMainSupport;
import cdc.util.cli.FeatureMask;
import cdc.util.cli.OptionEnum;
import cdc.util.function.Evaluation;
import cdc.util.lang.ExceptionWrapper;

public class SheetExtractor {
    protected static final Logger LOGGER = LogManager.getLogger(SheetExtractor.class);
    private final MainArgs margs;

    private SheetExtractor(MainArgs margs) {
        this.margs = margs;
    }

    public static class MainArgs {
        public File inputFile;
        public File outputFile;
        public List<String> sheetNames = new ArrayList<>();
        /** charset. */
        public Charset charset;
        /** separator. */
        public char separator = ';';

        public final FeatureMask<Feature> features = new FeatureMask<>();

        public final void setEnabled(Feature feature,
                                     boolean enabled) {
            features.setEnabled(feature, enabled);
        }

        public final boolean isEnabled(Feature feature) {
            return features.isEnabled(feature);
        }

        public enum Feature implements OptionEnum {
            SEPARATE_SHEETS("separate-sheets", "If enabled, inserts an empty line between consecutive sheets in CSV output."),
            WRITE_SHEET_NAMES("write-sheet-names", "If enabled, inserts sheet names in CSV output.");

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

    private void execute() throws IOException {
        final SheetParserFactory parserFactory = new SheetParserFactory();
        parserFactory.setCharset(margs.charset);
        parserFactory.setSeparator(margs.separator);
        final SheetParser parser = parserFactory.create(margs.inputFile);
        final WorkbookWriterFactory workbookFactory = new WorkbookWriterFactory();
        final WorkbookWriterFeatures features = WorkbookWriterFeatures.builder()
                                                                      .charset(margs.charset)
                                                                      .separator(margs.separator)
                                                                      .setEnabled(WorkbookWriterFeatures.Feature.CSV_SEPARATE_SHEETS,
                                                                                  margs.isEnabled(MainArgs.Feature.SEPARATE_SHEETS))
                                                                      .setEnabled(WorkbookWriterFeatures.Feature.CSV_WRITE_SHEET_NAMES,
                                                                                  margs.isEnabled(MainArgs.Feature.WRITE_SHEET_NAMES))
                                                                      .build();

        try (final WorkbookWriter<?> writer = workbookFactory.create(margs.outputFile, features)) {
            final TableHandler handler = new TableHandler() {
                @Override
                public Evaluation processHeader(Row header,
                                                RowLocation location) {
                    try {
                        writer.addRow(TableSection.HEADER, header);
                    } catch (final IOException e) {
                        throw new ExceptionWrapper(e);
                    }
                    return Evaluation.CONTINUE;
                }

                @Override
                public Evaluation processData(Row data,
                                              RowLocation location) {
                    try {
                        writer.addRow(TableSection.DATA, data);
                    } catch (final IOException e) {
                        throw new ExceptionWrapper(e);
                    }
                    return Evaluation.CONTINUE;
                }
            };
            for (final String sheetName : margs.sheetNames) {
                LOGGER.info("Extract sheet '{}' from '{}'", sheetName, margs.inputFile);
                writer.beginSheet(sheetName);
                parser.parse(margs.inputFile, null, sheetName, 0, handler);
            }
            writer.flush();
            LOGGER.info("Generated '{}'", margs.outputFile);
        }
    }

    public static void execute(MainArgs margs) throws IOException {
        final SheetExtractor instance = new SheetExtractor(margs);
        instance.execute();
    }

    public static void main(String[] args) {
        final MainSupport support = new MainSupport();
        support.main(args);
    }

    private static class MainSupport extends AbstractMainSupport<MainArgs, Void> {
        private static final String SEPARATOR = "separator";
        private static final String SHEET = "sheet";

        protected MainSupport() {
            super(SheetExtractor.class, LOGGER);
        }

        @Override
        protected String getVersion() {
            return Config.VERSION;
        }

        @Override
        protected void addSpecificOptions(Options options) {
            options.addOption(Option.builder()
                                    .longOpt(INPUT)
                                    .desc("Name of the csv, xls, xlsx or ods input file.")
                                    .hasArg()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(OUTPUT)
                                    .desc("Name of the csv, xls, xlsx or ods output file.")
                                    .hasArg()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(SHEET)
                                    .desc("Name(s) of the sheet(s) to extract.")
                                    .hasArgs()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(CHARSET)
                                    .desc("Optional name of the charset for csv files (default: platform default charset).")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(SEPARATOR)
                                    .desc("Optional char separator for csv files (default: ';').")
                                    .hasArg()
                                    .build());

            AbstractMainSupport.addNoArgOptions(options, MainArgs.Feature.class);
        }

        @Override
        protected MainArgs analyze(CommandLine cl) throws ParseException {
            final MainArgs margs = new MainArgs();

            margs.inputFile = getValueAsExistingFile(cl, INPUT, null);
            margs.outputFile = getValueAsFile(cl, OUTPUT, null);
            for (final String s : cl.getOptionValues(SHEET)) {
                margs.sheetNames.add(s);
            }
            margs.charset = getValueAsCharset(cl, CHARSET);
            margs.separator = AbstractMainSupport.getValueAsChar(cl, SEPARATOR, ';');
            AbstractMainSupport.setMask(cl, MainArgs.Feature.class, margs.features::setEnabled);
            return margs;
        }

        @Override
        protected Void execute(MainArgs margs) throws Exception {
            SheetExtractor.execute(margs);
            return null;
        }
    }
}