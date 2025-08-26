package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the result of parsing command-line arguments using the {@code CommandLineParser}.
 * This interface provides methods to retrieve parsed options, flags, and arguments.
 * <p>
 * Options and flags can be specified using their short names (single character)
 * or long names (full string). Arguments are accessed by their index.
 * <p>
 * A class implementing this interface is expected to provide the parsed data for: <br>
 * - Retrieving the value associated with an option, if it exists, via its short or long name.<br>
 * - Checking the presence of a flag via its short or long name.<br>
 * - Accessing indexed arguments.<br>
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public interface CommandLineParsedResult {
    static CommandLineParsedResult create() {
        return new CommandLineParsedResultImpl();
    }

    void recordParameter(@Nonnull String parameter);

    void recordOption(@Nonnull Option option);

    @Nullable
    String readOption(char shortName);

    @Nullable
    String readOption(@Nonnull String longName);

    boolean readFlag(char shortName);

    boolean readFlag(@Nonnull String longName);

    @Nullable
    String readParameter(int index);
}
