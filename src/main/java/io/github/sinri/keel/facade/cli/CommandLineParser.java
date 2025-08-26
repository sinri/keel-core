package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;

/**
 * Support to parse command-line arguments like:
 * <p>
 * {@code java -jar my-app.jar --option1 value1 --option2=value2 -p value3 -q=value4 --no-output -f -- Parameter1
 * Parameter2}
 * <br>
 * {@code java -jar my-app.jar Parameter1 Parameter2}
 * <p>
 * Definitions:<br>
 * Option: a flag with a short name or a long name, and followed by a value, e.g. {@code --option1 value1}.<br>
 * Flag: a flag with a short name or a long name, e.g. {@code -f} or {@code --no-output}.<br>
 * Parameter: a parameter that is not an option or flag, without named label but indexed,
 * usually on the head or at the tail after {@code --}, e.g. {@code Parameter1} or {@code  -- Parameter2}.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public interface CommandLineParser {
    /**
     * Parses the given array of command-line arguments and returns a result
     * containing the parsed options, flags, and arguments.
     *
     * @param args the array of command-line arguments to parse, from a main method's args parameter
     * @return a result object representing the parsed command-line options, flags, and arguments
     * @throws CommandLineParserParseError if the parsing fails
     */
    @Nonnull
    CommandLineParsedResult parse(String[] args) throws CommandLineParserParseError;

    /**
     * Whether the parser is strict, meaning that it will throw an exception if any undefined option is found.
     *
     * @return true if strict, false otherwise
     */
    boolean isStrictMode();

    /**
     * Sets whether the parser should operate in strict mode. In strict mode, the parser will
     * throw an exception if any undefined option is encountered during parsing.
     *
     * @param strictOrNot true to enable strict mode, false to disable it
     */
    void setStrictMode(boolean strictOrNot);

    void addOption(@Nonnull Option option) throws CommandLineParserBuildError;

    default void addOption(@Nonnull Handler<Option> optionHandler) throws CommandLineParserBuildError {
        Option option = new Option();
        optionHandler.handle(option);
        addOption(option);
    }
}
