package cdc.office.ss;

import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;

import cdc.util.lang.Checks;

public class WorkbookWriterFeatures {
    public static final WorkbookWriterFeatures DEFAULT = builder().build();
    public static final WorkbookWriterFeatures STANDARD_BEST = builder().enable(Feature.AUTO_FILTER_COLUMNS)
                                                                        .enable(Feature.AUTO_SIZE_COLUMNS)
                                                                        .enable(Feature.TRUNCATE_CELLS)
                                                                        .enable(Feature.CSV_SEPARATE_SHEETS)
                                                                        .enable(Feature.CSV_WRITE_SHEET_NAMES)
                                                                        .build();
    public static final WorkbookWriterFeatures STANDARD_FAST = builder().enable(Feature.AUTO_FILTER_COLUMNS)
                                                                        .enable(Feature.TRUNCATE_CELLS)
                                                                        .enable(Feature.CSV_SEPARATE_SHEETS)
                                                                        .enable(Feature.CSV_WRITE_SHEET_NAMES)
                                                                        .build();

    private final Set<Feature> features = EnumSet.noneOf(Feature.class);

    /** CSV separator. */
    private final char separator;

    /** CSV charset. */
    private final Charset charset;
    // TODO Locale

    private final int maxLineLength;

    protected WorkbookWriterFeatures(Builder builder) {
        this.features.addAll(builder.features);
        this.separator = builder.separator;
        this.charset = builder.charset;
        this.maxLineLength = builder.maxLineLength;
    }

    /**
     * Enumeration of possible features.
     * <p>
     * Some features can be used to configure a WorkbookWriter.<br>
     * All features can be used to know if the WorkbookWriter supports it, whether it is enabled or not.
     *
     * @author Damien Carbonne
     */
    public enum Feature {
        /**
         * If enabled, thousands separator are added for numbers.
         * <p>
         * Configuration feature.
         */
        USE_THOUSANDS_SEPARATOR,

        /**
         * CSV: If enabled, writes each sheet name.
         * <p>
         * Configuration feature.
         */
        CSV_WRITE_SHEET_NAMES,

        /**
         * CSV: If enabled, separates sheets with one line.
         * <p>
         * Configuration feature.
         */
        CSV_SEPARATE_SHEETS,

        /**
         * If enabled, auto sizes columns width.
         * <p>
         * Configuration feature.<br>
         * <b>WARNING:</b> This generally leads to much slower processing.
         */
        AUTO_SIZE_COLUMNS,

        /**
         * If enabled, adds filters to headers.
         * <p>
         * Configuration feature.<br>
         * <b>WARNING:</b> there can be at most one auto-filter in a sheet.
         */
        AUTO_FILTER_COLUMNS,

        /**
         * If enabled,make support of rich text possible.
         * <p>
         * Currently, this is necessary with POI streaming.
         */
        RICH_TEXT,

        /**
         * If enabled, cells are truncated to remain in allowed limits.
         * <p>
         * Limit depends on workbook kind.
         */
        TRUNCATE_CELLS,

        /**
         * If enabled, each cell line is truncated to remain in allowed limits.
         * <p>
         * The line max length is set with {@link WorkbookWriterFeatures.Builder#maxLineLength(int)}.<br>
         * Limit depends on workbook kind.
         */
        TRUNCATE_CELLS_LINES,

        /**
         * If enabled, do not set cell style.
         * <p>
         * With POI, setting styles several times seem to have unexpected results.
         */
        NO_CELL_STYLES,

        /**
         * Support feature: can add comments to cells.
         */
        COMMENTS,

        /**
         * Support feature: can create content validation.
         */
        CONTENT_VALIDATION;

        /**
         * @return {@code true} if this Feature is a configuration feature
         *         which can be enabled or disabled.
         */
        public boolean isConfiguration() {
            return !isSupportOnly();
        }

        /**
         * @return {@code true} if this Feature is a support-only feature,
         *         which cannot be enabled or disabled.
         */
        public boolean isSupportOnly() {
            return this == COMMENTS;
        }
    }

    public boolean isEnabled(Feature feature) {
        return features.contains(feature);
    }

    /**
     * @return The CSV separator to use. Default to ';'.<br>
     *         Used for {@link WorkbookKind#CSV}.
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * @return The CSV charset to use.<br>
     *         Used for {@link WorkbookKind#CSV}.
     */
    public Charset getCharset() {
        return charset;
    }

    public int getMaxLineLength() {
        return maxLineLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<Feature> features = EnumSet.noneOf(Feature.class);
        private char separator = ';';
        private Charset charset = Charset.defaultCharset();
        private int maxLineLength = 255;

        protected Builder() {
        }

        public Builder set(WorkbookWriterFeatures other) {
            this.features.clear();
            this.features.addAll(other.features);
            this.separator = other.separator;
            this.charset = other.charset;
            this.maxLineLength = other.maxLineLength;
            return this;
        }

        public Builder enable(Feature feature) {
            Checks.isNotNull(feature, "feature");
            Checks.isTrue(feature.isConfiguration(), feature + " is not a configuration feature");
            this.features.add(feature);
            return this;
        }

        public Builder setEnabled(Feature feature,
                                  boolean enabled) {
            Checks.isNotNull(feature, "feature");
            Checks.isTrue(feature.isConfiguration(), feature + " is not a configuration feature");
            if (enabled) {
                this.features.add(feature);
            } else {
                this.features.remove(feature);
            }
            return this;
        }

        public Builder separator(char separator) {
            this.separator = separator;
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder maxLineLength(int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public WorkbookWriterFeatures build() {
            return new WorkbookWriterFeatures(this);
        }
    }
}