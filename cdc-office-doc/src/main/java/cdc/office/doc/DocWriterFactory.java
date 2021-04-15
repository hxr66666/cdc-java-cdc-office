package cdc.office.doc;

import java.io.File;
import java.io.IOException;

import cdc.util.lang.FailureReaction;
import cdc.util.lang.Introspection;
import cdc.util.lang.UnexpectedValueException;

public class DocWriterFactory {

    private DocWriter create(File file,
                             String className) throws IOException {
        final Class<? extends DocWriter> cls = Introspection.getClass(className, DocWriter.class);
        final Class<?>[] parameterTypes = { File.class, DocWriterFactory.class };
        return Introspection.newInstance(cls, parameterTypes, FailureReaction.FAIL, file, this);
    }

    public DocWriter create(File file) throws IOException {
        final DocKind kind = DocKind.from(file);
        if (kind == null) {
            throw new IllegalArgumentException("Can not find doc kind of " + file);
        }
        switch (kind) {
        case DOC:
        case DOCX:
            return create(file, "cdc.util.office.doc.word.WordDocWriter");
        case HTML:
            return create(file, "cdc.util.office.doc.html.HtmlDocWriter");
        case MD:
            return create(file, "cdc.util.office.doc.md.MdDocWriter");
        case ODF:
            return create(file, "cdc.util.office.doc.odf.OdfDocWriter");
        case TXT:
            return create(file, "cdc.util.office.doc.txt.TxtDocWriter");
        default:
            throw new UnexpectedValueException(kind);
        }
    }

    public DocWriter create(String filename) throws IOException {
        return create(new File(filename));
    }
}