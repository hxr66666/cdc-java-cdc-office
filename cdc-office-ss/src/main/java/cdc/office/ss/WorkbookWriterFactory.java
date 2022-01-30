package cdc.office.ss;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import cdc.util.lang.FailureReaction;
import cdc.util.lang.Introspection;
import cdc.util.lang.UnexpectedValueException;

/**
 * WorkbookWriter factory.
 * <p>
 * It allows passing specific parameters to concrete factories.
 *
 * @author Damien Carbonne
 *
 */
public class WorkbookWriterFactory {
    private final Set<Hint> hints = EnumSet.noneOf(Hint.class);

    /**
     * Enumeration of hints that can be used to control factory.
     *
     * @author Damien Carbonne
     */
    public enum Hint {
        /**
         * Use Fast ODS.
         * <p>
         * <b>WARNING:</b> This is still experimental and incomplete. Do not use.
         */
        ODS_FAST,

        /**
         * Use ODF Toolkit.
         * <p>
         * <b>WARNING:</b> This is very slow, but seems to work.
         */
        ODF_TOOLKIT,

        /**
         * Use POI Streaming API.
         * <p>
         * This requires less memory, but is not compliant with certain features.
         */
        POI_STREAMING
        // TODO default header style for Excel / OpenOffice
    }

    public WorkbookWriterFactory() {
        super();
    }

    public WorkbookWriterFactory setEnabled(Hint hint,
                                            boolean enabled) {
        if (enabled) {
            hints.add(hint);
        } else {
            hints.remove(hint);
        }
        return this;
    }

    public boolean isEnabled(Hint hint) {
        return hints.contains(hint);
    }

    public Hint[] getHints() {
        final List<Hint> tmp = new ArrayList<>(hints);
        return tmp.toArray(new Hint[tmp.size()]);
    }

    private WorkbookWriter<?> create(File file,
                                     WorkbookWriterFeatures features,
                                     String className) throws IOException {
        final Class<? extends WorkbookWriter<?>> cls =
                Introspection.uncheckedCast(Introspection.getClass(className,
                                                                   WorkbookWriter.class,
                                                                   FailureReaction.FAIL));
        final Class<?>[] parameterTypes = { File.class, WorkbookWriterFeatures.class, WorkbookWriterFactory.class };
        return Introspection.newInstance(cls, parameterTypes, FailureReaction.FAIL, file, features, this);
    }

    /**
     * Creates a WorkbookWriter for a file.
     * <p>
     * Set parameters are used if appropriate for the file kind.
     *
     * @param file The file.
     * @param features The features.
     * @return An implementation of WorkbookWriter that supports the {@code file} type.
     * @throws IOException When an IO error occurs.
     * @throws IllegalArgumentException When {@code file} kind is not recognized.
     */
    public WorkbookWriter<?> create(File file,
                                    WorkbookWriterFeatures features) throws IOException {
        final WorkbookKind kind = WorkbookKind.from(file);
        if (kind == null) {
            throw new IllegalArgumentException("Can not find workbook kind of " + file);
        }
        // TODO features.checkAtMostOne(LOGGER, FailureReaction.WARN, Feature.ODS_FAST, Feature.ODS_SIMPLE);

        switch (kind) {
        case CSV:
            return create(file, features, "cdc.office.ss.csv.CsvWorkbookWriter");
        case ODS:
            if (isEnabled(Hint.ODS_FAST)) {
                return create(file, features, "cdc.office.ss.odf.FastOdsWorkbookWriter");
            } else {
                return create(file, features, "cdc.office.ss.odf.OdsWorkbookWriter");
            }
        case XLS:
        case XLSM:
        case XLSX:
            return create(file, features, "cdc.office.ss.excel.ExcelWorkbookWriter");
        default:
            throw new UnexpectedValueException(kind);
        }
    }

    /**
     * Creates a WorkbookWriter for a file.
     * <p>
     * Set parameters are used if appropriate for the file kind.
     *
     * @param file The file.
     * @return An implementation of WorkbookWriter that supports the {@code file} type.
     * @throws IOException When an IO error occurs.
     * @throws IllegalArgumentException When {@code file} kind is not recognized.
     */
    public WorkbookWriter<?> create(File file) throws IOException {
        return create(file, WorkbookWriterFeatures.DEFAULT);
    }

    /**
     * Creates a WorkbookWriter for a file.
     *
     * @param filename The file name.
     * @param features The features.
     * @return An implementation of WorkbookWriter that supports the {@code filename} type.
     * @throws IOException When an IO error occurs.
     * @throws IllegalArgumentException When {@code filename} kind is not recognized.
     */
    public WorkbookWriter<?> create(String filename,
                                    WorkbookWriterFeatures features) throws IOException {
        return create(new File(filename), features);
    }

    /**
     * Creates a WorkbookWriter for a file.
     *
     * @param filename The file name.
     * @return An implementation of WorkbookWriter that supports the {@code filename} type.
     * @throws IOException When an IO error occurs.
     * @throws IllegalArgumentException When {@code filename} kind is not recognized.
     */
    public WorkbookWriter<?> create(String filename) throws IOException {
        return create(filename, WorkbookWriterFeatures.DEFAULT);
    }

}