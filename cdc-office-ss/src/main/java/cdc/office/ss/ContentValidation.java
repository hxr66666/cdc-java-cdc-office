package cdc.office.ss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cdc.util.lang.Checks;

public class ContentValidation {
    private final ErrorReaction errorReaction;
    private final ValidationType validationType;
    private final Operator operator;
    private final String helpTitle;
    private final String helpText;
    private final String errorTitle;
    private final String errorText;
    private final List<String> values;
    private final boolean allowsEmptyCell;
    private final List<CellAddressRange> ranges;

    protected ContentValidation(ErrorReaction errorReaction,
                                ValidationType validationType,
                                Operator operator,
                                String helpTitle,
                                String helpText,
                                String errorTitle,
                                String errorText,
                                List<String> values,
                                boolean allowsEmptyCell,
                                List<CellAddressRange> ranges) {
        this.errorReaction = errorReaction;
        this.validationType = validationType;
        this.operator = operator;
        this.helpTitle = helpTitle;
        this.helpText = helpText;
        this.errorTitle = errorTitle;
        this.errorText = errorText;
        this.values = new ArrayList<>(values);
        this.allowsEmptyCell = allowsEmptyCell;
        this.ranges = new ArrayList<>(ranges);

        Checks.isTrue(validationType.accepts(operator),
                      validationType + " is not compliant with " + operator + " operator.");
        Checks.isTrue(operator.isCompliantWithValues(values.size()),
                      operator + " is not compliant with " + values.size() + " values.");
        Checks.isFalse(ranges.isEmpty(), "No ranges were defined.");
    }

    public enum ErrorReaction {
        STOP,
        WARN,
        INFO
    }

    public enum ValidationType {
        ANY,
        INTEGER,
        DECIMAL,
        LIST,
        DATE,
        TIME,
        TEXT_LENGTH,
        FORMULA;

        public boolean accepts(Operator operator) {
            if (this == ANY || this == FORMULA || this == LIST) {
                return operator == Operator.NONE;
            } else {
                return operator != Operator.NONE;
            }
        }
    }

    public enum Operator {
        NONE,
        BETWEEN,
        NOT_BETWEEN,
        EQUAL,
        NOT_EQUAL,
        GREATER_THAN,
        LESS_THAN,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL;

        boolean isCompliantWithValues(int size) {
            if (this == NONE) {
                return true;
            } else if (this == BETWEEN || this == NOT_BETWEEN) {
                return size == 2;
            } else {
                return size == 1;
            }
        }
    }

    public ErrorReaction getErrorReaction() {
        return errorReaction;
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getHelpTitle() {
        return helpTitle;
    }

    public String getHelpText() {
        return helpText;
    }

    public boolean showHelp() {
        return helpTitle != null || helpText != null;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public String getErrorText() {
        return errorText;
    }

    public boolean showError() {
        return errorTitle != null || errorText != null;
    }

    public List<String> getValues() {
        return values;
    }

    public String getValue1() {
        return values.get(0);
    }

    public String getValue2() {
        return values.size() >= 2 ? values.get(1) : null;
    }

    public boolean allowsEmptyCell() {
        return allowsEmptyCell;
    }

    public List<CellAddressRange> getRanges() {
        return ranges;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected ErrorReaction errorReaction = ErrorReaction.INFO;
        protected ValidationType validationType = ValidationType.ANY;
        protected Operator operator = Operator.NONE;
        protected String helpTitle = null;
        protected String helpText = null;
        protected String errorTitle = null;
        protected String errorText = null;
        protected List<String> values = new ArrayList<>();
        protected boolean allowsEmptyCell = true;
        protected final List<CellAddressRange> ranges = new ArrayList<>();

        protected Builder() {
        }

        public Builder errorReaction(ErrorReaction errorReaction) {
            this.errorReaction = errorReaction;
            return this;
        }

        public Builder validationType(ValidationType validationType) {
            this.validationType = validationType;
            return this;
        }

        public Builder operator(Operator operator) {
            this.operator = operator;
            return this;
        }

        public Builder help(String title,
                            String text) {
            this.helpTitle = title;
            this.helpText = text;
            return this;
        }

        public Builder error(String title,
                             String text) {
            this.errorTitle = title;
            this.errorText = text;
            return this;
        }

        public Builder value(String value) {
            this.values.clear();
            this.values.add(value);
            return this;
        }

        public Builder values(String value1,
                              String value2) {
            this.values.clear();
            this.values.add(value1);
            this.values.add(value2);
            return this;
        }

        public Builder values(String... values) {
            this.values.clear();
            Collections.addAll(this.values, values);
            return this;
        }

        public Builder allowsEmptyCell(boolean allowsEmptyCell) {
            this.allowsEmptyCell = allowsEmptyCell;
            return this;
        }

        public Builder addRange(CellAddressRange range) {
            this.ranges.add(range);
            return this;
        }

        public ContentValidation build() {
            return new ContentValidation(errorReaction,
                                         validationType,
                                         operator,
                                         helpTitle,
                                         helpText,
                                         errorTitle,
                                         errorText,
                                         values,
                                         allowsEmptyCell,
                                         ranges);
        }
    }
}