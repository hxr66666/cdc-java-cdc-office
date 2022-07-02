package cdc.office.ss;

import java.io.File;

import cdc.util.files.Files;
import cdc.util.lang.UnexpectedValueException;

/**
 * Enumeration of supported workbook (spreadsheet) kinds.
 *
 * @author Damien Carbonne
 *
 */
public enum WorkbookKind {
    /** Csv file. */
    CSV("csv"),
    /** ODF Spreadsheet. */
    ODS("ods"),
    /** MS Excel. */
    XLS("xls"),
    /** MS Excel. */
    XLSM("xlsm"),
    /** MS Excel. */
    XLSX("xlsx");

    private final String extension;

    private WorkbookKind(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static WorkbookKind from(File file) {
        final String ext = Files.getExtension(file).toLowerCase();
        for (final WorkbookKind kind : values()) {
            if (kind.extension.equals(ext)) {
                return kind;
            }
        }
        return null;
    }

    /**
     * @return The maximum number of rows supported by this kind of spreadsheet.
     */
    public int getMaxRows() {
        switch (this) {
        case CSV:
            return -1;
        case ODS:
            return 1_048_576;
        case XLS:
            return 65536;
        case XLSM:
        case XLSX:
            return 1_048_576;
        default:
            throw new UnexpectedValueException(this);
        }
    }

    /**
     * @return The maximum number of columns supported by this kind of spreadsheet.
     */
    public int getMaxColumns() {
        switch (this) {
        case CSV:
            return -1;
        case ODS:
            return 1024;
        case XLS:
            return 256;
        case XLSM:
        case XLSX:
            return 16384;
        default:
            throw new UnexpectedValueException(this);
        }
    }

    /**
     * @return The maximum number of characters in a cell supported by this kind of spreadsheet.
     */
    public int getMaxCellSize() {
        switch (this) {
        case CSV:
            return -1;
        case ODS:
        case XLS:
        case XLSM:
        case XLSX:
            return 32767;
        default:
            throw new UnexpectedValueException(this);
        }
    }

    /**
     * @return The maximum number of characters for content validation message supported by this kind of spreadsheet.
     */
    public int getMaxContentValidationMessageSize() {
        switch (this) {
        case CSV:
            return -1;
        case ODS:
        case XLS:
        case XLSM:
        case XLSX:
            return 250;
        default:
            throw new UnexpectedValueException(this);
        }
    }
}