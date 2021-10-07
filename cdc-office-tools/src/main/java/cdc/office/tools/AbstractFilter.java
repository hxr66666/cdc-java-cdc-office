package cdc.office.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cdc.util.cli.AbstractMainSupport;
import cdc.util.cli.FeatureMask;
import cdc.util.cli.OptionEnum;

/**
 * Base class used to create CSV filters.
 *
 * @author Damien Carbonne
 *
 * @param <M> The Main Arguments type.
 */
public abstract class AbstractFilter<M extends AbstractFilter.BaseMainArgs> {
    protected final M margs;
    private static final String INPUT_CHARSET = "input-charset";
    private static final String INPUT_SEPARATOR = "input-separator";
    private static final String OUTPUT_CHARSET = "output-charset";
    private static final String OUTPUT_SEPARATOR = "output-separator";
    protected static final String COLUMNS = "columns";

    protected AbstractFilter(M margs) {
        this.margs = margs;
    }

    public static class BaseMainArgs {
        public enum BaseFeature implements OptionEnum {
            HAS_HEADER("has-header", "If set, file has header line."),
            VERBOSE("verbose", "Be verbose.");

            private final String name;
            private final String description;

            private BaseFeature(String name,
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

        /** Input file. */
        public File input;
        /** Input charset. */
        public Charset inputCharset;
        /** Input separator. */
        public char inputSeparator = ';';
        /** Output file. */
        public File output;
        /** Output charset. */
        public Charset outputCharset;
        /** Output separator. */
        public char outputSeparator = ';';

        protected final FeatureMask<BaseFeature> baseFeatures = new FeatureMask<>();

        public Charset getInputCharset() {
            return inputCharset == null ? Charset.defaultCharset() : inputCharset;
        }

        public Charset getOutputCharset() {
            return outputCharset == null ? getInputCharset() : outputCharset;
        }

        public final void setEnabled(BaseFeature feature,
                                     boolean enabled) {
            baseFeatures.setEnabled(feature, enabled);
        }

        public final boolean isEnabled(BaseFeature feature) {
            return baseFeatures.isEnabled(feature);
        }
    }

    protected Reader getInputReader() throws IOException {
        if (margs.inputCharset == null) {
            return new BufferedReader(new InputStreamReader(new FileInputStream(margs.input)));
        } else {
            return new BufferedReader(new InputStreamReader(new FileInputStream(margs.input), margs.inputCharset));
        }
    }

    protected Writer getOutputWriter() throws IOException {
        if (margs.outputCharset == null) {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(margs.output)));
        } else {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(margs.output), margs.outputCharset));
        }
    }

    protected static void addSpecificBaseOptions(Options options) {
        options.addOption(Option.builder()
                                .longOpt(AbstractMainSupport.INPUT)
                                .desc("Name of the input file.")
                                .hasArg()
                                .required()
                                .build());

        options.addOption(Option.builder()
                                .longOpt(INPUT_CHARSET)
                                .desc("Optional name of the input charset (default: platform default charset).")
                                .hasArg()
                                .build());

        options.addOption(Option.builder()
                                .longOpt(INPUT_SEPARATOR)
                                .desc("Optional input char separator (default: ';').")
                                .hasArg()
                                .build());

        options.addOption(Option.builder()
                                .longOpt(AbstractMainSupport.OUTPUT)
                                .desc("Name of the output file.")
                                .hasArg()
                                .required()
                                .build());

        options.addOption(Option.builder()
                                .longOpt(OUTPUT_CHARSET)
                                .desc("Optional name of the output charset (default: input charset).")
                                .hasArg()
                                .build());
        options.addOption(Option.builder()
                                .longOpt(OUTPUT_SEPARATOR)
                                .desc("Optional output char separator (default: input separator).")
                                .hasArg()
                                .build());
        AbstractMainSupport.addNoArgOptions(options, BaseMainArgs.BaseFeature.class);
    }

    protected static void analyze(CommandLine cl,
                                  BaseMainArgs margs) throws ParseException {
        margs.input = AbstractMainSupport.getValueAsNullOrExistingFile(cl, AbstractMainSupport.INPUT, null);
        margs.inputCharset = AbstractMainSupport.getValueAsCharset(cl, INPUT_CHARSET);
        margs.output = AbstractMainSupport.getValueAsFile(cl, AbstractMainSupport.OUTPUT, null);
        margs.outputCharset = AbstractMainSupport.getValueAsCharset(cl, OUTPUT_CHARSET, margs.inputCharset);
        margs.inputSeparator = AbstractMainSupport.getValueAsChar(cl, INPUT_SEPARATOR, ';');
        margs.outputSeparator = AbstractMainSupport.getValueAsChar(cl, OUTPUT_SEPARATOR, margs.inputSeparator);
        AbstractMainSupport.setMask(cl, BaseMainArgs.BaseFeature.class, margs.baseFeatures::setEnabled);
    }
}