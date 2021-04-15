package cdc.office.doc.html;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import cdc.office.doc.DocKind;
import cdc.office.doc.DocWriterFactory;
import cdc.office.doc.ListKind;
import cdc.office.doc.TextModifier;
import cdc.office.doc.core.AbstractDocWriter;

public class HtmlDocWriter extends AbstractDocWriter {

    public HtmlDocWriter(File file,
                         DocWriterFactory factory) {
        // TODO
    }

    @Override
    public void close() throws IOException {
        // TODO
    }

    @Override
    public void flush() throws IOException {
        // TODO
    }

    @Override
    public DocKind getKind() {
        return DocKind.HTML;
    }

    @Override
    public void beginDoc(String title) {
        super.beginDoc(title);
        // TODO
    }

    @Override
    public void endDoc() {
        super.endDoc();
        // TODO
    }

    @Override
    public void beginChapter(String title) {
        super.beginChapter(title);
        // TODO
    }

    @Override
    public void endChapter() {
        super.endChapter();
        // TODO
    }

    @Override
    public void beginList(ListKind kind) {
        super.beginList(kind);
        // TODO
    }

    @Override
    public void endList() {
        super.endList();
        // TODO
    }

    @Override
    public void beginListItem() {
        super.beginListItem();
        // TODO
    }

    @Override
    public void endListItem() {
        super.endListItem();
        // TODO
    }

    @Override
    public void addText(String text,
                        TextModifier... modifiers) {
        super.addText(text, modifiers);
        // TODO
    }

    @Override
    public void addLink(String text,
                        URL url,
                        TextModifier... modifiers) {
        super.addLink(text, url, modifiers);
        // TODO
    }

    @Override
    public void beginTable() {
        super.beginTable();
        // TODO
    }

    @Override
    public void endTable() {
        super.endTable();
        // TODO
    }

    @Override
    public void beginRow() {
        super.beginRow();
        // TODO
    }

    @Override
    public void endRow() {
        super.endRow();
        // TODO
    }

    @Override
    public void beginCell() {
        super.beginCell();
        // TODO
    }

    @Override
    public void endCell() {
        super.endCell();
        // TODO
    }
}