package cdc.office.ss;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.util.lang.FailureReaction;
import cdc.util.lang.Introspection;
import cdc.util.lang.UnexpectedValueException;

public class SheetParserFactory {
    private static final Logger LOGGER = LogManager.getLogger(SheetParserFactory.class);
    private final Set<Feature> features = EnumSet.noneOf(Feature.class);
    /** CSV separator. */
    private char separator = ';';
    /** CSV charset. */
    private Charset charset = null;
    // TODO Locale

    public enum Feature {
        /**
         * If enabled, use standard POI for parsing Excel files.
         * <p>
         * This works for all Excel formats.
         */
        POI_STANDARD,

        /**
         * If enabled, use streaming API for parsing Excel files.
         * <p>
         * This works for xlsx and xlsm formats.
         * It is more efficient than standard parsing.
         */
        POI_STREAMING,

        /**
         * If enabled, use a SAX API for parsing Excel files.
         * <p>
         * This works for xlsx and xlsm formats.
         * <p>
         * <b>WARNING:</b> This is incomplete, experimental. Do not use yet.
         */
        POI_SAX,

        /**
         * If enabled, and a formula is found, evaluate it.
         * Otherwise, cached value is used.
         */
        EVALUATE_FORMULA,

        /**
         * If enabled, vulnerability protections are disabled.
         * <p>
         * This may typically useful with compressed formats (XLSX, ...) which can be used to create ZIP bombs.<br>
         * <b>WARNING:</b> this option should be used with care.
         * <p>
         * <b>WARNING:</b> Apache POI uses global variables to control ZIP bombs.
         * This may lead to unexpected behaviors in case of concurrent accesses.
         */
        DISABLE_VULNERABILITY_PROTECTIONS
    }

    public SheetParserFactory() {
        super();
    }

    public SheetParserFactory setEnabled(Feature feature,
                                         boolean enabled) {
        if (enabled) {
            features.add(feature);
        } else {
            features.remove(feature);
        }
        return this;
    }

    public boolean isEnabled(Feature feature) {
        return features.contains(feature);
    }

    public Feature[] getFeatures() {
        final List<Feature> tmp = new ArrayList<>(features);
        return tmp.toArray(new Feature[tmp.size()]);
    }

    /**
     * @return The CSV separator to use. Default to ';'.<br>
     *         Used for {@link WorkbookKind#CSV}.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Sets the separator to use for CSV files.
     *
     * @param separator The separator character.
     * @return This factory.
     */
    public SheetParserFactory setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    /**
     * @return The CSV charset to use. Default to {@code null}.<br>
     *         Used for {@link WorkbookKind#CSV}.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets the charset to use for CSV files.
     *
     * @param charset The charset.
     * @return This factory.
     */
    public SheetParserFactory setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    private SheetParser create(String className,
                               WorkbookKind kind) {
        final Class<? extends SheetParser> cls = Introspection.getClass(className, SheetParser.class, FailureReaction.FAIL);
        final Class<?>[] parameterTypes = { SheetParserFactory.class, WorkbookKind.class };
        return Introspection.newInstance(cls, parameterTypes, FailureReaction.FAIL, this, kind);
    }

    public SheetParser create(WorkbookKind kind) {
        // TODO
        // features.checkAtMostOne(LOGGER,
        // FailureReaction.WARN,
        // Feature.POI_STANDARD,
        // Feature.POI_STREAMING,
        // Feature.POI_SAX);
        if (isEnabled(Feature.POI_STREAMING) && kind == WorkbookKind.XLS) {
            LOGGER.warn("Streaming is not (yet) available for xls.");
        }
        if (isEnabled(Feature.POI_SAX) && kind == WorkbookKind.XLS) {
            LOGGER.warn("SAX is not available for xls.");
        }

        switch (kind) {
        case CSV:
            return create("cdc.office.ss.csv.CsvSheetParser", WorkbookKind.CSV);
        case ODS:
            return create("cdc.office.ss.odf.OdsSheetParser", WorkbookKind.ODS);
        case XLS:
        case XLSM:
        case XLSX:
            if (isEnabled(Feature.POI_STANDARD) || kind == WorkbookKind.XLS) {
                return create("cdc.office.ss.excel.PoiStandardSheetParser", kind);
            } else if (isEnabled(Feature.POI_SAX)) {
                return create("cdc.office.ss.excel.PoiSaxSheetParser", kind);
            } else {
                // At the moment, when no feature is enabled, use STREAMING
                return create("cdc.office.ss.excel.PoiStreamSheetParser", kind);
            }
        default:
            throw new UnexpectedValueException(kind);
        }
    }

    public SheetParser create(File file) {
        final WorkbookKind kind = WorkbookKind.from(file);
        return create(kind);
    }
}