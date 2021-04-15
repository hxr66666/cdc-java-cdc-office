package cdc.office.doc.core;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.doc.DocWriter;
import cdc.office.doc.ListKind;
import cdc.office.doc.TextModifier;
import cdc.office.doc.core.DocWriterContext.Type;
import cdc.util.lang.InvalidStateException;

public abstract class AbstractDocWriter implements DocWriter {
    private static final Logger LOGGER = LogManager.getLogger(AbstractDocWriter.class);
    protected DocWriterContext context = new DocWriterContext();

    protected void stateError(String caller,
                              DocWriterContext.Type... types) {
        try {
            flush();
        } catch (final IOException e) {
            LOGGER.catching(e);
        }
        final StringBuilder message = new StringBuilder();
        message.append("Invalid state: ")
               .append(context.getType())
               .append(", when calling: ")
               .append(caller)
               .append(", expecting one of: ")
               .append(Arrays.toString(types));
        throw new InvalidStateException(message.toString());
    }

    protected final void expect(String caller,
                                DocWriterContext.Type type) {
        if (context.getType() != type) {
            stateError(caller, type);
        }
    }

    protected final void expect(String caller,
                                DocWriterContext.Type type1,
                                DocWriterContext.Type type2) {
        if (context.getType() != type1
                && context.getType() != type2) {
            stateError(caller, type1, type2);
        }
    }

    protected final void expect(String caller,
                                DocWriterContext.Type type1,
                                DocWriterContext.Type type2,
                                DocWriterContext.Type type3,
                                DocWriterContext.Type type4) {
        if (context.getType() != type1
                && context.getType() != type2
                && context.getType() != type3
                && context.getType() != type4) {
            stateError(caller, type1, type2, type3, type4);
        }
    }

    private void pushContext(DocWriterContext.Type type) {
        context = context.pushContext(type);
    }

    private void popContext() {
        context = context.popContext();
    }

    @Override
    public void beginDoc(String title) {
        expect("beginDoc()", Type.START);
        pushContext(Type.IN_DOC);
    }

    @Override
    public void endDoc() {
        expect("endDoc()", Type.IN_DOC);
        popContext();
        context.setType(DocWriterContext.Type.STOP);
    }

    @Override
    public void beginChapter(String title) {
        expect("beginChapter(...)", Type.IN_DOC, Type.IN_CHAPTER);
        context.initOrIncrementIndex();
        pushContext(Type.IN_CHAPTER);
    }

    @Override
    public void endChapter() {
        expect("endChapter()", Type.IN_CHAPTER);
        popContext();
    }

    @Override
    public void beginList(ListKind kind) {
        expect("beginList()", Type.IN_DOC, Type.IN_CHAPTER, Type.IN_LIST_ITEM, Type.IN_CELL);
        pushContext(DocWriterContext.Type.IN_LIST);
        // TODO push kind
    }

    @Override
    public void endList() {
        expect("endList()", Type.IN_LIST);
        popContext();
    }

    @Override
    public void beginListItem() {
        expect("beginListItem()", Type.IN_LIST);
        context.initOrIncrementIndex();
        pushContext(DocWriterContext.Type.IN_LIST_ITEM);
    }

    @Override
    public void endListItem() {
        expect("endListItem()", Type.IN_LIST_ITEM);
        popContext();
    }

    @Override
    public void addText(String text,
                        TextModifier... modifiers) {
        expect("addText()", Type.IN_DOC, Type.IN_CHAPTER, Type.IN_LIST_ITEM, Type.IN_CELL);
    }

    @Override
    public void addLink(String text,
                        URL url,
                        TextModifier... modifiers) {
        expect("addText()", Type.IN_DOC, Type.IN_CHAPTER, Type.IN_LIST_ITEM, Type.IN_CELL);
    }

    @Override
    public void beginTable() {
        expect("beginTable()", Type.IN_DOC, Type.IN_CHAPTER, Type.IN_LIST_ITEM, Type.IN_CELL);
        pushContext(DocWriterContext.Type.IN_TABLE);
    }

    @Override
    public void endTable() {
        expect("endTable()", Type.IN_TABLE);
        popContext();
    }

    @Override
    public void beginRow() {
        expect("beginRow()", Type.IN_TABLE);
        pushContext(DocWriterContext.Type.IN_ROW);
    }

    @Override
    public void endRow() {
        expect("endRow()", Type.IN_ROW);
        popContext();
    }

    @Override
    public void beginCell() {
        expect("beginCell()", Type.IN_ROW);
        pushContext(DocWriterContext.Type.IN_CELL);
    }

    @Override
    public void endCell() {
        expect("endCell()", Type.IN_CELL);
        popContext();
    }
}