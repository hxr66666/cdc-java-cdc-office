package cdc.office.doc;

import java.io.Closeable;
import java.io.Flushable;
import java.net.URL;

public interface DocWriter extends Closeable, Flushable {
    public DocKind getKind();

    public void beginDoc(String title);

    public void endDoc();

    public void beginChapter(String title);

    public void endChapter();

    public void beginList(ListKind kind);

    public void endList();

    public void beginListItem();

    public void endListItem();

    public void addText(String text,
                        TextModifier... modifiers);

    public void addLink(String text,
                        URL url,
                        TextModifier... modifiers);

    public void beginTable();

    public void endTable();

    public void beginRow();

    public void endRow();

    public void beginCell();

    public void endCell();

    // Image
    // Anchor
    // Legend
}