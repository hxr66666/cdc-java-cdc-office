package cdc.office.ss.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.simple.SpreadsheetDocument;

import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.ss.csv.CsvWorkbookWriter;
import cdc.office.ss.excel.ExcelWorkbookWriter;
import cdc.office.ss.odf.SimpleOdsWorkbookWriter;
import cdc.office.tables.Header;
import cdc.office.tables.TableSection;
import cdc.office.tables.diff.CellDiff;
import cdc.office.tables.diff.DiffKind;
import cdc.office.tables.diff.KeyedTableDiff;
import cdc.office.tables.diff.RowDiff;
import cdc.office.tables.diff.Side;
import cdc.tuples.CTupleN;
import cdc.util.lang.UnexpectedValueException;

/**
 * Class used to export a KeyedTableDiff to an Office file.
 *
 * @author Damien Carbonne
 */
public class KeyedTableDiffExporter {
    private String lineMarkColumn;
    private String changedMark;
    private String addedMark;
    private String removedMark;
    private String unchangedMark;
    private String sheetName = "Delta";
    private WorkbookWriterFeatures features = WorkbookWriterFeatures.DEFAULT;
    private boolean insertLineMarkColumn = false;
    private boolean sortLines = false;
    private boolean showUnchangedLines = true;
    private boolean showColors = false;

    public KeyedTableDiffExporter() {
        super();
    }

    public KeyedTableDiffExporter setLineMarkColumn(String lineMarkColumn) {
        this.lineMarkColumn = lineMarkColumn;
        this.insertLineMarkColumn = lineMarkColumn != null;
        return this;
    }

    public KeyedTableDiffExporter setChangedMark(String changedMark) {
        this.changedMark = changedMark;
        return this;
    }

    public KeyedTableDiffExporter setAddedMark(String addedMark) {
        this.addedMark = addedMark;
        return this;
    }

    public KeyedTableDiffExporter setRemovedMark(String removedMark) {
        this.removedMark = removedMark;
        return this;
    }

    public KeyedTableDiffExporter setUnchangedMark(String unchangedMark) {
        this.unchangedMark = unchangedMark;
        return this;
    }

    public KeyedTableDiffExporter setFeatures(WorkbookWriterFeatures features) {
        this.features = features;
        return this;
    }

    public KeyedTableDiffExporter setSortLines(boolean sortLines) {
        this.sortLines = sortLines;
        return this;
    }

    public KeyedTableDiffExporter setShowUnchangedLines(boolean showUnchangedLines) {
        this.showUnchangedLines = showUnchangedLines;
        return this;
    }

    public KeyedTableDiffExporter setShowColors(boolean showColors) {
        this.showColors = showColors;
        return this;
    }

    public KeyedTableDiffExporter setSheetName(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    public void save(KeyedTableDiff diff,
                     File file) throws IOException {
        final WorkbookKind outputKind = WorkbookKind.from(file);

        if (outputKind == null) {
            throw new IOException("Unrecognized output format for " + file);
        }

        final Header header = diff.getHeader(Side.RIGHT);

        switch (outputKind) {
        case CSV:
            new CsvGenerator().generate(file, header, diff);
            break;
        case XLS:
        case XLSX:
        case XLSM:
            new ExcelGenerator().generate(file, header, diff);
            break;
        case ODS:
            new OdsGenerator().generate(file, header, diff);
            break;
        default:
            throw new UnexpectedValueException(outputKind);
        }
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

    @FunctionalInterface
    private interface Generator {
        public abstract void generate(File file,
                                      Header header,
                                      KeyedTableDiff diff) throws IOException;
    }

    private final class CsvGenerator implements Generator {
        public CsvGenerator() {
            super();
        }

        @Override
        public void generate(File file,
                             Header header,
                             KeyedTableDiff diff) throws IOException {
            try (final WorkbookWriter<?> writer = new CsvWorkbookWriter(file, features)) {
                writer.beginSheet(null);

                // Header
                writer.beginRow(TableSection.HEADER);
                if (insertLineMarkColumn) {
                    writer.addCell(lineMarkColumn);
                }
                writer.addCells(header.getNames());

                // Data
                final List<CTupleN<String>> keys = diff.getKeys();
                if (sortLines) {
                    Collections.sort(keys);
                }
                for (final CTupleN<String> key : keys) {
                    final RowDiff rdiff = diff.getDiff(key);
                    if (rdiff.getKind() != DiffKind.SAME || showUnchangedLines) {
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
    private final class ExcelGenerator implements Generator {
        private CellStyle addedStyle;
        private CellStyle removedStyle;
        private CellStyle changedStyle;
        private CellStyle unchangedStyle;
        private CellStyle headerStyle;

        public ExcelGenerator() {
            super();
        }

        private void createStyles(Workbook workbook) {
            if (showColors) {
                addedStyle = createStyle(workbook, IndexedColors.BLUE);
                removedStyle = createStyle(workbook, IndexedColors.RED);
                changedStyle = createStyle(workbook, IndexedColors.PINK);
                unchangedStyle = createStyle(workbook, IndexedColors.BLACK);
            } else {
                addedStyle = null;
                removedStyle = null;
                changedStyle = null;
                unchangedStyle = null;
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
        public void generate(File file,
                             Header header,
                             KeyedTableDiff diff) throws IOException {
            try (final ExcelWorkbookWriter writer = new ExcelWorkbookWriter(file,
                                                                            features,
                                                                            true)) {
                createStyles(writer.getWorkbook());

                writer.beginSheet(sheetName);

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
                if (sortLines) {
                    Collections.sort(keys);
                }

                for (final CTupleN<String> key : keys) {
                    final RowDiff rdiff = diff.getDiff(key);
                    if (rdiff.getKind() != DiffKind.SAME || showUnchangedLines) {
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
                                if (showColors) {
                                    writer.addCell(cdiff.getRight());
                                    writer.getCell().setCellStyle(getStyle(cdiff.getKind()));
                                } else {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getRight());
                                }
                                break;
                            case REMOVED:
                                if (showColors) {
                                    writer.addCell(cdiff.getLeft());
                                    writer.getCell().setCellStyle(getStyle(cdiff.getKind()));
                                } else {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getLeft());
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
    private final class OdsGenerator implements Generator {
        public OdsGenerator() {
            super();
        }

        private void createStyle(SpreadsheetDocument doc) {
            final OdfOfficeStyles styles = doc.getOrCreateDocumentStyles();
            final OdfStyle style = styles.newStyle("xxx", OdfStyleFamily.Text);

        }

        @Override
        public void generate(File file,
                             Header header,
                             KeyedTableDiff diff) throws IOException {
            try (WorkbookWriter<?> writer = new SimpleOdsWorkbookWriter(file, features)) {
                writer.beginSheet(sheetName);

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
                if (sortLines) {
                    Collections.sort(keys);
                }

                for (final CTupleN<String> key : keys) {
                    final RowDiff rdiff = diff.getDiff(key);
                    if (rdiff.getKind() != DiffKind.SAME || showUnchangedLines) {
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
                                if (showColors) {
                                    writer.addCell(cdiff.getRight());
                                    // TODO style
                                } else {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getRight());
                                }
                                break;
                            case REMOVED:
                                if (showColors) {
                                    writer.addCell(cdiff.getLeft());
                                    // TODO style
                                } else {
                                    writer.addCell(getMark(cdiff.getKind()) + cdiff.getLeft());
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
}