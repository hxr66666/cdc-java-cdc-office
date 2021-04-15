package cdc.office.doc;

import java.io.File;

import cdc.util.files.Files;

public enum DocKind {
    TXT("txt"),
    MD("md"),
    HTML("html"),
    DOC("doc"),
    DOCX("docx"),
    ODF("TODO");

    private final String extension;

    private DocKind(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static DocKind from(File file) {
        final String ext = Files.getExtension(file).toLowerCase();
        for (final DocKind kind : values()) {
            if (kind.extension.equals(ext)) {
                return kind;
            }
        }
        return null;
    }
}