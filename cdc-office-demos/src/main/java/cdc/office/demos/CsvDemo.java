package cdc.office.demos;

import java.io.File;
import java.nio.charset.StandardCharsets;

import cdc.office.tools.SeparatorConverter;
import cdc.office.tools.AbstractFilter.BaseMainArgs.BaseFeature;

public final class CsvDemo {
    private CsvDemo() {
    }

    public static void main(String[] args) throws Exception {
        final SeparatorConverter.MainArgs margs = new SeparatorConverter.MainArgs();
        margs.input = new File("src/test/resources/data01.csv");
        margs.inputCharset = StandardCharsets.UTF_8;
        margs.output = new File("target", CsvDemo.class.getSimpleName() + "-utf16.csv");
        margs.outputCharset = StandardCharsets.UTF_16;
        margs.setEnabled(BaseFeature.VERBOSE, true);
        SeparatorConverter.execute(margs);

        margs.input = margs.output;
        margs.inputCharset = StandardCharsets.UTF_16;
        margs.output = new File("target", CsvDemo.class.getSimpleName() + "-utf8.csv");
        margs.outputCharset = StandardCharsets.UTF_8;
        SeparatorConverter.execute(margs);
    }
}