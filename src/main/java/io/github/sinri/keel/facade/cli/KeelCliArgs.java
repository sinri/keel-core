package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the results of parsing command-line arguments. It provides methods
 * to retrieve options, flags, and positional parameters from the parsed input.
 * <p>
 * This interface is marked as a technical preview and may be subject to changes.
 */
@TechnicalPreview(since = "4.1.1")
public interface KeelCliArgs {

    /**
     * Retrieves the value associated with a specific short-named option
     * in the parsed command-line arguments.
     *
     * @param shortName the single-character name of the option to retrieve
     * @return the value of the specified option, or null if the option
     *         is not present or does not have a value
     */
    @Nullable
    String readOption(char shortName);

    /**
     * Retrieves the value associated with a specific long-named option
     * in the parsed command-line arguments.
     *
     * @param longName the full name of the option to retrieve; must not be null
     * @return the value of the specified option, or null if the option
     *         is not present or does not have a value
     */
    @Nullable
    String readOption(@Nonnull String longName);

    /**
     * Checks if a flag represented by a short name is present in the parsed command-line arguments.
     *
     * @param shortName the single-character name of the flag to check
     * @return true if the specified flag is present; false otherwise
     */
    boolean readFlag(char shortName);

    /**
     * Checks if a flag represented by a long name is present in the parsed command-line arguments.
     *
     * @param longName the full name of the flag to check; must not be null
     * @return true if the specified flag is present; false otherwise
     */
    boolean readFlag(@Nonnull String longName);

    /**
     * Retrieves the value of a positional parameter at the specified index
     * in the parsed command-line arguments.
     *
     * @param index the zero-based index of the positional parameter to retrieve
     * @return the value of the positional parameter at the given index,
     *         or null if the parameter is not present
     */
    @Nullable
    String readParameter(int index);
}
