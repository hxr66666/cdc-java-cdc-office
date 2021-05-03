package cdc.office.ss.tools;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.csv.CsvParser;
import cdc.office.csv.CsvWriter;
import cdc.office.ss.tools.AbstractFilter.BaseMainArgs.BaseFeature;
import cdc.office.tables.Row;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableHandler;
import cdc.util.cli.AbstractMainSupport;
import cdc.util.function.Evaluation;
import cdc.util.lang.ExceptionWrapper;

public final class SeparatorConverter extends AbstractFilter<SeparatorConverter.MainArgs> {
    protected static final Logger LOGGER = LogManager.getLogger(SeparatorConverter.class);

    private SeparatorConverter(MainArgs margs) {
        super(margs);
    }

    public static class MainArgs extends AbstractFilter.BaseMainArgs {
        //
    }

    private final class Handler implements TableHandler {
        final CsvWriter writer;

        public Handler() throws IOException {
            this.writer = new CsvWriter(margs.output, margs.outputCharset);
            this.writer.setSeparator(margs.outputSeparator);
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
                writer.writeln(data.getValues());
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
        final SeparatorConverter instance = new SeparatorConverter(margs);
        instance.execute();
    }

    public static void main(String[] args) {
        final MainSupport support = new MainSupport();
        support.main(args);
    }

    private static class MainSupport extends AbstractMainSupport<MainArgs, Void> {

        public MainSupport() {
            super(SeparatorConverter.class, LOGGER);
        }

        @Override
        protected String getVersion() {
            return Config.VERSION;
        }

        @Override
        protected void addSpecificOptions(Options options) {
            addSpecificBaseOptions(options);
        }

        @Override
        protected MainArgs analyze(CommandLine cl) throws ParseException {
            final MainArgs margs = new MainArgs();
            AbstractFilter.analyze(cl, margs);
            return margs;
        }

        @Override
        protected Void execute(MainArgs margs) throws Exception {
            SeparatorConverter.execute(margs);
            return null;
        }
    }
}