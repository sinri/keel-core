package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.core.TechnicalPreview;

/**
 * An exception defined for CommandLineParser preparation before parsing the command line.
 * <p>
 * Expected thrown causes: duplicated options defining, null option naming, etc.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class CommandLineParserBuildError extends Exception {
    public CommandLineParserBuildError(String msg) {
        super(msg);
    }
}
