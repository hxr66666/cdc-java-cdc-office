package cdc.office.doc.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DocWriterContext {
    private static final Logger LOGGER = LogManager.getLogger(DocWriterContext.class);

    public enum Type {
        /** DocWriter is created. */
        START,
        /** DocWriter is closed and can not be modified anymore. */
        STOP,
        /** Currently inside the document. */
        IN_DOC,
        /** Currently inside a chapter. */
        IN_CHAPTER,
        /** Currently inside a list. */
        IN_LIST,
        /** Currently inside a list item. */
        IN_LIST_ITEM,
        /** Currently inside a table. */
        IN_TABLE,
        /** Currently inside a table row. */
        IN_ROW,
        /** Currently inside a table cell. */
        IN_CELL
    }

    /** Type of the context. */
    private Type type;
    /** Index of the numbered sub-context. */
    private int index = -1;
    /** Parent context. */
    private final DocWriterContext parent;
    /** Child context. */
    private DocWriterContext child = null;

    private DocWriterContext(Type type,
                             DocWriterContext parent) {
        this.type = type;
        this.index = -1;
        this.parent = parent;
        if (parent != null) {
            parent.child = this;
        }
    }

    public DocWriterContext() {
        this(Type.START, null);
    }

    public DocWriterContext pushContext(Type type) {
        LOGGER.trace("pushContext({})", type);
        if (child == null) {
            final DocWriterContext result = new DocWriterContext(type, this);
            return result;
        } else {
            child.setType(type);
            child.resetIndex();
            return child;
        }
    }

    public DocWriterContext popContext() {
        LOGGER.trace("popContext()");
        type = null;
        return parent;
    }

    public DocWriterContext popAll() {
        DocWriterContext index = this;
        while (index.parent != null) {
            index = index.popContext();
        }
        return index;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        LOGGER.trace("setType({})", type);
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void resetIndex() {
        index = -1;
    }

    public void initOrIncrementIndex() {
        if (index <= 0) {
            index = 1;
        } else {
            index++;
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(type);
        if (index >= 0) {
            builder.append(" ").append(index);
        }
        return builder.toString();
    }
}