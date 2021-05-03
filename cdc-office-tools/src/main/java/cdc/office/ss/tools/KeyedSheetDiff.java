package cdc.office.ss.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.simple.SpreadsheetDocument;

import cdc.office.ss.SheetLoader;
import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.ss.csv.CsvWorkbookWriter;
import cdc.office.ss.excel.ExcelWorkbookWriter;
import cdc.office.ss.odf.SimpleOdsWorkbookWriter;
import cdc.office.tables.Header;
import cdc.office.tables.HeaderMapper;
import cdc.office.tables.Row;
import cdc.office.tables.TableSection;
import cdc.office.tables.diff.CellDiff;
import cdc.office.tables.diff.DiffKind;
import cdc.office.tables.diff.KeyedTableDiff;
import cdc.office.tables.diff.RowDiff;
import cdc.tuples.CTupleN;
import cdc.util.cli.AbstractMainSupport;
import cdc.util.cli.FeatureMask;
import cdc.util.cli.OptionEnum;
import cdc.util.lang.UnexpectedValueException;

/**
 * Utility used to compare 2 csv files whose lines are identified by key columns.
 *
 * @author Damien Carbonne
 *
 */
public final class KeyedSheetDiff {
    protected static final Logger LOGGER = LogManager.getLogger(KeyedSheetDiff.class);
    protected final MainArgs margs;

    private KeyedSheetDiff(MainArgs margs) {
        this.margs = margs;
    }

    public static class MainArgs {
        public static final String DEFAULT_ADDED_MARK = "<A>";
        public static final String DEFAULT_REMOVED_MARK = "<R>";
        public static final String DEFAULT_CHANGED_MARK = "<C>";
        public static final String DEFAULT_UNCHANGED_MARK = "";

        public enum Feature implements OptionEnum {
            NO_UNCHANGED_LINES("no-unchanged-lines", "Do not output unchanged lines."),
            NO_ADDED_OR_REMOVED_MARKS("no-added-or-removed-marks",
                                      "Do not print added or removed marks. This forces insertion of the line mark column."),
            NO_COLORS("no-colors", "Do not use colors with xsl, xlsx or ods output format."),
            SORT_LINES("sort-lines", "Sort lines using keys. Order of key columns declaration matters."),
            SYNTHESIS("synthesis", "Prints a synthesis of differences.");

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

        public File file1;
        public String sheet1;
        public File file2;
        public String sheet2;
        public File output;
        public String sheet;
        public final List<String> keys = new ArrayList<>();
        /** charset. */
        public String charset;
        /** separator. */
        public char separator = ';';
        public String addedMark = DEFAULT_ADDED_MARK;
        public String removedMark = DEFAULT_REMOVED_MARK;
        public String changedMark = DEFAULT_CHANGED_MARK;
        public String unchangedMark = DEFAULT_UNCHANGED_MARK;
        public String lineMarkColumn = null;

        public final FeatureMask<Feature> features = new FeatureMask<>();

        public final void setEnabled(Feature feature,
                                     boolean enabled) {
            features.setEnabled(feature, enabled);
        }

        public final boolean isEnabled(Feature feature) {
            return features.isEnabled(feature);
        }
    }

    protected void execute() throws IOException {
        // Load input files as rows
        final SheetLoader loader = new SheetLoader();
        loader.getFactory().setCharset(margs.charset);
        loader.getFactory().setSeparator(margs.separator);
        final List<Row> rows1 =
                margs.sheet1 == null ? loader.load(margs.file1, null, 0) : loader.load(margs.file1, null, margs.sheet1);
        final List<Row> rows2 =
                margs.sheet2 == null ? loader.load(margs.file2, null, 0) : loader.load(margs.file2, null, margs.sheet2);

        // Retrieve headers of both files
        final Header header1 = new Header(rows1.get(0));
        final Header header2 = new Header(rows2.get(0));

        // Check that both headers contain the expected keys
        final Header expected = new Header(margs.keys);
        final HeaderMapper mapper1 = new HeaderMapper(expected, header1);
        final HeaderMapper mapper2 = new HeaderMapper(expected, header2);

        if (!mapper1.hasAllExpectedNames()) {
            throw new IllegalArgumentException("Invalid file1 header: " + header1);
        }

        if (!mapper2.hasAllExpectedNames()) {
            throw new IllegalArgumentException("Invalid file2 header: " + header2);
        }

        // Remove header in both input rows
        rows1.remove(0);
        rows2.remove(0);

        // Compare the data rows
        final KeyedTableDiff diff = new KeyedTableDiff(header1,
                                                       rows1,
                                                       header2,
                                                       rows2,
                                                       margs.keys);

        // Now generate the output
        LOGGER.info("Generate {}", margs.output);
        final WorkbookKind outputKind = WorkbookKind.from(margs.output);

        if (outputKind == null) {
            throw new IOException("Unrecognized output format for " + margs.output);
        }

        if (margs.isEnabled(MainArgs.Feature.SYNTHESIS)) {
            printSynthesis(diff);
        }

        switch (outputKind) {
        case CSV:
            new CsvGenerator().generate(header2, diff);
            break;
        case XLS:
        case XLSX:
        case XLSM:
            new ExcelGenerator().generate(header2, diff);
            break;
        case ODS:
            new OdsGenerator().generate(header2, diff);
            break;
        default:
            throw new UnexpectedValueException(outputKind);
        }
        LOGGER.info("Done");
    }

    private static void printSynthesis(KeyedTableDiff diff) {
        int addedLines = 0;
        int removedLines = 0;
        int unchangedLines = 0;
        int changedLines = 0;
        int addedCells = 0;
        int removedCells = 0;
        int unchangedCells = 0;
        int changedCells = 0;

        for (final RowDiff rdiff : diff.getDiffs()) {
            switch (rdiff.getKind()) {
            case ADDED:
                addedLines++;
                break;
            case CHANGED:
                changedLines++;
                for (final CellDiff cdiff : rdiff.getDiffs()) {
                    switch (cdiff.getKind()) {
                    case ADDED:
                        addedCells++;
                        break;
                    case CHANGED:
                        changedCells++;
                        break;
                    case REMOVED:
                        removedCells++;
                        break;
                    case SAME:
                        unchangedCells++;
                        break;
                    default:
                        throw new UnexpectedValueException(cdiff.getKind());
                    }
                }
                break;
            case REMOVED:
                removedLines++;
                break;
            case SAME:
                unchangedLines++;
                break;
            default:
                throw new UnexpectedValueException(rdiff.getKind());
            }
        }
        LOGGER.info("Lines");
        LOGGER.info("   Added:     {}", addedLines);
        LOGGER.info("   Removed:   {}", removedLines);
        LOGGER.info("   Changed:   {}", changedLines);
        LOGGER.info("   Unchanged: {}", unchangedLines);
        LOGGER.info("Cells");
        LOGGER.info("   Added:     {}", addedCells);
        LOGGER.info("   Removed:   {}", removedCells);
        LOGGER.info("   Changed:   {}", changedCells);
        LOGGER.info("   Unchanged: {}", unchangedCells);
    }

    protected String getLineMarkColumn() {
        if (margs.lineMarkColumn != null) {
            return margs.lineMarkColumn;
        } else if (margs.isEnabled(MainArgs.Feature.NO_ADDED_OR_REMOVED_MARKS)) {
            return "Line Diff";
        } else {
            return null;
        }
    }

    /**
     * Base Generator.
     *
     * @author Damien Carbonne
     */
    private abstract class Generator {
        protected final String lineMarkColumn = getLineMarkColumn();
        protected final boolean insertLineMarkColumn = lineMarkColumn != null;
        protected final String changedMark = margs.changedMark;
        protected final String addedMark = margs.isEnabled(MainArgs.Feature.NO_ADDED_OR_REMOVED_MARKS) ? "" : margs.addedMark;
        protected final String removedMark = margs.isEnabled(MainArgs.Feature.NO_ADDED_OR_REMOVED_MARKS) ? "" : margs.removedMark;
        protected final String unchangedMark = margs.unchangedMark;

        Generator() {
            super();
        }

        protected String getMark(DiffKind kind) {
            switch (kind) {
            case ADDED:
                return addedMark;
            case CHANGED:
                return changedMark;
            case REMOVED:
                return removedMark;
            case SAME:
                return unchangedMark;
            default:
                throw new UnexpectedValueException(kind);
            }
        }

        protected abstract void generate(Header header,
                                         KeyedTableDiff diff) throws IOException;
    }

    /**
     * Csv Generator.
     *
     * @author Damien Carbonne
     */
    private final class CsvGenerator extends Generator {
        public CsvGenerator() {
            super();
        }

        @Override
        public void generate(Header header,
                             KeyedTableDiff diff) throws IOException {
            final WorkbookWriterFeatures features = WorkbookWriterFeatures.builder()
                                                                          .charset(margs.charset)
                                                                          .separator(margs.separator)
                                                                          .build();
            try (final WorkbookWriter<?> writer = new CsvWorkbookWriter(margs.output, features)) {
                writer.beginSheet(null);

                // Header
                writer.beginRow(TableSection.HEADER);
                if (insertLineMarkColumn) {
                    writer.addCell(lineMarkColumn);
                }
                writer.addCells(header.getNames());

                // Data
                final List<CTupleN<String>> keys = diff.getKeys();
                if (margs.isEnabled(MainArgs.Feature.SORT_LINES)) {
                    Collections.sort(keys);
                }
                for (final CTupleN<String> key : keys) {
                    final RowDiff rdiff = diff.getDiff(key);
                    if (rdiff.getKind() != DiffKind.SAME || !margs.isEnabled(MainArgs.Feature.NO_UNCHANGED_LINES)) {
                        writer.beginRow(TableSection.DATA);
                        if (insertLineMarkColumn) {
                            writer.addCell(rdiff.getKind());
                        }
                        for (final CellDiff cdiff : rdiff.getDiffs()) {
                            switch (cdiff.getKind()) {
                            case ADDED:
                            case CHANGED:
                            case SAME:
                                writer.addCell(getMark(cdiff.getKind()) + cdiff.getRight());
                                break;
                            case REMOVED:
                                writer.addCell(getMark(cdiff.getKind()) + cdiff.getLeft());
                                break;
                            default:
                                throw new UnexpectedValueException(cdiff.getKind());
                            }
                        }
                    }
                }
            }
        }
    }

    static CellStyle createStyle(Workbook workbook,
                                 IndexedColors color) {
        final CellStyle style = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setColor(color.index);
        style.setFont(font);
        return style;
    }

    /**
     * Excel Generator.
     *
     * @author Damien Carbonne
     */
    private final class ExcelGenerator extends Generator {
        private CellStyle addedStyle;
        private CellStyle removedStyle;
        private CellStyle changedStyle;
        private CellStyle unchangedStyle;
        private CellStyle headerStyle;

        public ExcelGenerator() {
            super();
        }

        private void createStyles(Workbook workbook) {
            if (margs.isEnabled(MainArgs.Feature.NO_COLORS)) {
                addedStyle = null;
                removedStyle = null;
                changedStyle = null;
                unchangedStyle = null;
            } else {
                addedStyle = createStyle(workbook, IndexedColors.BLUE);
                removedStyle = createStyle(workbook, IndexedColors.RED);
                changedStyle = createStyle(workbook, IndexedColors.PINK);
                unchangedStyle = createStyle(workbook, IndexedColors.BLACK);
            }
            headerStyle = createStyle(workbook, IndexedColors.BLACK);
        }

        private CellStyle getStyle(DiffKind kind) {
            switch (kind) {
            case ADDED:
                return addedStyle;
            case CHANGED:
                return changedStyle;
            case REMOVED:
                return removedStyle;
            case SAME:
                return unchangedStyle;
            default:
                throw new UnexpectedValueException(kind);
            }
        }

        @Override
        public void generate(Header header,
                             KeyedTableDiff diff) throws IOException {
            final WorkbookWriterFeatures features = WorkbookWriterFeatures.builder()
                                                                          .maxLineLength(-1)
                                                                          .build();
            try (final ExcelWorkbookWriter writer = new ExcelWorkbookWriter(margs.output,
                                                                            features,
                                                                            true)) {
                createStyles(writer.getWorkbook());

                if (margs.sheet == null) {
                    writer.beginSheet("Delta");
                } else {
                    writer.beginSheet(margs.sheet);
                }

                // Header
                writer.beginRow(TableSection.HEADER);
                if (insertLineMarkColumn) {
                    writer.addCell(lineMarkColumn);
                    writer.getCell().setCellStyle(headerStyle);
                }
                for (final String name : header.getNames()) {
                    writer.addCell(name);
                    writer.getCell().setCellStyle(headerStyle);
                }

                // Data
                final List<CTupleN<String>> keys = diff.getKeys();
                if (margs.isEnabled(MainArgs.Feature.SORT_LINES)) {
                    Collections.sort(keys);
                }

                for (final CTupleN<String> key : keys) {
                    final RowDiff rdiff = diff.getDiff(key);
                    if (rdiff.getKind() != DiffKind.SAME || !margs.isEnabled(MainArgs.Feature.NO_UNCHANGED_LINES)) {
                        writer.beginRow(TableSection.DATA);
                        if (insertLineMarkColumn) {
                            writer.addCell(rdiff.getKind().toString());
                            writer.getCell().setCellStyle(getStyle(rdiff.getKind()));
                        }
                        for (final CellDiff cdiff : rdiff.getDiffs()) {
                            switch (cdiff.getKind()) {
                            case ADDED:
                            case CHANGED:
                            case SAME:
                                if (margs.isEnabled(MainArgs.Feature.NO_COLORS)) {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getRight());
                                } else {
                                    writer.addCell(cdiff.getRight());
                                    writer.getCell().setCellStyle(getStyle(cdiff.getKind()));
                                }
                                break;
                            case REMOVED:
                                if (margs.isEnabled(MainArgs.Feature.NO_COLORS)) {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getLeft());
                                } else {
                                    writer.addCell(cdiff.getLeft());
                                    writer.getCell().setCellStyle(getStyle(cdiff.getKind()));
                                }
                                break;
                            default:
                                throw new UnexpectedValueException(cdiff.getKind());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Ods Generator.
     *
     * @author Damien Carbonne
     */
    private final class OdsGenerator extends Generator {
        public OdsGenerator() {
            super();
        }

        private void createStyle(SpreadsheetDocument doc) {
            final OdfOfficeStyles styles = doc.getOrCreateDocumentStyles();
            final OdfStyle style = styles.newStyle("xxx", OdfStyleFamily.Text);

        }

        @Override
        public void generate(Header header,
                             KeyedTableDiff diff) throws IOException {
            try (WorkbookWriter<?> writer = new SimpleOdsWorkbookWriter(margs.output, WorkbookWriterFeatures.DEFAULT)) {
                if (margs.sheet == null) {
                    writer.beginSheet("Delta");
                } else {
                    writer.beginSheet(margs.sheet);
                }

                // Header
                writer.beginRow(TableSection.HEADER);
                if (insertLineMarkColumn) {
                    writer.addCell(lineMarkColumn);
                    // TODO style
                }
                for (final String name : header.getNames()) {
                    writer.addCell(name);
                    // TODO style
                }

                // Data
                final List<CTupleN<String>> keys = diff.getKeys();
                if (margs.isEnabled(MainArgs.Feature.SORT_LINES)) {
                    Collections.sort(keys);
                }

                for (final CTupleN<String> key : keys) {
                    final RowDiff rdiff = diff.getDiff(key);
                    if (rdiff.getKind() != DiffKind.SAME || !margs.isEnabled(MainArgs.Feature.NO_UNCHANGED_LINES)) {
                        writer.beginRow(TableSection.DATA);
                        if (insertLineMarkColumn) {
                            writer.addCell(rdiff.getKind().toString());
                            // TODO style
                        }

                        for (final CellDiff cdiff : rdiff.getDiffs()) {
                            switch (cdiff.getKind()) {
                            case ADDED:
                            case CHANGED:
                            case SAME:
                                if (margs.isEnabled(MainArgs.Feature.NO_COLORS)) {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getRight());
                                } else {
                                    writer.addCell(cdiff.getRight());
                                    // TODO style
                                }
                                break;
                            case REMOVED:
                                if (margs.isEnabled(MainArgs.Feature.NO_COLORS)) {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getLeft());
                                } else {
                                    writer.addCell(cdiff.getLeft());
                                    // TODO style
                                }
                                break;
                            default:
                                throw new UnexpectedValueException(cdiff.getKind());
                            }
                        }

                    }
                }
            } catch (final IOException e) {
                throw e;
            } catch (final Exception e) {
                throw new IOException(e);
            }
        }
    }

    public static void execute(MainArgs margs) throws IOException {
        final KeyedSheetDiff instance = new KeyedSheetDiff(margs);
        instance.execute();
    }

    public static void main(String[] args) {
        final MainSupport support = new MainSupport();
        support.main(args);
    }

    private static class MainSupport extends AbstractMainSupport<MainArgs, Void> {
        private static final String FILE1 = "file1";
        private static final String FILE2 = "file2";
        private static final String SHEET1 = "sheet1";
        private static final String SHEET2 = "sheet2";
        private static final String SHEET = "sheet";
        private static final String KEY = "key";
        private static final String CHARSET = "charset";
        private static final String SEPARATOR = "separator";
        private static final String ADDED_MARK = "added-mark";
        private static final String REMOVED_MARK = "removed-mark";
        private static final String CHANGED_MARK = "changed-mark";
        private static final String UNCHANGED_MARK = "unchanged-mark";
        private static final String LINE_MARK_COLUMN = "line-mark-column";

        public MainSupport() {
            super(KeyedSheetDiff.class, LOGGER);
        }

        @Override
        protected String getVersion() {
            return Config.VERSION;
        }

        @Override
        protected String getHelpHeader() {
            return KeyedSheetDiff.class.getSimpleName()
                    + " is used to compare two sheets (csv, xls, xlsx or ods).\n"
                    + "Lines in sheets are matched by a set of key columns.\n"
                    + "Input and output files can use different formats.\n"
                    + "Differences are indicated with textual marks or colors (if output format supports it).\n";
        }

        @Override
        protected String getHelpFooter() {
            return "\nKNOWN LIMITATIONS\n"
                    + "All csv files (input and output) must use the same charset and separator.\n"
                    + "When mixing input file formats with CSV, if a key column contains numbers, comparison will fail.\n"
                    + "Ods handling is experimental. Ods output does not support coloring.";
        }

        @Override
        protected void addSpecificOptions(Options options) {
            options.addOption(Option.builder()
                                    .longOpt(FILE1)
                                    .desc("Name of the first csv, xls, xlsx or ods input file.")
                                    .hasArg()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(FILE2)
                                    .desc("Name of the second csv, xls, xlsx or ods input file.")
                                    .hasArg()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(SHEET1)
                                    .desc("Name of the sheet in the first xls, xlsx or ods input file. If omitted, the first sheet is loaded")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(SHEET2)
                                    .desc("Name of the sheet in the second xls, xlsx or ods input file. If omitted, the first sheet is loaded")
                                    .hasArg()
                                    .build());
            options.addOption(Option.builder()
                                    .longOpt(SHEET)
                                    .desc("Name of the sheet in the xls, xlsx or ods output file.")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(OUTPUT)
                                    .desc("Name of the csv, xls, xlsx or ods output file.")
                                    .hasArg()
                                    .required()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(KEY)
                                    .desc("Name of key column(s).")
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

            options.addOption(Option.builder()
                                    .longOpt(ADDED_MARK)
                                    .desc("Optional mark for added cells (default: \"" + MainArgs.DEFAULT_ADDED_MARK + "\").")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(REMOVED_MARK)
                                    .desc("Optional mark for removed cells (default: \"" + MainArgs.DEFAULT_REMOVED_MARK + "\").")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(CHANGED_MARK)
                                    .desc("Optional mark for changed cells (default: \"" + MainArgs.DEFAULT_CHANGED_MARK + "\").")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(UNCHANGED_MARK)
                                    .desc("Optional mark for unchanged cells (default: \"" + MainArgs.DEFAULT_UNCHANGED_MARK
                                            + "\").")
                                    .hasArg()
                                    .build());

            options.addOption(Option.builder()
                                    .longOpt(LINE_MARK_COLUMN)
                                    .desc("Optional name of a line mark column.")
                                    .hasArg()
                                    .build());

            AbstractMainSupport.addNoArgOptions(options, MainArgs.Feature.class);
        }

        @Override
        protected MainArgs analyze(CommandLine cl) throws ParseException {
            final MainArgs margs = new MainArgs();

            margs.file1 = getValueAsExistingFile(cl, FILE1, null);
            margs.sheet1 = getValueAsString(cl, SHEET1, null);
            margs.file2 = getValueAsExistingFile(cl, FILE2, null);
            margs.sheet2 = getValueAsString(cl, SHEET2, null);
            margs.output = getValueAsFile(cl, OUTPUT, null);
            margs.sheet = getValueAsString(cl, SHEET, null);
            if (cl.hasOption(CHARSET)) {
                margs.charset = cl.getOptionValue(CHARSET);
            }
            margs.separator = AbstractMainSupport.getValueAsChar(cl, SEPARATOR, ';');
            margs.addedMark = AbstractMainSupport.getValueAsString(cl, ADDED_MARK, MainArgs.DEFAULT_ADDED_MARK);
            margs.removedMark = AbstractMainSupport.getValueAsString(cl, REMOVED_MARK, MainArgs.DEFAULT_REMOVED_MARK);
            margs.changedMark = AbstractMainSupport.getValueAsString(cl, CHANGED_MARK, MainArgs.DEFAULT_CHANGED_MARK);
            margs.unchangedMark = AbstractMainSupport.getValueAsString(cl, UNCHANGED_MARK, MainArgs.DEFAULT_UNCHANGED_MARK);
            margs.lineMarkColumn = AbstractMainSupport.getValueAsString(cl, LINE_MARK_COLUMN, null);

            for (final String s : cl.getOptionValues(KEY)) {
                margs.keys.add(s);
            }
            AbstractMainSupport.setMask(cl, MainArgs.Feature.class, margs.features::setEnabled);
            return margs;
        }

        @Override
        protected Void execute(MainArgs margs) throws Exception {
            KeyedSheetDiff.execute(margs);
            return null;
        }
    }
}