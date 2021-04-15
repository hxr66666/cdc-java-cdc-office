package cdc.office.ss.excel;

import cdc.util.lang.UnexpectedValueException;

public enum XssfDataType {
    BOOLEAN,
    ERROR,
    FORMULA,
    INLINE_STRING,
    SST_STRING,
    NUMBER;

    public static XssfDataType from(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        } else {
            switch (s) {
            case "b":
                return BOOLEAN;
            case "e":
                return ERROR;
            case "inlineStr":
                return INLINE_STRING;
            case "s":
                return SST_STRING;
            case "str":
                return FORMULA;
            case "n":
                return NUMBER;
            default:
                throw new UnexpectedValueException(s);
            }
        }
    }
}